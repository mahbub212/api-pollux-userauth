package id.co.msiglife.pollux.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


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
	private List<Integer> roleId;

	@JsonProperty(value = "full_name")
	private String fullName;

	@JsonProperty(value = "expired_at")
	private Long expiredAt;

	@JsonProperty(value = "user_id")
	private Long userId;

	private String token;
}