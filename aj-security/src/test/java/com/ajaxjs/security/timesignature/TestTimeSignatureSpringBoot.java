package com.ajaxjs.security.timesignature;

import com.ajaxjs.security.SecurityConfiguration;
import com.ajaxjs.spring.DiContextUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Starts a Spring Boot MVC context and verifies time-signature interception on a controller method.
 */
@SpringBootTest(
        classes = TestTimeSignatureSpringBoot.TestApplication.class,
        properties = {
                "security.time-signature.enabled=true",
                "security.time-signature.secret-key=integration-test-secret",
                "security.time-signature.expiration-min=15",
                "security.time-signature.allowed-clock-skew-seconds=0"
        }
)
/**
 * Represents the time signature spring boot test component.
 */
@AutoConfigureMockMvc
class TestTimeSignatureSpringBoot {
    /**
     * MVC test client backed by the started Spring application context.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Time-signature component created and configured by Spring Boot.
     */
    @Autowired
    private TimeSignature timeSignature;

    /**
     * Verifies that the security component is available from the Spring context.
     */
    @Test
    void testComponentInjection() {
        assertNotNull(timeSignature);
    }

    /**
     * Verifies that a request carrying a current timestamp reaches the protected controller.
     */
    @Test
    void testValidTimestampRequest() throws Exception {
        String signature = timeSignature.generateSignature();

        mockMvc.perform(get("/test/time-signature").param("tsign", signature))
                .andExpect(status().isOk())
                .andExpect(content().string("accepted"));
    }

    /**
     * Verifies that a request carrying an expired timestamp is rejected before controller invocation.
     */
    @Test
    void testExpiredTimestampRequest() throws Exception {
        long expiredTimestamp = System.currentTimeMillis() - 16 * 60_000L;
        String signature = timeSignature.generateSignature(expiredTimestamp);

        mockMvc.perform(get("/test/time-signature").param("tsign", signature))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that a request without tsign is rejected before controller invocation.
     */
    @Test
    void testMissingTimestampRequest() throws Exception {
        mockMvc.perform(get("/test/time-signature"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Minimal Spring Boot application used by this integration test.
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({SecurityConfiguration.class, DiContextUtil.class, TestController.class, TestExceptionHandler.class})
    static class TestApplication {
    }

    /**
     * Controller whose method is protected by the time-signature interceptor.
     */
    @RestController
    static class TestController {
        /**
         * Returns a marker only after the interceptor accepts the request.
         */
        @GetMapping("/test/time-signature")
        @TimeSignatureVerify
        public String verifyTimestamp() {
            return "accepted";
        }
    }

    /**
     * Converts interceptor validation exceptions into deterministic HTTP responses for MockMvc assertions.
     */
    @RestControllerAdvice
    static class TestExceptionHandler {
        /**
         * Maps an invalid or expired signature to HTTP 403.
         */
        @ExceptionHandler(SecurityException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public void handleSecurityException() {
        }

        /**
         * Maps a missing request parameter to HTTP 400.
         */
        @ExceptionHandler(IllegalArgumentException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public void handleIllegalArgumentException() {
        }
    }
}
