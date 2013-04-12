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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import gr.grnet.pithos.web.client.catalog.UpdateUserCatalogs;
import gr.grnet.pithos.web.client.catalog.UserCatalogs;


public class PermissionsList extends Composite {

	Map<String, Boolean[]> permissions = null;
	
	final FileShareDialog.PrivateSharingImages images;
	
	final VerticalPanel permPanel = new VerticalPanel();
	
	final FlexTable permTable = new FlexTable();
	
	final String owner;
	
	protected boolean hasChanges = false;

    private boolean readonly = false;
    
    Command changePermissionsCallback;

    private final Pithos app;
	
	public PermissionsList(Pithos app, final FileShareDialog.PrivateSharingImages theImages, Map<String, Boolean[]> thePermissions, String theOwner, boolean inheritsPermissions, Command _changePermissionsCallback){
        this.app = app;
		changePermissionsCallback = _changePermissionsCallback;
		images = theImages;
		owner = theOwner;
		permissions =  new HashMap<String, Boolean[]>(thePermissions);
		permTable.setText(0, 0, "Users/Groups");
		permTable.setText(0, 1, "Read Only");
		permTable.setText(0, 2, "Read/Write");
		permTable.setText(0, 3, "");
		permTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 1, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 2, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 3, "props-toplabels");
		permPanel.add(permTable);
		permPanel.addStyleName("pithos-TabPanelBottom");
		initWidget(permPanel);
		updatePermissionTable();
	}

	public boolean hasChanges(){
		return hasChanges;
	}

	/**
	 * Retrieve the permissions.
	 *
	 * @return the permissions
	 */
	public Map<String, Boolean[]> getPermissions() {
		return permissions;
	}

	public void addPermission(String userID, boolean read, boolean write){
		permissions.put(userID, new Boolean[] {Boolean.valueOf(read), Boolean.valueOf(write)});
		hasChanges = true;
        updatePermissionTable();
        if (changePermissionsCallback != null)
        	changePermissionsCallback.execute();
	}

	/**
	 * Shows the permission table 
	 * 
	 */
	void updatePermissionTable(){
		int i = 1;
        final int ii = i;
        for (int j=1; j<permTable.getRowCount(); j++)
            permTable.removeRow(j);
		for(final String userID : permissions.keySet()) {
            if (!userID.contains(":")) {
                 //not a group
                final String displayName = app.getDisplayNameForUserID(userID);
                if(displayName != null) {
                    permTable.setHTML(
                        i,
                        0,
                        "<span>" + AbstractImagePrototype.create(images.permUser()).getHTML() + "&nbsp;" + displayName + "</span>"
                    );
                }
                else {
                    new UpdateUserCatalogs(app, userID) {
                        @Override
                        public void onSuccess(UserCatalogs requestedUserCatalogs, UserCatalogs updatedUserCatalogs) {
                            final String displayName = updatedUserCatalogs.getDisplayName(userID);
                            permTable.setHTML(
                                ii,
                                0,
                                "<span>" + AbstractImagePrototype.create(images.permUser()).getHTML() + "&nbsp;" + displayName + "</span>"
                            );
                        }
                    }.scheduleDeferred();
                }
            }
            else {
                permTable.setHTML(i, 0, "<span>" + AbstractImagePrototype.create(images.permGroup()).getHTML() + "&nbsp;" + userID.split(":")[1].trim() + "</span>");
            }
            permTable.getFlexCellFormatter().setStyleName(i, 0, "props-values");

            Boolean[] userPerms = permissions.get(userID);
            Boolean readP = userPerms[0];
            Boolean writeP = userPerms[1];

			RadioButton read = new RadioButton("permissions" + i);
			read.setValue(readP != null ? readP : false);
            permTable.setWidget(i, 1, read);
            permTable.getFlexCellFormatter().setHorizontalAlignment(i, 1, HasHorizontalAlignment.ALIGN_CENTER);

            RadioButton write = new RadioButton("permissions" + i);
            write.setValue(writeP != null ? writeP : false);
            permTable.setWidget(i, 2, write);
            permTable.getFlexCellFormatter().setHorizontalAlignment(i, 2, HasHorizontalAlignment.ALIGN_CENTER);

            if (!readonly) {
                read.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                        Boolean[] ps = permissions.get(userID);
                        ps[0] = booleanValueChangeEvent.getValue();
                        ps[1] = !booleanValueChangeEvent.getValue();
                        hasChanges = true;
                        if (changePermissionsCallback != null)
                        	changePermissionsCallback.execute();
                    }
                });
                write.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                        Boolean[] ps = permissions.get(userID);
                        ps[0] = !booleanValueChangeEvent.getValue();
                        ps[1] = booleanValueChangeEvent.getValue();
                        hasChanges = true;
                        if (changePermissionsCallback != null)
                        	changePermissionsCallback.execute();
                    }
                });
                Anchor removeButton = new Anchor("remove");
                removeButton.addStyleName(Pithos.resources.pithosCss().commandAnchor());
                removeButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        permissions.remove(userID);
                        updatePermissionTable();
                        hasChanges = true;
                        if (changePermissionsCallback != null)
                        	changePermissionsCallback.execute();
                    }
                });
                permTable.setWidget(i, 3, removeButton);
                permTable.getFlexCellFormatter().setHorizontalAlignment(i, 3, HasHorizontalAlignment.ALIGN_CENTER);
            }
            else {
                read.setEnabled(false);
                write.setEnabled(false);
            }
			i++;
		}
	}
}
