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

package gr.grnet.pithos.web.client.catalog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import gr.grnet.pithos.web.client.Const;
import gr.grnet.pithos.web.client.Helpers;
import gr.grnet.pithos.web.client.Pithos;

import java.util.List;

/**
 * The request via which we obtain user catalog info.
 */
public class GetUserCatalogs implements Scheduler.ScheduledCommand {
    private final Pithos app;
    private final String url;
    private final String userToken;
    private final List<String> ids;
    private final List<String> names;

    public static final int SuccessCode = 200;
    public static final String CallEndPoint = "/user_catalogs";
    public static final String RequestField_uuids = "uuids";
    public static final String RequestField_displaynames = "displaynames";
    public static final String ResponseField_displayname_catalog = "displayname_catalog";
    public static final String ResponseField_uuid_catalog = "uuid_catalog";

    public GetUserCatalogs(Pithos app) {
        this(app, null, null);
    }

    public GetUserCatalogs(Pithos app, String userID) {
        this(app, Helpers.toList(userID), null);
    }

    public GetUserCatalogs(Pithos app, List<String> ids) {
        this(app, ids, null);
    }

    public GetUserCatalogs(Pithos app, List<String> ids, List<String> names) {
        assert app != null;

        this.app = app;

        // FIXME: Probably use Window.Location.getHost()
        // https://server.com/v1/ --> https://server.com
        String path = app.getApiPath();
        path = Helpers.stripTrailing(path, "/");
        path = Helpers.upToIncludingLastPart(path, "/");
        path = Helpers.stripTrailing(path, "/");

        // https://server.com/user_catalogs
        this.url = path + CallEndPoint;

        this.ids = Helpers.safeList(ids);
        this.names = Helpers.safeList(names);
        this.userToken = app.getUserToken();
    }

    public String getURL() {
        return url;
    }

    private JSONObject makeRequestData() {
        final JSONObject root = new JSONObject();
        final JSONArray uuids = Helpers.listToJSONArray(ids);
        final JSONArray displaynames = Helpers.listToJSONArray(names);

        root.put(RequestField_uuids, uuids);
        root.put(RequestField_displaynames, displaynames);

        return root;
    }

    public UserCatalogs parseResult(JSONObject result) {
        final UserCatalogs userCatalogs = new UserCatalogs();
        final JSONObject uuid_catalog = result.get(ResponseField_uuid_catalog).isObject();
        final JSONObject displayname_catalog = result.get(ResponseField_displayname_catalog).isObject();

        for(String uuid: uuid_catalog.keySet()) {
            final String name = uuid_catalog.get(uuid).isString().stringValue();
            userCatalogs.updateWithIDAndName(uuid, name);
        }

        for(String name: displayname_catalog.keySet()) {
            final String uuid = displayname_catalog.get(name).isString().stringValue();
            userCatalogs.updateWithIDAndName(uuid, name);
        }

        return userCatalogs;
    }

    public void onSuccess(Request request, Response response, JSONObject result, UserCatalogs userCatalogs) {
    }

    public void onBadStatusCode(Request request, Response response) {
    }

    public void onError(Request request, Throwable t) {
        app.LOG("GetUserCatalogs", t);
    }

    @Override
    public void execute() {
        final RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, getURL());
        rb.setHeader(Const.X_AUTH_TOKEN, userToken);
        final String requestData = makeRequestData().toString();
        app.LOG("GetUserCatalogs => ", requestData);
        Pithos.LOG("POST ", getURL());

        try {
            rb.sendRequest(requestData, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    final int statusCode = response.getStatusCode();

                    if(statusCode != SuccessCode) {
                        app.LOG("GetUserCatalogs <= [", statusCode, " ", response.getStatusText(), "]");
                        GetUserCatalogs.this.onBadStatusCode(request, response);
                        return;
                    }

                    final String responseText = response.getText();
                    final JSONValue jsonValue = JSONParser.parseStrict(responseText);
                    app.LOG("GetUserCatalogs <= ", jsonValue.toString());
                    final JSONObject result = jsonValue.isObject();

                    if(result == null) {
                        GetUserCatalogs.this.onError(request, new Exception("GetUserCatalogs: json result is not an object"));
                        return;
                    }

                    final UserCatalogs userCatalogs = parseResult(result);

                    GetUserCatalogs.this.onSuccess(request, response, result, userCatalogs);
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    GetUserCatalogs.this.onError(request, exception);
                }
            });
        }
        catch(Exception e) {
            app.LOG("GetUserCatalogs", e);
        }
    }

    public void scheduleDeferred() {
        Scheduler.get().scheduleDeferred(this);
    }
}
