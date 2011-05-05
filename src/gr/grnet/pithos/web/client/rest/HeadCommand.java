/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest;

import gr.grnet.pithos.web.client.GSS;
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
			GSS.get().showLoadingIndicator("Getting ",pathToGet);

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
				GSS.get().hideLoadingIndicator();
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
			result1.createFromJSON(response.getHeader("X-GSS-Metadata"));
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
