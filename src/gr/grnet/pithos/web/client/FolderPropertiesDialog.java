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
import com.google.gwt.user.client.Command;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.PostRequest;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.Iterator;
import java.util.Map;

/**
 * The 'Folder properties' dialog box implementation.
 */
public class FolderPropertiesDialog extends DialogBox {

    private Pithos app;

	/**
	 * The widget that holds the folderName of the folder.
	 */
	private TextBox folderName = new TextBox();

	/**
	 * A flag that denotes whether the dialog will be used to create or modify a
	 * folder.
	 */
	private final boolean create;

    private PermissionsList permList;

	final Folder folder;

	final TabPanel inner;

	/**
	 * The widget's constructor.
	 */
	public FolderPropertiesDialog(final Pithos app, boolean _create,  Folder selected) {
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
        FlexTable generalTable = new FlexTable();
        generalTable.setText(0, 0, "Name");
        generalTable.setText(1, 0, "Parent");
        generalTable.setText(2, 0, "Creator");
        generalTable.setText(3, 0, "Last modified");
        folderName.setText(create ? "" : folder.getName());
        folderName.setReadOnly(folder.isContainer() && !create);
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
        generalPanel.add(generalTable);
        inner.add(generalPanel, "General");

        VerticalPanel permPanel = new VerticalPanel();
        FilePropertiesDialog.Images images = GWT.create(FilePropertiesDialog.Images.class);
        boolean permsReadonly = folder.getInheritedPermissionsFrom() != null || folder.existChildrenPermissions();
        permList = new PermissionsList(images, folder.getPermissions(), folder.getOwner(), permsReadonly);
        permPanel.add(permList);

        if (!permsReadonly) {
            HorizontalPanel permButtons = new HorizontalPanel();
            Button add = new Button("Add Group", new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    PermissionsAddDialog dlg = new PermissionsAddDialog(app, app.getAccount().getGroups(), permList, false);
                    dlg.center();
                }
            });
            permButtons.add(add);
            permButtons.setCellHorizontalAlignment(add, HasHorizontalAlignment.ALIGN_CENTER);

            Button addUser = new Button("Add User", new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    PermissionsAddDialog dlg = new PermissionsAddDialog(app, app.getAccount().getGroups(), permList, true);
                    dlg.center();
                }
            });
            addUser.getElement().setId("folderPropertiesDialog.button.addUser");
            permButtons.add(addUser);
            permButtons.setCellHorizontalAlignment(addUser, HasHorizontalAlignment.ALIGN_CENTER);
            permButtons.setSpacing(8);
            permButtons.addStyleName("gss-TabPanelBottom");
            permPanel.add(permButtons);
        }

        if (!create)
            inner.add(permPanel, "Sharing");
        inner.selectTab(0);

        outer.add(inner);

        HorizontalPanel buttons = new HorizontalPanel();
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
        outer.add(buttons);
        outer.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
        outer.addStyleName("pithos-TabPanelBottom");

        setWidget(outer);
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
					createOrUpdateFolder();
                    closeDialog();
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
        String path = folder.getUri() + "/" + name;
        PutRequest createFolder = new PutRequest(app.getApiPath(), app.getUsername(), path) {
            @Override
            public void onSuccess(Resource result) {
                app.updateFolder(folder, true);
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
        final Map<String, Boolean[]> perms = (permList.hasChanges() ? permList.getPermissions() : null);
        final String newName = folderName.getText().trim();
        if (!folder.isContainer() && !folder.getName().equals(newName)) {
            final String path = folder.getParent().getUri() + "/" + newName;
            PutRequest newFolder = new PutRequest(app.getApiPath(), app.getUsername(), path) {
                @Override
                public void onSuccess(Resource result) {
                    Iterator<File> iter = folder.getFiles().iterator();
                    app.copyFiles(iter, folder.getParent().getUri() + "/" + newName, new Command() {
                        @Override
                        public void execute() {
                            Iterator<Folder> iterf = folder.getSubfolders().iterator();
                            app.copySubfolders(iterf, folder.getParent().getUri() + "/" + newName, new Command() {
                                @Override
                                public void execute() {
                                    app.deleteFolder(folder);
                                    updateMetadata(path + "?update=", perms);
                                }
                            });
                        }
                    });
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
                    if(t instanceof RestException){
                        app.displayError("Unable to update folder: " + ((RestException) t).getHttpStatusText());
                    }
                    else
                        app.displayError("System error modifying folder: " + t.getMessage());
                }
            };
            newFolder.setHeader("X-Auth-Token", app.getToken());
            newFolder.setHeader("Content-Type", "application/folder");
            newFolder.setHeader("Accept", "*/*");
            newFolder.setHeader("Content-Length", "0");
            Scheduler.get().scheduleDeferred(newFolder);
        }
        else
            updateMetadata(folder.getUri() + "?update=", perms);
	}

    private void updateMetadata(String path, Map<String, Boolean[]> newPermissions) {
        if (newPermissions != null) {
            PostRequest updateFolder = new PostRequest(app.getApiPath(), folder.getOwner(), path) {
                @Override
                public void onSuccess(Resource result) {
                    app.updateFolder(folder.getParent(), false);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
                    app.displayError("System error modifying folder: " + t.getMessage());
                }
            };
            updateFolder.setHeader("X-Auth-Token", app.getToken());
            if (newPermissions != null) {
                String readPermHeader = "read=";
                String writePermHeader = "write=";
                for (String u : newPermissions.keySet()) {
                    Boolean[] p = newPermissions.get(u);
                    if (p[0] != null && p[0])
                        readPermHeader += u + ",";
                    if (p[1] != null && p[1])
                        writePermHeader += u + ",";
                }
                if (readPermHeader.endsWith("="))
                    readPermHeader = "";
                else if (readPermHeader.endsWith(","))
                    readPermHeader = readPermHeader.substring(0, readPermHeader.length() - 1);
                if (writePermHeader.endsWith("="))
                    writePermHeader = "";
                else if (writePermHeader.endsWith(","))
                    writePermHeader = writePermHeader.substring(0, writePermHeader.length() - 1);
                String permHeader = readPermHeader +  ((readPermHeader.length()  > 0 && writePermHeader.length() > 0) ?  ";" : "") + writePermHeader;
                if (permHeader.length() == 0)
                    permHeader="~";
                updateFolder.setHeader("X-Object-Sharing", permHeader);
            }
            Scheduler.get().scheduleDeferred(updateFolder);
        }
        else
            app.updateFolder(folder.getParent(), false);
    }

	public void selectTab(int _tab) {
		inner.selectTab(_tab);
	}
}
