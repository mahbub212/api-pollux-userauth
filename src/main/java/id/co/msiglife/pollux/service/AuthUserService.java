package id.co.msiglife.pollux.service;

import id.co.msiglife.pollux.dto.*;
import id.co.msiglife.pollux.dto.passwordcomplexity.ChangePasswordData;
import id.co.msiglife.pollux.dto.passwordcomplexity.LockCheckData;
import id.co.msiglife.pollux.dto.passwordcomplexity.LoginAttemptData;
import id.co.msiglife.pollux.dto.passwordcomplexity.PasswordComplexityApiResponse;
import id.co.msiglife.pollux.entity.Menu;
import id.co.msiglife.pollux.entity.PolluxRoleAccessMenu;
import id.co.msiglife.pollux.entity.User;
import id.co.msiglife.pollux.entity.UserRole;
import id.co.msiglife.pollux.repository.MenuRepository;
import id.co.msiglife.pollux.repository.RoleAccessMenuRepository;
import id.co.msiglife.pollux.repository.UserRepository;
import id.co.msiglife.pollux.repository.UserRoleRepository;
import id.co.msiglife.pollux.util.JwtUtil;
import id.co.msiglife.pollux.util.StaticValue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthUserService {

	private final DataSource dataSource;
	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final JwtUtil jwtUtil;
	private final MenuRepository menuRepository;
	private final RoleAccessMenuRepository roleAccessMenuRepository;
	private final PasswordComplexityService passwordComplexityService;

	@PersistenceContext
	private EntityManager entityManager;

    @Value("${custom.timeout.alert}")
    private int alertTimeoutInSeconds;

    @Value("${custom.timeout.actual}")
    private int actualTimeoutInSeconds;

	@Transactional
	public LoginUserResponse login(LoginUserRequest request) throws ParseException {
		User user = userRepository.findByUsername(request.getUsername());
		if (user == null) {
            // throw new NotFoundException(StaticValue.LOGIN_MSG_RESP_NOT_FOUND, WorkFlowProcessIdStaticValue.LOGIN_PROCESS_ID);
			return createLoginUserErrorResponse(
					StaticValue.LOGIN_CODE_RESP_NOT_FOUND,
					StaticValue.LOGIN_MSG_RESP_NOT_FOUND
			);
		}

		// check blocked user
		PasswordComplexityApiResponse<LockCheckData> lockCheckResponse = passwordComplexityService.checkAccountLock(user.getUsername());

		if (lockCheckResponse != null && lockCheckResponse.getData() != null && lockCheckResponse.getData().getIsBlokir()) {
			return createLoginUserErrorResponse(
					StaticValue.BLOCKED_ACCOUNT_CODE,
					StaticValue.BLOCKED_ACCOUNT_MSG
			);
		}

		if (!request.getPassword().equals(decryptPassword(user.getPassword()))) {
			// Hit Login Attempt
			PasswordComplexityApiResponse<LoginAttemptData> loginAttemptResponse = passwordComplexityService.attemptLogin(request.getUsername());
            // throw new IncorrectPasswordException(StaticValue.LOGIN_MSG_RESP_INCORRECT_PASSWORD, WorkFlowProcessIdStaticValue.LOGIN_PROCESS_ID);
			return createLoginUserErrorResponse(
					StaticValue.LOGIN_CODE_RESP_INCORRECT_PASSWORD,
                    StaticValue.LOGIN_MSG_RESP_INCORRECT_PASSWORD + " | " + loginAttemptResponse.getMessage()
			);
		}

		List<UserRole> userRole = userRoleRepository.findByLusIdWithRoleAccessList(user.getUserId());
		if (userRole == null) {
            // throw new NotFoundException(StaticValue.LOGIN_MSG_RESP_NOT_FOUND, WorkFlowProcessIdStaticValue.LOGIN_PROCESS_ID);
			return createLoginUserErrorResponse(
					StaticValue.LOGIN_CODE_RESP_NOT_FOUND,
					StaticValue.LOGIN_MSG_RESP_NOT_FOUND + " in POLLUX"
			);
		}

		if (!isUserActive(user)) {
            // throw new InactiveException(StaticValue.LOGIN_MSG_RESP_INACTIVE, WorkFlowProcessIdStaticValue.LOGIN_PROCESS_ID);
			return createLoginUserErrorResponse(
					StaticValue.LOGIN_CODE_RESP_INACTIVE,
					StaticValue.LOGIN_MSG_RESP_INACTIVE
			);
		}

		JwtUtil jwt = new JwtUtil();
		String token = jwt.generate(user, userRole);

		updateLussOnline(user);
		return createLoginUserResponse(token, userRole, user);
	}


    @Transactional
    public LogoutUserResponse logout(String authToken) throws Exception {
        if (authToken == null || authToken.isEmpty()) {
            return createLogoutUserErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Authorization token is missing or empty");
        }

        String username = jwtUtil.getUsernameFromJwtToken(authToken);
        User user = userRepository.findByUsername(username);
        if(user != null) {
            // commented for login lus active keep deactivating issue - 16-01-2025
//			updateLusNonActive(user);
//			updateNullLussOnline(user);
            return createLogoutUserResponse(user);
        } else {
//            throw new NotFoundException(StaticValue.LOGIN_MSG_RESP_NOT_FOUND, WorkFlowProcessIdStaticValue.LOGOUT_PROCESS_ID);
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
            user.setLusNonActive(new Date());
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
    private LoginUserResponse createLoginUserResponse(String token, List<UserRole> userRoles, User user) {
        HashMap<String, Object> dataResp = new HashMap<String, Object>();
        dataResp.put("token", token);
        dataResp.put("is_expired", false);
        dataResp.put("is_first_login", false);
        dataResp.put("expired_at", jwtUtil.getExpirationDateFromToken(token).getTime());
        //dataResp.put("role_id", userRole.getRoleId());
        List<Integer> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        dataResp.put("role_id", roleIds);
        dataResp.put("full_name", user.getLusFullName());
        dataResp.put("user_id", user.getUserId());
        dataResp.put("list_menu_access", getMenuTreeByRoles(roleIds));

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
        String fullname = jwtUtil.getFullnameFromJwtToken(token);
        Long expiredAt = jwtUtil.getExpirationDateFromToken(token).getTime();
        Long userId = jwtUtil.getAllClaimsFromToken(token).get("user_id", Long.class);
        List<Integer> roleId = null;
        List<MenuTreeDto> menuTreeDtos = null;

        if (!isExpired) {
            roleId = jwtUtil.getRoleIdFromJwtToken(token);
            menuTreeDtos = getMenuTreeByRoles(roleId);
        }

        TokenDataDTO tokenData = TokenDataDTO.builder()
                .isFirstLogin(false)
                .isExpired(isExpired)
                .roleId(roleId)
                .fullName(fullname)
                .expiredAt(expiredAt)
                .userId(userId)
                .token(token)
                .menuTreeDtos(menuTreeDtos)
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

        List<Integer> roleId = request.getRoleId();

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
//            throw new NotFoundException(StaticValue.CHECK_ROLE_MSG_RESP_NOT_FOUND, WorkFlowProcessIdStaticValue.CHECK_ROLE_PROCESS_ID);
            return createCheckRoleAccessErrorResponse(StaticValue.CHECK_ROLE_CODE_RESP_NOT_FOUND, StaticValue.CHECK_ROLE_MSG_RESP_NOT_FOUND, roleId);
        }
    }

    // RESPONSE CHECK ROLE ACCESS LIST
    private CheckRoleResponse createCheckRoleAccessResponse(int code, String message, List<HashMap<String, Object>> menuList, List<Integer> roleId) {
        return CheckRoleResponse.builder()
                .code(code)
                .message(message)
                .data(menuList)
                .roleId(roleId)
                .build();
    }

    private CheckRoleResponse createCheckRoleAccessErrorResponse(int code, String message, List<Integer> roleId) {
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

	public List<MenuTreeDto> getMenuTreeByRoles(List<Integer> request) {
		List<Map<String, Object>> rows = menuRepository.findMenuTreeByRoles(request);
		Map<Long, MenuTreeDto> nodeMap = new LinkedHashMap<>();
		List<MenuTreeDto> roots = new ArrayList<>();
		Map<Long, List<MenuTreeDto>> tempChild = new HashMap<>();

		for (Map<String, Object> row : rows) {
            Object valId = row.get("ID");
            Long id = null;
            if (valId != null) {
                id = Long.parseLong(valId.toString());
            }

            Object val = row.get("PARENT_ID");
            Long parentId = null;
            if (val != null) {
                parentId = Long.parseLong(val.toString());
            }

			MenuTreeDto node = new MenuTreeDto();
			node.label = (String) row.get("LABEL");
			node.path = (String) row.get("PATH");

            Object valType = row.get("TYPE");
            node.type = valType != null ? Integer.parseInt(valType.toString()) : 99;

			node.icon = (String) row.get("ICON");
			node.is_accessible = ((Number) row.get("IS_ACCESSIBLE")).intValue();
			nodeMap.put(id, node);

			// Simpan temp sementara jika parent belum ada
			if (tempChild.containsKey(id)) {
				node.children.addAll(tempChild.get(id));
				tempChild.remove(id);
			}

			if (parentId == 0) {
				roots.add(node);
			} else {
				MenuTreeDto parent = nodeMap.get(parentId);
				if (parent != null) {
					parent.children.add(node);
				} else {
					//cek udah ada di tempChild berdasarkan parent id
					if (!tempChild.containsKey(parentId)) {
						tempChild.put(parentId, new ArrayList<>());
					}
					tempChild.get(parentId).add(node);
				}
			}
		}
        return sortMenu(roots);
	}


    private List<MenuTreeDto> sortMenu(List<MenuTreeDto> menu){

        List<String> customOrder = Arrays.asList("Master Data", "SPAJK Search", "SPAJK Data Entry", "Workbasket", "Billing Information");

            Map<String, Integer> orderMap = new HashMap<>();
            for (int i = 0; i < customOrder.size(); i++) {
                orderMap.put(customOrder.get(i), i);
            }

            menu.sort(Comparator.comparingInt(m ->
                    orderMap.getOrDefault(m.label, Integer.MAX_VALUE)
            ));

        return menu;
    }


	@Transactional
	public ChangePasswordResponse changePassword(String token) {

		// validate input
		if (token == null || token.isEmpty()) {
			return ChangePasswordResponse.builder()
					.code(HttpStatus.UNAUTHORIZED.value())
					.message("Authorization token is missing or empty")
					.build();
		}

		// Extract username from token
		boolean isExpired = isTokenExpired(token);
		Long userId = jwtUtil.getAllClaimsFromToken(token).get("user_id", Long.class);
		String username =  jwtUtil.getUsernameFromJwtToken(token);
		if (userId == null || isExpired) {
			return ChangePasswordResponse.builder()
					.code(HttpStatus.UNAUTHORIZED.value())
					.message("Unauthorized or token expired")
					.build();
		}

		User user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			return ChangePasswordResponse.builder()
					.code(HttpStatus.UNAUTHORIZED.value())
					.message("User not found")
					.build();
		}

		// CHANGE PASSWORD
		PasswordComplexityApiResponse<ChangePasswordData> changePasswordResponse = passwordComplexityService.changePassword(username);

		if (changePasswordResponse.isError()) {
			return ChangePasswordResponse.builder()
					.code(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.message(changePasswordResponse.getMessage())
					.build();
		}

		HashMap<String, Object> dataResp = new HashMap<String, Object>();
		dataResp.put("url", changePasswordResponse.getData().getUrl());

        return ChangePasswordResponse.builder()
                .code(HttpStatus.OK.value())
                .message(changePasswordResponse.getMessage())
				.data(dataResp)
                .build();
    }
}
