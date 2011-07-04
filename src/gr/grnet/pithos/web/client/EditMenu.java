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
import gr.grnet.pithos.web.client.commands.ToTrashCommand;
import gr.grnet.pithos.web.client.rest.resource.FileResource;

import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'Edit' menu implementation.
 */
public class EditMenu extends PopupPanel implements ClickHandler {

	/**
	 * The widget's images.
	 */
	private final Images images;

	private final MenuBar contextMenu  = new MenuBar(true);

	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images extends ClientBundle, FileMenu.Images, MessagePanel.Images {

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
		@Override
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

		/**
		 * Will bundle the file 'border_remove.png' residing in the package
		 * 'gr.grnet.pithos.web.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("gr/grnet/pithos/resources/border_remove.png")
		ImageResource unselectAll();
	}

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public EditMenu(final Images newImages) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;
		createMenu();
		add(contextMenu);
	}

	@Override
	public void onClick(ClickEvent event) {
		final EditMenu menu = new EditMenu(images);
		final int left = event.getRelativeElement().getAbsoluteLeft();
		final int top = event.getRelativeElement().getAbsoluteTop() + event.getRelativeElement().getOffsetHeight();
		menu.setPopupPosition(left, top);
		menu.show();
	}

	public MenuBar createMenu() {
		contextMenu.clearItems();
		contextMenu.setAutoOpen(false);

		final Command selectAllCommand = new Command() {

			@Override
			public void execute() {
				hide();
				if(GSS.get().isFileListShowing())
					GSS.get().getFileList().selectAllRows();
			}
		};
		final Command unselectAllCommand = new Command() {

			@Override
			public void execute() {
				hide();
				if(GSS.get().isFileListShowing())
					GSS.get().getFileList().clearSelectedRows();
			}
		};

		boolean cutcopyVisible = GSS.get().getCurrentSelection() != null && (GSS.get().getCurrentSelection() instanceof RestResourceWrapper
					|| GSS.get().getCurrentSelection() instanceof FileResource || GSS	.get().getCurrentSelection() instanceof GroupUserResource || GSS	.get().getCurrentSelection() instanceof List);
		String cutLabel = "Cut";
		String copyLabel ="Copy";
		String pasteLabel = "Paste";
		if(GSS.get().getCurrentSelection() != null)
			if(GSS.get().getCurrentSelection() instanceof RestResourceWrapper){
				cutLabel = "Cut Folder";
				copyLabel = "Copy Folder";
			}
			else if(GSS.get().getCurrentSelection() instanceof FileResource){
				cutLabel = "Cut File";
				copyLabel = "Copy File";
			}
			else if(GSS.get().getCurrentSelection() instanceof List){
				cutLabel = "Cut Files";
				copyLabel = "Copy Files";
			}
		if(GSS.get().getClipboard().getItem() != null)
			if(GSS.get().getClipboard().getItem().getFile() != null)
				pasteLabel = "Paste File";
			else if(GSS.get().getClipboard().getItem().getFiles() != null)
				pasteLabel = "Paste Files";
			else if(GSS.get().getClipboard().getItem().getRestResourceWrapper() != null)
				pasteLabel = "Paste Folder";
		MenuItem cutItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.cut()).getHTML() + "&nbsp;"+cutLabel+"</span>", true, new CutCommand(this));
		cutItem.getElement().setId("topMenu.edit.cut");
		contextMenu.addItem(cutItem).setVisible(cutcopyVisible);
		
		MenuItem copyItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.copy()).getHTML() + "&nbsp;"+copyLabel+"</span>", true, new CopyCommand(this));
		copyItem.getElement().setId("topMenu.edit.copy");
		contextMenu.addItem(copyItem).setVisible(cutcopyVisible);

		MenuItem pasteItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.paste()).getHTML() + "&nbsp;"+pasteLabel+"</span>", true, new PasteCommand(this));
		pasteItem.getElement().setId("topMenu.edit.paste");
		if (GSS.get().getClipboard().getItem() != null)
			if(GSS.get().isUserListVisible() && GSS.get().getClipboard().getItem().getUser() == null){
				contextMenu.addItem(pasteItem);
			}
			else if(!GSS.get().isUserListVisible() && GSS.get().getClipboard().getItem().getUser() != null){
				//do not show paste
			}
			else if (GSS.get().getTreeView().getSelection() instanceof RestResourceWrapper){
				contextMenu.addItem(pasteItem);
			}
		MenuItem moveToTrashItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
		moveToTrashItem.getElement().setId("topMenu.edit.moveToTrash");
		contextMenu	.addItem(moveToTrashItem)
					.setVisible(cutcopyVisible);
		
		MenuItem deleteItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images));
		deleteItem.getElement().setId("topMenu.edit.delete");
		contextMenu	.addItem(deleteItem)
					.setVisible(cutcopyVisible);
		
		MenuItem selectAllItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.selectAll()).getHTML() + "&nbsp;Select All</span>", true, selectAllCommand);
		selectAllItem.getElement().setId("topMenu.edit.selectAll");
		contextMenu.addItem(selectAllItem);
		
		MenuItem unSelectAllItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.unselectAll()).getHTML() + "&nbsp;Unselect All</span>", true, unselectAllCommand);
		unSelectAllItem.getElement().setId("topMenu.edit.unSelectAll");
		contextMenu.addItem(unSelectAllItem);
		return contextMenu;
	}



}
