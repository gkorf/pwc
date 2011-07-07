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

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import gr.grnet.pithos.web.client.foldertree.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: chstath Date: 7/5/11 Time: 5:29 PM To change this template use File | Settings | File
 * Templates.
 */
public abstract class DeleteRequest implements ScheduledCommand {
    private String path;

    private Map<String, String> headers = new HashMap<String, String>();

    public abstract void onSuccess(Resource result);

    public abstract void onError(Throwable t);

    public DeleteRequest(String path) {
        this.path = path;
    }

    @Override
    public void execute() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE, path);
        for (String header : headers.keySet()) {
            builder.setHeader(header, headers.get(header));
        }
        try {
            builder.sendRequest("", new RestRequestCallback(path, Response.SC_NO_CONTENT) {
                @Override
                public void onSuccess(Resource object) {
                    DeleteRequest.this.onSuccess(object);
                }

                @Override
                public Resource deserialize(Response response) {
                    return Resource.createFromResponse(Resource.class, response, null);
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    DeleteRequest.this.onError(throwable);
                }
            });
        }
        catch (RequestException e) {
        }
    }

    public void setHeader(String header, String value) {
        headers.put(header, value);
    }
}