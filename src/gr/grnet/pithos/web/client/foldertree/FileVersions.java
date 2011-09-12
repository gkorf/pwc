package gr.grnet.pithos.web.client.foldertree;

import gr.grnet.pithos.web.client.SharingUsers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

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
                	Date date = new Date((long) o.get(1).isNumber().doubleValue());
                	Version v = new Version(num, date);
                	versions.add(v);
                }
            }
        }
	}
}
