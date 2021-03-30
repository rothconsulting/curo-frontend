package ch.umb.curo.starter.auth

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.exception.GlobalExceptionHandler
import ch.umb.curo.starter.property.CuroProperties
import com.auth0.jwt.exceptions.InvalidClaimException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.rest.dto.ExceptionDto
import org.camunda.bpm.engine.rest.exception.InvalidRequestException
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 * This filter is a copy of ProcessEngineAuthenticationFilter extended with our error handling.
 */
class CuroProcessEngineAuthenticationFilter(private val properties: CuroProperties, private val mapper: ObjectMapper) : ProcessEngineAuthenticationFilter() {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest, response: ServletResponse,
        chain: FilterChain
    ) {
        val req = request as HttpServletRequest
        val resp = response as HttpServletResponse
        var servletPath = servletPathPrefix
        if (servletPath == null) {
            servletPath = req.servletPath
        }
        val requestUrl = req.requestURI.substring(req.contextPath.length + servletPath!!.length)
        val requiresEngineAuthentication = requiresEngineAuthentication(requestUrl)
        if (!requiresEngineAuthentication) {
            chain.doFilter(request, response)
            return
        }
        val engineName = extractEngineName(requestUrl)
        val engine = getAddressedEngine(engineName)
        if (engine == null) {
            resp.status = Response.Status.NOT_FOUND.statusCode
            val exceptionDto = ExceptionDto()
            exceptionDto.type = InvalidRequestException::class.java.simpleName
            exceptionDto.message = "Process engine $engineName not available"
            val objectMapper = ObjectMapper()
            resp.contentType = MediaType.APPLICATION_JSON
            objectMapper.writer().writeValue(resp.writer, exceptionDto)
            resp.writer.flush()
            return
        }

        // Start Curo part
        val authenticationResult = try {
            authenticationProvider.extractAuthenticatedUser(req, engine)
        } catch (e: Exception) {
            clearAuthentication(engine)
            when (e) {
                is ApiException -> {
                    GlobalExceptionHandler(properties, mapper).handleApiException(e, request, response)
                }
                is InvalidClaimException -> {
                    GlobalExceptionHandler(
                        properties,
                        mapper
                    ).handleApiException(ApiException.unauthorized403(e.message ?: "", e), request, response)
                }
                is JWTVerificationException,
                is TokenExpiredException,
                is JWTDecodeException -> {
                    GlobalExceptionHandler(
                        properties,
                        mapper
                    ).handleApiException(ApiException.unauthorized401(e.message ?: "", e), request, response)
                }
            }

            authenticationProvider.augmentResponseByAuthenticationChallenge(resp, engine)
            return
        }

        if (authenticationResult.isAuthenticated) {
            try {
                val authenticatedUser = authenticationResult.authenticatedUser
                val groups = authenticationResult.groups
                val tenants = authenticationResult.tenants
                setAuthenticatedUser(engine, authenticatedUser, groups, tenants)
                chain.doFilter(request, response)
            } finally {
                clearAuthentication(engine)
            }
        } else {
            resp.status = Response.Status.UNAUTHORIZED.statusCode
            authenticationProvider.augmentResponseByAuthenticationChallenge(resp, engine)
        }
    }

}
