/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest.resource;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;


public class GroupUserResource extends RestResource{

	public GroupUserResource(String aUri) {
		super(aUri);
	}

	String username;
	String name;
	String home;

	/**
	 * Retrieve the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Modify the username.
	 *
	 * @param aUsername the username to set
	 */
	public void setUsername(String aUsername) {
		username = aUsername;
	}

	/**
	 * Retrieve the name.
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Modify the name.
	 *
	 * @param aName the name to set
	 */
	public void setName(String aName) {
		name = aName;
	}

	/**
	 * Retrieve the home.
	 *
	 * @return the home
	 */
	public String getHome() {
		return home;
	}

	/**
	 * Modify the home.
	 *
	 * @param aHome the home to set
	 */
	public void setHome(String aHome) {
		home = aHome;
	}

	@Override
	public void createFromJSON(String text) {
		JSONObject json = (JSONObject) JSONParser.parse(text);
		name = unmarshallString(json, "name");
		home = unmarshallString(json, "home");
		username = unmarshallString(json, "username");
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
}
