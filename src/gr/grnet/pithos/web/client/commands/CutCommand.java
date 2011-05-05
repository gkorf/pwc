/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.clipboard.Clipboard;
import gr.grnet.pithos.web.client.clipboard.ClipboardItem;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Command for cutting a file, folder or user to GSS Clipboard
 *
 */
public class CutCommand implements Command{
	private PopupPanel containerPanel;

	public CutCommand( PopupPanel _containerPanel ){
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null)
			return;
		GWT.log("selection: " + selection.toString(), null);
		if (selection instanceof RestResourceWrapper) {
			ClipboardItem clipboardItem = new ClipboardItem(Clipboard.CUT, (RestResourceWrapper) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		} else if (selection instanceof FileResource) {
			ClipboardItem clipboardItem = new ClipboardItem(Clipboard.CUT, (FileResource) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		} else if (selection instanceof GroupUserResource) {
			ClipboardItem clipboardItem = new ClipboardItem(Clipboard.CUT, (GroupUserResource) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		}
		else if (selection instanceof List){
			 ClipboardItem clipboardItem = new ClipboardItem(Clipboard.CUT, (List<FileResource>) selection);
			 GSS.get().getClipboard().setItem(clipboardItem);
		 }
	}

}
