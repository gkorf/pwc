/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;


public abstract class RestCallback  implements RequestCallback {

	private static final int HTTP_OK = 200;
	private String path;
	private int okcode = -1;

	public RestCallback(String aPath) {
		path = aPath;
	}

	public RestCallback(String aPath, int okCode) {
		path = aPath;
		okcode = okCode;
	}

	@Override
	public void onError(Request request, Throwable exception) {
		handleError(request, exception);
	}

	@Override
	public void onResponseReceived(Request request, Response response) {
		try {
			if (okcode == -1 && response.getStatusCode() == HTTP_OK)
				handleSuccess(deserialize(response));
			//this one is only used for trash handling where empty trash has 201 status code
			else if(okcode !=-1 && (response.getStatusCode() == okcode || response.getStatusCode() == HTTP_OK))
				handleSuccess(deserialize(response));
			else if(response.getStatusCode() == 403)
				RestCommand.sessionExpired();
			else {
				String statusText = "";
				String text = "";
				// Ignore JavaScript errors caused by non-existent text.
				try {
					statusText = response.getStatusText();
				} catch (Exception e) {	}
				try {
					text = response.getText();
				} catch (Exception e) {	}
				handleError(request, new RestException(path, response.getStatusCode(), statusText, text));
			}
		} catch (Exception e) {
			handleError(request,e);
		}
	}

	public abstract void handleSuccess(Object object);

	public abstract void handleError(Request request, Throwable exception);

	public abstract Object deserialize(Response response);

}
