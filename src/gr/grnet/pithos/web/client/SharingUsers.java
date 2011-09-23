package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.foldertree.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class SharingUsers extends Resource {
	private List<String> users;

	public static SharingUsers createFromResponse(Response response, SharingUsers result) {
		SharingUsers u;
		if (result == null)
			u = new SharingUsers();
		else
			u = result;
		u.populate(response);
		return u;
	}

	private void populate(Response response) {
		users = new ArrayList<String>();
        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONArray array = json.isArray();
        if (array != null) {
            for (int i=0; i<array.size(); i++) {
                JSONObject o = array.get(i).isObject();
                if (o != null) {
                	users.add(unmarshallString(o, "name"));
                }
            }
        }
	}

	public List<String> getUsers() {
		return users;
	}

	@Override
	public Date getLastModified() {
		return null;
	}
}
