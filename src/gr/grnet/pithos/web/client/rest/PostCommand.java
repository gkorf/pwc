/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest;

import gr.grnet.pithos.web.client.GSS;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;


public abstract class PostCommand extends RestCommand{
	boolean complete = false;
	String postBody=null;

	public PostCommand(final String path, String data, final int okStatusCode) {
		this(path, data, okStatusCode, true);
	}

	public PostCommand(final String path, String data, final int okStatusCode, boolean showLoading) {
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator("Updating ",path);

		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, path);

		try {
			handleHeaders(builder, path);
			builder.sendRequest(data, new RequestCallback() {

				@Override
				public void onError(Request arg0, Throwable arg1) {
					complete = true;
					PostCommand.this.onError(arg1);
				}

				@Override
				public void onResponseReceived(Request req, Response resp) {
					complete=true;
					int status = resp.getStatusCode();
					// Normalize IE status 1223 to a regular 204.
					if (status == 1223)
						status = 204;

					if (status == okStatusCode) {
						postBody = resp.getText();
						onComplete();
					} else if (status == 403)
						sessionExpired();
					else
						PostCommand.this.onError(new RestException(path, status, resp.getStatusText(), resp.getText()));
				}

			});
		} catch (Exception ex) {
			complete=true;
			onError(ex);
		}
	}

	public boolean isComplete() {
		return complete;
	}

	@Override
	public boolean execute() {
		boolean com = isComplete();
		if (com) {
			if (isShowLoadingIndicator())
				GSS.get().hideLoadingIndicator();
			return false;
		}
		return true;
	}

	public String getPostBody() {
		return postBody;
	}

}
