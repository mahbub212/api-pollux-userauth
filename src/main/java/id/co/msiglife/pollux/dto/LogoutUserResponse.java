package id.co.msiglife.pollux.dto;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogoutUserResponse {
	 
	private int code;
	 
	private String message;
	
	private HashMap<String, Object> data;
	
}
