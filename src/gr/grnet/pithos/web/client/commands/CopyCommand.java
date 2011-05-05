/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.clipboard.ClipboardItem;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;
/**
 *
 * Command for copying a file, folder or user to GSS Clipboard
 *
 */
public class CopyCommand implements Command{
	private PopupPanel containerPanel;

	public CopyCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null)
			return;

		if (selection instanceof RestResourceWrapper) {
			ClipboardItem clipboardItem = new ClipboardItem((RestResourceWrapper) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		} else if (selection instanceof FileResource) {
			ClipboardItem clipboardItem = new ClipboardItem((FileResource) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		} else if (selection instanceof GroupUserResource) {
			ClipboardItem clipboardItem = new ClipboardItem((GroupUserResource) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		}
		 else if (selection instanceof List){
			 ClipboardItem clipboardItem = new ClipboardItem((List<FileResource>) selection);
			 GSS.get().getClipboard().setItem(clipboardItem);
		 }

	}

}
