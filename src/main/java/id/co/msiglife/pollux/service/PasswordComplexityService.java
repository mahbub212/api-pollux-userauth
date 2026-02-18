package id.co.msiglife.pollux.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.msiglife.pollux.dto.passwordcomplexity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordComplexityService {

    @Value("${spring.custom.password-complexity-url}")
    private String baseUrl;

    @Value("${spring.custom.password-complexity-apptype}")
    private Integer appType;

    @Value("${spring.custom.password-complexity-api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public PasswordComplexityApiResponse<LoginAttemptData> attemptLogin(String username) {
        String url = baseUrl + "/v1/account/login/attempt";
        PasswordComplexityRequest req = PasswordComplexityRequest.builder()
                .username(username)
                .jenisApp(appType)
                .build();

        try {
            HttpEntity<Object> entity = new HttpEntity<>(req, headers());

            ParameterizedTypeReference<PasswordComplexityApiResponse<LoginAttemptData>> loginAttemptType =
                    new ParameterizedTypeReference<PasswordComplexityApiResponse<LoginAttemptData>>() {
                    };

            ResponseEntity<PasswordComplexityApiResponse<LoginAttemptData>> response = restTemplate.exchange(url, HttpMethod.POST, entity, loginAttemptType);
            log.info("Login attempt response: {}", response.getBody());

            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("POST {} failed: status={}, body={}", url, ex.getRawStatusCode(), ex.getResponseBodyAsString());
            JavaType envelopeType = mapper.getTypeFactory()
                    .constructParametricType(PasswordComplexityApiResponse.class, LockCheckData.class);

            try {
                PasswordComplexityApiResponse<LoginAttemptData> response =
                        mapper.readValue(ex.getResponseBodyAsString(), envelopeType);
                return response;
            } catch (JsonProcessingException e) {
                log.error("", e);
                return PasswordComplexityApiResponse.<LoginAttemptData>builder()
                        .message("Json Processing error" + url)
                        .data(null)
                        .error(true)
                        .build();
            }
        } catch (Exception e) {
            log.error("POST {} failed: {}", url, e.getMessage(), e);
            return PasswordComplexityApiResponse.<LoginAttemptData>builder()
                    .message("Unexpected error calling " + url)
                    .data(null)
                    .error(true)
                    .build();
        }
    }


    public PasswordComplexityApiResponse<LockCheckData> checkAccountLock(String username) {
        String url = baseUrl + "/v1/account/login/block";
        PasswordComplexityRequest req = PasswordComplexityRequest.builder()
                .username(username)
                .jenisApp(appType)
                .build();

        ParameterizedTypeReference<PasswordComplexityApiResponse<LockCheckData>> lockCheckType =
                new ParameterizedTypeReference<PasswordComplexityApiResponse<LockCheckData>>() {
                };

        try {
            HttpEntity<Object> entity = new HttpEntity<>(req, headers());

            ResponseEntity<PasswordComplexityApiResponse<LockCheckData>> response = restTemplate.exchange(url, HttpMethod.POST, entity, lockCheckType);
            log.info("Lock check response: {}", response.getBody());

            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            JavaType envelopeType = mapper.getTypeFactory()
                    .constructParametricType(PasswordComplexityApiResponse.class, LockCheckData.class);

            try {
                PasswordComplexityApiResponse<LockCheckData> response =
                        mapper.readValue(ex.getResponseBodyAsString(), envelopeType);
                log.info("Lock check response: {}", response);
                return response;
            } catch (JsonProcessingException e) {
                log.error("", e);
                return PasswordComplexityApiResponse.<LockCheckData>builder()
                        .message("Json Processing error" + url)
                        .data(null)
                        .error(true)
                        .build();
            }

        } catch (Exception e) {
            log.error("POST {} failed: {}", url, e.getMessage(), e);
            return PasswordComplexityApiResponse.<LockCheckData>builder()
                    .message("Unexpected error calling " + url)
                    .data(null)
                    .error(true)
                    .build();
        }
    }

    public PasswordComplexityApiResponse<ChangePasswordData> changePassword(String username) {
        String url = baseUrl + "/v1/password/change";
        PasswordComplexityRequest req = PasswordComplexityRequest.builder()
                .username(username)
                .jenisApp(appType)
                .build();

        try {
            HttpEntity<Object> entity = new HttpEntity<>(req, headers());

            ParameterizedTypeReference<PasswordComplexityApiResponse<ChangePasswordData>> dataType =
                    new ParameterizedTypeReference<PasswordComplexityApiResponse<ChangePasswordData>>() {
                    };

            ResponseEntity<PasswordComplexityApiResponse<ChangePasswordData>> response = restTemplate.exchange(url, HttpMethod.POST, entity, dataType);

            log.info("Lock check response: {}", response.getBody());
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("POST {} failed: status={}, body={}", url, ex.getRawStatusCode(), ex.getResponseBodyAsString());
            return PasswordComplexityApiResponse.<ChangePasswordData>builder()
                    .message("HTTP " + ex.getRawStatusCode() + " calling " + url)
                    .data(null)
                    .error(true)
                    .build();
        } catch (Exception e) {
            log.error("POST {} failed: {}", url, e.getMessage(), e);
            return PasswordComplexityApiResponse.<ChangePasswordData>builder()
                    .message("Unexpected error calling " + url)
                    .data(null)
                    .error(true)
                    .build();
        }
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API-Key", apiKey);
        return headers;
    }
}
