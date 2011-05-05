/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.UserAddDialog;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


public class NewUserCommand implements Command {
	private PopupPanel containerPanel;

	/**
	 * @param _containerPanel
	 */
	public NewUserCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		displayNewUser();
	}

	private void displayNewUser() {
		UserAddDialog dlg = new UserAddDialog();
		dlg.center();
	}

}
