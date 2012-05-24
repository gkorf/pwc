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

import gr.grnet.pithos.web.client.grouptree.Group;

import java.util.List;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PermissionsAddDialog extends DialogBox {

	private TextBox userBox = new TextBox();

	private ListBox groupBox = new ListBox();

	private RadioButton read = new RadioButton("permissions");

	private RadioButton write = new RadioButton("permissions");

	private PermissionsList permList;

	boolean userAdd;

    private Pithos app;

	public PermissionsAddDialog(Pithos _app, List<Group> _groups, PermissionsList _permList, boolean _userAdd) {
        app = _app;
		userAdd = _userAdd;
		permList = _permList;

		Anchor close = new Anchor("close");
		close.addStyleName("close");
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		setText("Add permission");
		setStyleName("pithos-DialogBox");

        final VerticalPanel panel = new VerticalPanel();
        panel.add(close);

        VerticalPanel inner = new VerticalPanel();
		inner.addStyleName("inner");

        final FlexTable permTable = new FlexTable();
        permTable.setText(0, 0, "Users/Groups");
        permTable.setText(0, 1, "Read Only");
        permTable.setText(0, 2, "Read/Write");
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
                
        read.setValue(true);
        permTable.setWidget(1, 1, read);
        permTable.setWidget(1, 2, write);

        permTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
        permTable.getFlexCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
        permTable.getFlexCellFormatter().setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_CENTER);
        inner.add(permTable);

        final Button ok = new Button("OK", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addPermission();
                hide();
            }
        });

        ok.addStyleName("button");
        inner.add(ok);

        panel.add(inner);
        panel.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);
        
        setWidget(panel);
	}

	protected void addPermission() {
        String selected = null;
		if (userAdd) {
			selected = userBox.getText().trim();
			RegExp emailValidator = RegExp.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+[.][A-Z]{2,4}$", "i");
			if (!emailValidator.test(selected)) {
				app.displayWarning("Username must be a valid email address");
				return;
			}
		} else if (groupBox.getSelectedIndex() > -1) {
			String groupName = groupBox.getValue(groupBox.getSelectedIndex());
			selected = app.getUsername() + ":" + groupName;
		}
        if (permList.getPermissions().get(selected) != null) {
            return;
        }
        if (selected == null || selected.length() == 0 || selected.equals(app.getUsername() + ":")) {
        	app.displayWarning("You have to select a username or group");
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
