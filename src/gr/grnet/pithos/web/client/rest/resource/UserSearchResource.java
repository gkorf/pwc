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
