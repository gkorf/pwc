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

import com.google.gwt.core.client.Scheduler;
import gr.grnet.pithos.web.client.Clipboard;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.Resource;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;

public class PasteCommand implements Command {

	protected Pithos app;
	private PopupPanel containerPanel;
	protected Folder folder;

	public PasteCommand(Pithos _app, PopupPanel _containerPanel, Folder _folder) {
        app = _app;
		containerPanel = _containerPanel;
        folder = _folder;
	}

	@Override
	public void execute() {
        if (containerPanel != null)
    		containerPanel.hide();

        Object clipboardItem = app.getClipboard().getItem();
        if (clipboardItem == null)
            return;
        int operation = app.getClipboard().getOperation();
        if (clipboardItem instanceof Folder) {
            final Folder tobeCopied = (Folder) clipboardItem;
            if (operation == Clipboard.COPY) {
            	String targetUri = folder.getUri() + "/" + tobeCopied.getName();
                app.copyFolder(tobeCopied, folder.getOwnerID(), targetUri, false, new Command() {
                    @Override
                    public void execute() {
                        app.getClipboard().clear();
                        app.updateFolder(folder, true, new Command() {
							
							@Override
							public void execute() {
								app.updateStatistics();
							}
						}, true);
                    }
                });
            }
            else {
            	String targetUri = folder.getUri() + "/" + tobeCopied.getName();
                app.copyFolder(tobeCopied, folder.getOwnerID(), targetUri, true, new Command() {
                    @Override
                    public void execute() {
                        app.getClipboard().clear();
                        app.updateFolder(folder, true, null, true);
                    }
                });
            }
        }
        else {
            @SuppressWarnings("unchecked")
			List<File> tobeCopied = (List<File>) clipboardItem;
            Iterator<File> iter = tobeCopied.iterator();
            if (operation == Clipboard.COPY) {
                app.copyFiles(iter, folder.getOwnerID(), folder.getUri(), new Command() {
                    @Override
                    public void execute() {
                        app.getClipboard().clear();
                        app.updateFolder(folder, true, new Command() {
							
							@Override
							public void execute() {
								app.updateStatistics();
							}
						}, true);
                    }
                });
            }
            else {
                moveFiles(iter, new Command() {
                    @Override
                    public void execute() {
                        app.getClipboard().clear();
                        app.updateFolder(folder, true, null, true);
                    }
                });
            }
        }
	}

	protected void moveFiles(final Iterator<File> iter, final Command callback) {
        if (iter.hasNext()) {
            File file = iter.next();
            String path = folder.getUri() + "/" + file.getName();
            PutRequest copyFile = new PutRequest(Pithos.getStorageAPIURL(), folder.getOwnerID(), path) {
                @Override
                public void onSuccess(Resource result) {
                    moveFiles(iter, callback);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
					app.setError(t);
                    if (t instanceof RestException) {
                        app.displayError("Unable to copy file: " + ((RestException) t).getHttpStatusText());
                    }
                    else
                        app.displayError("System error unable to copy file: "+t.getMessage());
                }

				@Override
				protected void onUnauthorized(Response response) {
					app.sessionExpired();
				}
            };
            copyFile.setHeader("X-Auth-Token", app.getUserToken());
            copyFile.setHeader("X-Move-From", URL.encodePathSegment(file.getUri()));
            copyFile.setHeader("Content-Type", file.getContentType());
            Scheduler.get().scheduleDeferred(copyFile);
        }
        else if (callback != null) {
            callback.execute();
        }
    }
}
