/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest.resource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * A container for the results of a search query for users.
 *
 */
public class UserSearchResource extends RestResource {

	public UserSearchResource(String aUri) {
		super(aUri);
	}

	List<UserResource> users = new ArrayList<UserResource>();

	@Override
	public void createFromJSON(String text) {
		JSONArray json = JSONParser.parse(text).isArray();
		if (json != null)
			for (int i = 0; i < json.size(); i++) {
				JSONObject j = json.get(i).isObject();
				if (j != null) {
					String username = unmarshallString(j, "username");
					String name = unmarshallString(j, "name");
					String home = unmarshallString(j, "home");
					UserResource user = new UserResource(home);
					user.setName(name);
					user.setUsername(username);
					users.add(user);
				}
			}
	}

	/**
	 * Retrieve the users.
	 *
	 * @return the users
	 */
	public List<UserResource> getUsers() {
		return users;
	}

	/**
	 * Modify the users.
	 *
	 * @param newUsers the users to set
	 */
	public void setUsers(List<UserResource> newUsers) {
		users = newUsers;
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
}
