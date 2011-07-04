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
