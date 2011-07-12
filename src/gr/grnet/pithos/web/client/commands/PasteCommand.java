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
import gr.grnet.pithos.web.client.Clipboard;
import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;

public class PasteCommand implements Command {

    private GSS app;
	private PopupPanel containerPanel;
    private Folder folder;

	public PasteCommand(GSS _app, PopupPanel _containerPanel, Folder _folder) {
        app = _app;
		containerPanel = _containerPanel;
        folder = _folder;
	}

	@Override
	public void execute() {
		containerPanel.hide();
        Object clipboardItem = app.getClipboard().getItem();
        if (clipboardItem == null)
            return;
        int operation = app.getClipboard().getOperation();
        if (clipboardItem instanceof Folder) {
            final Folder tobeCopied = (Folder) clipboardItem;
            if (operation == Clipboard.COPY) {
                copyFolder(tobeCopied, folder.getUri(), new Command() {
                    @Override
                    public void execute() {
                        app.getClipboard().clear();
                        app.updateFolder(folder);
                    }
                });
            }
            else {
                copyFolder(tobeCopied, folder.getUri(), new Command() {
                    @Override
                    public void execute() {
                        app.getClipboard().clear();
                        app.deleteFolder(tobeCopied);
                        app.updateFolder(folder);
                    }
                });

            }
        }
        else {
            List<File> tobeCopied = (List<File>) clipboardItem;
            Iterator<File> iter = tobeCopied.iterator();
            if (operation == Clipboard.COPY) {
                copyFiles(iter, folder.getUri(), new Command() {
                    @Override
                    public void execute() {
                        app.getClipboard().clear();
                        app.updateFolder(folder);
                    }
                });
            }
            else {
                moveFiles(iter, new Command() {
                    @Override
                    public void execute() {
                        app.getClipboard().clear();
                        app.updateFolder(folder);
                    }
                });
            }
        }
	}

    private void moveFiles(final Iterator<File> iter, final Command callback) {
        if (iter.hasNext()) {
            File file = iter.next();
            String path = app.getApiPath() + app.getUsername() + folder.getUri() + "/" + file.getName();
            PutRequest copyFile = new PutRequest(path) {
                @Override
                public void onSuccess(Resource result) {
                    moveFiles(iter, callback);
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
            copyFile.setHeader("X-Auth-Token", app.getToken());
            copyFile.setHeader("X-Move-From", file.getUri());
            Scheduler.get().scheduleDeferred(copyFile);
        }
        else if (callback != null) {
            callback.execute();
        }
    }

    private void copyFiles(final Iterator<File> iter, final String targetUri, final Command callback) {
        if (iter.hasNext()) {
            File file = iter.next();
            String path = app.getApiPath() + app.getUsername() + targetUri + "/" + file.getName();
            PutRequest copyFile = new PutRequest(path) {
                @Override
                public void onSuccess(Resource result) {
                    copyFiles(iter, targetUri, callback);
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
            copyFile.setHeader("X-Auth-Token", app.getToken());
            copyFile.setHeader("X-Copy-From", file.getUri());
            Scheduler.get().scheduleDeferred(copyFile);
        }
        else  if (callback != null) {
            callback.execute();
        }
    }

    private void copyFolder(final Folder f, final String targetUri, final Command callback) {
        String path = app.getApiPath() + app.getUsername() + targetUri + "/" + f.getName();
        PutRequest createFolder = new PutRequest(path) {
            @Override
            public void onSuccess(Resource result) {
                Iterator<File> iter = f.getFiles().iterator();
                copyFiles(iter, targetUri + "/" + f.getName(), new Command() {
                    @Override
                    public void execute() {
                        Iterator<Folder> iterf = f.getSubfolders().iterator();
                        copySubfolders(iterf, targetUri + "/" + f.getName(), new Command() {
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
        createFolder.setHeader("X-Auth-Token", app.getToken());
        createFolder.setHeader("Accept", "*/*");
        createFolder.setHeader("Content-Length", "0");
        createFolder.setHeader("Content-Type", "application/folder");
        Scheduler.get().scheduleDeferred(createFolder);
    }

    private void copySubfolders(final Iterator<Folder> iter, final String targetUri, final Command callback) {
        if (iter.hasNext()) {
            final Folder f = iter.next();
            copyFolder(f, targetUri, callback);
        }
        else  if (callback != null) {
            callback.execute();
        }
    }
}
