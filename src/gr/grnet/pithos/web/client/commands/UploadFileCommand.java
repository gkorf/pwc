/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.FileUploadDialog;
import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.rest.GetCommand;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Upload a file command
 *
 */
public class UploadFileCommand implements Command {

	private PopupPanel containerPanel;
	private List<FileResource> files;

	public UploadFileCommand(PopupPanel _containerPanel) {
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		if(containerPanel!=null)
			containerPanel.hide();
		displayNewFile();
	}

	/**
	 * Display the 'new file' dialog for uploading a new file to the system.
	 */
	private void displayNewFile() {
		RestResource currentFolder = GSS.get().getTreeView().getSelection();
		if (currentFolder == null) {
			GSS.get().displayError("You have to select the parent folder first");
			return;
		}
		getFileList();
		DeferredCommand.addCommand(new IncrementalCommand() {

			@Override
			public boolean execute() {
				boolean res = canContinue();
				if (res) {
					FileUploadDialog dlg = GWT.create(FileUploadDialog.class);
					dlg.setFiles(files);
					dlg.center();
					return false;
				}
				return true;
			}

		});
	}

	private boolean canContinue() {
		if (files != null )
			return true;
		return false;
	}

	private void getFileList() {
		GetCommand<FolderResource> eg = new GetCommand<FolderResource>(FolderResource.class,((RestResourceWrapper)GSS.get().getTreeView().getSelection()).getUri(), null){

			@Override
			public void onComplete() {
				files = getResult().getFiles();
			}

			@Override
			public void onError(Throwable t) {
				files = new ArrayList<FileResource>();
			}

		};
		DeferredCommand.addCommand(eg);
	}

}
