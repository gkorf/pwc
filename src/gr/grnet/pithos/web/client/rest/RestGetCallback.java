/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class RestGetCallback implements RequestCallback {

	private static final int HTTP_OK = 200;

	private AsyncCallback callback;
	private String path;
	private int okcode = -1;

	public RestGetCallback(String aPath, AsyncCallback aCallback) {
		callback = aCallback;
		path = aPath;
	}

	public RestGetCallback(String aPath, AsyncCallback aCallback, int okCode) {
		callback = aCallback;
		path = aPath;
		okcode = okCode;
	}

	@Override
	public void onError(Request request, Throwable exception) {
		callback.onFailure(exception);
	}

	@Override
	public void onResponseReceived(Request request, Response response) {
		try {
			if (okcode == -1 && response.getStatusCode() == HTTP_OK)
				callback.onSuccess(deserialize(response));
			//this one is only used for trash handling where empty trash has 201 status code
			else if(okcode !=-1 && (response.getStatusCode() == okcode || response.getStatusCode() == HTTP_OK))
				callback.onSuccess(deserialize(response));
			else
				callback.onFailure(new RestException(path, response.getStatusCode(), response.getStatusText(), response.getText()));
		} catch (Exception e) {
			callback.onFailure(e);
		}
	}

	public abstract Object deserialize(Response response);

}
