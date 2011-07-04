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
