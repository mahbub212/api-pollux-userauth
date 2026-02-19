package id.co.msiglife.pollux.util;

public class StaticValue {

	 public final static int LOGIN_CODE_RESP_SUCCESS = 200;
	 public final static int LOGIN_CODE_RESP_INACTIVE = 403;
	 public final static int LOGIN_CODE_RESP_INCORRECT_PASSWORD = 401;
	 public final static int LOGIN_CODE_RESP_NOT_FOUND = 404;
	 public final static int CHECK_ROLE_CODE_RESP_SUCCESS = 200;
	 public final static int CHECK_ROLE_CODE_RESP_NOT_FOUND = 404;
	 public final static int BLOCKED_ACCOUNT_CODE = 401;
	 
	 public final static String LOGIN_MSG_RESP_SUCCESS = "Login Success";
	 public final static String LOGIN_MSG_RESP_INACTIVE = "User is Inactive or Locked";
	 public final static String LOGIN_MSG_RESP_INCORRECT_PASSWORD = "Incorrect Password";
	 public final static String LOGIN_MSG_RESP_NOT_FOUND = "User Not Found";
	 public final static String CHECK_ROLE_MSG_RESP_SUCCESS = "Berhasil mendapatkan Access List";
	 public final static String CHECK_ROLE_MSG_RESP_NOT_FOUND = "Role ID tidak ditemukan";
	 public final static String BLOCKED_ACCOUNT_MSG = "Akun anda terblokir";
	 
	 public final static String SECRET_KEY_LOGIN_TOKEN = "rdwPjGQK1IiCEd2QinzcwZJNTne3DbtN";
	 public final static String EXP_TIME_LOGIN_TOKEN = "24"; // expiration times in hour - catch to the next day, the set it to 3 am
	 public final static String TIMEZONE = "Asia/Jakarta";
}
