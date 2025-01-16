package id.co.msiglife.pollux.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.sql.DataSource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import id.co.msiglife.pollux.dto.*;
import id.co.msiglife.pollux.entity.Menu;
import id.co.msiglife.pollux.entity.PolluxRoleAccessMenu;
import id.co.msiglife.pollux.repository.MenuRepository;
import id.co.msiglife.pollux.repository.RoleAccessMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import id.co.msiglife.pollux.entity.User;
import id.co.msiglife.pollux.entity.UserRole;
import id.co.msiglife.pollux.repository.UserRepository;
import id.co.msiglife.pollux.repository.UserRoleRepository;
import id.co.msiglife.pollux.util.JwtUtil;
import id.co.msiglife.pollux.util.StaticValue;
import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class AuthUserService {

	private final DataSource dataSource;
	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final JwtUtil jwtUtil;
	private final MenuRepository menuRepository;
	private final RoleAccessMenuRepository roleAccessMenuRepository;
	
	@PersistenceContext
	private EntityManager entityManager;

	@Value("${custom.timeout.alert}")
	private int alertTimeoutInSeconds;

	@Value("${custom.timeout.actual}")
	private int actualTimeoutInSeconds;

	public LoginUserResponse login(LoginUserRequest request) throws ParseException {
		User user = userRepository.findByUsername(request.getUsername());
		if (user != null) {
			if (request.getPassword().equals(decryptPassword(user.getPassword()))) {
				UserRole userRole = userRoleRepository.findByLusIdWithRoleAccess(user.getUserId());
				if (userRole != null) {
					JwtUtil jwt = new JwtUtil();
					String token = jwt.generate(user, userRole);
					if (isUserActive(user)) {
						updateLussOnline(user);
						return createLoginUserResponse(token, userRole, user);
					} else {
						return createLoginUserErrorResponse(StaticValue.LOGIN_CODE_RESP_INACTIVE, StaticValue.LOGIN_MSG_RESP_INACTIVE);
					}
				} else {
					return createLoginUserErrorResponse(StaticValue.LOGIN_CODE_RESP_NOT_FOUND, StaticValue.LOGIN_MSG_RESP_NOT_FOUND + " in POLLUX");
				}
			} else {
				return createLoginUserErrorResponse(StaticValue.LOGIN_CODE_RESP_INCORRECT_PASSWORD, StaticValue.LOGIN_MSG_RESP_INCORRECT_PASSWORD);
			}
		} else {
			return createLoginUserErrorResponse(StaticValue.LOGIN_CODE_RESP_NOT_FOUND, StaticValue.LOGIN_MSG_RESP_NOT_FOUND);
		}
	}


	@Transactional
	public LogoutUserResponse logout(LogoutUserRequest request) throws ParseException {
		User user = userRepository.findByUsername(request.getUsername());
		if(user != null) {
			// commented for login lus active keep deactivating issue - 16-01-2025
//			updateLusNonActive(user);
//			updateNullLussOnline(user);
			return createLogoutUserResponse(user);
		} else {
			return createLogoutUserErrorResponse(StaticValue.LOGIN_CODE_RESP_NOT_FOUND, StaticValue.LOGIN_MSG_RESP_NOT_FOUND);
		}
	}
	
	@Transactional
	public void updateNullLussOnline(User user) {
		try {
			user.setLussOnline(null);
			userRepository.save(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Transactional
	public void updateLusNonActive(User user) {
		try {
			ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Jakarta"));
			user.setLusNonActive(Date.from(zonedDateTime.toInstant()));
			userRepository.save(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	public void updateLussOnline(User user) {
		try {
			user.setLussOnline(new Date());
			userRepository.save(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Validasi status user active 
	private boolean isUserActive(User user) {
		boolean isActive = false;
		if(user.getActive() == 1) isActive = true;
		return isActive;
	}

	private String encryptPassword(String password) {
		String encryptedPassword = null;
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = dataSource.getConnection();

			String sql = "SELECT EKA.ENCRYPT(?) AS encrypted_password FROM DUAL";
			statement = connection.prepareStatement(sql);
			statement.setString(1, password);
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				encryptedPassword = resultSet.getString("encrypted_password");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return encryptedPassword;
	}

	private String decryptPassword(String encryptedPassword) {
		String decryptedPassword = null;
		Connection connection = null;
		PreparedStatement statement = null;

		try {
			connection = dataSource.getConnection();

			String sql = "SELECT EKA.DECRYPT(?) AS decrypted_password FROM DUAL";
			statement = connection.prepareStatement(sql);
			statement.setString(1, encryptedPassword);
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				decryptedPassword = resultSet.getString("decrypted_password");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return decryptedPassword;
	}

	public boolean isTokenExpired(String token) {
		return jwtUtil.isTokenExpired(token);
	}

	// RESPONSE LOGIN USER 
	private LoginUserResponse createLoginUserResponse(String token, UserRole userRole, User user) {
		HashMap<String, Object> dataResp = new HashMap<String, Object>();
		dataResp.put("token", token);
		dataResp.put("is_expired", false);
		dataResp.put("is_first_login", false);
		dataResp.put("role_id", userRole.getRoleId());
		dataResp.put("full_name", user.getLusFullName());
		
		return LoginUserResponse.builder()
				.code(HttpStatus.OK.value())
				.message("Login Success")
				.data(dataResp)
				.build();
	}
	
	private LoginUserResponse createLoginUserErrorResponse(int code, String message) {
		return LoginUserResponse.builder()
				.code(code)
				.message(message)
				.build();
	}	
	// END RESPONSE LOGIN USER
	
	// RESPONSE LOGOUT USER
		private LogoutUserResponse createLogoutUserResponse(User user) {
			HashMap<String, Object> dataResp = new HashMap<String, Object>();
			dataResp.put("user_id", user.getUserId());
			
			return LogoutUserResponse.builder()
					.code(HttpStatus.OK.value())
					.message("Logout Success")
					.data(dataResp)
					.build();
		}
		
		private LogoutUserResponse createLogoutUserErrorResponse(int code, String message) {
			return LogoutUserResponse.builder()
					.code(code)
					.message(message)
					.build();
		}
	// END RESPONSE LOGOUT USER

	public TokenCheckResponseDTO checkToken(String token) {
		boolean isExpired = isTokenExpired(token);
		Integer roleId = null;

		if (!isExpired) {
			roleId = jwtUtil.getRoleIdFromJwtToken(token);
		}

		TokenDataDTO tokenData = TokenDataDTO.builder()
				.isFirstLogin(false)
				.isExpired(isExpired)
				.roleId(roleId)
				.token(token)
				.build();

		TokenCheckResponseDTO responseDTO = TokenCheckResponseDTO.builder()
				.code(HttpStatus.OK.value())
				.message("Token check success")
				.data(tokenData)
				.build();

		return responseDTO;
	}

	public TimeoutDTO getTimeoutValues() {
		return new TimeoutDTO(alertTimeoutInSeconds, actualTimeoutInSeconds);
	}

	public CheckRoleResponse checkRoleAccess(CheckRoleRequest request) {

		int roleId = request.getRoleId();
		List<PolluxRoleAccessMenu> roleAccessMenu = roleAccessMenuRepository.getRoleAccessMenu(roleId);
		if(roleAccessMenu.size() > 0) {
			List<HashMap<String, Object>> menuList = new ArrayList<HashMap<String,Object>>();
			HashMap<String, Object> menuMap;
			HashMap<String, Object> actionMap;

			for(PolluxRoleAccessMenu role : roleAccessMenu) {
				actionMap = new HashMap<String, Object>();
				actionMap.put("update", convertFlagValue(role.getUpdated()));
				actionMap.put("view", convertFlagValue(role.getView()));
				actionMap.put("add", convertFlagValue(role.getAdd()));
				actionMap.put("delete", convertFlagValue(role.getDelete()));
				actionMap.put("edit", convertFlagValue(role.getEdit()));
				actionMap.put("download", convertFlagValue(role.getDownload()));
				actionMap.put("approve", convertFlagValue(role.getApprove()));
				actionMap.put("change", convertFlagValue(role.getChange()));

				System.out.println("//menu id : "+role.getMenuId());

				Menu menu = menuRepository.findByMenuId(role.getMenuId().longValue());
				menuMap = new HashMap<String, Object>();
				menuMap.put("menu_id", role.getMenuId());
				menuMap.put("menu_name", menu.getNamaMenu());
				menuMap.put("is_enable", convertFlagValue(role.getFlag()));
				menuMap.put("actions",actionMap);
				menuList.add(menuMap);
			}
			return createCheckRoleAccessResponse(StaticValue.CHECK_ROLE_CODE_RESP_SUCCESS, StaticValue.CHECK_ROLE_MSG_RESP_SUCCESS, menuList, roleId);
		} else {
			return createCheckRoleAccessErrorResponse(StaticValue.CHECK_ROLE_CODE_RESP_NOT_FOUND, StaticValue.CHECK_ROLE_MSG_RESP_NOT_FOUND, roleId);
		}
	}

	// RESPONSE CHECK ROLE ACCESS LIST
	private CheckRoleResponse createCheckRoleAccessResponse(int code, String message, List<HashMap<String, Object>> menuList, int roleId) {
		return CheckRoleResponse.builder()
				.code(code)
				.message(message)
				.data(menuList)
				.roleId(roleId)
				.build();
	}

	private CheckRoleResponse createCheckRoleAccessErrorResponse(int code, String message, int roleId) {
		return CheckRoleResponse.builder()
				.code(code)
				.message(message)
				.roleId(roleId)
				.build();
	}
	// END RESPONSE CHECK ROLE ACCESS LIST

	private Boolean convertFlagValue(Integer value) {
		if (value != null) {
			return value == 1;
		} else {
			return false;
		}
	}

}
