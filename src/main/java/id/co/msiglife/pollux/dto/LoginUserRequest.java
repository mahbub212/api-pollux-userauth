package id.co.msiglife.pollux.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginUserRequest {

	@NotBlank
	@Size(max = 100)
	private String username;
	
	@NotBlank
	@Size(max = 100)
	private String password;
	
}
