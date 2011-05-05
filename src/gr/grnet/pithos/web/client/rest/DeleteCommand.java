/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest;

import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.InsufficientPermissionsException;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public abstract class DeleteCommand extends RestCommand{

	boolean complete = false;

	public DeleteCommand(String pathToDelete){
		this(pathToDelete, true);
	}


	public DeleteCommand(String pathToDelete, boolean showLoading){
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator("Deleting ",pathToDelete);
		final String path;
		if(pathToDelete.endsWith("/"))
			path = pathToDelete;
		else
			path = pathToDelete+"/";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE, path);

		try {
			handleHeaders(builder, path);
			builder.sendRequest("", new RequestCallback() {

				@Override
				public void onError(Request arg0, Throwable arg1) {
					complete = true;
					DeleteCommand.this.onError(arg1);
				}

				@Override
				public void onResponseReceived(Request arg0, Response arg1) {
					complete=true;
					if(arg1.getStatusCode() == 204)
						onComplete();
					else if(arg1.getStatusCode() == 403)
						sessionExpired();
					else if(arg1.getStatusCode() == 405)
						DeleteCommand.this.onError(new InsufficientPermissionsException("You don't have permissions to delete this resource"));
					else
						DeleteCommand.this.onError(new RestException(path, arg1.getStatusCode(), arg1.getStatusText(), arg1.getText()));
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
		if(com){
			if(isShowLoadingIndicator())
				GSS.get().hideLoadingIndicator();
			return false;
		}
		return true;
	}

}
