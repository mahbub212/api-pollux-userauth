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
public class TimeoutDTO {
	@JsonProperty(value = "alert_timeout")
	private Integer alertTimeout;
	@JsonProperty(value = "actual_timeout")
	private Integer actualTimeout;
}