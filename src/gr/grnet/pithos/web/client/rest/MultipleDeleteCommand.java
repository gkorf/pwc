/*
 * Copyright 2009 Electronic Business Systems Ltd.
 *
 * This file is part of GSS.
 *
 * GSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GSS.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.grnet.pithos.web.client.rest;

import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.InsufficientPermissionsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

/**
 * @author kman
 */
public abstract class MultipleDeleteCommand extends RestCommand {


	Map<String, Throwable> errors = new HashMap<String, Throwable>();

	List<String> successPaths = new ArrayList<String>();

	String[] paths;

	public MultipleDeleteCommand(String[] pathToDelete){
		this(pathToDelete, true);
	}

	public MultipleDeleteCommand(String[] pathToDelete, boolean showLoading){
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator("Deleting "+pathToDelete.length+" items",null);
		paths = pathToDelete;
		for (final String pathg : pathToDelete) {
			GWT.log("[DEL]"+pathg, null);
			RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE, pathg);

			try {
				handleHeaders(builder, pathg);
				builder.sendRequest("", new RequestCallback() {

					@Override
					public void onError(Request arg0, Throwable arg1) {
						errors.put(pathg, arg1);
					}

					@Override
					public void onResponseReceived(Request arg0, Response arg1) {
						if (arg1.getStatusCode() == 204)
							successPaths.add(pathg);
						else if(arg1.getStatusCode() == 403)
							sessionExpired();
						else if (arg1.getStatusCode() == 405)
							errors.put(pathg, new InsufficientPermissionsException("You don't have permissions to delete this resource"));
						else
							errors.put(pathg, new RestException(pathg, arg1.getStatusCode(), arg1.getStatusText(), arg1.getText()));
					}

				});
			} catch (Exception ex) {
				errors.put(pathg, ex);
			}
		}
	}

	public boolean isComplete() {
		return errors.size() + successPaths.size() == paths.length;
	}

	@Override
	public boolean execute() {
		boolean com = isComplete();
		if (com) {
			if(hasErrors())
				for(String p : errors.keySet())
					onError(p, errors.get(p));
			onComplete();
			if(isShowLoadingIndicator())
				GSS.get().hideLoadingIndicator();
			return false;
		}
		return true;
	}

	public boolean hasErrors(){
		return errors.size() >0;
	}


	/**
	 * Retrieve the errors.
	 *
	 * @return the errors
	 */
	public Map<String, Throwable> getErrors() {
		return errors;
	}

	public void debug(){
		GWT.log("-ERRORS-->"+getErrors().size(), null);
		for(String p : getErrors().keySet())
			GWT.log("error:"+p, getErrors().get(p));
	}

	public abstract void onError(String path, Throwable throwable);

}
