/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.FileMenu;
import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;

import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


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
