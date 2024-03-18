package id.co.msiglife.pollux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenCheckResponseDTO {
	private int code;
	private String message;
	private TokenDataDTO data;
}