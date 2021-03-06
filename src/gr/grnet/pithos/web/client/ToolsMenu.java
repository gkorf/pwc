/*
 * Copyright 2011-2013 GRNET S.A. All rights reserved.
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
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.commands.*;
import gr.grnet.pithos.web.client.commands.PurgeContainerCommand;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.grouptree.Group;
import gr.grnet.pithos.web.client.grouptree.GroupTreeView;
import gr.grnet.pithos.web.client.grouptree.User;

import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'Folder Context' menu implementation.
 */
public class ToolsMenu extends PopupPanel {

	/**
	 * The widget's images.
	 */
	private final Images images;

	/**
	 * The image bundle for this widget's images that reuses images defined in
	 * other menus.
	 */
	public interface Images extends GroupTreeView.Images {
	}

	private MenuItem pasteItem;

	private boolean empty = true;
	
	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public ToolsMenu(final Pithos app, Images newImages, TreeView selectedTree, Folder folder, final List<File> files) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		images = newImages;
        MenuBar contextMenu = new MenuBar(true);

        if (folder != null) {
	        Boolean[] permissions = folder.getPermissions().get(app.getUserID());
	    	boolean canWrite = folder.getOwnerID().equals(app.getUserID()) || (permissions!= null && permissions[1] != null && permissions[1]);
	    	boolean isFolderTreeSelected = selectedTree.equals(app.getFolderTreeView());
	    	boolean isMysharedTreeSelected = app.isMySharedSelected();
	    	
	        if (!folder.isInTrash()) {
	        	if (canWrite) {

			        if (isFolderTreeSelected || isMysharedTreeSelected) {
			        	MenuItem cut = null;
				        if (files != null && !files.isEmpty()) {
							cut = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut files</span>", true, new CutCommand(app, this, files));
				        }
				        else if (!folder.isContainer()) {
				            cut = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut folder</span>", true, new CutCommand(app, this, folder));
				        }
				        if (cut != null) {
				        	contextMenu.addItem(cut);
				        	empty = false;
				        }
			        }
	        	}
	
        		MenuItem copy = null;
	        	if (files != null && !files.isEmpty())
	        		copy = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy files</span>", true, new CopyCommand(app, this, files));
	        	else if (isFolderTreeSelected && !folder.isContainer())
	        		copy = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy folder</span>", true, new CopyCommand(app, this, folder));
	        	if (copy != null) {
	        		contextMenu.addItem(copy);
		        	empty = false;
	        	}
		
	        	if (canWrite) {
			        if (!isMysharedTreeSelected && !app.getClipboard().isEmpty()) {
			        	Object item = app.getClipboard().getItem();
			        	boolean showPaste = false;
			        	if (item instanceof List) {
			        		@SuppressWarnings("unchecked")
							List<File> _files = (List<File>) item;
			        		if (_files.get(0).getOwnerID().equals(folder.getOwnerID()))
			        			showPaste = true;
			        	}
			        	else {
			        		Folder f = (Folder) item;
			        		if (f.getOwnerID().equals(folder.getOwnerID()))
			        			showPaste = true;
			        	}
			        	if (showPaste) {
				            pasteItem = new MenuItem("<span id = 'folderContextMenu.paste'>" + AbstractImagePrototype.create(newImages.paste()).getHTML() + "&nbsp;Paste</span>", true, new PasteCommand(app, this, folder));
				            contextMenu.addItem(pasteItem);
				        	empty = false;
			        	}
			        }
	
				    if (isFolderTreeSelected || isMysharedTreeSelected) {
				    	MenuItem moveToTrash = null;
				    	if (files != null && !files.isEmpty()) {
				    		moveToTrash = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move files to Trash</span>", true, new ToTrashCommand(app, this, files));
				    	}
				    	else if (!folder.isContainer()) {
				    		moveToTrash = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move folder to Trash</span>", true, new ToTrashCommand(app, this, folder));
				    	}
				    	if (moveToTrash != null) {
				    		contextMenu.addItem(moveToTrash);
				        	empty = false;
				    	}
				    }
				    if (isFolderTreeSelected || isMysharedTreeSelected) {
				        if (files != null && files.size() == 1) {
				        	contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;File properties</span>", true, new PropertiesCommand(app, this, files, PropertiesCommand.PROPERTIES)));
				        	contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.group()).getHTML() + "&nbsp;Share</span>", true, new PropertiesCommand(app, this, files, PropertiesCommand.PERMISSIONS)));
			        		contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.versions()).getHTML() + "&nbsp;Versions</span>", true, new PropertiesCommand(app, this, files, PropertiesCommand.VERSIONS)));
				        	empty = false;
				        }
				        else if (!folder.isContainer()) {
				        	contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Folder properties</span>", true, new PropertiesCommand(app, this, folder, PropertiesCommand.PROPERTIES)));
				        	contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Share</span>", true, new PropertiesCommand(app, this, folder, PropertiesCommand.PERMISSIONS)));
				        	empty = false;
				        }
				    }
			        if (files != null && !files.isEmpty()) {
					    contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.download()).getHTML() + "&nbsp;Download</span>", true, new Command() {
							
							@Override
							public void execute() {
					        	for (File f: files)
					        		Window.open(Pithos.getStorageAPIURL() + f.getOwnerID() + f.getUri(), "_blank", "");
							}
						}));
			        	empty = false;
			        }
			        if (isFolderTreeSelected && folder.isContainer()) {
		    			MenuItem emptyContainer = new MenuItem(
                            "<span>" + Const.PurgeContainer(folder.getName()) + "</span>",
                            true,
                            new PurgeContainerCommand(app, this, folder)
                        );
		    			contextMenu.addItem(emptyContainer);
			        	empty = false;
			        }
	        	}
	        }
	        else {
	        	if (!folder.isTrash()) {
	        		MenuItem restore = null;
	        		if (files != null && !files.isEmpty())
	        			restore = new MenuItem("<span>" + AbstractImagePrototype.create(images.versions()).getHTML() + "&nbsp;Restore</span>", true, new RestoreTrashCommand(app, this, files));
	        		else
	        			restore = new MenuItem("<span>" + AbstractImagePrototype.create(images.versions()).getHTML() + "&nbsp;Restore</span>", true, new RestoreTrashCommand(app, this, folder));
        			contextMenu.addItem(restore);

	    			MenuItem delete = new MenuItem("<span id = 'folderContextMenu.delete'>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(app, this, folder, MessagePanel.images));
			        contextMenu.addItem(delete);
		        	empty = false;
	        	}
	        	else {
	    			MenuItem emptyTrash = new MenuItem("<span>" + AbstractImagePrototype.create(images.emptyTrash()).getHTML() + "&nbsp;Empty Trash</span>", true, new PurgeContainerCommand(app, this, folder));
	    			contextMenu.addItem(emptyTrash);
		        	empty = false;
	        	}
	        }
        }
        else {
        	Object o = app.getGroupTreeView().getSelected();
        	if (o != null) {
	        	if (o instanceof User) {
	                MenuItem removeUser = new MenuItem("<span>" + AbstractImagePrototype.create(images.delete()).getHTML() + "&nbsp;Remove User</span>", true, new RemoveUserCommand(app, this, (User) o));
	                contextMenu.addItem(removeUser);
		        	empty = false;
	        	}
	        	else if (o instanceof Group) {
	    	        MenuItem addUser = new MenuItem("<span>" + AbstractImagePrototype.create(images.user()).getHTML() + "&nbsp;Add User</span>", true, new AddUserCommand(app, this, (Group) o));
	    	        contextMenu.addItem(addUser);
	    	
	    	        MenuItem deleteGroup = new MenuItem("<span>" + AbstractImagePrototype.create(images.delete()).getHTML() + "&nbsp;Delete Group</span>", true, new DeleteGroupCommand(app, this, (Group) o));
	    	        contextMenu.addItem(deleteGroup);
		        	empty = false;
	        	}
        	}
        }
        MenuItem createGroup = new MenuItem("<span>" + AbstractImagePrototype.create(images.group()).getHTML() + "&nbsp;Create Group</span>", true, new CreateGroupCommand(app, this));
        contextMenu.addItem(createGroup);
    	empty = false;
		add(contextMenu);
	}
	
	public boolean isEmpty() {
		return empty;
	}
}
