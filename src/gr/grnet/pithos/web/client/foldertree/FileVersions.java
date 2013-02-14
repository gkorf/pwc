/*
 * Copyright 2011-2013 GRNET S.A. All rights reserved.
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
package gr.grnet.pithos.web.client.foldertree;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import gr.grnet.pithos.web.client.Resource;

public class FileVersions extends Resource {
	private List<Version> versions;
	
	public List<Version> getVersions() {
		return versions;
	}

	public static FileVersions createFromResponse(Response response) {
		FileVersions res =  new FileVersions();
		res.populate(response);
		return res;
	}

	private void populate(Response response) {
		versions = new ArrayList<Version>();
        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONArray array = json.isObject().get("versions").isArray();
        if (array != null) {
            for (int i=0; i<array.size(); i++) {
                JSONArray o = array.get(i).isArray();
                if (o != null) {
                	int num = (int) o.get(0).isNumber().doubleValue();
                	Date date = null;
                	JSONNumber n = o.get(1).isNumber();
                	if (n != null)
                		date = new Date((long) (n.doubleValue() * 1000)); //Convert to millis
                	else {
                		JSONString s = o.get(1).isString();
                		if (s != null)
                			date = new Date((long) (Double.parseDouble(s.stringValue()) * 1000));
                	}
                	Version v = new Version(num, date);
                	versions.add(v);
                }
            }
        }
	}

	@Override
	public Date getLastModified() {
		return null;
	}
}
