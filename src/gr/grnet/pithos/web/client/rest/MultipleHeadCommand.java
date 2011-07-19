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
import gr.grnet.pithos.web.client.rest.MultipleGetCommand.Cached;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.GroupResource;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;
import gr.grnet.pithos.web.client.rest.resource.GroupsResource;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import gr.grnet.pithos.web.client.rest.resource.SharedResource;
import gr.grnet.pithos.web.client.rest.resource.TrashResource;
import gr.grnet.pithos.web.client.rest.resource.UserResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DeferredCommand;


public abstract class MultipleHeadCommand <T extends RestResource> extends RestCommand {
	String[] paths;
	Class<T> aclass;
	List<T> result = new ArrayList<T>();
	Map<String, Throwable> errors = new HashMap<String, Throwable>();
	private boolean requestSent=false;
	Cached[] cached;

	public MultipleHeadCommand(Class<T> theClass, String[] pathToGet, Cached[] theCached) {
		this(theClass, pathToGet, true, theCached);
	}

	public MultipleHeadCommand(Class<T> theClass, String[] pathToGet, boolean showLoading, Cached[] theCached) {
		setShowLoadingIndicator(showLoading);
		if(isShowLoadingIndicator())
			Pithos.get().showLoadingIndicator("Getting "+pathToGet.length+" items", null);
		paths = pathToGet;
		this.aclass = theClass;
		this.cached = theCached;
		//sendRequest();
	}

	private void sendRequest() {
		if(requestSent)
			return;
		requestSent=true;
		if(cached!=null)
			for (final Cached c : cached){
				final String path;
				if(aclass.equals(FileResource.class)){
					if(c.uri.indexOf("?") == -1)
						path=c.uri+"?"+Math.random();
					else
						path=c.uri;
				}
				else
					path = fixPath(c.uri);
				DeferredCommand.addCommand(new HeadCommand<T>(aclass,path,false, (T)c.cache) {

					@Override
					public void onComplete() {
						MultipleHeadCommand.this.result.add(getResult());
					}

					@Override
					public void onError(Throwable t) {
						errors.put(path, t);
					}

				});
			}
		else
			for (String pathg : paths) {
				final String path;
				if(aclass.equals(FileResource.class))
					path = pathg;
				else
					path = fixPath(pathg);
				DeferredCommand.addCommand(new HeadCommand<T>(aclass,path,false, null) {
					@Override
					public void onComplete() {
						MultipleHeadCommand.this.result.add(getResult());
					}

					@Override
					public void onError(Throwable t) {
						errors.put(path, t);
					}
				});
			}
	}
	public boolean isComplete() {
		return result.size()+errors.size() == paths.length;
	}

	public List<T> getResult() {
		return result;
	}

	@Override
	public boolean execute() {
		if(!requestSent)
			sendRequest();
		boolean com = isComplete();
		if (com) {
			if(isShowLoadingIndicator())
				Pithos.get().hideLoadingIndicator();
			if(hasErrors())
				for(String p : errors.keySet())
					onError(p, errors.get(p));
			onComplete();
			return false;
		}
		return true;
	}

	/**
	 * @param p
	 * @param throwable
	 */
	public abstract void onError(String p, Throwable throwable);

	public Object deserializeResponse(String path, Response response) {
		RestResource result1 = null;
		if (aclass.equals(FolderResource.class)) {
			result1 = new FolderResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(FileResource.class)) {
			result1 = new FileResource(path);
			result1.createFromJSON(response.getHeader("X-Pithos-Metadata"));
		} else if (aclass.equals(GroupsResource.class)) {
			result1 = new GroupsResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(TrashResource.class)) {
			result1 = new TrashResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(SharedResource.class)) {
			result1 = new SharedResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(GroupResource.class)) {
			result1 = new GroupResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(GroupUserResource.class)) {
			result1 = new GroupUserResource(path);
			result1.createFromJSON(response.getText());
		} else if (aclass.equals(UserResource.class)) {
			result1 = new UserResource(path);
			result1.createFromJSON(response.getText());
		}
		return result1;
	}

	public boolean hasErrors(){
		return errors.size() >0;
	}

	/**
	 * Retrieve the errors.
	 *
	 * @return the errors
	 */
	public Map<String, Throwable> getErrors() {
		return errors;
	}

	public void debug(){
		GWT.log("--->"+result.size(), null);
		GWT.log("-ERRORS-->"+getErrors().size(), null);
		for(String p : getErrors().keySet())
			GWT.log("error:"+p, getErrors().get(p));
	}
}
