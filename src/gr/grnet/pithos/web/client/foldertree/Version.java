package gr.grnet.pithos.web.client.foldertree;

import java.util.Date;

public class Version {
	private int version;
	
	private Date date;
	
	public Version(int v, Date d) {
		version = v;
		date = d;
	}

	public int getVersion() {
		return version;
	}

	public Date getDate() {
		return date;
	}
}
