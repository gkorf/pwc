/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.rest.DeleteCommand;
import gr.grnet.pithos.web.client.rest.RestException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Command to empty trash bin.
 */
public class EmptyTrashCommand implements Command{
	private PopupPanel containerPanel;

	public EmptyTrashCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		DeleteCommand df = new DeleteCommand(GSS.get().getTreeView().getTrash().getUri()){

			@Override
			public void onComplete() {
				GSS.get().getTreeView().updateTrashNode();
				GSS.get().showFileList(true);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if(statusCode == 404)
						GSS.get().displayError("Resource does not exist");
					else
						GSS.get().displayError("Unable to empty trash:"+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error emptying trash:"+t.getMessage());
			}
		};
		DeferredCommand.addCommand(df);
	}

}
