package id.co.msiglife.pollux.config;

import id.co.msiglife.pollux.interceptor.OutboundLoggingInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {

    @Value("${http.pool.max-total:200}")
    private int maxTotal;

    @Value("${http.pool.default-max-per-route:50}")
    private int defaultMaxPerRoute;

    @Value("${http.pool.connect-timeout-ms:10000}")
    private int connectTimeoutMs;

    @Value("${http.pool.connection-request-timeout-ms:5000}")
    private int connectionRequestTimeoutMs;

    @Value("${http.pool.socket-timeout-ms:20000}")
    private int socketTimeoutMs;

    @Value("${http.pool.keep-alive-ms:30000}")
    private long keepAliveMs;

    @Value("${http.pool.idle-evict-seconds:30}")
    private int idleEvictSeconds;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(defaultMaxPerRoute);

        // Timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeoutMs)
                .setConnectionRequestTimeout(connectionRequestTimeoutMs)
                .setSocketTimeout(socketTimeoutMs)
                .build();

        // Keep-alive strategy
        ConnectionKeepAliveStrategy keepAliveStrategy = (response, context) -> keepAliveMs;

        // Build HttpClient with evict idle/expired connections
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .setKeepAliveStrategy(keepAliveStrategy)
                .evictIdleConnections(idleEvictSeconds, TimeUnit.SECONDS)
                .evictExpiredConnections()
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = builder
                .requestFactory(() -> requestFactory)
                .additionalInterceptors(new OutboundLoggingInterceptor())
                .build();

        // error handler Anda (non-throwing)
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) {
                // no-op
            }
        });

        return restTemplate;
    }
}
