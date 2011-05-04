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
import gr.grnet.pithos.web.client.SessionExpiredDialog;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.IncrementalCommand;

/**
 * @author kman
 */
public abstract class RestCommand implements IncrementalCommand {
	protected boolean showLoadingIndicator = true;

	protected void handleHeaders(String username, RequestBuilder requestBuilder, String path) {
		String date = getDate();
		requestBuilder.setHeader("X-GSS-Date", date);

		GSS app = GSS.get();
		String token = app.getToken();
		if (token == null)
			token = "aa";
		String resource = path.substring(app.getApiPath().length()-1,path.length());
		String sig = calculateSig(requestBuilder.getHTTPMethod(), date, resource, base64decode(token));
		requestBuilder.setHeader("Authorization", username + " " + sig);
		requestBuilder.setHeader("Accept", "application/json; charset=utf-8");
	}

	protected void handleHeaders(RequestBuilder requestBuilder, String path) {
		if (GSS.get().getCurrentUserResource() != null) {
			String username = GSS.get().getCurrentUserResource().getUsername();
			handleHeaders(username, requestBuilder, path);
		} else
			GSS.get().displayError("no username");
	}

	public static native String getDate()/*-{
		return (new Date()).toUTCString();
	}-*/;

	public static native String getDate(Long ms)/*-{
	return (new Date(ms)).toUTCString();
	}-*/;

	public static native String calculateSig(String method, String date, String resource, String token)/*-{
		$wnd.b64pad = "=";
		var q = resource.indexOf('?');
		var res = q == -1? resource: resource.substring(0, q);
		var data = method + date + res;
		var sig = $wnd.b64_hmac_sha1(token, data);
		return sig;
	}-*/;

	public static native String base64decode(String encStr)/*-{
		if (typeof atob === 'function') {
           return atob(encStr);
        }
        var base64s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        var bits;
        var decOut = "";
        var i = 0;
        for(; i<encStr.length; i += 4){
            bits = (base64s.indexOf(encStr.charAt(i)) & 0xff) <<18 | (base64s.indexOf(encStr.charAt(i +1)) & 0xff) <<12 | (base64s.indexOf(encStr.charAt(i +2)) & 0xff) << 6 | base64s.indexOf(encStr.charAt(i +3)) & 0xff;
            decOut += String.fromCharCode((bits & 0xff0000) >>16, (bits & 0xff00) >>8, bits & 0xff);
        }
        if(encStr.charCodeAt(i -2) == 61){
            return(decOut.substring(0, decOut.length -2));
        }
        else if(encStr.charCodeAt(i -1) == 61){
            return(decOut.substring(0, decOut.length -1));
        }
        else {
            return(decOut);
        }
	}-*/;

	public void onComplete() {}

	public abstract void onError(Throwable t);

	public String fixPath(String pathToFix) {
		if(pathToFix.endsWith("/"))
			return pathToFix;
		return pathToFix+"/";
	}

	/**
	 * Retrieve the showLoadingIndicator.
	 *
	 * @return the showLoadingIndicator
	 */
	public boolean isShowLoadingIndicator() {
		return showLoadingIndicator;
	}

	/**
	 * Modify the showLoadingIndicator.
	 *
	 * @param newShowLoadingIndicator the showLoadingIndicator to set
	 */
	public void setShowLoadingIndicator(boolean newShowLoadingIndicator) {
		showLoadingIndicator = newShowLoadingIndicator;
	}

	static void sessionExpired() {
		SessionExpiredDialog dlg = new SessionExpiredDialog();
		dlg.center();
	}

}
