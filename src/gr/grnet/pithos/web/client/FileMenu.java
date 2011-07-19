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

import gr.grnet.pithos.web.client.commands.NewFolderCommand;
import gr.grnet.pithos.web.client.commands.PropertiesCommand;
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

/**
 * The 'File' menu implementation.
 */
public class FileMenu extends MenuBar {

	/**
	 * The widget's images.
	 */
	private final Images images;

	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ClientBundle,FilePropertiesDialog.Images {

		@Source("gr/grnet/pithos/resources/folder_new.png")
		ImageResource folderNew();

		@Source("gr/grnet/pithos/resources/folder_outbox.png")
		ImageResource fileUpdate();

		@Source("gr/grnet/pithos/resources/view_text.png")
		ImageResource viewText();

		@Override
		@Source("gr/grnet/pithos/resources/folder_inbox.png")
		ImageResource download();

		@Source("gr/grnet/pithos/resources/trashcan_empty.png")
		ImageResource emptyTrash();

		@Source("gr/grnet/pithos/resources/internet.png")
		ImageResource sharing();

		@Source("gr/grnet/pithos/resources/refresh.png")
		ImageResource refresh();

        @Source("gr/grnet/pithos/resources/")
        ImageResource unselectAll();
    }

	/**
	 * The widget's constructor.
	 *
	 * @param _images the image bundle passed on by the parent object
	 */
	public FileMenu(Pithos _app, final Images _images) {
        super(true);
		setAnimationEnabled(true);
		images = _images;

        Folder selectedFolder = _app.getFolderTreeView().getSelection();
        List<File> selectedFiles = _app.getFileList().getSelectedFiles();
        if (selectedFolder != null) {
		    MenuItem newFolderItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.folderNew()).getHTML() + "&nbsp;New Folder</span>", true, new NewFolderCommand(null, selectedFolder, images));
		    addItem(newFolderItem);

            MenuItem uploadItem = new MenuItem("<span id='topMenu.file.upload'>" + AbstractImagePrototype.create(images.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(null, selectedFolder));
            addItem(uploadItem);
        }
        if (selectedFiles.size() == 1) {
            addItem(new MenuItem("<span><a class='hidden-link' href='" + Pithos.get().getApiPath() + Pithos.get().getUsername() + selectedFiles.get(0).getUri() + "?X-Auth-Token=" + Pithos.get().getToken() + "' target='_blank'>" + AbstractImagePrototype.create(images.download()).getHTML() + " Download</a></span>", true, (Command) null));
        }

//        MenuItem emptyTrashItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.emptyTrash()).getHTML() + "&nbsp;Empty Trash</span>", true, new EmptyTrashCommand(this));
//        emptyTrashItem.getElement().setId("topMenu.file.emptyTrash");
//        contextMenu.addItem(emptyTrashItem);

//        MenuItem refreshItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
//        refreshItem.getElement().setId("topMenu.file.refresh");
//        contextMenu.addItem(refreshItem);

//        MenuItem sharingItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, images, 1));
//        sharingItem.getElement().setId("topMenu.file.sharing");
//        contextMenu.addItem(sharingItem)
//                       .setVisible(propertiesVisible);
//
        if (selectedFiles.size() > 0 || selectedFolder != null) {
            MenuItem propertiesItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(Pithos.get(), null, selectedFiles.size() > 0 ? selectedFiles : selectedFolder, images, 0));
            addItem(propertiesItem);
        }
	}
}
