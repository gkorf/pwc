/*
 * Copyright 2011 GRNET S.A. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 *   1. Redistributions of source code must retain the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials
 *      provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY GRNET S.A. ``AS IS'' AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GRNET S.A OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and
 * documentation are those of the authors and should not be
 * interpreted as representing official policies, either expressed
 * or implied, of GRNET S.A.
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
