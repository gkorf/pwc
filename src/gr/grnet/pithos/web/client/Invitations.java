package gr.grnet.pithos.web.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import gr.grnet.pithos.web.client.foldertree.Resource;

public class Invitations extends Resource {

	private int invitationsLeft = 0;
	
	private List<Invitation> sentInvitations = new ArrayList<Invitation>();
	
	@Override
	public Date getLastModified() {
		return null;
	}

	public int getInvitationsLeft() {
		return invitationsLeft;
	}

	public static Invitations createFromResponse(Response response) {
		Invitations result = new Invitations();
		result.populate(response);
		return result;
	}

	private void populate(Response response) {
        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONObject o = json.isObject();
        if (o != null) {
        	invitationsLeft = unmarshallInt(o, "invitations");
        	JSONArray sent = o.get("sent").isArray();
        	if (sent != null) {
        		for (int i=0; i<sent.size(); i++) {
        			JSONObject ob = sent.get(i).isObject();
        			if (ob != null) {
        				boolean accepted = unmarshallBoolean(ob, "is_accepted");
        				String email = unmarshallString(ob, "email");
        				String realname = unmarshallString(ob, "realname");
        				Invitation inv = new Invitation(accepted, realname, email);
        				sentInvitations.add(inv);
        			}
        		}
        	}
        }
	}

	public List<Invitation> getSentInvitations() {
		return sentInvitations;
	}

}
