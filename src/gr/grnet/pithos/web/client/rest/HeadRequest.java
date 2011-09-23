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

import gr.grnet.pithos.web.client.foldertree.Resource;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public abstract class HeadRequest<T extends Resource> implements ScheduledCommand {

	protected Class<T> aClass;

    private String api;

    protected String owner;
    
    private String path;

    private int okCode;
    
    protected T cached;

    protected T result;

    private Map<String, String> headers = new HashMap<String, String>();

    public abstract void onSuccess(T _result);

    public abstract void onError(Throwable t);

    public HeadRequest(Class<T> aClass, String api, String owner, String path, int okCode, T result) {
        this.aClass = aClass;
        this.api = api;
        this.owner = owner;
        this.path = path;
        this.okCode = okCode;
        this.result = result;
    }

    public HeadRequest(Class<T> aClass, String api, String owner, String path) {
        this(aClass, api, owner, path, Response.SC_NO_CONTENT, null);
    }

    public HeadRequest(Class<T> aClass, String api, String owner, String path, T result) {
        this(aClass, api, owner, path, Response.SC_NO_CONTENT, result);
    }

    @Override
    public void execute() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.HEAD, api + owner + path);
        builder.setHeader("If-Modified-Since", "0");
        for (String header : headers.keySet()) {
            builder.setHeader(header, headers.get(header));
        }
        try {
            builder.sendRequest("", new RestRequestCallback<T>(api + owner + path, okCode) {
                @Override
                public void onSuccess(T object) {
                    HeadRequest.this.onSuccess(object);
                }

                @Override
                public T deserialize(Response response) {
                    return Resource.createFromResponse(aClass, owner, response, result);
                }

                @Override
                public void onError(@SuppressWarnings("unused") Request request, Throwable throwable) {
                    if (throwable instanceof RestException) {
                        if (((RestException) throwable).getHttpStatusCode() == 304 && cached != null){
                            GWT.log("Using cache: " + cached.toString(), null);
                            onSuccess(cached);
                            return;
                        }
                    }
                    HeadRequest.this.onError(throwable);
                }

				@Override
				public void onUnauthorized(Response response) {
					HeadRequest.this.onUnauthorized(response);
				}
            });
        }
        catch (RequestException e) {
        }
    }

    protected abstract void onUnauthorized(Response response);

	public void setHeader(String header, String value) {
        headers.put(header, value);
    }
}
