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
package gr.grnet.pithos.web.client.commands;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import gr.grnet.pithos.web.client.Const;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.Resource;
import gr.grnet.pithos.web.client.grouptree.Group;
import gr.grnet.pithos.web.client.rest.PostRequest;
import gr.grnet.pithos.web.client.rest.RestException;


/**
 * Delete selected object command
 *
 */
public class DeleteGroupCommand implements Command {
	private PopupPanel containerPanel;

    Group group;

    Pithos app;
    
	/**
	 * @param _containerPanel
	 */
	public DeleteGroupCommand(Pithos _app, PopupPanel _containerPanel, Group _group){
        app = _app;
		containerPanel = _containerPanel;
        group = _group;
	}

	@Override
	public void execute() {
        if (containerPanel != null) {
            containerPanel.hide();
        }
        final String groupName = group.getName();
        final String safeGroupName = SafeHtmlUtils.htmlEscape(groupName);
        if (Window.confirm("Are you sure you want to delete group " + safeGroupName)) {
        	String path = "?update=";
        	PostRequest updateGroup = new PostRequest(Pithos.getStorageAPIURL(), app.getUserID(), path) {
				
				@Override
				public void onSuccess(Resource result) {
					app.removeGroup(group);
				}
				
				@Override
				public void onError(Throwable t) {
					GWT.log("", t);
					app.setError(t);
					if (t instanceof RestException) {
						app.displayError("Unable to delete group:" + ((RestException) t).getHttpStatusText());
					}
					else
						app.displayError("System error deleting group:" + t.getMessage());
				}

				@Override
				protected void onUnauthorized(Response response) {
					app.sessionExpired();
				}
			};
			updateGroup.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());
			updateGroup.setHeader("X-Account-Group-" + URL.encodePathSegment(groupName), "~");
			Scheduler.get().scheduleDeferred(updateGroup);
        }
	}
}
