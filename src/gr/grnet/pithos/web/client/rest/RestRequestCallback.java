/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.rest;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import gr.grnet.pithos.web.client.foldertree.Resource;

public abstract class RestRequestCallback<T extends Resource> implements RequestCallback {

    private static final int HTTP_OK = 200;
    private int okcode = -1;
    private String path;

    public RestRequestCallback(String path, int okCode) {
        this.path = path;
        this.okcode = okCode;
    }

    public RestRequestCallback(String path) {
        this(path, -1);
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
        try {
            if (response.getStatusCode() == HTTP_OK || (okcode !=-1 && response.getStatusCode() == okcode))
                onSuccess(deserialize(response));
            else {
                String statusText = "";
                String text = "";
                // Ignore JavaScript errors caused by non-existent text.
                try {
                    statusText = response.getStatusText();
                }
                catch (Exception e) {}

                try {
                    text = response.getText();
                }
                catch (Exception e) {}

                onError(request, new RestException(path, response.getStatusCode(), statusText, text));
            }
        } catch (Exception e) {
            onError(request, e);
        }
    }

    public abstract void onSuccess(T result);

    public abstract T deserialize(Response response);
}
