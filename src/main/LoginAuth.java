package main;

public class LoginAuth {

	String UUID;
	String IMEI;
	String PhoneNo;
	String AuthCode;
	
	public LoginAuth(){
		UUID = "";
		IMEI = "";
		PhoneNo = "";
		AuthCode = "";
	}
	
	public LoginAuth(String UUID, String IMEI, String PhoneNo, String AuthCode){
		this.UUID = UUID;
		this.IMEI = IMEI;
		this.PhoneNo = PhoneNo;
		this.AuthCode = AuthCode;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(CharSequence uuid) {
		UUID = uuid.toString();
	}

	public String getIMEI() {
		return IMEI;
	}

	public void setIMEI(CharSequence imei) {
		IMEI = imei.toString();
	}

	public String getPhoneNo() {
		return PhoneNo;
	}

	public void setPhoneNo(CharSequence phoneNo) {
		PhoneNo = phoneNo.toString();
	}

	public String getAuthCode() {
		return AuthCode;
	}

	public void setAuthCode(CharSequence authCode) {
		AuthCode = authCode.toString();
	}
}
