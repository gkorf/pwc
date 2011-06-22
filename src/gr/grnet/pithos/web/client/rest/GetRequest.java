/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.rest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import gr.grnet.pithos.web.client.foldertree.Resource;
import java.util.HashMap;
import java.util.Map;

public abstract class GetRequest<T extends Resource> implements ScheduledCommand {

    private Class<T> aClass;

    private String path;

    private int okCode;
    
    private String username;

    private T cached;

    private T result;

    private Map<String, String> headers = new HashMap<String, String>();

    public abstract void onSuccess(T result);

    public abstract void onError(Throwable t);

    public GetRequest(Class<T> aClass, String path, int okCode, T result) {
        this.aClass = aClass;
        this.path = path;
        this.okCode = okCode;
        this.result = result;
    }

    public GetRequest(Class<T> aClass, String path) {
        this(aClass, path, -1, null);
    }

    public GetRequest(Class<T> aClass, String path, T result) {
        this(aClass, path, -1, result);
    }

    @Override
    public void execute() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, path);
        for (String header : headers.keySet())
            builder.setHeader(header, headers.get(header));
        try {
            builder.sendRequest("", new RestRequestCallback<T>(path, okCode) {
                @Override
                public void onSuccess(T object) {
                    GetRequest.this.onSuccess(object);
                }

                @Override
                public T deserialize(Response response) {
                    return Resource.createFromResponse(aClass, response, result);
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    if (throwable instanceof RestException) {
                        if (((RestException) throwable).getHttpStatusCode() == 304 && cached != null){
                            GWT.log("Using cache: " + cached.getUri(), null);
                            onSuccess(cached);
                            return;
                        }
                    }
                    GetRequest.this.onError(throwable);
                }
            });
        }
        catch (RequestException e) {
        }
    }

    public void setHeader(String header, String value) {
        headers.put(header, value);
    }
}
