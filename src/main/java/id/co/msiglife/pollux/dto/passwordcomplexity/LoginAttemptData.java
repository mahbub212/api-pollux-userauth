package id.co.msiglife.pollux.dto.passwordcomplexity;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttemptData {
    @JsonProperty("unblock_date")
    private String unblockDate;

    @JsonProperty("total_attemps")
    private Integer totalAttemps;

    @JsonProperty("blokir_date")
    private String blokirDate;

    private String username;
}

