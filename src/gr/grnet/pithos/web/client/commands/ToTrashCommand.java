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

import com.google.gwt.core.client.Scheduler;
import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.MultiplePostCommand;
import gr.grnet.pithos.web.client.rest.PostCommand;
import gr.grnet.pithos.web.client.rest.PostRequest;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 *
 * Move file or folder to trash.
 *
 *
 */
public class ToTrashCommand implements Command{
	private PopupPanel containerPanel;
    private GSS app;
    private Object resource;

	public ToTrashCommand(GSS _app, PopupPanel _containerPanel, Object _resource){
		containerPanel = _containerPanel;
        app = _app;
        resource = _resource;
	}

	@Override
	public void execute() {
		containerPanel.hide();
        if (resource instanceof List) {
            Iterator<File> iter = ((List<File>) resource).iterator();
            trashFiles(iter, new Command() {
                @Override
                public void execute() {
                    app.get().updateFolder(((List<File>) resource).get(0).getParent());
                }
            });
        }
        else if (resource instanceof Folder) {
            final Folder toBeTrashed = (Folder) resource;
            trashFolder(toBeTrashed, new Command() {
                @Override
                public void execute() {
                    app.updateFolder(toBeTrashed.getParent());
                }
            });

        }
	}

    private void trashFolder(final Folder f, final Command callback) {
        String path = app.getApiPath() + app.getUsername() + f.getUri() + "?update=";
        PostRequest trashFolder = new PostRequest(path) {
            @Override
            public void onSuccess(Resource result) {
                Iterator<File> iter = f.getFiles().iterator();
                trashFiles(iter, new Command() {
                    @Override
                    public void execute() {
                        Iterator<Folder> iterf = f.getSubfolders().iterator();
                        trashSubfolders(iterf, new Command() {
                            @Override
                            public void execute() {
                                callback.execute();
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
        trashFolder.setHeader("X-Auth-Token", app.getToken());
        trashFolder.setHeader("X-Object-Meta-Trash", "true");
        Scheduler.get().scheduleDeferred(trashFolder);
    }

    private void trashFiles(final Iterator<File> iter, final Command callback) {
        if (iter.hasNext()) {
            File file = iter.next();
            String path = app.getApiPath() + app.getUsername() + file.getUri() + "?update=";
            PostRequest trashFile = new PostRequest(path) {
                @Override
                public void onSuccess(Resource result) {
                    trashFiles(iter, callback);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
                    if (t instanceof RestException) {
                        GSS.get().displayError("Unable to copy file: " + ((RestException) t).getHttpStatusText());
                    }
                    else
                        GSS.get().displayError("System error unable to copy file: "+t.getMessage());
                }
            };
            trashFile.setHeader("X-Auth-Token", app.getToken());
            trashFile.setHeader("X-Object-Meta-Trash", "true");
            Scheduler.get().scheduleDeferred(trashFile);
        }
        else  if (callback != null) {
            callback.execute();
        }
    }

    private void trashSubfolders(final Iterator<Folder> iter, final Command callback) {
        if (iter.hasNext()) {
            final Folder f = iter.next();
            trashFolder(f, callback);
        }
        else  if (callback != null) {
            callback.execute();
        }
    }
}
