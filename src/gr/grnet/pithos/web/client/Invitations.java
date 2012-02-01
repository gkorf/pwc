/*
 * Copyright 2011-2012 GRNET S.A. All rights reserved.
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
package gr.grnet.pithos.web.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import gr.grnet.pithos.web.client.foldertree.Resource;

public class Invitations extends Resource {

	private int invitationsLeft = 0;
	
	private List<Invitation> sentInvitations = new ArrayList<Invitation>();
	
	@Override
	public Date getLastModified() {
		return null;
	}

	public int getInvitationsLeft() {
		return invitationsLeft;
	}

	public static Invitations createFromResponse(Response response) {
		Invitations result = new Invitations();
		result.populate(response);
		return result;
	}

	private void populate(Response response) {
        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONObject o = json.isObject();
        if (o != null) {
        	invitationsLeft = unmarshallInt(o, "invitations");
        	JSONArray sent = o.get("sent").isArray();
        	if (sent != null) {
        		for (int i=0; i<sent.size(); i++) {
        			JSONObject ob = sent.get(i).isObject();
        			if (ob != null) {
        				boolean accepted = unmarshallBoolean(ob, "is_accepted");
        				String email = unmarshallString(ob, "email");
        				String realname = unmarshallString(ob, "realname");
        				Invitation inv = new Invitation(accepted, realname, email);
        				sentInvitations.add(inv);
        			}
        		}
        	}
        }
	}

	public List<Invitation> getSentInvitations() {
		return sentInvitations;
	}

}
