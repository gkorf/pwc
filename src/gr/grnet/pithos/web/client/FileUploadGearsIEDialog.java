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
package gr.grnet.pithos.web.client;

import java.util.List;

import gr.grnet.pithos.web.client.rest.RestCommand;

import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.gears.client.httprequest.HttpRequest;
import com.google.gwt.gears.client.httprequest.ProgressEvent;
import com.google.gwt.gears.client.httprequest.ProgressHandler;
import com.google.gwt.gears.client.httprequest.RequestCallback;

/**
 * The 'File upload' dialog box implementation with Google Gears support
 * for IE.
 */
public class FileUploadGearsIEDialog extends FileUploadGearsDialog implements Updateable {

	/**
	 * Perform the HTTP request to upload the specified file.
	 */	
	@Override
	protected void doSend(final List<File> filesRemaining) {
		final GSS app = GSS.get();
		HttpRequest request = factory.createHttpRequest();
		requests.add(request);
		String method = "POST";

		String path;
		final String filename = getFilename(filesRemaining.get(0).getName());
		path = folder.getUri();
		if (!path.endsWith("/"))
			path = path + "/";
		path = path + encode(filename);

		String token = app.getToken();
		String resource = path.substring(app.getApiPath().length()-1, path.length());
		String date = RestCommand.getDate();
		String sig = RestCommand.calculateSig(method, date, resource, RestCommand.base64decode(token));
		request.open(method, path);
		request.setRequestHeader("X-GSS-Date", date);
		request.setRequestHeader("Authorization", app.getCurrentUserResource().getUsername() + " " + sig);
		request.setRequestHeader("Accept", "application/json; charset=utf-8");
		request.setCallback(new RequestCallback() {
			@Override
			public void onResponseReceived(HttpRequest req) {
				// XXX: No error checking, since IE throws an Internal Error
				// when accessing req.getStatus().
				filesRemaining.remove(0);
				if(filesRemaining.isEmpty()){					
					finish();					
				}
				doSend(filesRemaining);	
			}
		});
		request.getUpload().setProgressHandler(new ProgressHandler() {
			@Override
			public void onProgress(ProgressEvent event) {
				double pcnt = (double) event.getLoaded() / event.getTotal();
				progressBars.get(0).setProgress((int) Math.floor(pcnt * 100));
				if(pcnt*100 == 100)
					progressBars.remove(0);
			}
		});
		request.send(filesRemaining.get(0).getBlob());
	}

}
