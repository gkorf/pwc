/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.clipboard.Clipboard;
import gr.grnet.pithos.web.client.clipboard.ClipboardItem;
import gr.grnet.pithos.web.client.rest.PostCommand;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.GroupResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.PopupPanel;

public class PasteCommand implements Command {

	private PopupPanel containerPanel;

	public PasteCommand(PopupPanel _containerPanel) {
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		FolderResource selectedFolder = null;
		if(selection != null && selection instanceof RestResourceWrapper)
			selectedFolder = ((RestResourceWrapper)selection).getResource();
		//TODO:CELLTREE
		/*
		else if(GSS.get().getFolders().getCurrent() != null && ((DnDTreeItem)GSS.get().getFolders().getCurrent()).getFolderResource() != null)
			selectedFolder = ((DnDTreeItem)GSS.get().getFolders().getCurrent()).getFolderResource();
		*/
		if (selectedFolder != null) {
			final ClipboardItem citem = GSS.get().getClipboard().getItem();
			if (citem != null && citem.getRestResourceWrapper() != null) {
				String target = selectedFolder.getUri();
				target = target.endsWith("/") ? target : target + '/';
				target = target + URL.encodeComponent(citem.getRestResourceWrapper().getResource().getName());
				if (citem.getOperation() == Clipboard.COPY) {
					PostCommand cf = new PostCommand(citem.getRestResourceWrapper().getUri() + "?copy=" + target, "", 200) {

						@Override
						public void onComplete() {
							//TODO:CELLTREE
							//GSS.get().getFolders().updateFolder((DnDTreeItem) GSS.get().getFolders().getCurrent());
							GSS.get().getTreeView().updateNodeChildren(GSS.get().getTreeView().getSelection());
							GSS.get().getStatusPanel().updateStats();
							GSS.get().getClipboard().setItem(null);
						}

						@Override
						public void onError(Throwable t) {
							GWT.log("", t);
							if(t instanceof RestException){
								int statusCode = ((RestException)t).getHttpStatusCode();
								if(statusCode == 405)
									GSS.get().displayError("You don't have the necessary permissions");

								else if(statusCode == 409)
									GSS.get().displayError("A folder with the same name already exists");
								else if(statusCode == 413)
									GSS.get().displayError("Your quota has been exceeded");
								else
									GSS.get().displayError("Unable to copy folder:"+((RestException)t).getHttpStatusText());
							}
							else
								GSS.get().displayError("System error copying folder:"+t.getMessage());
						}
					};
					DeferredCommand.addCommand(cf);
				} else if (citem.getOperation() == Clipboard.CUT) {
					PostCommand cf = new PostCommand(citem.getRestResourceWrapper().getUri() + "?move=" + target, "", 200) {

						@Override
						public void onComplete() {
							//TODO:CELLTREE
							/*
							List<TreeItem> items = GSS.get().getFolders().getItemsOfTreeForPath(citem.getFolderResource().getUri());
							for (TreeItem item : items)
								if (item.getParentItem() != null && !item.equals(GSS.get().getFolders().getCurrent()))
									GSS.get().getFolders().updateFolder((DnDTreeItem) item.getParentItem());
							GSS.get().getFolders().updateFolder((DnDTreeItem) GSS.get().getFolders().getCurrent());
							*/
							GSS.get().getTreeView().updateNodeChildren(GSS.get().getTreeView().getSelection());
							GSS.get().getTreeView().updateNodeChildrenForRemove(citem.getRestResourceWrapper().getResource().getParentURI());
							GSS.get().getStatusPanel().updateStats();		
							GSS.get().getClipboard().setItem(null);
						}

						@Override
						public void onError(Throwable t) {
							GWT.log("", t);
							if(t instanceof RestException){
								int statusCode = ((RestException)t).getHttpStatusCode();
								if(statusCode == 405)
									GSS.get().displayError("You don't have the necessary permissions");
								else if(statusCode == 409)
									GSS.get().displayError("A folder with the same name already exists");
								else if(statusCode == 413)
									GSS.get().displayError("Your quota has been exceeded");
								else
									GSS.get().displayError("Unable to move folder:"+((RestException)t).getHttpStatusText());
							}
							else
								GSS.get().displayError("System error moving folder:"+t.getMessage());
						}
					};
					DeferredCommand.addCommand(cf);
				}
				return;
			} else if (citem != null && citem.getFile() != null) {
				String target = selectedFolder.getUri();
				target = target.endsWith("/") ? target : target + '/';
				target = target + URL.encodeComponent(citem.getFile().getName());
				if (citem.getOperation() == Clipboard.COPY) {
					PostCommand cf = new PostCommand(citem.getFile().getUri() + "?copy=" + target, "", 200) {

						@Override
						public void onComplete() {
							GSS.get().showFileList(true);
							GSS.get().getStatusPanel().updateStats();
							GSS.get().getClipboard().setItem(null);
						}

						@Override
						public void onError(Throwable t) {
							GWT.log("", t);
							if(t instanceof RestException){
								int statusCode = ((RestException)t).getHttpStatusCode();
								if(statusCode == 405)
									GSS.get().displayError("You don't have the necessary permissions");
								else if(statusCode == 404)
									GSS.get().displayError("File not found");
								else if(statusCode == 409)
									GSS.get().displayError("A file with the same name already exists");
								else if(statusCode == 413)
									GSS.get().displayError("Your quota has been exceeded");
								else
									GSS.get().displayError("Unable to copy file:"+((RestException)t).getHttpStatusText());
							}
							else
								GSS.get().displayError("System error copying file:"+t.getMessage());
						}
					};
					DeferredCommand.addCommand(cf);
				} else if (citem.getOperation() == Clipboard.CUT) {
					PostCommand cf = new PostCommand(citem.getFile().getUri() + "?move=" + target, "", 200) {

						@Override
						public void onComplete() {
							GSS.get().showFileList(true);
							GSS.get().getStatusPanel().updateStats();
							GSS.get().getClipboard().setItem(null);
						}

						@Override
						public void onError(Throwable t) {
							GWT.log("", t);
							if(t instanceof RestException){
								int statusCode = ((RestException)t).getHttpStatusCode();
								if(statusCode == 405)
									GSS.get().displayError("You don't have the necessary permissions");
								else if(statusCode == 404)
									GSS.get().displayError("File not found");
								else if(statusCode == 409)
									GSS.get().displayError("A file with the same name already exists");
								else if(statusCode == 413)
									GSS.get().displayError("Your quota has been exceeded");
								else
									GSS.get().displayError("Unable to copy file:"+((RestException)t).getHttpStatusText());
							}
							else
								GSS.get().displayError("System error copying file:"+t.getMessage());
						}
					};
					DeferredCommand.addCommand(cf);
				}
				return;
			} else if (citem != null && citem.getFiles() != null) {
				List<FileResource> res = citem.getFiles();
				List<String> fileIds = new ArrayList<String>();
				String target = selectedFolder.getUri();
				target = target.endsWith("/") ? target : target + '/';

				if (citem.getOperation() == Clipboard.COPY) {
					for (FileResource fileResource : res) {
						String fileTarget = target + fileResource.getName();
						fileIds.add(fileResource.getUri() + "?copy=" + fileTarget);
					}
					int index = 0;
					executeCopyOrMove(index, fileIds);

				} else if (citem.getOperation() == Clipboard.CUT) {
					for (FileResource fileResource : res) {
						String fileTarget = target + fileResource.getName();
						fileIds.add(fileResource.getUri() + "?move=" + fileTarget);
					}
					int index = 0;
					executeCopyOrMove(index, fileIds);
				}
				return;
			}
		}
	}

	private void executeCopyOrMove(final int index, final List<String> paths){
		if(index >= paths.size()){
			GSS.get().showFileList(true);
			GSS.get().getStatusPanel().updateStats();
			GSS.get().getClipboard().setItem(null);
			return;
		}
		PostCommand cf = new PostCommand(paths.get(index), "", 200) {

			@Override
			public void onComplete() {
				executeCopyOrMove(index+1, paths);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if(statusCode == 404)
						GSS.get().displayError("File not found");
					else if(statusCode == 409)
						GSS.get().displayError("A file with the same name already exists");
					else if(statusCode == 413)
						GSS.get().displayError("Your quota has been exceeded");
					else
						GSS.get().displayError("Unable to copy file:"+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error copying file:"+t.getMessage());
			}
		};
		DeferredCommand.addCommand(cf);
	}
}
