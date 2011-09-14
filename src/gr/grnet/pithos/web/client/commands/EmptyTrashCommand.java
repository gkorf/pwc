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
package gr.grnet.pithos.web.client.commands;

import java.util.Iterator;

import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.DeleteRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Command to empty trash bin.
 */
public class EmptyTrashCommand implements Command{
	private PopupPanel containerPanel;

    Pithos app;

	public EmptyTrashCommand(Pithos _app, PopupPanel _containerPanel){
        app = _app;
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		if (containerPanel != null)
			containerPanel.hide();
		
		final Folder trash = app.getAccount().getTrash();
		if (trash != null) {
			Iterator<File> iter = trash.getFiles().iterator();
			deleteFile(iter, new Command() {
				
				@Override
				public void execute() {
					Iterator<Folder> iter2 = trash.getSubfolders().iterator();
					deleteSubfolder(iter2, new Command() {
						
						@Override
						public void execute() {
							app.updateTrash(true, null);
						}
					});
				}
			});
		}
	}

	protected void deleteSubfolder(final Iterator<Folder> iter2, final Command callback) {
		if (iter2.hasNext()) {
			final Folder f = iter2.next();
			Iterator<File> iter3 = f.getFiles().iterator();
			deleteFile(iter3, new Command() {
				
				@Override
				public void execute() {
					Iterator<Folder> iter4 = f.getSubfolders().iterator();
					deleteSubfolder(iter4, new Command() {
						
						@Override
						public void execute() {
							String path = f.getUri();
							DeleteRequest deleteF = new DeleteRequest(app.getApiPath(), f.getOwner(), path) {
								
								@Override
								public void onSuccess(Resource result) {
									deleteSubfolder(iter2, callback);
								}
								
								@Override
								public void onError(Throwable t) {
									GWT.log("", t);
									if (t instanceof RestException) {
										app.displayError("Unable to delete file:" + ((RestException) t).getHttpStatusText());
									}
									else
										app.displayError("System error deleting file:" + t.getMessage());
								}
							};
							deleteF.setHeader("X-Auth-Token", app.getToken());
							Scheduler.get().scheduleDeferred(deleteF);
						}
					});
				}
			});
		}
		else {
			if (callback != null)
				callback.execute();
		}
	}

	void deleteFile(final Iterator<File> iter, final Command callback) {
		if (iter.hasNext()) {
			File f = iter.next();
			String path = f.getUri();
			DeleteRequest deleteF = new DeleteRequest(app.getApiPath(), f.getOwner(), path) {
				
				@Override
				public void onSuccess(Resource result) {
					deleteFile(iter, callback);
				}
				
				@Override
				public void onError(Throwable t) {
					GWT.log("", t);
					if (t instanceof RestException) {
						app.displayError("Unable to delete file:" + ((RestException) t).getHttpStatusText());
					}
					else
						app.displayError("System error deleting file:" + t.getMessage());
				}
			};
			deleteF.setHeader("X-Auth-Token", app.getToken());
			Scheduler.get().scheduleDeferred(deleteF);
		}
		else {
			if (callback != null)
				callback.execute();
		}
	}
}
