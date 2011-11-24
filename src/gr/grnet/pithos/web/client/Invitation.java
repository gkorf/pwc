package gr.grnet.pithos.web.client;

public class Invitation {
	private boolean accepted;
	private String realname;
	private String email;
	
	public Invitation(boolean accepted, String realname, String email) {
		this.accepted = accepted;
		this.realname = realname;
		this.email = email;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public String getRealname() {
		return realname;
	}

	public String getEmail() {
		return email;
	}
}
