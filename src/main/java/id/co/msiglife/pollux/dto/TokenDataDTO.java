package id.co.msiglife.pollux.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenDataDTO {
	@JsonProperty(value = "is_first_login")
	private boolean isFirstLogin;

	@JsonProperty(value = "is_expired")
	private boolean isExpired;

	@JsonProperty(value = "role_id")
	private int roleId;

	private String token;
}