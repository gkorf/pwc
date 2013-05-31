/*
 * Copyright 2011-2013 GRNET S.A. All rights reserved.
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

import gr.grnet.pithos.web.client.Helpers;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.Resource;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public abstract class PostRequest implements ScheduledCommand {

    private String api;

    protected String owner;

    private String path;
    
    String data = "";

    private Map<String, String> headers = new HashMap<String, String>();

    public abstract void onSuccess(Resource result);

    public abstract void onError(Throwable t);

    public PostRequest(String api, String owner, String path) {
        this.api = api;
        this.owner = owner;
        this.path = path;
    }

    public PostRequest(String api, String owner, String path, String data) {
        this.api = api;
        this.owner = owner;
        this.path = path;
        this.data = data;
    }

    @Override
    public void execute() {
        Pithos.LOG("POST ", api + owner + path);

        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, api + owner + path);
        Helpers.setHeaders(builder, headers);

        try {
            builder.sendRequest(data, new RestRequestCallback<Resource>(api + owner + path, Response.SC_ACCEPTED) {
                @Override
                public void onSuccess(Resource object) {
                    PostRequest.this.onSuccess(object);
                }

                @Override
                public Resource deserialize(Response response) {
                    return Resource.createFromResponse(Resource.class, owner, response, null);
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    PostRequest.this.onError(throwable);
                }

				@Override
				public void onUnauthorized(Response response) {
					PostRequest.this.onUnauthorized(response);
				}
            });
        }
        catch (RequestException e) {
        	Pithos.LOG(e);
        }
    }

    protected abstract void onUnauthorized(Response response);

	public void setHeader(String header, String value) {
        headers.put(header, value);
    }
}
