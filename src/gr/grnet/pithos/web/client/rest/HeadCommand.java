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
import gr.grnet.pithos.web.client.ObjectNotFoundException;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.GroupResource;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;
import gr.grnet.pithos.web.client.rest.resource.GroupsResource;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import gr.grnet.pithos.web.client.rest.resource.SharedResource;
import gr.grnet.pithos.web.client.rest.resource.TrashResource;
import gr.grnet.pithos.web.client.rest.resource.UserResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;


public  abstract class HeadCommand<T extends RestResource> extends RestCommand{

	boolean complete = false;
	T result = null;
	Class<T> aclass;
	private boolean requestSent = false;
	T cached;
	final String path;

	public HeadCommand(Class<T> theclass, String pathToGet, T theCached){
		this(theclass, pathToGet, true, theCached);
	}

	public HeadCommand(Class<T> theClass, String pathToGet, boolean showLoading, T theCached){
		setShowLoadingIndicator(showLoading);
		this.aclass = theClass;
		if(isShowLoadingIndicator())
			Pithos.get().showLoadingIndicator("Getting ",pathToGet);

		if(theClass.equals(FileResource.class))
			path = pathToGet;
		else
			path = fixPath(pathToGet);
		this.cached = theCached;

	}

	private void sendRequest(){
		if(requestSent)
			return;
		requestSent=true;
		RequestBuilder builder = new RequestBuilder(RequestBuilder.HEAD, path);
		if(cached!=null && cached.getLastModifiedSince() != null){
			GWT.log("ADDING IF MODIFIED HEADERS", null);
			builder.setHeader("If-Modified-Since", cached.getLastModifiedSince());
		}
		try {
			handleHeaders(builder, path);
			builder.sendRequest("", new RestCallback(path) {

				@Override
				public Object deserialize(Response response) {
					return deserializeResponse(path, response);
				}

				@Override
				public void handleError(Request request, Throwable exception) {
					if(exception instanceof RestException)
						if(((RestException)exception).getHttpStatusCode() == 304 && cached != null){
							GWT.log("Using cache:"+cached.getUri(), null);
							handleSuccess(cached);
							return;
						}
					complete = true;
					HeadCommand.this.onError(exception);
				}

				@Override
				public void handleSuccess(Object object) {
					result = (T) object;
					complete = true;
				}

			});
		} catch (Exception ex) {
			complete = true;
			onError(ex);
		}
	}

	public boolean isComplete() {
		return complete;
	}

	public T getResult(){
		return result;
	}

	@Override
	public boolean execute() {
		if(!requestSent)
			sendRequest();
		boolean com = isComplete();
		if(com){
			if(isShowLoadingIndicator())
				Pithos.get().hideLoadingIndicator();
			if(getResult() != null)
				onComplete();
			else
				onError(new ObjectNotFoundException("Resource Not Found"));
			return false;
		}
		return true;
	}

	public  Object deserializeResponse(String aPath, Response response){
		RestResource result1 = null;
		if(aclass.equals(FolderResource.class)){
			result1 = new FolderResource(aPath);
			result1.createFromJSON(response.getText());

		}
		else if(aclass.equals(FileResource.class)){
			result1 = new FileResource(aPath);
			result1.createFromJSON(response.getHeader("X-Pithos-Metadata"));
		}
		else if(aclass.equals(GroupsResource.class)){
			result1 = new GroupsResource(aPath);
			result1.createFromJSON(response.getText());
		}
		else if(aclass.equals(TrashResource.class)){
			result1 = new TrashResource(aPath);
			result1.createFromJSON(response.getText());

		}
		else if(aclass.equals(SharedResource.class)){
			result1 = new SharedResource(aPath);
			result1.createFromJSON(response.getText());

		}
		else if(aclass.equals(GroupResource.class)){
			result1 = new GroupResource(aPath);
			result1.createFromJSON(response.getText());

		}
		else if(aclass.equals(GroupUserResource.class)){
			result1 = new GroupUserResource(aPath);
			result1.createFromJSON(response.getText());

		}
		else if(aclass.equals(UserResource.class)){
			result1 = new UserResource(aPath);
			result1.createFromJSON(response.getText());

		}
		return result1;
	}
}
