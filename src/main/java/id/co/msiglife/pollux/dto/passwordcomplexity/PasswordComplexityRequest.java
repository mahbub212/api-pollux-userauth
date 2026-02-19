package id.co.msiglife.pollux.dto.passwordcomplexity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordComplexityRequest {
    private String username;

    @JsonProperty("jenis_app")
    private Integer jenisApp;
}
