/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest.resource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;


public class TagsResource extends RestResource{

	public TagsResource(String aUri) {
		super(aUri);
	}

	List<String> tags = new ArrayList<String>();

	/**
	 * Retrieve the tags.
	 *
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * Modify the tags.
	 *
	 * @param newTags the tags to set
	 */
	public void setTags(List<String> newTags) {
		tags = newTags;
	}

	@Override
	public void createFromJSON(String text) {
		JSONArray array = (JSONArray) JSONParser.parse(text);
		if(array != null)
			for (int i = 0; i < array.size(); i++)
				getTags().add(array.get(i).isString().stringValue());
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
}
