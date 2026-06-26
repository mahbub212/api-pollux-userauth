package id.co.msiglife.pollux.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
public class OutboundLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Set<String> SENSITIVE_HEADERS = new HashSet<>(Arrays.asList(
            HttpHeaders.AUTHORIZATION, HttpHeaders.COOKIE, "Set-Cookie", "X-Api-Key"
    ));

    // Max bytes to log for bodies
    private static final int MAX_LOG_BYTES = 8 * 1024; // 8 KB

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Request ID
        String requestId = Optional.ofNullable(MDC.get("request-Id"))
                .orElse(UUID.randomUUID().toString());
        MDC.put("request-Id", requestId);

        Instant start = Instant.now();

        // ---- REQUEST LOG ----
        Map<String, String> safeRequestHeaders = redactHeaders(request.getHeaders());
        String requestBodyPreview = formatBodyForLog(body, request.getHeaders().getContentType());

        log.info("HTTP OUTBOUND REQUEST | id={} | method={} | uri={} | contentType={} | contentLength={} | headers={} | body={}",
                requestId,
                request.getMethod(),
                request.getURI(),
                request.getHeaders().getContentType(),
                body != null ? body.length : 0,
                safeRequestHeaders,
                requestBodyPreview
        );

        // Execute and buffer response
        ClientHttpResponse response = execution.execute(request, body);

        byte[] responseBytes = StreamUtils.copyToByteArray(response.getBody());
        Duration duration = Duration.between(start, Instant.now());

        // ---- RESPONSE LOG ----
        Map<String, String> safeResponseHeaders = redactHeaders(response.getHeaders());
        String responseBodyPreview = formatBodyForLog(responseBytes, response.getHeaders().getContentType());

        log.info("HTTP OUTBOUND RESPONSE | id={} | status={} | reason={} | durationMs={} | contentType={} | contentLength={} | headers={} | body={}",
                requestId,
                response.getRawStatusCode(),
                response.getStatusText(),
                duration.toMillis(),
                response.getHeaders().getContentType(),
                responseBytes.length,
                safeResponseHeaders,
                responseBodyPreview
        );

        // Return a wrapped response with a fresh InputStream
        return new HttpClientResponseWrapper(response, responseBytes);
    }

    private static Map<String, String> redactHeaders(HttpHeaders headers) {
        Map<String, String> map = new LinkedHashMap<>();
        headers.forEach((k, v) -> {
            if (SENSITIVE_HEADERS.contains(k)) {
                map.put(k, "***REDACTED***");
            } else {
                map.put(k, String.join(",", v));
            }
        });
        return map;
    }

    private static String formatBodyForLog(byte[] bytes, MediaType contentType) {
        if (bytes == null) return "null";
        if (bytes.length == 0) return "<empty>";

        // Only attempt to render textual types
        if (!isTextual(contentType)) {
            return String.format("<%s %d bytes>", contentType != null ? contentType.toString() : "binary", bytes.length);
        }

        int len = Math.min(bytes.length, MAX_LOG_BYTES);
        Charset charset = contentType != null && contentType.getCharset() != null
                ? contentType.getCharset()
                : StandardCharsets.UTF_8;

        String text = new String(bytes, 0, len, charset);

        if (bytes.length > MAX_LOG_BYTES) {
            return text + String.format("... <truncated %d/%d bytes>", len, bytes.length);
        }
        return text;
    }


    private static boolean isTextual(MediaType mediaType) {
        if (mediaType == null) return false;
        return mediaType.isCompatibleWith(MediaType.TEXT_PLAIN)
                || mediaType.isCompatibleWith(MediaType.APPLICATION_JSON)
                || mediaType.isCompatibleWith(MediaType.APPLICATION_XML)
                || mediaType.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)
                || Objects.equals(mediaType.getType(), "text")
                || Objects.equals(mediaType.getSubtype(), "javascript")
                || Objects.equals(mediaType, MimeTypeUtils.APPLICATION_XML);
    }
}

