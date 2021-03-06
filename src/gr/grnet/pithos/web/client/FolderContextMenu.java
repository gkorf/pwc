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
import gr.grnet.pithos.web.client.foldertree.Folder;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'Folder Context' menu implementation.
 */
public class FolderContextMenu extends PopupPanel {

	/**
	 * The widget's images.
	 */
	private final Images images;

	/**
	 * The image bundle for this widget's images that reuses images defined in
	 * other menus.
	 */
	public interface Images extends FileContextMenu.Images {
	}

	private MenuItem pasteItem;

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public FolderContextMenu(Pithos app, final Images newImages, TreeView selectedTree, Folder folder) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		images = newImages;
        MenuBar contextMenu = new MenuBar(true);

        Boolean[] permissions = folder.getPermissions().get(app.getUserID());
    	boolean canWrite = folder.getOwnerID().equals(app.getUserID()) || (permissions!= null && permissions[1] != null && permissions[1]);
    	boolean isFolderTreeSelected = selectedTree.equals(app.getFolderTreeView());
    	boolean otherSharedTreeSelected = selectedTree.equals(app.getOtherSharedTreeView());
    	boolean mysharedTreeSelected = selectedTree.equals(app.getMySharedTreeView());
    	
    	MenuItem refresh = new MenuItem("<span id = 'folderContextMenu.refresh'>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(app, this, folder));
        contextMenu.addItem(refresh);

        if (!folder.isInTrash()) {
        	if (canWrite) {
        		if (!mysharedTreeSelected) {
        			MenuItem newFolder = new MenuItem("<span id = 'folderContextMenu.newFolder'>" + AbstractImagePrototype.create(newImages.folderNew()).getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(app, this, folder));
        			contextMenu.addItem(newFolder);
        		}

		        if (isFolderTreeSelected && !folder.isContainer()) {
		            MenuItem cut = new MenuItem("<span id = 'folderContextMenu.cut'>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(app, this, folder));
		            contextMenu.addItem(cut);
		        }
        	}

        	if (!folder.isContainer()) {
        		MenuItem copy = new MenuItem("<span id = 'folderContextMenu.copy'>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(app, this, folder));
        		contextMenu.addItem(copy);
        	}
	
        	if (canWrite) {
		        if (!app.getClipboard().isEmpty() && !mysharedTreeSelected) {
		        	Object item = app.getClipboard().getItem();
		        	boolean showPaste = true;
		        	if (item instanceof Folder) {
		        		Folder f = (Folder) item;
		        		if (f.contains(folder))
		        			showPaste = false;
		        	}
		        	if (showPaste) {
			            pasteItem = new MenuItem("<span id = 'folderContextMenu.paste'>" + AbstractImagePrototype.create(newImages.paste()).getHTML() + "&nbsp;Paste</span>", true, new PasteCommand(app, this, folder));
			            contextMenu.addItem(pasteItem);
		        	}
		        }

			    if (isFolderTreeSelected && !folder.isContainer()) {
			        MenuItem moveToTrash = new MenuItem("<span id = 'folderContextMenu.moveToTrash'>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(app, this, folder));
			        contextMenu.addItem(moveToTrash);

			        contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(app, this, folder, PropertiesCommand.PROPERTIES)));
			        contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Share</span>", true, new PropertiesCommand(app, this, folder, PropertiesCommand.PERMISSIONS)));
			    }
			    
			    if (folder.isContainer()) {
	    			MenuItem purgeContainer = new MenuItem(
                        "<span>" + Const.PurgeContainer(folder.getName()) + "</span>",
                        true,
                        new PurgeContainerCommand(app, this, folder)
                    );
	    			contextMenu.addItem(purgeContainer);
			    }
        	}
        }
        else if(!folder.isTrash()) {
            MenuItem restore = new MenuItem("<span>" + AbstractImagePrototype.create(images.versions()).getHTML() + "&nbsp;Restore</span>", true, new RestoreTrashCommand(app, this, folder));
            contextMenu.addItem(restore);

            MenuItem delete = new MenuItem("<span id = 'folderContextMenu.delete'>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(app, this, folder, MessagePanel.images));
            contextMenu.addItem(delete);
        }
        else {
            MenuItem emptyTrash = new MenuItem("<span>" + AbstractImagePrototype.create(images.emptyTrash()).getHTML() + "&nbsp;Empty Trash</span>", true, new PurgeContainerCommand(app, this, folder));
            contextMenu.addItem(emptyTrash);
        }
		add(contextMenu);
	}
}
