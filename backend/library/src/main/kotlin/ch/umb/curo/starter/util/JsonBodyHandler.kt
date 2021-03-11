package ch.umb.curo.starter.util

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.net.http.HttpResponse
import java.util.function.Supplier

class JsonBodyHandler<T>(private val targetClass: Class<T>) : HttpResponse.BodyHandler<Supplier<T>> {
    override fun apply(responseInfo: HttpResponse.ResponseInfo?): HttpResponse.BodySubscriber<Supplier<T>> {
        return asJSON(targetClass)
    }

    companion object {
        fun <W> asJSON(targetType: Class<W>?): HttpResponse.BodySubscriber<Supplier<W>> {
            val upstream: HttpResponse.BodySubscriber<InputStream> = HttpResponse.BodySubscribers.ofInputStream()
            return HttpResponse.BodySubscribers.mapping(upstream) { inputStream ->
                toSupplierOfType(
                    inputStream,
                    targetType
                )
            }
        }

        private fun <W> toSupplierOfType(inputStream: InputStream, targetType: Class<W>?): Supplier<W> {
            return Supplier<W> {
                try {
                    inputStream.use { stream ->
                        val objectMapper = ObjectMapper()
                        return@Supplier objectMapper.readValue(stream, targetType)
                    }
                } catch (e: IOException) {
                    throw UncheckedIOException(e)
                }
            }
        }
    }
}
