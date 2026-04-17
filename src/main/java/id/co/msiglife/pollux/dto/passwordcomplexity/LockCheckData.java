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
public class LockCheckData {
    @JsonProperty("unblock_date")
    private String unblockDate;

    @JsonProperty("blokir_date")
    private String blokirDate;

    @JsonProperty("is_blokir")
    private Boolean isBlokir;

    private String username;
}

