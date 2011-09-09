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

import com.google.gwt.user.client.ui.TextBox;
import gr.grnet.pithos.web.client.foldertree.Group;

import java.util.List;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PermissionsAddDialog extends DialogBox {

	private TextBox userBox = new TextBox();

	private ListBox groupBox = new ListBox();

	private CheckBox read = new CheckBox();

	private CheckBox write = new CheckBox();

	private PermissionsList permList;

	boolean userAdd;

    private Pithos app;

	public PermissionsAddDialog(Pithos _app, List<Group> _groups, PermissionsList _permList, boolean _userAdd) {
        app = _app;
		userAdd = _userAdd;
		permList = _permList;

        final VerticalPanel panel = new VerticalPanel();

        final FlexTable permTable = new FlexTable();
        permTable.setText(0, 0, "Users/Groups");
        permTable.setText(0, 1, "Read");
        permTable.setText(0, 2, "Write");
        permTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
        permTable.getFlexCellFormatter().setStyleName(0, 1, "props-toplabels");
        permTable.getFlexCellFormatter().setStyleName(0, 2, "props-toplabels");

        if (userAdd) {
            permTable.setWidget(1, 0, userBox);
        }
        else {
            for (Group group : _groups)
                groupBox.addItem(group.getName(), group.getName());
            permTable.setWidget(1, 0, groupBox);
        }

        permTable.setWidget(1, 1, read);
        permTable.setWidget(1, 2, write);

        permTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
        permTable.getFlexCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
        permTable.getFlexCellFormatter().setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_CENTER);
        panel.add(permTable);

        final HorizontalPanel buttons = new HorizontalPanel();
        final Button ok = new Button("OK", new ClickHandler() {
            @Override
            public void onClick(@SuppressWarnings("unused") ClickEvent event) {
                addPermission();
                hide();
            }
        });
        buttons.add(ok);
        buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
        // Create the 'Cancel' button, along with a listener that hides the
        // dialog
        // when the button is clicked.
        final Button cancel = new Button("Cancel", new ClickHandler() {
            @Override
            public void onClick(@SuppressWarnings("unused") ClickEvent event) {
                hide();
            }
        });
        buttons.add(cancel);
        buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
        buttons.setSpacing(8);
        buttons.addStyleName("pithos-TabPanelBottom");

        panel.add(buttons);
        panel.addStyleName("pithos-TabPanelBottom");

        setWidget(panel);
	}

	protected void addPermission() {
        String selected = null;
		if (userAdd) {
			selected = userBox.getText();
		} else {
			String groupName = groupBox.getValue(groupBox.getSelectedIndex());
            selected = app.getUsername() + ":" + groupName;
		}
        if (permList.getPermissions().get(selected) != null) {
            return;
        }
		boolean readValue = read.getValue();
		boolean writeValue = write.getValue();

		permList.addPermission(selected, readValue, writeValue);
	}

	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
		super.onPreviewNativeEvent(preview);

		NativeEvent evt = preview.getNativeEvent();
		if (evt.getType().equals("keydown"))
			// Use the popup's key preview hooks to close the dialog when either
			// enter or escape is pressed.
			switch (evt.getKeyCode()) {
				case KeyCodes.KEY_ENTER:
					addPermission();
					hide();
					break;
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}


	@Override
	public void center() {
		super.center();
		if (userAdd)
			userBox.setFocus(true);
	}
}
