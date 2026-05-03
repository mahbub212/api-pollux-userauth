package id.co.msiglife.pollux.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordRequest {
    @JsonProperty(value = "old_password")
    @Size(max = 100)
    @NotBlank(message = "password cannot be null")
    private String oldPassword;

    @JsonProperty(value = "new_password")
    @Size(max = 100)
    @NotBlank(message = "new password cannot be null")
    private String newPassword;

    @JsonProperty(value = "confirm_new_password")
    @Size(max = 100)
    @NotBlank(message = "confirm password cannot be null")
    private String confirmNewPassword;
}