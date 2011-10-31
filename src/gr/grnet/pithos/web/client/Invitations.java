package gr.grnet.pithos.web.client;

import java.util.Date;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import gr.grnet.pithos.web.client.foldertree.Resource;

public class Invitations extends Resource {

	private int invitationsLeft = 0;
	
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

	public static Invitations createFromResponse(Response response) {
		Invitations result = new Invitations();
		result.populate(response);
		return result;
	}

	private void populate(Response response) {
        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONObject o = json.isObject();
        if (o != null)
        	invitationsLeft = unmarshallInt(o, "invitations");
	}

}
