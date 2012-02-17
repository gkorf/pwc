/*
 * Copyright 2011-2012 GRNET S.A. All rights reserved.
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

import gr.grnet.pithos.web.client.FilePermissionsDialog;
import gr.grnet.pithos.web.client.FilePropertiesDialog;
import gr.grnet.pithos.web.client.FileVersionsDialog;
import gr.grnet.pithos.web.client.FilesPropertiesDialog;
import gr.grnet.pithos.web.client.FolderPermissionsDialog;
import gr.grnet.pithos.web.client.FolderPropertiesDialog;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.HeadRequest;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The command that displays the appropriate Properties dialog, according to the
 * selected object in the application.
 *
 */
public class PropertiesCommand implements Command {

	public static final int PROPERTIES = 0;
	public static final int PERMISSIONS = 1;
	public static final int VERSIONS = 2;

	private PopupPanel containerPanel;

	private int tabToShow = 0;

    private Object resource;

    Pithos app;

	/**
	 * @param _containerPanel
	 * @param _tab the tab to switch to
	 */
	public PropertiesCommand(Pithos _app, PopupPanel _containerPanel, Object _resource, int _tab) {
		containerPanel = _containerPanel;
		tabToShow = _tab;
        resource = _resource;
        app = _app;
	}

	@Override
	public void execute() {
        if (containerPanel != null)
		    containerPanel.hide();

        if (resource instanceof Folder) {
            final Folder folder = (Folder) resource;
            switch (tabToShow) {
				case PROPERTIES:
					FolderPropertiesDialog dlg = new FolderPropertiesDialog(app, false, folder);
					dlg.center();
					break;
				case PERMISSIONS:
					scheduleFolderHeadCommand(folder, new Command() {
						
						@Override
						public void execute() {
							FolderPermissionsDialog dlg1 = new FolderPermissionsDialog(app, folder);
							dlg1.center();
						}
					});
					break;
				default:
					break;
			}
        }
        else if (resource instanceof List) {
            @SuppressWarnings("unchecked")
			List<File> files = (List<File>) resource;
            if (files.size() > 1) {
                FilesPropertiesDialog dlg = new FilesPropertiesDialog(app, files);
                dlg.center();
            }
            else {
				final File f = files.get(0);
            	switch (tabToShow) {
					case PROPERTIES:
		                scheduleFileHeadCommand(f, new Command() {
							
							@Override
							public void execute() {
								FilePropertiesDialog dlg = new FilePropertiesDialog(app, f);
				                dlg.center();
							}
						});
						break;
					case PERMISSIONS:
		                scheduleFileHeadCommand(f, new Command() {
							
							@Override
							public void execute() {
								FilePermissionsDialog dlg = new FilePermissionsDialog(app, f);
								dlg.center();
							}
						});
						break;
					case VERSIONS:
		                FileVersionsDialog dlg2 = new FileVersionsDialog(app, files.get(0));
		                dlg2.center();
						break;
					default:
						break;
				}
            }
        }
	}

	private void scheduleFileHeadCommand(File f, final Command callback) {
		HeadRequest<File> headFile = new HeadRequest<File>(File.class, app.getApiPath(), f.getOwner(), f.getUri(), f) {

			@Override
			public void onSuccess(File _result) {
				if (callback != null)
					callback.execute();
			}

			@Override
			public void onError(Throwable t) {
		        GWT.log("Error heading file", t);
				app.setError(t);
		        if (t instanceof RestException)
		            app.displayError("Error heading file: " + ((RestException) t).getHttpStatusText());
		        else
		            app.displayError("System error heading file: " + t.getMessage());
			}

			@Override
			protected void onUnauthorized(Response response) {
				app.sessionExpired();
			}
		};
		headFile.setHeader("X-Auth-Token", app.getToken());
		Scheduler.get().scheduleDeferred(headFile);
	}
	
	void scheduleFolderHeadCommand(final Folder folder, final Command callback) {
		HeadRequest<Folder> headFolder = new HeadRequest<Folder>(Folder.class, app.getApiPath(), folder.getOwner(), folder.getUri(), folder) {

			@Override
			public void onSuccess(Folder _result) {
				if (callback != null)
					callback.execute();
			}

			@Override
			public void onError(Throwable t) {
		        if (t instanceof RestException) {
		        	if (((RestException) t).getHttpStatusCode() == Response.SC_NOT_FOUND) {
                        final String path = folder.getUri();
                        PutRequest newFolder = new PutRequest(app.getApiPath(), folder.getOwner(), path) {
                            @Override
                            public void onSuccess(Resource _result) {
                            	scheduleFolderHeadCommand(folder, callback);
                            }

                            @Override
                            public void onError(Throwable _t) {
                                GWT.log("", _t);
        						app.setError(_t);
                                if(_t instanceof RestException){
                                    app.displayError("Unable to create folder: " + ((RestException) _t).getHttpStatusText());
                                }
                                else
                                    app.displayError("System error creating folder: " + _t.getMessage());
                            }

            				@Override
            				protected void onUnauthorized(Response response) {
            					app.sessionExpired();
            				}
                        };
                        newFolder.setHeader("X-Auth-Token", app.getToken());
                        newFolder.setHeader("Content-Type", "application/folder");
                        newFolder.setHeader("Accept", "*/*");
                        newFolder.setHeader("Content-Length", "0");
                        Scheduler.get().scheduleDeferred(newFolder);
		        	}
		        	else
		        		app.displayError("Error heading folder: " + ((RestException) t).getHttpStatusText());
		        }
		        else
		            app.displayError("System error heading folder: " + t.getMessage());

		        GWT.log("Error heading folder", t);
				app.setError(t);
			}

			@Override
			protected void onUnauthorized(Response response) {
				app.sessionExpired();
			}
		};
		headFolder.setHeader("X-Auth-Token", app.getToken());
		Scheduler.get().scheduleDeferred(headFolder);
	}
}
