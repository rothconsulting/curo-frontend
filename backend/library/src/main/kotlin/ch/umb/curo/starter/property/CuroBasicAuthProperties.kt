package ch.umb.curo.starter.property

import org.springframework.boot.convert.DurationUnit
import java.time.Duration
import java.time.temporal.ChronoUnit

class CuroBasicAuthProperties {

    /**
     * Should basic auth allow session cookies
     */
    var useSessionCookie: Boolean = true

    /**
     * Specifies the time, in seconds, between client requests before the
     * servlet container will invalidate this session. A zero or negative time
     * indicates that the session should never timeout.
     */
    @DurationUnit(ChronoUnit.SECONDS)
    var sessionTimeout: Duration = Duration.ZERO

    /**
     * Only send session cookie over SSL
     */
    var secureOnlySessionCookie: Boolean = true

    /**
     * Session cookie name
     */
    var sessionCookieName: String = "CURO_SESSION"
}
