/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
 *
 * This file is part of GSS.
 *
 * GSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GSS.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.FileMenu.Images;
import gr.grnet.pithos.web.client.FolderPropertiesDialog;
import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.rest.GetCommand;
import gr.grnet.pithos.web.client.rest.MultipleGetCommand;
import gr.grnet.pithos.web.client.rest.resource.GroupResource;
import gr.grnet.pithos.web.client.rest.resource.GroupsResource;
import gr.grnet.pithos.web.client.rest.resource.RestResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Display the 'new folder' dialog for creating a new folder.
 * @author kman
 *
 */
public class NewFolderCommand implements Command{
	private PopupPanel containerPanel;
	final Images images;

	private List<GroupResource> groups = null;

	/**
	 * @param aContainerPanel
	 * @param newImages the images of the new folder dialog
	 */
	public NewFolderCommand(PopupPanel aContainerPanel, final Images newImages){
		containerPanel = aContainerPanel;
		images=newImages;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		getGroups();
		DeferredCommand.addCommand(new IncrementalCommand() {

			@Override
			public boolean execute() {
				boolean res = canContinue();
				if (res) {
					displayNewFolder();
					return false;
				}
				return true;
			}

		});
	}

	private boolean canContinue() {
		if (groups == null)
			return false;
		return true;
	}

	void displayNewFolder() {
		RestResource currentFolder = GSS.get().getTreeView().getSelection();
		if (currentFolder == null) {
			GSS.get().displayError("You have to select the parent folder first");
			return;
		}
		FolderPropertiesDialog dlg = new FolderPropertiesDialog(images, true,  groups);
		dlg.center();
	}

	private void getGroups() {
		GetCommand<GroupsResource> gg = new GetCommand<GroupsResource>(GroupsResource.class, GSS.get().getCurrentUserResource().getGroupsPath(), null){

			@Override
			public void onComplete() {
				GroupsResource res = getResult();
				MultipleGetCommand<GroupResource> ga = new MultipleGetCommand<GroupResource>(GroupResource.class, res.getGroupPaths().toArray(new String[]{}), null){

					@Override
					public void onComplete() {
						List<GroupResource> groupList = getResult();
						groups = groupList;
					}

					@Override
					public void onError(Throwable t) {
						GWT.log("", t);
						GSS.get().displayError("Unable to fetch groups");
						groups = new ArrayList<GroupResource>();
					}

					@Override
					public void onError(String p, Throwable throwable) {
						GWT.log("Path:"+p, throwable);
					}
				};
				DeferredCommand.addCommand(ga);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				GSS.get().displayError("Unable to fetch groups");
				groups = new ArrayList<GroupResource>();
			}
		};
		DeferredCommand.addCommand(gg);
	}

}
