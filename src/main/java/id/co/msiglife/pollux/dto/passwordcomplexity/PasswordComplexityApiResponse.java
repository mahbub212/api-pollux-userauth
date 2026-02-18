package id.co.msiglife.pollux.dto.passwordcomplexity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordComplexityApiResponse<T> {
    private String message;
    private T data;
    private boolean error;
}

