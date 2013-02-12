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

import gr.grnet.pithos.web.client.commands.CopyCommand;
import gr.grnet.pithos.web.client.commands.CutCommand;
import gr.grnet.pithos.web.client.commands.DeleteCommand;
import gr.grnet.pithos.web.client.commands.PasteCommand;
import gr.grnet.pithos.web.client.commands.PropertiesCommand;
import gr.grnet.pithos.web.client.commands.RestoreTrashCommand;
import gr.grnet.pithos.web.client.commands.ToTrashCommand;
import gr.grnet.pithos.web.client.commands.UploadFileCommand;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;

import java.util.List;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'File Context' menu implementation.
 */
public class FileContextMenu extends PopupPanel {

	/**
	 * The widget's images.
	 */
	private final Images images;

	private MenuItem cutItem;

	private MenuItem copyItem;

	private MenuItem pasteItem;

	private MenuItem updateItem;

	private MenuItem sharingItem;

	private MenuItem propItem;

	private MenuItem trashItem;

	private MenuItem deleteItem;

	private MenuItem downloadItem;

	private MenuItem saveAsItem;

	/**
	 * The image bundle for this widget's images that reuses images defined in
	 * other menus.
	 */
	public interface Images extends ClientBundle {

		@Source("gr/grnet/pithos/resources/mimetypes/document.png")
		ImageResource fileContextMenu();

		@Source("gr/grnet/pithos/resources/doc_versions.png")
		ImageResource versions();

		@Source("gr/grnet/pithos/resources/groups22.png")
		ImageResource sharing();

		@Source("gr/grnet/pithos/resources/border_remove.png")
		ImageResource unselectAll();

		@Source("gr/grnet/pithos/resources/demo.png")
		ImageResource viewImage();

        @Source("gr/grnet/pithos/resources/folder_new.png")
        ImageResource folderNew();

        @Source("gr/grnet/pithos/resources/folder_outbox.png")
        ImageResource fileUpdate();

        @Source("gr/grnet/pithos/resources/view_text.png")
        ImageResource viewText();

        @Source("gr/grnet/pithos/resources/folder_inbox.png")
        ImageResource download();

        @Source("gr/grnet/pithos/resources/trash.png")
        ImageResource emptyTrash();

        @Source("gr/grnet/pithos/resources/refresh.png")
        ImageResource refresh();

        /**
         * Will bundle the file 'editcut.png' residing in the package
         * 'gr.grnet.pithos.web.resources'.
         *
         * @return the image prototype
         */
        @Source("gr/grnet/pithos/resources/editcut.png")
        ImageResource cut();

        /**
         * Will bundle the file 'editcopy.png' residing in the package
         * 'gr.grnet.pithos.web.resources'.
         *
         * @return the image prototype
         */
        @Source("gr/grnet/pithos/resources/editcopy.png")
        ImageResource copy();

        /**
         * Will bundle the file 'editpaste.png' residing in the package
         * 'gr.grnet.pithos.web.resources'.
         *
         * @return the image prototype
         */
        @Source("gr/grnet/pithos/resources/editpaste.png")
        ImageResource paste();

        /**
         * Will bundle the file 'editdelete.png' residing in the package
         * 'gr.grnet.pithos.web.resources'.
         *
         * @return the image prototype
         */
        @Source("gr/grnet/pithos/resources/editdelete.png")
        ImageResource delete();

        /**
         * Will bundle the file 'translate.png' residing in the package
         * 'gr.grnet.pithos.web.resources'.
         *
         * @return the image prototype
         */
        @Source("gr/grnet/pithos/resources/translate.png")
        ImageResource selectAll();
        
        @Source("gr/grnet/pithos/resources/internet.png")
        ImageResource internet();
    }

	public static native String getDate()/*-{
		return (new Date()).toUTCString();
	}-*/;

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public FileContextMenu(final Pithos app, Images newImages, TreeView selectedTree, Folder selectedFolder, final List<File> selectedFiles) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		images = newImages;
        MenuBar contextMenu = new MenuBar(true);
        Boolean[] permissions = null;
        boolean canWrite = true;
        if (selectedFolder != null) {
        	permissions = selectedFolder.getPermissions().get(app.getUserID());
        	canWrite = selectedFolder.getOwnerID().equals(app.getUserID()) || (permissions!= null && permissions[1] != null && permissions[1]);
		}
        else {
        	for (File f : selectedFiles) {
        		permissions = f.getPermissions().get(app.getUserID());
        		canWrite &= (f.getOwnerID().equals(app.getUserID()) || (permissions!= null && permissions[1] != null && permissions[1]));
        	}
        }
        boolean isFolderTreeSelected = selectedTree.equals(app.getFolderTreeView());
        boolean isMysharedSelected = selectedTree.equals(app.getMySharedTreeView());
        if (selectedFolder != null) {
		    if (!selectedFolder.isInTrash()) {
		        if (canWrite && app.getClipboard().hasFiles() && !isMysharedSelected) {
	        		pasteItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.paste()).getHTML() + "&nbsp;Paste</span>", true, new PasteCommand(app, this, selectedFolder));
	        		contextMenu.addItem(pasteItem);
		        }
		
		        if (canWrite && !isMysharedSelected) {
			        MenuItem upload = new MenuItem("<span>" + AbstractImagePrototype.create(images.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(app, this, selectedFolder));
			        contextMenu.addItem(upload);
		        }
		        if (isFolderTreeSelected || isMysharedSelected) {
					cutItem = new MenuItem("<span id='fileContextMenu.cut'>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(app, this, selectedFiles));
		            contextMenu.addItem(cutItem);
		        }
		
				copyItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(app, this, selectedFiles));
		        contextMenu.addItem(copyItem);
		
		        if (isFolderTreeSelected || isMysharedSelected) {
					trashItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(app, this, selectedFiles));
		            contextMenu.addItem(trashItem);
		        }
		    }
		    else {
				MenuItem restore = new MenuItem("<span>" + AbstractImagePrototype.create(images.versions()).getHTML() + "&nbsp;Restore</span>", true, new RestoreTrashCommand(app, this, selectedFiles));
				contextMenu.addItem(restore);
		    }
        }

        if (isFolderTreeSelected || isMysharedSelected) {
			deleteItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(app, this, selectedFiles, MessagePanel.images));
	        contextMenu.addItem(deleteItem);
        }

        if (selectedFolder != null && !selectedFolder.isInTrash()) {
        	if ((isFolderTreeSelected || isMysharedSelected) && selectedFiles.size() == 1) {
        		contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(app, this, selectedFiles, PropertiesCommand.PROPERTIES)));
        		contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(app, this, selectedFiles, PropertiesCommand.PERMISSIONS)));
        		contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.internet()).getHTML() + "&nbsp;Publish</span>", true, new PropertiesCommand(app, this, selectedFiles, PropertiesCommand.PUBLISH)));
       			contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.versions()).getHTML() + "&nbsp;Versions</span>", true, new PropertiesCommand(app, this, selectedFiles, PropertiesCommand.VERSIONS)));
        	}

        }
	    contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.download()).getHTML() + "&nbsp;Download</span>", true, new Command() {
			
			@Override
			public void execute() {
				for (File f : selectedFiles)
					Window.open(app.getApiPath() + f.getOwnerID() + f.getUri(), "_blank", "");
			}
		}));

		add(contextMenu);
	}
}
