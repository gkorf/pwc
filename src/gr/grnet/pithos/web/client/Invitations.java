package gr.grnet.pithos.web.client;

import java.util.Date;

import gr.grnet.pithos.web.client.foldertree.Resource;

public class Invitations extends Resource {

	private int invitationsLeft;
	
	@Override
	public Date getLastModified() {
		return null;
	}

	public int getInvitationsLeft() {
		return invitationsLeft;
	}

	public void setInvitationsLeft(int invitationsLeft) {
		this.invitationsLeft = invitationsLeft;
	}

}
