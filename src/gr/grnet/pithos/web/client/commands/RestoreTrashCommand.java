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

import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.rest.MultiplePostCommand;
import gr.grnet.pithos.web.client.rest.PostCommand;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.TrashFolderResource;
import gr.grnet.pithos.web.client.rest.resource.TrashResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 *
 * Restore trashed files and folders.
 *
 */
public class RestoreTrashCommand implements Command{
	private PopupPanel containerPanel;

	public RestoreTrashCommand(PopupPanel _containerPanel){
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null){
			// Check to see if Trash Node is selected.
			List folderList = new ArrayList();
			TrashResource trashItem = GSS.get().getTreeView().getTrash();
			for(int i=0 ; i < trashItem.getFolders().size() ; i++)
				folderList.add(trashItem.getFolders().get(i));
			return;
		}
		GWT.log("selection: " + selection.toString(), null);
		if (selection instanceof FileResource) {
			final FileResource resource = (FileResource)selection;
			PostCommand rt = new PostCommand(resource.getUri()+"?restore=","", 200){

				@Override
				public void onComplete() {
					//TODO:CELLTREE
					//GSS.get().getFolders().update(GSS.get().getFolders().getTrashItem());
					
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
							GSS.get().displayError("File does not exist");
						else if(statusCode == 409)
							GSS.get().displayError("A file with the same name already exists");
						else if(statusCode == 413)
							GSS.get().displayError("Your quota has been exceeded");
						else
							GSS.get().displayError("Unable to restore file:"+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error restoring file:"+t.getMessage());
				}
			};
			DeferredCommand.addCommand(rt);
		}
		else if (selection instanceof List) {
			final List<FileResource> fdtos = (List<FileResource>) selection;
			final List<String> fileIds = new ArrayList<String>();
			for(FileResource f : fdtos)
				fileIds.add(f.getUri()+"?restore=");
			MultiplePostCommand rt = new MultiplePostCommand(fileIds.toArray(new String[0]), 200){

				@Override
				public void onComplete() {
					//TODO:CELLTREE
					//GSS.get().getFolders().update(GSS.get().getFolders().getTrashItem());
					GSS.get().showFileList(true);
				}

				@Override
				public void onError(String p, Throwable t) {
					GWT.log("", t);
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						if(statusCode == 405)
							GSS.get().displayError("You don't have the necessary permissions");
						else if(statusCode == 404)
							GSS.get().displayError("File does not exist");
						else if(statusCode == 409)
							GSS.get().displayError("A file with the same name already exists");
						else if(statusCode == 413)
							GSS.get().displayError("Your quota has been exceeded");
						else
							GSS.get().displayError("Unable to restore file::"+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error restoring file:"+t.getMessage());
				}
			};
			DeferredCommand.addCommand(rt);
		}
		else if (selection instanceof TrashFolderResource) {
			final FolderResource resource = ((TrashFolderResource)selection).getResource();
			PostCommand rt = new PostCommand(resource.getUri()+"?restore=","", 200){

				@Override
				public void onComplete() {
					//TODO:CELLTREE
					/*
					GSS.get().getFolders().updateFolder((DnDTreeItem) GSS.get().getFolders().getRootItem());

					GSS.get().getFolders().update(GSS.get().getFolders().getTrashItem());
					*/
					
					GSS.get().getTreeView().updateTrashNode();
					GSS.get().getTreeView().updateRootNode();
				}

				@Override
				public void onError(Throwable t) {
					GWT.log("", t);
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						if(statusCode == 405)
							GSS.get().displayError("You don't have the necessary permissions");
						else if(statusCode == 404)
							GSS.get().displayError("Folder does not exist");
						else if(statusCode == 409)
							GSS.get().displayError("A folder with the same name already exists");
						else if(statusCode == 413)
							GSS.get().displayError("Your quota has been exceeded");
						else
							GSS.get().displayError("Unable to restore folder::"+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error restoring folder:"+t.getMessage());
				}
			};
			DeferredCommand.addCommand(rt);
		}

	}

}
