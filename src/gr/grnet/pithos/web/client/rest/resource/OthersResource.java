/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest.resource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.TreeItem;

public class OthersResource extends RestResource {

	public OthersResource(String aUri) {
		super(aUri);
	}

	List<String> others = new ArrayList<String>();
	List<OtherUserResource> otherUsers = new ArrayList<OtherUserResource>();

	/**
	 * Retrieve the others.
	 *
	 * @return the others
	 */
	public List<String> getOthers() {
		return others;
	}

	/**
	 * Modify the others.
	 *
	 * @param newOthers the others to set
	 */
	public void setOthers(List<String> newOthers) {
		others = newOthers;
	}

	public List<OtherUserResource> getOtherUsers() {
		return otherUsers;
	}

	public void setOtherUsers(List<OtherUserResource> newOtherUsers) {
		otherUsers = newOtherUsers;
	}

	@Override
	public void createFromJSON(String text) {
		JSONArray array = (JSONArray) JSONParser.parse(text);
		if (array != null)
			for (int i = 0; i < array.size(); i++) {
				JSONObject js = array.get(i).isObject();
				if (js != null) {
					String othersUri = unmarshallString(js, "uri");
					String username = unmarshallString(js, "username");
					if(othersUri != null){
						getOthers().add(othersUri);
						OtherUserResource r = new OtherUserResource(othersUri);
						r.setUsername(username);
						getOtherUsers().add(r);
					}
				}
			}
	}

	public String getUsernameOfUri(String u){
		if(!u.endsWith("/"))
			u=u+"/";
		for(OtherUserResource o : getOtherUsers()){
			GWT.log("CHECKING USER URI:"+o.getUri(), null);
			String toCheck = o.getUri();
			if(!toCheck.endsWith("/"))
				toCheck=toCheck+"/";
			if(toCheck.equals(u))
				return o.getUsername();
		}
		return null;
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}

	@Override
	public String constructUri(TreeItem treeItem,String path){
		String constructedUri = "Files/"+ path.substring(path.lastIndexOf("/")+1) + "others/";
		return constructedUri;
	}
}
