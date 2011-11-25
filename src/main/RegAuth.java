package main;

public class RegAuth {

	String Username;
	String Password;
	String RegCode;
	
	public RegAuth(){
		this.Username = "";
		this.Password = "";
		this.RegCode = "";
	}
	
	public RegAuth(String user, String pass, String reg){
		this.Username = user;
		this.Password = pass;
		this.RegCode = reg;
	}
	public String getUsername() {
		return Username;
	}

	public void setUsername(CharSequence username) {
		Username = username.toString();
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(CharSequence password) {
		Password = password.toString();
	}

	public String getRegCode() {
		return RegCode;
	}

	public void setRegCode(CharSequence regCode) {
		RegCode = regCode.toString();
	}


	
}
