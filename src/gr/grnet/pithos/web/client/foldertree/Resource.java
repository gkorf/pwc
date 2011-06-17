/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.foldertree;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import java.util.Date;

public abstract class Resource {

    String uri;

    public String getUri() {
        return uri;
    }

    protected static String unmarshallString(JSONObject obj, String key){
        if(obj.get(key) != null) {
            JSONString s = obj.get(key).isString();
            if(s != null)
                return s.stringValue();
        }
        return null;
    }

    protected static int unmarshallInt(JSONObject obj, String key){
        if(obj.get(key) != null)
            if(obj.get(key).isNumber() != null)
                return (int) obj.get(key).isNumber().getValue();
        return -1;
    }

    protected static long unmarshallLong(JSONObject obj, String key){
        if(obj.get(key) != null) {
            JSONNumber value = obj.get(key).isNumber();
            if(value != null)
                return (long) value.doubleValue();
        }
        return -1;
    }

    protected static boolean unmarshallBoolean(JSONObject obj, String key){
        if(obj.get(key) != null)
            if(obj.get(key).isBoolean() != null)
                return obj.get(key).isBoolean().booleanValue();
        return false;
    }

    protected static Date unmarshallTimestamp(JSONObject obj, String key){
        if(obj.get(key) != null) {
            JSONString s = obj.get(key).isString();
            if (s != null)
                return new Date(Long.valueOf(s.stringValue()).longValue());
        }
        return null;
    }

    protected static Date unmarshallDate(JSONObject obj, String key){
        if(obj.get(key) != null) {
            JSONString s = obj.get(key).isString();
            if (s != null)
                return DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss").parse(s.stringValue());
        }
        return null;
    }

    public static native String getDate(Long ms)/*-{
        return (new Date(ms)).toUTCString();
    }-*/;

    public abstract String getLastModifiedSince();

    public static <T> T createFromResponse(Class<T> aClass, Response response) {
        JSONValue json = JSONParser.parseStrict(response.getText());
        T result = null;
        if (aClass.equals(AccountResource.class)) {
            AccountResource a = new AccountResource();
            JSONArray array = json.isArray();
            if (array != null) {
                for (int i=0; i<array.size(); i++) {
                    JSONObject o = array.get(i).isObject();
                    if (o != null) {
                        ContainerResource container = new ContainerResource();
                        container.setName(unmarshallString(o, "name"));
                        container.setCount(unmarshallLong(o, "count"));
                        container.setBytes(unmarshallLong(o, "bytes"));
                        container.setCreated(unmarshallTimestamp(o, "created"));
                        container.setLastModified(unmarshallDate(o, "last_modified"));
                        a.getContainers().add(container);
                    }
                }
                result = (T) a;
            }
        }
        return result;
    }
}
