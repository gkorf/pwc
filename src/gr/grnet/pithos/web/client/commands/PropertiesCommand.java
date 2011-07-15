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

import gr.grnet.pithos.web.client.FileMenu;
import gr.grnet.pithos.web.client.FilePropertiesDialog;
import gr.grnet.pithos.web.client.FilesPropertiesDialog;
import gr.grnet.pithos.web.client.FolderPropertiesDialog;
import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.FileMenu.Images;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.rest.GetCommand;
import gr.grnet.pithos.web.client.rest.HeadCommand;
import gr.grnet.pithos.web.client.rest.MultipleGetCommand;
import gr.grnet.pithos.web.client.rest.MultipleHeadCommand;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.MultipleGetCommand.Cached;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.GroupResource;
import gr.grnet.pithos.web.client.rest.resource.GroupsResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.PopupPanel;
import org.w3c.css.sac.ElementSelector;

/**
 * The command that displays the appropriate Properties dialog, according to the
 * selected object in the application.
 *
 */
public class PropertiesCommand implements Command {

	final FileMenu.Images newImages;

	private PopupPanel containerPanel;

	private List<GroupResource> groups = null;

	private List<FileResource> versions = null;

	private int tabToShow = 0;

	private String userName;

    private Object resource;

    private GSS app;

	/**
	 * @param _containerPanel
	 * @param _newImages the images of all the possible delete dialogs
	 * @param _tab the tab to switch to
	 */
	public PropertiesCommand(GSS _app, PopupPanel _containerPanel, Object _resource, final FileMenu.Images _newImages, int _tab) {
		containerPanel = _containerPanel;
		newImages = _newImages;
		tabToShow = _tab;
        resource = _resource;
        app = _app;
	}

	@Override
	public void execute() {
        if (containerPanel != null)
		    containerPanel.hide();

        if (resource instanceof Folder) {
            Folder folder = (Folder) resource;
            FolderPropertiesDialog dlg = new FolderPropertiesDialog(app, false, folder);
            dlg.selectTab(tabToShow);
            dlg.center();
        }
        else if (resource instanceof List) {
            List<File> files = (List<File>) resource;
            if (files.size() > 1) {
                FilesPropertiesDialog dlg = new FilesPropertiesDialog(app, files);
                dlg.selectTab(tabToShow);
                dlg.center();
            }
            else {
                FilePropertiesDialog dlg = new FilePropertiesDialog(app, files.get(0));
                dlg.selectTab(tabToShow);
                dlg.center();
            }
        }
	}
}
