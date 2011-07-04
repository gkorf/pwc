/*
 * Copyright 2011 GRNET S.A. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 *   1. Redistributions of source code must retain the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials
 *      provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY GRNET S.A. ``AS IS'' AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GRNET S.A OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and
 * documentation are those of the authors and should not be
 * interpreted as representing official policies, either expressed
 * or implied, of GRNET S.A.
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
