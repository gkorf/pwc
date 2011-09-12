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
import java.util.List;

import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.DeleteRequest;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 *
 * Restore trashed files and folders.
 *
 */
public class RestoreTrashCommand implements Command {
	private PopupPanel containerPanel;
	protected Pithos app;
	protected Object resource;

	public RestoreTrashCommand(Pithos _app, PopupPanel _containerPanel, Object _resource){
		containerPanel = _containerPanel;
        app = _app;
        resource = _resource;
	}

	@Override
	public void execute() {
        if (containerPanel != null)
    		containerPanel.hide();
        if (resource instanceof List) {
            @SuppressWarnings("unchecked")
			Iterator<File> iter = ((List<File>) resource).iterator();
            untrashFiles(iter, new Command() {
                @SuppressWarnings("unchecked")
				@Override
                public void execute() {
                    app.updateFolder(((List<File>) resource).get(0).getParent(), true, null);
                }
            });
        }
        else if (resource instanceof Folder) {
            final Folder toBeUnTrashed = (Folder) resource;
            untrashFolder(toBeUnTrashed, new Command() {
                @Override
                public void execute() {
                    app.updateFolder(toBeUnTrashed.getParent(), true, null);
                }
            });

        }
	}

    private void untrashFolder(final Folder f, final Command callback) {
        String path = "/" + Pithos.HOME_CONTAINER + "/" + f.getPrefix();
        PutRequest createFolder = new PutRequest(app.getApiPath(), app.getUsername(), path) {
            @Override
            public void onSuccess(@SuppressWarnings("unused") Resource result) {
                Iterator<File> iter = f.getFiles().iterator();
                untrashFiles(iter, new Command() {
                    @Override
                    public void execute() {
                        Iterator<Folder> iterf = f.getSubfolders().iterator();
                        untrashSubfolders(iterf, new Command() {
							
							@Override
							public void execute() {
								DeleteRequest deleteFolder = new DeleteRequest(app.getApiPath(), f.getOwner(), f.getUri()) {
									
									@Override
									public void onSuccess(Resource result) {
										if (callback != null)
											callback.execute();
									}
									
									@Override
									public void onError(Throwable t) {
					                    GWT.log("", t);
					                    if (t instanceof RestException) {
					                        app.displayError("Unable to delete folder: " + ((RestException) t).getHttpStatusText());
					                    }
					                    else
					                        app.displayError("System error unable to delete folder: "+t.getMessage());
									}
								};
								deleteFolder.setHeader("X-Auth-Token", app.getToken());
								Scheduler.get().scheduleDeferred(deleteFolder);
							}
						});
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("", t);
                if (t instanceof RestException) {
                    app.displayError("Unable to create folder:" + ((RestException) t).getHttpStatusText());
                }
                else
                    app.displayError("System error creating folder:" + t.getMessage());
            }
        };
        createFolder.setHeader("X-Auth-Token", app.getToken());
        createFolder.setHeader("Accept", "*/*");
        createFolder.setHeader("Content-Length", "0");
        createFolder.setHeader("Content-Type", "application/folder");
        Scheduler.get().scheduleDeferred(createFolder);
    }

    protected void untrashFiles(final Iterator<File> iter, final Command callback) {
        if (iter.hasNext()) {
            File file = iter.next();
            String path = "/" + Pithos.HOME_CONTAINER + "/" + file.getPath();
            PutRequest untrashFile = new PutRequest(app.getApiPath(), app.getUsername(), path) {
                @Override
                public void onSuccess(@SuppressWarnings("unused") Resource result) {
                    untrashFiles(iter, callback);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
                    if (t instanceof RestException) {
                        app.displayError("Unable to copy file: " + ((RestException) t).getHttpStatusText());
                    }
                    else
                        app.displayError("System error unable to copy file: "+t.getMessage());
                }
            };
            untrashFile.setHeader("X-Auth-Token", app.getToken());
            untrashFile.setHeader("X-Move-From", file.getUri());
            Scheduler.get().scheduleDeferred(untrashFile);
        }
        else if (callback != null) {
            callback.execute();
        }
    }

    protected void untrashSubfolders(final Iterator<Folder> iter, final Command callback) {
        if (iter.hasNext()) {
            final Folder f = iter.next();
            untrashFolder(f, callback);
        }
        else  {
        	if (callback != null)
        		callback.execute();
        }
    }
}
