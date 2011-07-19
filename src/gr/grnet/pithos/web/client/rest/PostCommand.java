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

import gr.grnet.pithos.web.client.Pithos;

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
			Pithos.get().showLoadingIndicator("Updating ",path);

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
				Pithos.get().hideLoadingIndicator();
			return false;
		}
		return true;
	}

	public String getPostBody() {
		return postBody;
	}

}
