/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


public class ResreshOthersSharesCommand implements Command{
	private PopupPanel containerPanel;

	public ResreshOthersSharesCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		//TODO:CELLTREE
		//GSS.get().getFolders().update( GSS.get().getFolders().getCurrent());
	}

}
