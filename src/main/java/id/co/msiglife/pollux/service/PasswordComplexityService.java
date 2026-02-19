package id.co.msiglife.pollux.service;

import id.co.msiglife.pollux.dto.passwordcomplexity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

    private final RestTemplate restTemplate;

    public ResponseEntity<PasswordComplexityApiResponse<LoginAttemptData>> attemptLogin(String username) {
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

            return response;

        } catch (Exception e) {
            log.error("POST {} failed: {}", url, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(PasswordComplexityApiResponse.<LoginAttemptData>builder()
                    .message("Unexpected error calling " + url + " " + e.getMessage())
                    .data(null)
                    .error(true)
                    .build()
            );
        }
    }


    public ResponseEntity<PasswordComplexityApiResponse<LockCheckData>> checkAccountLock(String username) {
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

            return response;

        } catch (Exception e) {
            log.error("POST {} failed: {}", url, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(PasswordComplexityApiResponse.<LockCheckData>builder()
                    .message("Unexpected error calling " + url + " " + e.getMessage())
                    .data(null)
                    .error(true)
                    .build()
            );
        }
    }

    public ResponseEntity<PasswordComplexityApiResponse<ChangePasswordData>> changePassword(String username) {
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

            return response;

        } catch (Exception e) {
            log.error("POST {} failed: {}", url, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(PasswordComplexityApiResponse.<ChangePasswordData>builder()
                    .message("Unexpected error calling " + url + " " + e.getMessage())
                    .data(null)
                    .error(true)
                    .build()
            );
        }
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API-Key", apiKey);
        return headers;
    }
}
