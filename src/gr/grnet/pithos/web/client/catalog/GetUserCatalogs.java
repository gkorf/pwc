package gr.grnet.pithos.web.client.catalog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import gr.grnet.pithos.web.client.Helpers;
import gr.grnet.pithos.web.client.Pithos;

import java.util.List;

/**
 * The request via which we obtain user catalog info.
 */
public class GetUserCatalogs implements Scheduler.ScheduledCommand {
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
        System.out.println("GetUserCatalogs: " + result);
    }

    public void onBadStatusCode(Request request, Response response) {

    }

    public void onError(Request request, Throwable t) {
        GWT.log("GetUserCatalogs", t);
        System.out.print("GetUserCatalogs: " + t.getClass().getName() + ": " + t.getMessage());
    }

    @Override
    public void execute() {
        final RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, getURL());
        rb.setHeader("X-Auth-Token", userToken);
        final String requestData = makeRequestData().toString();

        try {
            System.out.println("==================================");
            System.out.println("POST " + getURL());
            System.out.println("==================================");
            rb.sendRequest(requestData, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    final int statusCode = response.getStatusCode();

                    if(statusCode != SuccessCode) {
                        GetUserCatalogs.this.onBadStatusCode(request, response);
                        return;
                    }

                    final String responseText = response.getText();
                    final JSONValue jsonValue = JSONParser.parseStrict(responseText);
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
            GWT.log("GetUserCatalogs", e);
        }
    }

    public void scheduleDeferred() {
        Scheduler.get().scheduleDeferred(this);
    }
}
