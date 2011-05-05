/*
 * Copyright (c) 2011 Greek Research and Technology Network
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
