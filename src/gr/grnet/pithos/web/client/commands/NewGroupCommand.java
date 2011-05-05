/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.GroupPropertiesDialog;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


public class NewGroupCommand implements Command{
	private PopupPanel containerPanel;

	/**
	 * @param _containerPanel
	 */
	public NewGroupCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		displayNewGroup();
	}

	void displayNewGroup() {
		GroupPropertiesDialog dlg = new GroupPropertiesDialog(true);
		dlg.center();
	}

}
