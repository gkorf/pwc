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

import gr.grnet.pithos.web.client.*;
import gr.grnet.pithos.web.client.FileShareDialog;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;

import java.util.List;

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
	public static final int PUBLISH = 3;

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

        Pithos.LOG("PropertiesCommand::execute(), resource = ", resource);
        if (resource instanceof Folder) {
            final Folder folder = (Folder) resource;
            switch (tabToShow) {
				case PROPERTIES:
					FolderPropertiesDialog dlg = new FolderPropertiesDialog(app, false, folder);
					dlg.center();
					break;
				case PERMISSIONS:
					app.scheduleFolderHeadCommand(folder, new Command() {
						
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
		                app.scheduleFileHeadCommand(f, new Command() {
							
							@Override
							public void execute() {
								FilePropertiesDialog dlg = new FilePropertiesDialog(app, f);
				                dlg.center();
							}
						});
						break;
					case PERMISSIONS:
		                app.scheduleFileHeadCommand(f, new Command() {
							
							@Override
							public void execute() {
                                FileShareDialog dlg = new FileShareDialog(app, f);
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
}
