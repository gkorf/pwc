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

		@Source("gr/grnet/pithos/resources/group.png")
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

        @Source("gr/grnet/pithos/resources/trashcan_empty.png")
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
    }

	public static native String getDate()/*-{
		return (new Date()).toUTCString();
	}-*/;

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public FileContextMenu(final Pithos app, Images newImages, Folder selectedFolder, List<File> selectedFiles, boolean isTrash) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;
        MenuBar contextMenu = new MenuBar(true);

        if (!selectedFolder.isInTrash()) {
	        if (app.getClipboard().hasFiles()) {
	            pasteItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.paste()).getHTML() + "&nbsp;Paste</span>", true, new PasteCommand(app, this, selectedFolder));
	            contextMenu.addItem(pasteItem);
	        }

	        MenuItem upload = new MenuItem("<span>" + AbstractImagePrototype.create(images.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(app, this, selectedFolder));
	        contextMenu.addItem(upload);

			cutItem = new MenuItem("<span id='fileContextMenu.cut'>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(app, this, selectedFiles));
            contextMenu.addItem(cutItem);

			copyItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(app, this, selectedFiles));
            contextMenu.addItem(copyItem);

			trashItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(app, this, selectedFiles));
            contextMenu.addItem(trashItem);
        }
        else {
			MenuItem restore = new MenuItem("<span>" + AbstractImagePrototype.create(images.versions()).getHTML() + "&nbsp;Restore</span>", true, new RestoreTrashCommand(app, this, selectedFiles));
			contextMenu.addItem(restore);
        }

		deleteItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(app, this, selectedFiles, MessagePanel.images));
        contextMenu.addItem(deleteItem);

//      MenuItem refresh = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
//      contextMenu.addItem(refresh);
//			sharingItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, images, 1));
//            contextMenu.addItem(sharingItem);
        if (!selectedFolder.isInTrash()) {
        	contextMenu.addItem(new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(app, this, selectedFiles, images, 0)));

            if (!selectedFiles.isEmpty())
			    contextMenu.addItem(new MenuItem("<span><a class='hidden-link' href='" + app.getApiPath() + app.getUsername() + selectedFiles.get(0).getUri() + "?X-Auth-Token=" + app.getToken() + "' target='_blank'>" + AbstractImagePrototype.create(newImages.download()).getHTML() + " Download</a></span>", true, (Command) null));
        }
		MenuItem unSelect = new MenuItem("<span>" + AbstractImagePrototype.create(images.unselectAll()).getHTML() + "&nbsp;Unselect</span>", true, new Command() {
            @Override
            public void execute() {
                hide();
                app.getFileList().clearSelectedRows();
            }
        });
		contextMenu.addItem(unSelect);

//		}
		add(contextMenu);
	}
}
