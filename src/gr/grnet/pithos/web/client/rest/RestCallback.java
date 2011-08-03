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
			else if(response.getStatusCode() == 403);
//				RestCommand.sessionExpired();
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
