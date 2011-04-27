/*
 * Copyright 2009 Electronic Business Systems Ltd.
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
package org.gss_project.gss.web.client.commands;

import org.gss_project.gss.web.client.FileMenu;
import org.gss_project.gss.web.client.GSS;
import org.gss_project.gss.web.client.rest.resource.FileResource;
import org.gss_project.gss.web.client.rest.resource.GroupUserResource;

import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * @author kman
 *
 */
public class RefreshCommand implements Command {

	final FileMenu.Images newImages;

	private PopupPanel containerPanel;

	/**
	 * @param _containerPanel
	 * @param _newImages the images of all the possible delete dialogs
	 */
	public RefreshCommand(PopupPanel _containerPanel, final FileMenu.Images _newImages) {
		containerPanel = _containerPanel;
		newImages = _newImages;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		if (GSS.get().getCurrentSelection() instanceof FileResource || GSS.get().getCurrentSelection() instanceof List)
			GSS.get().showFileList(true);
		else if (GSS.get().getCurrentSelection() instanceof GroupUserResource)
			return;
		else{
			//TODO:CELLTREE
			//DnDTreeItem selectedTreeItem = (DnDTreeItem) GSS.get().getFolders().getCurrent();
			//if(selectedTreeItem != null){
				//GSS.get().getFolders().updateFolder(selectedTreeItem);
			GSS.get().getTreeView().updateNode(GSS.get().getTreeView().getSelection());
				GSS.get().showFileList(true);
			//}
		}
	}
}
