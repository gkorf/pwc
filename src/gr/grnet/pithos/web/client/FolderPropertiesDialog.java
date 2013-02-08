/*
 * Copyright 2011-2012 GRNET S.A. All rights reserved.
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.*;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;

/**
 * The 'Folder properties' dialog box implementation.
 */
public class FolderPropertiesDialog extends DialogBox {

    protected Pithos app;

    /**
     * The widget that holds the folderName of the folder.
     */
    TextBox folderName = new TextBox();

    /**
     * A flag that denotes whether the dialog will be used to create or modify a
     * folder.
     */
    private final boolean create;

    final Folder folder;

    final VerticalPanel inner;

    /**
     * The widget's constructor.
     */
    public FolderPropertiesDialog(final Pithos app, boolean _create, Folder selected) {
        this.app = app;
        Anchor close = new Anchor("close");
        close.addStyleName("close");
        close.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        setGlassEnabled(true);
        setStyleName("pithos-DialogBox");

        // Enable IE selection for the dialog (must disable it upon closing it)
        Pithos.enableIESelection();

        create = _create;

        folder = selected;

        // Use this opportunity to set the dialog's caption.
        if(create) {
            setText("Create folder");
        }
        else {
            setText("Folder properties");
        }

        // Outer contains inner and buttons
        VerticalPanel outer = new VerticalPanel();
        outer.add(close);
        // Inner contains generalPanel and permPanel
        inner = new VerticalPanel();
        inner.addStyleName("inner");

        VerticalPanel generalPanel = new VerticalPanel();
        FlexTable generalTable = new FlexTable();
        generalTable.setText(0, 0, "Name");
        generalTable.setText(1, 0, "Parent");
        generalTable.setText(2, 0, "Creator");
        generalTable.setText(3, 0, "Last modified");

        folderName.setText(create ? "" : folder.getName());
        folderName.setReadOnly(folder.isContainer() && !create);
        generalTable.setWidget(0, 1, folderName);

        final Label folderNameNote = new Label("Please note that slashes ('/') are not allowed in folder names.", true);
        folderNameNote.setVisible(false);
        folderNameNote.setStylePrimaryName("gss-readForAllNote");
        generalTable.setWidget(0, 2, folderNameNote);

        if(create) {
            generalTable.setText(1, 1, folder.getName());
        }
        else {
            generalTable.setText(1, 1, folder.getParent().getName());
        }
        if(create) {
            generalTable.setText(2, 1, app.getCurrentUserDisplayNameOrID());
        }
        else {
            final String ownerID = folder.getOwnerID();
            final String displayName = app.getDisplayNameForUserID(ownerID);
            final String ownerDisplayName;
            if(displayName == null) {
                // FIXME: Get the actual display name and do not use the id
                ownerDisplayName = ownerID;
            }
            else {
                ownerDisplayName = displayName;
            }
            generalTable.setText(2, 1, ownerDisplayName);
        }
        DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
        if(folder.getLastModified() != null) {
            generalTable.setText(3, 1, formatter.format(folder.getLastModified()));
        }
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
        inner.add(generalPanel);

        outer.add(inner);

        // Create the 'Create/Update' button, along with a listener that hides the dialog
        // when the button is clicked and quits the application.
        String okLabel;
        if(create) {
            okLabel = "Create";
        }
        else {
            okLabel = "Update";
        }
        final Button ok = new Button(okLabel, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createOrUpdateFolder();
                closeDialog();
            }
        });
        ok.addStyleName("button");
        outer.add(ok);
        outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);

        folderName.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                if(folderName.getText().contains("/")) {
                    folderNameNote.setVisible(true);
                    ok.setEnabled(false);
                }
                else {
                    folderNameNote.setVisible(false);
                    ok.setEnabled(true);
                }
            }
        });

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
        if(evt.getType().equals(KeyDownEvent.getType().getName()))
        // Use the popup's key preview hooks to close the dialog when either
        // enter or escape is pressed.
        {
            switch(evt.getKeyCode()) {
                case KeyCodes.KEY_ENTER:
                    createOrUpdateFolder();
                    closeDialog();
                    break;
                case KeyCodes.KEY_ESCAPE:
                    closeDialog();
                    break;
            }
        }
    }


    /**
     * Enables IE selection prevention and hides the dialog
     * (we disable the prevention on creation of the dialog)
     */
    public void closeDialog() {
        Pithos.preventIESelection();
        hide();
    }

    /**
     * Generate an RPC request to create a new folder.
     */
    private void createFolder() {
        String name = folderName.getText().trim();
        if(name.length() == 0) {
            return;
        }
        String path = folder.getUri() + "/" + name;
        PutRequest createFolder = new PutRequest(app.getApiPath(), folder.getOwnerID(), path) {
            @Override
            public void onSuccess(Resource result) {
                app.updateFolder(folder, true, new Command() {

                    @Override
                    public void execute() {
                        app.updateStatistics();
                    }
                }, true);
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("", t);
                app.setError(t);
                if(t instanceof RestException) {
                    app.displayError("Unable to create folder:" + ((RestException) t).getHttpStatusText());
                }
                else {
                    app.displayError("System error creating folder:" + t.getMessage());
                }
            }

            @Override
            protected void onUnauthorized(Response response) {
                app.sessionExpired();
            }
        };
        createFolder.setHeader("X-Auth-Token", app.getUserToken());
        createFolder.setHeader("Accept", "*/*");
        createFolder.setHeader("Content-Length", "0");
        createFolder.setHeader("Content-Type", "application/directory");
        Scheduler.get().scheduleDeferred(createFolder);
    }

    /**
     * Upon closing the dialog by clicking OK or pressing ENTER this method does
     * the actual work of modifying folder properties or creating a new Folder
     * depending on the value of the create field
     */
    protected void createOrUpdateFolder() {
        if(create) {
            createFolder();
        }
        else {
            updateFolder();
        }

    }

    private void updateFolder() {
        final String newName = folderName.getText().trim();
        if(newName.length() == 0) {
            return;
        }
        if(!folder.isContainer() && !folder.getName().equals(newName)) {
            final String path = folder.getParent().getUri() + "/" + newName;
            app.copyFolder(folder, folder.getOwnerID(), path, true, new Command() {

                @Override
                public void execute() {
                    app.updateFolder(folder.getParent(), false, new Command() {

                        @Override
                        public void execute() {
                            app.updateMySharedRoot();
                        }
                    }, true);
                }
            });
        }
        else {
            app.updateFolder(folder.getParent(), false, new Command() {

                @Override
                public void execute() {
                    app.updateMySharedRoot();
                }
            }, true);
        }
    }
}
