/*
 * Copyright 2011-2012 GRNET S.A. All rights reserved.
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
import gr.grnet.pithos.web.client.foldertree.Resource;

public abstract class RestRequestCallback<T extends Resource> implements RequestCallback {

    private static final int HTTP_OK = 200;
    private int okcode = -1;
    private String path;

    public RestRequestCallback(String path, int okCode) {
        this.path = path;
        this.okcode = okCode;
    }

    public RestRequestCallback(String path) {
        this(path, -1);
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
        try {
            if (response.getStatusCode() == HTTP_OK || (okcode !=-1 && response.getStatusCode() == okcode))
                onSuccess(deserialize(response));
            else if (response.getStatusCode() == Response.SC_UNAUTHORIZED) {
            	log(request, response);
            	onUnauthorized(response);
            }
            else {
                String statusText = "";
                String text = "";
                // Ignore JavaScript errors caused by non-existent text.
                try {
                    statusText = response.getStatusText();
                }
                catch (Exception e) {}

                try {
                    text = response.getText();
                }
                catch (Exception e) {}

                onError(request, new RestException(path, response.getStatusCode(), statusText, text));
            }
        } catch (Exception e) {
            onError(request, e);
        }
    }

    private native void log(Request req, Response resp)/*-{
    	if ($wnd.console && $wnd.console.log) {
    		$wnd.console.log(req.@com.google.gwt.http.client.Request::toString()());
    		$wnd.console.log(resp.@com.google.gwt.http.client.Response::toString()());
    		$wnd.console.log(resp.@com.google.gwt.http.client.Response::getStatusCode()());
    		$wnd.console.log(resp.@com.google.gwt.http.client.Response::getStatusText()());
    		$wnd.console.log(resp.@com.google.gwt.http.client.Response::getStatusCode()());
    		$wnd.console.log(resp.@com.google.gwt.http.client.Response::getText()());
    		$wnd.console.log(resp.@com.google.gwt.http.client.Response::getHeadersAsString()());
    	}
    }-*/;
    
    public abstract void onSuccess(T result);

    public abstract T deserialize(Response response);
    
    public abstract void onUnauthorized(Response response);
}
