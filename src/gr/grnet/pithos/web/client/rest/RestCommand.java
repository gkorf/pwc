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
import gr.grnet.pithos.web.client.SessionExpiredDialog;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.IncrementalCommand;

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
