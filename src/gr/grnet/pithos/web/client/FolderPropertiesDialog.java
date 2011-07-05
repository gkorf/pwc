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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.Event;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.PostCommand;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'Folder properties' dialog box implementation.
 */
public class FolderPropertiesDialog extends DialogBox {

    private GSS app;

	private CheckBox readForAll;

	/**
	 * The widget that holds the folderName of the folder.
	 */
	private TextBox folderName = new TextBox();

	/**
	 * A flag that denotes whether the dialog will be used to create or modify a
	 * folder.
	 */
	private final boolean create;

	final Folder folder;

	final TabPanel inner;

	/**
	 * The widget's constructor.
	 */
	public FolderPropertiesDialog(GSS app, boolean _create,  Folder selected) {
        this.app = app;
		setAnimationEnabled(true);

		// Enable IE selection for the dialog (must disable it upon closing it)
		app.enableIESelection();

		create = _create;
		
		folder = selected;

		// Use this opportunity to set the dialog's caption.
		if (create)
			setText("Create folder");
		else
			setText("Folder properties");

		// Outer contains inner and buttons
		VerticalPanel outer = new VerticalPanel();
		// Inner contains generalPanel and permPanel
		inner = new DecoratedTabPanel();
		inner.setAnimationEnabled(true);
		VerticalPanel generalPanel = new VerticalPanel();
		VerticalPanel permPanel = new VerticalPanel();
		final HorizontalPanel permForAll = new HorizontalPanel();
		final HorizontalPanel pathPanel = new HorizontalPanel();
		HorizontalPanel buttons = new HorizontalPanel();
		HorizontalPanel permButtons = new HorizontalPanel();

		inner.add(generalPanel, "General");
		if (!create)
			inner.add(permPanel, "Sharing");
		inner.selectTab(0);

		FlexTable generalTable = new FlexTable();
		generalTable.setText(0, 0, "Name");
		generalTable.setText(1, 0, "Parent");
		generalTable.setText(2, 0, "Creator");
		generalTable.setText(3, 0, "Last modified");
		folderName.setText(create ? "" : folder.getName());
		generalTable.setWidget(0, 1, folderName);

		if (create)
			generalTable.setText(1, 1, folder.getName());
		else
			generalTable.setText(1, 1, folder.getPrefix());
		generalTable.setText(2, 1, "");
		DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
		if(folder.getLastModified() != null)
			generalTable.setText(3, 1, formatter.format(folder.getLastModified()));
		generalTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(2, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(3, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(2, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(3, 1, "props-values");
		generalTable.setCellSpacing(4);

		// Create the 'Create/Update' button, along with a listener that hides the dialog
		// when the button is clicked and quits the application.
		String okLabel;
		if (create)
			okLabel = "Create";
		else
			okLabel = "Update";
		Button ok = new Button(okLabel, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				createOrUpdateFolder();
				closeDialog();
			}
		});
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog
		// when the button is clicked.
		Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				closeDialog();
			}
		});
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("pithos-TabPanelBottom");

		generalPanel.add(generalTable);


		outer.add(inner);
		outer.add(buttons);
		outer.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		outer.addStyleName("pithos-TabPanelBottom");

		setWidget(outer);

		/*if (create)
			folderName.setFocus(true);
		else
			ok.setFocus(true);*/
	}

	@Override
	public void center() {
		super.center();
		folderName.setFocus(true);
	}

	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
		super.onPreviewNativeEvent(preview);

		NativeEvent evt = preview.getNativeEvent();
		if (evt.getType().equals(KeyDownEvent.getType().getName()))
			// Use the popup's key preview hooks to close the dialog when either
			// enter or escape is pressed.
			switch (evt.getKeyCode()) {
				case KeyCodes.KEY_ENTER:
					closeDialog();
					createOrUpdateFolder();
					break;
				case KeyCodes.KEY_ESCAPE:
					closeDialog();
					break;
			}
	}


	/**
	 * Enables IE selection prevention and hides the dialog
	 * (we disable the prevention on creation of the dialog)
	 */
	public void closeDialog() {
		app.preventIESelection();
		hide();
	}

	/**
	 * Generate an RPC request to create a new folder.
	 */
	private void createFolder() {
		String name = folderName.getText();
        String prefix = folder.getPrefix();
        String path = app.getApiPath() + app.getUsername() + "/" + folder.getContainer() + "/" + (prefix.length() == 0 ? "" : prefix +  "/") + name;
        PutRequest createFolder = new PutRequest(path) {
            @Override
            public void onSuccess(Resource result) {
                app.updateFolder(folder);
            }

            @Override
            public void onError(Throwable t) {
				GWT.log("", t);
				if (t instanceof RestException) {
					app.displayError("Unable to create folder:" + ((RestException) t).getHttpStatusText());
				}
				else
					app.displayError("System error creating folder:" + t.getMessage());
            }
        };
        createFolder.setHeader("X-Auth-Token", app.getToken());
        createFolder.setHeader("Accept", "*/*");
        createFolder.setHeader("Content-Length", "0");
        createFolder.setHeader("Content-Type", "application/folder");
        Scheduler.get().scheduleDeferred(createFolder);
	}

	/**
	 * Upon closing the dialog by clicking OK or pressing ENTER this method does
	 * the actual work of modifying folder properties or creating a new Folder
	 * depending on the value of the create field
	 */
	private void createOrUpdateFolder() {
		if (create)
			createFolder();
		else
			updateFolder();

	}

	private void updateFolder() {
//		permList.updatePermissionsAccordingToInput();
//		Set<PermissionHolder> perms = permList.getPermissions();
//		JSONObject json = new JSONObject();
//		if(!folder.getName().equals(folderName.getText()))
//			json.put("name", new JSONString(folderName.getText()));
//		//only update the read for all perm if the user is the owner
//		if (readForAll.getValue() != folder.isReadForAll())
//			if (folder.getOwner().equals(app.getCurrentUserResource().getUsername()))
//				json.put("readForAll", JSONBoolean.getInstance(readForAll.getValue()));
//		if (permList.hasChanges()) {
//			JSONArray perma = new JSONArray();
//			int i=0;
//			for(PermissionHolder p : perms){
//				JSONObject po = new JSONObject();
//				if(p.getUser() != null)
//					po.put("user", new JSONString(p.getUser()));
//				if(p.getGroup() != null)
//					po.put("group", new JSONString(p.getGroup()));
//				po.put("read", JSONBoolean.getInstance(p.isRead()));
//				po.put("write", JSONBoolean.getInstance(p.isWrite()));
//				po.put("modifyACL", JSONBoolean.getInstance(p.isModifyACL()));
//				perma.set(i,po);
//				i++;
//			}
//			json.put("permissions", perma);
//			GWT.log(json.toString(), null);
//		}
//		PostCommand ep = new PostCommand(folder.getUri()+"?update=", json.toString(), 200){
//
//			@Override
//			public void onComplete() {
//				//TODO:CELLTREE
//
//				if(getPostBody() != null && !"".equals(getPostBody().trim())){
//
//
//					FolderResource fres = ((RestResourceWrapper) app.getTreeView().getSelection()).getResource();
//					String initialPath = fres.getUri();
//					String newPath =  getPostBody().trim();
//					fres.setUri(newPath);
//					((RestResourceWrapper) app.getTreeView().getSelection()).getResource().setUri(newPath);
//					((RestResourceWrapper) app.getTreeView().getSelection()).setUri(newPath);
//					app.getTreeView().updateNodeChildren(fres.getParentURI());
//					if (permList.hasChanges()) {
//						app.getTreeView().updateMySharedNode();
//					}
//					/*
//					if(folderItem.getParentItem() != null && ((DnDTreeItem)folderItem.getParentItem()).getFolderResource() != null){
//						((DnDTreeItem)folderItem.getParentItem()).getFolderResource().removeSubfolderPath(initialPath);
//						((DnDTreeItem)folderItem.getParentItem()).getFolderResource().getSubfolderPaths().add(newPath);
//					}*/
//				}
//				//app.getFolders().updateFolder( (DnDTreeItem) app.getFolders().getCurrent());
//
//				app.get().showFileList(true);
//			}
//
//			@Override
//			public void onError(Throwable t) {
//				GWT.log("", t);
//				if(t instanceof RestException){
//					int statusCode = ((RestException)t).getHttpStatusCode();
//					if(statusCode == 405)
//						app.displayError("You don't have the necessary permissions or" +
//								" a folder with same name already exists");
//					else if(statusCode == 404)
//						app.displayError("Resource not found, or user specified in sharing does not exist");
//					else
//						app.displayError("Unable to update folder: "+((RestException)t).getHttpStatusText());
//				}
//				else
//					app.displayError("System error moifying file: "+t.getMessage());
//				//TODO:CELLTREE
//				//app.getFolders().updateFolder( (DnDTreeItem) app.getFolders().getCurrent());
//			}
//		};
//		DeferredCommand.addCommand(ep);
	}

	public void selectTab(int _tab) {
		inner.selectTab(_tab);
	}
}
