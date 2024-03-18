package id.co.msiglife.pollux.controller;

import java.text.ParseException;

import id.co.msiglife.pollux.dto.*;
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

	private final Logger logger = LoggerFactory.getLogger(AuthUserController.class);

	@PostMapping(
			path = "/api/auth/login",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoginUserResponse> login(@Validated @RequestBody LoginUserRequest request) throws ParseException {
		LoginUserResponse response = authUserService.login(request);
		if (response.getCode() == HttpStatus.OK.value()) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(response.getCode()).body(response);
		}
	}
	
	@PostMapping(
			path = "/api/auth/logout",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LogoutUserResponse> logout(@Validated @RequestBody LogoutUserRequest request) throws ParseException {
		LogoutUserResponse response = authUserService.logout(request);
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
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyTokenResponse);
		}

		boolean isExpired = authUserService.isTokenExpired(authorizationHeader);

		TokenCheckResponseDTO responseDTO;
		if (isExpired) {
			responseDTO = TokenCheckResponseDTO.builder()
					.code(HttpStatus.UNAUTHORIZED.value())
					.message("Token expired")
					.data(TokenDataDTO.builder().isExpired(true).token(authorizationHeader).build())
					.build();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);
		} else {
			responseDTO = authUserService.checkToken(authorizationHeader);
			return ResponseEntity.status(responseDTO.getCode()).body(responseDTO);
		}
	}

	@PostMapping(
			path = "/api/auth/check-role-access",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CheckRoleResponse> checkRoleAccess(@Validated @RequestBody CheckRoleRequest request) throws ParseException {
		CheckRoleResponse response = authUserService.checkRoleAccess(request);
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
		return ResponseEntity.ok(response);
	}
	
}
