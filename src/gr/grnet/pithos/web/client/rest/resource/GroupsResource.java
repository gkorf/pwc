/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest.resource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class GroupsResource extends RestResource {

	public GroupsResource(String aUri) {
		super(aUri);
	}

	List<String> groupPaths = new ArrayList<String>();

	/**
	 * Retrieve the groupPaths.
	 *
	 * @return the groupPaths
	 */
	public List<String> getGroupPaths() {
		return groupPaths;
	}

	/**
	 * Modify the groupPaths.
	 *
	 * @param newGroupPaths the groupPaths to set
	 */
	public void setGroupPaths(List<String> newGroupPaths) {
		groupPaths = newGroupPaths;
	}

	@Override
	public void createFromJSON(String text) {
		JSONArray array = (JSONArray) JSONParser.parse(text);
		if (array != null)
			for (int i = 0; i < array.size(); i++) {
				JSONObject js = array.get(i).isObject();
				if(js != null){
					String groupUri = unmarshallString(js, "uri");
					if(groupUri != null)
						getGroupPaths().add(groupUri);
				}
			}
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
}
