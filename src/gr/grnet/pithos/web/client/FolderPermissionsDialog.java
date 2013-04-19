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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.*;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.rest.PostRequest;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.Map;

/**
 * The 'Folder properties' dialog box implementation.
 */
public class FolderPermissionsDialog extends DialogBox {

    protected Pithos app;

    /**
     * The widget that holds the folderName of the folder.
     */
    Label folderName = new Label();

    protected PermissionsList permList;

    final Folder folder;

    final VerticalPanel inner;

    /**
     * The widget's constructor.
     */
    public FolderPermissionsDialog(final Pithos app, Folder selected) {
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

        folder = selected;

        setText(Const.TXT_SHARE_FOLDER);

        // Outer contains inner and buttons
        VerticalPanel outer = new VerticalPanel();
        outer.add(close);
        // Inner contains generalPanel and permPanel
        inner = new VerticalPanel();
        inner.addStyleName("inner");

        folderName.setText(folder.getName());

        final HorizontalPanel privateInfoPanel = new HorizontalPanel();
        privateInfoPanel.setSpacing(8);
        final Label privateInfoTitle = new  InlineHTML("<b>Private sharing</b>");
        final Label privateInfoText = new Label("Only people explicitly granted permission can access. Sign-in required.", true);
        privateInfoPanel.add(privateInfoTitle);
        privateInfoPanel.add(privateInfoText);
        inner.add(privateInfoPanel);

        VerticalPanel permPanel = new VerticalPanel();
        FileShareDialog.PrivateSharingImages images = GWT.create(FileShareDialog.PrivateSharingImages.class);

        permList = new PermissionsList(app, images, folder.getPermissions(), folder.getOwnerID(), false, new Command() {
            @Override
            public void execute() {
                updateMetadataForPrivateSharing();
            }
        });

        permPanel.add(permList);

        HorizontalPanel permButtons = new HorizontalPanel();
        Button addUser = new Button("Add User", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PermissionsAddDialog dlg = new PermissionsAddDialog(app, app.getAccount().getGroups(), permList, true);
                dlg.center();
                permList.updatePermissionTable();
            }
        });
        addUser.addStyleName("button");
        permButtons.add(addUser);
        permButtons.setCellHorizontalAlignment(addUser, HasHorizontalAlignment.ALIGN_CENTER);

        final boolean haveGroups = app.getAccount().getGroups().size() > 0;
        if(haveGroups) {
            Button addGroup = new Button("Add Group", new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    PermissionsAddDialog dlg = new PermissionsAddDialog(app, app.getAccount().getGroups(), permList, false);
                    dlg.center();
                    permList.updatePermissionTable();
                }
            });
            addGroup.addStyleName("button");
            permButtons.add(addGroup);
            permButtons.setCellHorizontalAlignment(addGroup, HasHorizontalAlignment.ALIGN_CENTER);
        }

        permButtons.setSpacing(8);
        permPanel.add(permButtons);

        inner.add(permPanel);

        outer.add(inner);

        final Button ok = new Button("OK", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                closeDialog();
            }
        });
        ok.addStyleName("button");

        outer.add(ok);
        outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);

        setWidget(outer);
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
                    updateMetadataForPrivateSharing();
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

    private void updateMetadataForPrivateSharing() {
        final Map<String, Boolean[]> perms = (permList.hasChanges() ? permList.getPermissions() : null);
        updateMetadataForPrivateSharing(
            folder.getUri() + Const.QUESTION_MARK_UPDATE_EQ,
            perms
        );
    }

    private void updateMetadataForPrivateSharing(final String path, final Map<String, Boolean[]> newPermissions) {
        if(newPermissions != null) {
            PostRequest updateFolder = new PostRequest(app.getApiPath(), folder.getOwnerID(), path) {
                @Override
                public void onSuccess(Resource result) {
                    app.updateFolder(folder.getParent(), false, new Command() {
                        @Override
                        public void execute() {
                            app.updateMySharedRoot();
                        }
                    }, true);
                }

                @Override
                public void onError(Throwable t) {
                    app.setError(t);
                    if(t instanceof RestException) {
                        if(((RestException) t).getHttpStatusCode() == Response.SC_NOT_FOUND) { //Probably a virtual folder
                            final String path1 = folder.getUri();
                            PutRequest newFolder = new PutRequest(app.getApiPath(), folder.getOwnerID(), path1) {
                                @Override
                                public void onSuccess(Resource result) {
                                    updateMetadataForPrivateSharing(path, newPermissions);
                                }

                                @Override
                                public void onError(Throwable _t) {
                                    app.setError(_t);
                                    if(_t instanceof RestException) {
                                        app.displayError("Unable to update folder: " + ((RestException) _t).getHttpStatusText());
                                    }
                                    else {
                                        app.displayError("System error modifying folder: " + _t.getMessage());
                                    }
                                }

                                @Override
                                protected void onUnauthorized(Response response) {
                                    app.sessionExpired();
                                }
                            };
                            newFolder.setHeader("X-Auth-Token", app.getUserToken());
                            newFolder.setHeader("Content-Type", "application/folder");
                            newFolder.setHeader("Accept", "*/*");
                            newFolder.setHeader("Content-Length", "0");
                            Scheduler.get().scheduleDeferred(newFolder);
                        }
                        else if(((RestException) t).getHttpStatusCode() == Response.SC_CONFLICT) {
                            app.displayError("Cannot set permissions. Probably subfolders or files already have permissions set");
                        }
                        else {
                            app.displayError("Εrror modifying folder: " + t.getMessage());
                        }
                    }
                    else {
                        app.displayError("System error modifying folder: " + t.getMessage());
                    }
                }

                @Override
                protected void onUnauthorized(Response response) {
                    app.sessionExpired();
                }
            };
            updateFolder.setHeader("X-Auth-Token", app.getUserToken());
            String readPermHeader = "read=";
            String writePermHeader = "write=";
            for(String u : newPermissions.keySet()) {
                Boolean[] p = newPermissions.get(u);
                if(p[0] != null && p[0]) {
                    readPermHeader += u + ",";
                }
                if(p[1] != null && p[1]) {
                    writePermHeader += u + ",";
                }
            }
            if(readPermHeader.endsWith("=")) {
                readPermHeader = "";
            }
            else if(readPermHeader.endsWith(",")) {
                readPermHeader = readPermHeader.substring(0, readPermHeader.length() - 1);
            }
            if(writePermHeader.endsWith("=")) {
                writePermHeader = "";
            }
            else if(writePermHeader.endsWith(",")) {
                writePermHeader = writePermHeader.substring(0, writePermHeader.length() - 1);
            }
            String permHeader = readPermHeader + ((readPermHeader.length() > 0 && writePermHeader.length() > 0) ? ";" : "") + writePermHeader;
            if(permHeader.length() == 0) {
                permHeader = "~";
            }
            else {
                permHeader = URL.encodePathSegment(permHeader);
            }
            updateFolder.setHeader("X-Object-Sharing", permHeader);
            Scheduler.get().scheduleDeferred(updateFolder);
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
