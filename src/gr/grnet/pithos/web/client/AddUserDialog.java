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
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.*;
import gr.grnet.pithos.web.client.catalog.UpdateUserCatalogs;
import gr.grnet.pithos.web.client.catalog.UserCatalogs;
import gr.grnet.pithos.web.client.grouptree.Group;
import gr.grnet.pithos.web.client.rest.PostRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.Arrays;

/**
 * The 'Folder properties' dialog box implementation.
 */
public class AddUserDialog extends DialogBox {

    protected Pithos app;

    Group group;
    /**
     * The widget that holds the folderName of the folder.
     */
    TextBox userNameInput = new TextBox();

    final VerticalPanel inner;

    /**
     * The widget's constructor.
     */
    public AddUserDialog(final Pithos app, Group _group) {
        this.app = app;
        this.group = _group;

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

        // Use this opportunity to set the dialog's caption.
        setText("Add user");

        // Outer contains inner and buttons
        VerticalPanel outer = new VerticalPanel();
        outer.add(close);
        // Inner contains generalPanel and permPanel
        inner = new VerticalPanel();
        inner.addStyleName("inner");

        VerticalPanel generalPanel = new VerticalPanel();
        FlexTable generalTable = new FlexTable();
        generalTable.setText(0, 0, "Username");

        generalTable.setWidget(0, 1, userNameInput);

        generalTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(0, 1, "props-values");
        generalTable.setCellSpacing(4);
        generalPanel.add(generalTable);
        inner.add(generalPanel);

        outer.add(inner);

        // Create the 'Create/Update' button, along with a listener that hides the dialog
        // when the button is clicked and quits the application.
        String okLabel = "Create";
        final Button ok = new Button(okLabel, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addUser();
                closeDialog();
            }
        });
        ok.addStyleName("button");
        outer.add(ok);
        outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);

        setWidget(outer);
    }

    @Override
    public void center() {
        super.center();
        userNameInput.setFocus(true);
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
                    addUser();
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

    private void doAddUserByName(final String userDisplayName) {
        final String userID = app.getIDForUserDisplayName(userDisplayName);
        group.addMemberID(userID);
        String path = "?update=";
        PostRequest updateGroup = new PostRequest(Pithos.getStorageAPIURL(), app.getUserID(), path) {

            @Override
            public void onSuccess(Resource result) {
                app.updateGroupNode(group);
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("", t);
                app.setError(t);
                if(t instanceof RestException) {
                    app.displayError("Unable to update group:" + ((RestException) t).getHttpStatusText());
                }
                else {
                    app.displayError("System error updating group:" + t.getMessage());
                }
            }

            @Override
            protected void onUnauthorized(Response response) {
                app.sessionExpired();
            }
        };
        updateGroup.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());
        String groupMembers = "";
        for(String u : group.getMemberIDs()) {
            groupMembers += (URL.encodePathSegment(u) + ",");
        }
        updateGroup.setHeader(Const.X_ACCOUNT_GROUP_ + URL.encodePathSegment(group.getName()), groupMembers);
        Scheduler.get().scheduleDeferred(updateGroup);
    }
    /**
     * Generate an RPC request to create a new folder.
     */
    void addUser() {
        final String userDisplayName = userNameInput.getText().trim();
        if(userDisplayName.length() == 0) {
            return;
        }
        if(!Const.EMAIL_REGEX.test(userDisplayName)) {
            app.displayWarning("Username must be a valid email address");
            return;
        }

        if(app.hasIDForUserDisplayName(userDisplayName)) {
            doAddUserByName(userDisplayName);
        }
        else {
            new UpdateUserCatalogs(app, null, Arrays.asList(userDisplayName)) {
                @Override
                public void onSuccess(UserCatalogs requestedUserCatalogs, UserCatalogs updatedUserCatalogs) {
                    doAddUserByName(userDisplayName);
                }
            }.scheduleDeferred();
        }


    }
}
