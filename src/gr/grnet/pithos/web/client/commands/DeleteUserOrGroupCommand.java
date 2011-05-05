/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.DeleteGroupDialog;
import gr.grnet.pithos.web.client.DeleteUserDialog;
import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.Groups.Images;
import gr.grnet.pithos.web.client.rest.resource.GroupResource;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


public class DeleteUserOrGroupCommand implements Command{
	private PopupPanel containerPanel;
	final Images images;

	/**
	 * @param aContainerPanel
	 * @param newImages the images of the new folder dialog
	 */
	public DeleteUserOrGroupCommand(PopupPanel aContainerPanel, final Images newImages){
		containerPanel = aContainerPanel;
		images = newImages;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		if(GSS.get().getCurrentSelection() instanceof GroupResource)
			displayNewGroup();
		else if(GSS.get().getCurrentSelection() instanceof GroupUserResource)
			displayNewUser();
		else
			GSS.get().displayError("No user or group selected");
	}

	void displayNewGroup() {
		DeleteGroupDialog dlg = new DeleteGroupDialog(images);
		dlg.center();
	}

	void displayNewUser() {
		DeleteUserDialog dlg = new DeleteUserDialog(images);
		dlg.center();
	}

}
