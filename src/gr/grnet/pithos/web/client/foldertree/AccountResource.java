/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.foldertree;

import com.google.gwt.http.client.Header;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: chstath Date: 5/19/11 Time: 2:55 PM To change this template use File | Settings |
 * File Templates.
 */
public class AccountResource extends Resource {

    private List<ContainerResource> containers = new ArrayList<ContainerResource>();

    @Override
    public String getLastModifiedSince() {
        return null;
    }

    public List<ContainerResource> getContainers() {
        return containers;
    }
}
