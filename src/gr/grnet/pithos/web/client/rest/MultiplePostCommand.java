/*
 * Copyright 2011 GRNET S.A. All rights reserved.
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


public abstract class MultiplePostCommand extends RestCommand {


	Map<String, Throwable> errors = new HashMap<String, Throwable>();

	List<String> successPaths = new ArrayList<String>();

	String[] paths;

	public MultiplePostCommand(String[] pathToDelete, final int okStatusCode){
		this(pathToDelete, okStatusCode, true);
	}

	public MultiplePostCommand(String[] pathToDelete, String data, final int okStatusCode){
		this(pathToDelete, data, okStatusCode, true);
	}

	public MultiplePostCommand(String[] pathToDelete, final int okStatusCode, boolean showLoading){
		this(pathToDelete, "", okStatusCode, showLoading);
	}

	public MultiplePostCommand(String[] pathToDelete, String data, final int okStatusCode, boolean showLoading){
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			GSS.get().showLoadingIndicator("Updating "+pathToDelete.length+" items", null);
		paths = pathToDelete;
		for (final String pathg : pathToDelete) {
			GWT.log("[DEL]"+pathg, null);
			RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, pathg);

			try {
				handleHeaders(builder, pathg);
				builder.sendRequest(data, new RequestCallback() {

					@Override
					public void onError(Request arg0, Throwable arg1) {
						errors.put(pathg, arg1);
					}

					@Override
					public void onResponseReceived(Request arg0, Response arg1) {
						if (arg1.getStatusCode() == okStatusCode)
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


	/**
	 * @param p
	 * @param throwable
	 */
	public abstract void onError(String p, Throwable throwable);

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

	@Override
	public void onError(Throwable t) {


	}

}
