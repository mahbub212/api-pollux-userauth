package id.co.msiglife.pollux.interceptor;

import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpClientResponseWrapper implements ClientHttpResponse {
    private final ClientHttpResponse original;
    private final byte[] body;

    public HttpClientResponseWrapper(ClientHttpResponse response, byte[] body) {
        this.original = response;
        this.body = body;
    }

    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(body);
    }

    @Override public org.springframework.http.HttpStatus getStatusCode() throws IOException { return original.getStatusCode(); }
    @Override public int getRawStatusCode() throws IOException { return original.getRawStatusCode(); }
    @Override public String getStatusText() throws IOException { return original.getStatusText(); }
    @Override public void close() { original.close(); }
    @Override public org.springframework.http.HttpHeaders getHeaders() { return original.getHeaders(); }
}

