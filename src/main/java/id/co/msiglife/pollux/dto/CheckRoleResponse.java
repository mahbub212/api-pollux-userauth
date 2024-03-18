package id.co.msiglife.pollux.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckRoleResponse {

	private int code;
	 
	private String message;
	
	@JsonProperty(value = "role_id")
	private int roleId;
	
	private List<HashMap<String, Object>> data;
	
}
