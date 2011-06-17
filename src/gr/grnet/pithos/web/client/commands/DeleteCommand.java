/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.DeleteFileDialog;
import gr.grnet.pithos.web.client.DeleteFolderDialog;
import gr.grnet.pithos.web.client.EditMenu.Images;
import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Delete selected object command
 *
 */
public class DeleteCommand implements Command{
	private PopupPanel containerPanel;
	final Images newImages;

	/**
	 * @param _containerPanel
	 * @param _newImages the images of all the possible delete dialogs
	 */
	public DeleteCommand( PopupPanel _containerPanel, final Images _newImages ){
		containerPanel = _containerPanel;
		newImages=_newImages;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		displayDelete();
	}
	/**
	 * Display the delete dialog, according to the selected object.
	 *
	 *
	 */
	void displayDelete() {
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null)
			return;
		GWT.log("selection: " + selection.toString(), null);
		if (selection instanceof RestResourceWrapper) {
			DeleteFolderDialog dlg = new DeleteFolderDialog(newImages);
			dlg.center();
		} else if (selection instanceof FileResource || selection instanceof List) {
			DeleteFileDialog dlg = new DeleteFileDialog(newImages);
			dlg.center();
		} else if (selection instanceof GroupUserResource) {
			// TODO implement user deletion
		}
    }
}
