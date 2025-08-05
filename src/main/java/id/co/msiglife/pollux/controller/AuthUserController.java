package id.co.msiglife.pollux.controller;

import java.text.ParseException;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import id.co.msiglife.pollux.dto.*;
import id.co.msiglife.pollux.exception.RequestContext;
import id.co.msiglife.pollux.kafkalogger.KafkaMessageProducer;
import id.co.msiglife.pollux.util.JsonUtil;
import id.co.msiglife.pollux.util.WorkFlowProcessIdStaticValue;
import org.example.LogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import id.co.msiglife.pollux.service.AuthUserService;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
public class AuthUserController {

	@Autowired
	private AuthUserService authUserService;

	@Autowired
	private KafkaMessageProducer kafkaMessageProducer;

	@Autowired
	private JsonUtil jsonUtil;

	@Autowired
	RequestContext requestContext;

	private final Logger logger = LoggerFactory.getLogger(AuthUserController.class);

	@PostMapping(
			path = "/api/auth/login",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<LoginUserResponse> login(@Validated @RequestBody LoginUserRequest request) throws ParseException {
		String json = jsonUtil.toJson(request);
		requestContext.setPayload(json);

		LoginUserResponse response = authUserService.login(request);
		String jsonResponse = jsonUtil.toJson(response);
		String jsonRequest = jsonUtil.toJson(request);
		String msg = new LogMessage(111, new Date(), new Date(), 111, WorkFlowProcessIdStaticValue.LOGIN_PROCESS_ID, jsonResponse, jsonRequest, "1", "", false).getMessage();
		kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);
		if (response.getCode() == HttpStatus.OK.value()) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(response.getCode()).body(response);
		}
	}
	
	@PostMapping(
			path = "/api/auth/logout",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<LogoutUserResponse> logout(HttpServletRequest request) throws Exception {
		String authorizationHeader = request.getHeader("Authorization");
		logger.info("Authorization Header: {}", authorizationHeader);

		LogoutUserResponse response = authUserService.logout(authorizationHeader);
        requestContext.setPayload("{}");

		String jsonResponse = jsonUtil.toJson(response);
		String msg = new LogMessage(111, new Date(), new Date(), 111, WorkFlowProcessIdStaticValue.LOGOUT_PROCESS_ID, jsonResponse, "{}", "1", "", false).getMessage();
		kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);
		if (response.getCode() == HttpStatus.OK.value()) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(response.getCode()).body(response);
		}
	}

	@GetMapping("/api/auth/check-token")
	public ResponseEntity<TokenCheckResponseDTO> checkToken(HttpServletRequest request) {
		String authorizationHeader = request.getHeader("Authorization");
		logger.info("Authorization Header: {}", authorizationHeader);

		if (authorizationHeader == null || authorizationHeader.isEmpty()) {
			logger.error("Authorization header is null or empty.");
			TokenCheckResponseDTO emptyTokenResponse = TokenCheckResponseDTO.builder()
					.code(HttpStatus.BAD_REQUEST.value())
					.message("Authorization header is null or empty.")
					.build();

			String jsonResponse = jsonUtil.toJson(emptyTokenResponse);
			String msg = new LogMessage(111, new Date(), new Date(), 111, WorkFlowProcessIdStaticValue.CHECK_TOKEN_PROCESS_ID, jsonResponse, "", "1", "Authorization header is null or empty.", false).getMessage();
			kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyTokenResponse);
		}

		boolean isExpired = authUserService.isTokenExpired(authorizationHeader);

		TokenCheckResponseDTO responseDTO;
		TokenCheckResponseDTO responseKafkaDTO;
		if (isExpired) {
			responseDTO = TokenCheckResponseDTO.builder()
					.code(HttpStatus.UNAUTHORIZED.value())
					.message("Token expired")
					.data(TokenDataDTO.builder().isExpired(true).token(authorizationHeader).build())
					.build();

			responseKafkaDTO = TokenCheckResponseDTO.builder()
					.code(HttpStatus.UNAUTHORIZED.value())
					.message("Token expired")
					.build();

			String jsonResponse = jsonUtil.toJson(responseKafkaDTO);
			String msg = new LogMessage(111, new Date(), new Date(), 111, WorkFlowProcessIdStaticValue.CHECK_TOKEN_PROCESS_ID, jsonResponse, "", "1", "Token expired", false).getMessage();
			kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);
		} else {
			responseDTO = authUserService.checkToken(authorizationHeader);

			String jsonResponse = jsonUtil.toJson(responseDTO.getMessage());
			String msg = new LogMessage(111, new Date(), new Date(), 111, WorkFlowProcessIdStaticValue.CHECK_TOKEN_PROCESS_ID, jsonResponse, "", "1", "", false).getMessage();
			kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);

			return ResponseEntity.status(responseDTO.getCode()).body(responseDTO);
		}
	}

	@PostMapping(
			path = "/api/auth/check-role-access",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CheckRoleResponse> checkRoleAccess(@Validated @RequestBody CheckRoleRequest request) throws ParseException {
		String json = jsonUtil.toJson(request);
		requestContext.setPayload(json);
		CheckRoleResponse response = authUserService.checkRoleAccess(request);

		String jsonResponse = jsonUtil.toJson(response);
		String jsonRequest = jsonUtil.toJson(request);
		String msg = new LogMessage(111, new Date(), new Date(), 111, WorkFlowProcessIdStaticValue.CHECK_ROLE_PROCESS_ID, jsonResponse, jsonRequest, "1", "", false).getMessage();
		kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);
		if (response.getCode() == HttpStatus.OK.value()) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(response.getCode()).body(response);
		}
	}

	@GetMapping("/api/auth/timeout")
	public ResponseEntity<?> getTimeoutValues() {
		TimeoutDTO timeoutDTO = authUserService.getTimeoutValues();
		ApiResponse<TimeoutDTO> response = new ApiResponse<>(HttpStatus.OK.value(), "Success Get Data", timeoutDTO);
		String jsonResponse = jsonUtil.toJson(response);
		String msg = new LogMessage(111, new Date(), new Date(), 111, WorkFlowProcessIdStaticValue.GET_TIMEOUT_PROCESS_ID, jsonResponse, "", "1", "", false).getMessage();
		kafkaMessageProducer.sendMessage("POLLUX_LOGS", msg);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/api/menus")
	public List<MenuTreeDto> getTree(@RequestBody List<Integer> request) {
		return authUserService.getMenuTreeByRoles(request);
	}


}
