/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest.resource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;

public class GroupResource extends RestResource {

	public GroupResource(String aUri) {
		super(aUri);
	}

	List<String> userPaths = new ArrayList<String>();

	/**
	 * Retrieve the userPaths.
	 *
	 * @return the userPaths
	 */
	public List<String> getUserPaths() {
		return userPaths;
	}

	/**
	 * Modify the userPaths.
	 *
	 * @param newUserPaths the userPaths to set
	 */
	public void setUserPaths(List<String> newUserPaths) {
		userPaths = newUserPaths;
	}

	@Override
	public void createFromJSON(String text) {
		JSONArray array = (JSONArray) JSONParser.parse(text);
		if (array != null)
			for (int i = 0; i < array.size(); i++)
				if(array.get(i).isString() != null)
					getUserPaths().add(array.get(i).isString().stringValue());
	}
	@Override
	public String getName() {
		String[] names = uri.split("/");
		return URL.decodeComponent(names[names.length - 1]);
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
}
