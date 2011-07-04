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
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 *
 * Move file or folder to trash.
 *
 *
 */
public class ToTrashCommand implements Command{
	private PopupPanel containerPanel;

	public ToTrashCommand(PopupPanel _containerPanel){
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
			FolderResource fdto = ((RestResourceWrapper) selection).getResource();
			PostCommand tot = new PostCommand(fdto.getUri()+"?trash=","",200){

				@Override
				public void onComplete() {
					//TODO:CELLTREE
					/*
					TreeItem folder = GSS.get().getFolders().getCurrent();
					if(folder.getParentItem() != null){
						GSS.get().getFolders().select(folder.getParentItem());
						GSS.get().getFolders().updateFolder((DnDTreeItem) folder.getParentItem());
					}
					GSS.get().getFolders().update(GSS.get().getFolders().getTrashItem());
					*/
					FolderResource fres = ((RestResourceWrapper) GSS.get().getTreeView().getSelection()).getResource();
					GSS.get().getTreeView().updateNodeChildrenForRemove(fres.getParentURI());
					GSS.get().getTreeView().clearSelection();
					//GSS.get().getTreeView().updateNode(GSS.get().getTreeView().getTrash());
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
							GSS.get().displayError("Folder does not exist");
						else
							GSS.get().displayError("Unable to trash folder:"+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error trashing folder:"+t.getMessage());
				}
			};
			DeferredCommand.addCommand(tot);
		} else if (selection instanceof FileResource) {
			FileResource fdto = (FileResource) selection;
			PostCommand tot = new PostCommand(fdto.getUri()+"?trash=","",200){

				@Override
				public void onComplete() {
					GSS.get().getTreeView().updateNode(GSS.get().getTreeView().getSelection());
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
						else
							GSS.get().displayError("Unable to trash file:"+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error trashing file:"+t.getMessage());
				}
			};
			DeferredCommand.addCommand(tot);

		}
		else if (selection instanceof List) {
			List<FileResource> fdtos = (List<FileResource>) selection;
			final List<String> fileIds = new ArrayList<String>();
			for(FileResource f : fdtos)
				fileIds.add(f.getUri()+"?trash=");
			MultiplePostCommand tot = new MultiplePostCommand(fileIds.toArray(new String[0]),200){

				@Override
				public void onComplete() {
					GSS.get().getTreeView().updateNode(GSS.get().getTreeView().getSelection());
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
						else
							GSS.get().displayError("Unable to trash file:"+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error trashing file:"+t.getMessage());
				}
			};
			DeferredCommand.addCommand(tot);
		}
	}

}
