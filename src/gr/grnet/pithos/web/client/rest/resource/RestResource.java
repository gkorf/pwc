/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest.resource;

import java.io.Serializable;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.TreeItem;


public abstract class RestResource implements Serializable{
	String uri;

	public RestResource(String aUri) {
		super();
		setUri(aUri);
	}

	/**
	 * Retrieve the uri.
	 *
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Modify the uri.
	 *
	 * @param aUri the path to set
	 */
	public void setUri(String aUri) {
		uri = aUri;
		if (uri!=null) {
			// Remove any parameter part
			int qm = uri.indexOf('?');
			if (qm>=0) uri = uri.substring(0, qm);
		}
	}

	public abstract void createFromJSON(String text);

	protected String unmarshallString(JSONObject obj, String key){
		if(obj.get(key) != null)
			if(obj.get(key).isString() != null)
				return obj.get(key).isString().stringValue();
		return null;
	}
	
	protected int unmarshallInt(JSONObject obj, String key){
		if(obj.get(key) != null)
			if(obj.get(key).isNumber() != null)
				return (int) obj.get(key).isNumber().getValue();
		return -1;
	}

	protected boolean unmarshallBoolean(JSONObject obj, String key){
		if(obj.get(key) != null)
			if(obj.get(key).isBoolean() != null)
				return obj.get(key).isBoolean().booleanValue();
		return false;
	}

	public static native String getDate(Long ms)/*-{
		return (new Date(ms)).toUTCString();
	}-*/;

	public abstract String getLastModifiedSince();

	public String constructUri(@SuppressWarnings("unused") TreeItem treeItem, @SuppressWarnings("unused") String path){
		return "";
	}

	public String getName(){
		String[] names = uri.split("/");
		return names[names.length -1];
	}
}
