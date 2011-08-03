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
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.GroupResource;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;
import gr.grnet.pithos.web.client.rest.resource.GroupsResource;
import gr.grnet.pithos.web.client.rest.resource.OtherUserResource;
import gr.grnet.pithos.web.client.rest.resource.OthersResource;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import gr.grnet.pithos.web.client.rest.resource.SharedResource;
import gr.grnet.pithos.web.client.rest.resource.TrashResource;
import gr.grnet.pithos.web.client.rest.resource.UserResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DeferredCommand;

public abstract class MultipleGetCommand<T extends RestResource> extends RestCommand {

	Class<T> aclass;
	List<T> result = new ArrayList<T>();
	Map<String, Throwable> errors = new HashMap<String, Throwable>();
	Cached[] cached;
	String[] paths;
	private boolean requestSent=false;

	public MultipleGetCommand(Pithos _app, Class<T> aNewClass, String[] pathToGet, Cached[] theCached) {
		this(_app, aNewClass, pathToGet, true, theCached);
	}

	public MultipleGetCommand(Pithos _app, Class<T> aNewClass, String[] pathToGet, boolean showLoading, Cached[] theCached) {
        super(_app);
		setShowLoadingIndicator(showLoading);
		if (isShowLoadingIndicator())
			app.showLoadingIndicator("Getting "+pathToGet.length+" items", null);
		aclass = aNewClass;
		paths = pathToGet;
		this.cached = theCached;
		//sendRequest();
	}

	private void sendRequest() {
		if (requestSent)
			return;
		requestSent=true;
		if (cached!=null)
			for (final Cached pathg : cached)
				DeferredCommand.addCommand(new GetCommand<T>(app, aclass,pathg.uri,false,(T)pathg.cache) {

					@Override
					public void onComplete() {
						MultipleGetCommand.this.result.add(getResult());
					}

					@Override
					public void onError(Throwable t) {
						errors.put(pathg.uri, t);
					}

				});
		else
			for (final String pathg : paths)
				DeferredCommand.addCommand(new GetCommand<T>(app, aclass,pathg,false,null) {

					@Override
					public void onComplete() {
						MultipleGetCommand.this.result.add(getResult());
					}

					@Override
					public void onError(Throwable t) {
						errors.put(pathg, t);
					}

				});
	}

	public boolean isComplete() {
		return result.size()+errors.size() == paths.length;
	}

	public List<T> getResult() {
		if (aclass.equals(FolderResource.class))
			Collections.sort(result, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((FolderResource)o1).getName().compareTo(((FolderResource)o2).getName());
				}

			});
		else if(aclass.equals(GroupResource.class))
			Collections.sort(result, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((GroupResource)o1).getName().compareTo(((GroupResource)o2).getName());
				}

			});
		else if(aclass.equals(GroupUserResource.class))
			Collections.sort(result, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((GroupUserResource)o1).getName().compareTo(((GroupUserResource)o2).getName());
				}

			});
		return result;
	}

	@Override
	public boolean execute() {
		if (!requestSent)
			sendRequest();
		boolean com = isComplete();
		if (com) {
			if (isShowLoadingIndicator())
				app.hideLoadingIndicator();
			if (hasErrors())
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
		}
		else if (aclass.equals(FileResource.class)){
			result1 = new FileResource(path);
			result1.createFromJSON(response.getHeader("X-Pithos-Metadata"));
		}
		else if (aclass.equals(GroupsResource.class)) {
			result1 = new GroupsResource(path);
			result1.createFromJSON(response.getText());
		}
		else if (aclass.equals(TrashResource.class)) {
			result1 = new TrashResource(path);
			result1.createFromJSON(response.getText());
		}
		else if (aclass.equals(SharedResource.class)) {
			result1 = new SharedResource(path);
			result1.createFromJSON(response.getText());
		}
		else if (aclass.equals(OthersResource.class)) {
			result1 = new OthersResource(path);
			result1.createFromJSON(response.getText());
		}
		else if (aclass.equals(OtherUserResource.class)) {
			result1 = new OtherUserResource(path);
			result1.createFromJSON(response.getText());
		}
		else if (aclass.equals(GroupResource.class)) {
			result1 = new GroupResource(path);
			result1.createFromJSON(response.getText());
		}
		else if (aclass.equals(GroupUserResource.class)) {
			result1 = new GroupUserResource(path);
			result1.createFromJSON(response.getText());
		}
		else if (aclass.equals(UserResource.class)) {
			result1 = new UserResource(path);
			result1.createFromJSON(response.getText());
		}
		return result1;
	}

	public boolean hasErrors() {
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

	protected void debug() {
		GWT.log("--->"+result.size(), null);
		GWT.log("-ERRORS-->"+getErrors().size(), null);
		for(String p : getErrors().keySet())
			GWT.log("error:"+p, getErrors().get(p));
	}

	public static class Cached {
		public String uri;
		public RestResource cache;
	}
}
