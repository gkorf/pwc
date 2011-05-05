/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.commands.CopyCommand;
import gr.grnet.pithos.web.client.commands.CutCommand;
import gr.grnet.pithos.web.client.commands.DeleteCommand;
import gr.grnet.pithos.web.client.commands.PasteCommand;
import gr.grnet.pithos.web.client.commands.PropertiesCommand;
import gr.grnet.pithos.web.client.commands.RefreshCommand;
import gr.grnet.pithos.web.client.commands.RestoreTrashCommand;
import gr.grnet.pithos.web.client.commands.ToTrashCommand;
import gr.grnet.pithos.web.client.commands.UploadFileCommand;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;
import gr.grnet.pithos.web.client.rest.resource.TrashFolderResource;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'File Context' menu implementation.
 */
public class FileContextMenu extends PopupPanel implements ClickHandler {

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
	public interface Images extends ClientBundle,FileMenu.Images, EditMenu.Images {

		@Source("gr/grnet/pithos/resources/mimetypes/document.png")
		ImageResource fileContextMenu();

		@Source("gr/grnet/pithos/resources/doc_versions.png")
		ImageResource versions();

		@Override
		@Source("gr/grnet/pithos/resources/group.png")
		ImageResource sharing();

		@Override
		@Source("gr/grnet/pithos/resources/border_remove.png")
		ImageResource unselectAll();

		@Source("gr/grnet/pithos/resources/demo.png")
		ImageResource viewImage();
}

	public static native String getDate()/*-{
		return (new Date()).toUTCString();
	}-*/;

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public FileContextMenu(Images newImages, boolean isTrash, boolean isEmpty) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		GSS gss = GSS.get();
		setAnimationEnabled(true);
		images = newImages;

		// The command that does some validation before downloading a file.
		Command downloadCmd = new Command() {

			@Override
			public void execute() {
				hide();
				GSS.get().getTopPanel().getFileMenu().preDownloadCheck();
			}
		};

		pasteItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.paste()).getHTML() + "&nbsp;Paste</span>", true, new PasteCommand(this));
		pasteItem.getElement().setId("FileContextMenu.paste");
		RestResource sel = GSS.get().getTreeView().getSelection();
		MenuBar contextMenu = new MenuBar(true);
		if (isEmpty) {
			contextMenu.addItem(pasteItem);
			if (sel != null)
				/*TODO:CELLTREE
				if (GSS.get().getTreeView().isFileItem(GSS.get().getTreeView().getCurrent()))
					contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
				else if (GSS.get().getTreeView().isMySharedItem(GSS.get().getTreeView().getCurrent()) || GSS	.get()
																											.getTreeView()
																											.isOthersSharedItem(GSS	.get()
																																	.getTreeView()
																																	.getCurrent()))
					if(sel instanceof FolderResource)
						contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
			*/
			if(sel instanceof RestResourceWrapper && !(sel instanceof TrashFolderResource)){
				MenuItem upload = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
				upload.getElement().setId("fileContextMenu.upload");
				contextMenu.addItem(upload);
			}
			MenuItem refresh = new MenuItem("<span>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
			refresh.getElement().setId("fileContextMenu.refresh");
			contextMenu.addItem(refresh);
			
		} else if (isTrash) {
			MenuItem restore = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.versions()).getHTML() + "&nbsp;Restore</span>", true, new RestoreTrashCommand(this));
			restore.getElement().setId("fileContextMenu.restore");
			contextMenu.addItem(restore);
			
			MenuItem delete = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images));
			delete.getElement().setId("fileContextMenu.delete");
			contextMenu.addItem(delete);
		} else {
			final Command unselectAllCommand = new Command() {

				@Override
				public void execute() {
					hide();
					if(GSS.get().isFileListShowing())
						GSS.get().getFileList().clearSelectedRows();
					else if(GSS.get().isSearchResultsShowing())
						GSS.get().getSearchResults().clearSelectedRows();
				}
			};
			cutItem = new MenuItem("<span id='fileContextMenu.cut'>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Cut</span>", true, new CutCommand(this));
			cutItem.getElement().setId("fileContextMenu.cut");
			
			copyItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new CopyCommand(this));
			copyItem.getElement().setId("fileContextMenu.copy");
			
			updateItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.fileUpdate()).getHTML() + "&nbsp;Upload</span>", true, new UploadFileCommand(this));
			updateItem.getElement().setId("fileContextMenu.upload");

			trashItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.emptyTrash()).getHTML() + "&nbsp;Move to Trash</span>", true, new ToTrashCommand(this));
			trashItem.getElement().setId("fileContextMenu.moveToTrash");
			
			deleteItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteCommand(this, images));
			deleteItem.getElement().setId("fileContextMenu.delete");

			sharingItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.sharing()).getHTML() + "&nbsp;Sharing</span>", true, new PropertiesCommand(this, images, 1));
			sharingItem.getElement().setId("fileContextMenu.sharing");
			
			propItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.viewText()).getHTML() + "&nbsp;Properties</span>", true, new PropertiesCommand(this, images, 0));
			propItem.getElement().setId("fileContextMenu.properties");

			
			if(sel!=null && sel instanceof FolderResource)
				contextMenu.addItem(updateItem);
			String[] link = {"", ""};
			gss.getTopPanel().getFileMenu().createDownloadLink(link, false);
			downloadItem = new MenuItem("<span>" + link[0] + AbstractImagePrototype.create(newImages.download()).getHTML() + " Download" + link[1] + "</span>", true, downloadCmd);
			downloadItem.getElement().setId("fileContextMenu.download");
			contextMenu.addItem(downloadItem);
			
			gss.getTopPanel().getFileMenu().createDownloadLink(link, true);
			saveAsItem = new MenuItem("<span>" + link[0] + AbstractImagePrototype.create(newImages.download()).getHTML() + " Save As" + link[1] + "</span>", true, downloadCmd);
			saveAsItem.getElement().setId("fileContextMenu.saveAs");
			contextMenu.addItem(saveAsItem);
			contextMenu.addItem(cutItem);
			contextMenu.addItem(copyItem);
			if(sel!=null && sel instanceof FolderResource)
				contextMenu.addItem(pasteItem);
			MenuItem unSelect = new MenuItem("<span>" + AbstractImagePrototype.create(images.unselectAll()).getHTML() + "&nbsp;Unselect</span>", true, unselectAllCommand);
			unSelect.getElement().setId("fileContextMenu.unSelect");
			contextMenu.addItem(unSelect);
			
			contextMenu.addItem(trashItem);
			contextMenu.addItem(deleteItem);
			
			MenuItem refresh = new MenuItem("<span id='fileContextMenu.refresh'>" + AbstractImagePrototype.create(images.refresh()).getHTML() + "&nbsp;Refresh</span>", true, new RefreshCommand(this, images));
			refresh.getElement().setId("fileContextMenu.refresh");
			contextMenu.addItem(refresh);
			
			contextMenu.addItem(sharingItem);
			contextMenu.addItem(propItem);
		}
		add(contextMenu);
		if (gss.getClipboard().hasFileItem())
			pasteItem.setVisible(true);
		else
			pasteItem.setVisible(false);
	}

	void onMultipleSelection() {
		updateItem.setVisible(false);
		downloadItem.setVisible(false);
		saveAsItem.setVisible(false);
		sharingItem.setVisible(false);
	}
	@Override
	public void onClick(ClickEvent event) {
		if (GSS.get().getCurrentSelection() != null)
			if (GSS.get().getCurrentSelection() instanceof FileResource) {
				FileResource res = (FileResource) GSS.get().getCurrentSelection();
				FileContextMenu menu;
				if (res.isDeleted())
					menu = new FileContextMenu(images, true, false);
				else
					menu = new FileContextMenu(images, false, false);
				int left = event.getRelativeElement().getAbsoluteLeft();
				int top = event.getRelativeElement().getAbsoluteTop() + event.getRelativeElement().getOffsetHeight();
				menu.setPopupPosition(left, top);
				menu.show();
			} else if (GSS.get().getCurrentSelection() instanceof List) {
				FileContextMenu menu;
				/*TODO: CELLTREE
				if (GSS.get().getTreeView().isTrashItem(GSS.get().getTreeView().getCurrent()))
					menu = new FileContextMenu(images, true, false);
				else {
					menu = new FileContextMenu(images, false, false);
					menu.onMultipleSelection();
				}
				*/
				menu = new FileContextMenu(images, false, false);
				menu.onMultipleSelection();
				int left = event.getRelativeElement().getAbsoluteLeft();
				int top = event.getRelativeElement().getAbsoluteTop() + event.getRelativeElement().getOffsetHeight();
				menu.setPopupPosition(left, top);
				menu.show();
			}
	}

	
	public void onContextEvent(ContextMenuEvent event) {
		if (GSS.get().getCurrentSelection() != null)
			if (GSS.get().getCurrentSelection() instanceof FileResource) {
				FileResource res = (FileResource) GSS.get().getCurrentSelection();
				FileContextMenu menu;
				if (res.isDeleted())
					menu = new FileContextMenu(images, true, false);
				else
					menu = new FileContextMenu(images, false, false);
				int left = event.getNativeEvent().getClientX();
				int top = event.getNativeEvent().getClientY();
				menu.setPopupPosition(left, top);
				menu.show();

			} else if (GSS.get().getCurrentSelection() instanceof List) {
				FileContextMenu menu;
				/*TODO: CELLTREE
				if (GSS.get().getTreeView().isTrashItem(GSS.get().getTreeView().getCurrent()))
					menu = new FileContextMenu(images, true, false);
				else {
					menu = new FileContextMenu(images, false, false);
					menu.onMultipleSelection();
				}
				*/
				int left = event.getNativeEvent().getClientX();
				int top = event.getNativeEvent().getClientY();
				//menu.setPopupPosition(left, top);
				//menu.show();
			}
	}

	public FileContextMenu onEvent(Event event) {
		FileContextMenu menu=null;
		if (GSS.get().getCurrentSelection() != null)
			if (GSS.get().getCurrentSelection() instanceof FileResource) {
				FileResource res = (FileResource) GSS.get().getCurrentSelection();

				if (res.isDeleted())
					menu = new FileContextMenu(images, true, false);
				else
					menu = new FileContextMenu(images, false, false);
				int left = event.getClientX();
				int top = event.getClientY();
				menu.setPopupPosition(left, top);
				menu.show();
			} else if (GSS.get().getCurrentSelection() instanceof List) {
				/*TODO: CELLTREE
				if (GSS.get().getTreeView().isTrashItem(GSS.get().getTreeView().getSelection()))
					menu = new FileContextMenu(images, true, false);
				else {
					menu = new FileContextMenu(images, false, false);
					menu.onMultipleSelection();
				}*/
				menu = new FileContextMenu(images, false, false);
				menu.onMultipleSelection();
				int left = event.getClientX();
				int top = event.getClientY();
				menu.setPopupPosition(left, top);
				menu.show();
			}
		return menu;
	}

	public FileContextMenu onEmptyEvent(Event event) {
		FileContextMenu menu=null;
		/*TODO: CELLTREE
		if (GSS.get().getTreeView().isTrashItem(GSS.get().getTreeView().getCurrent()))
			menu = new FileContextMenu(images, true, true);
		else if(((DnDTreeItem)GSS.get().getTreeView().getCurrent()).getFolderResource() != null)
			menu = new FileContextMenu(images, false, true);
		else return menu;
		*/
		menu = new FileContextMenu(images, false, true);
		int left = event.getClientX();
		int top = event.getClientY();
		menu.setPopupPosition(left, top);
		menu.show();
		return menu;
	}


}
