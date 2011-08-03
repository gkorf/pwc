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

import gr.grnet.pithos.web.client.rest.GetCommand;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.GroupResource;
import gr.grnet.pithos.web.client.rest.resource.PermissionHolder;
import gr.grnet.pithos.web.client.rest.resource.UserResource;
import gr.grnet.pithos.web.client.rest.resource.UserSearchResource;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PermissionsAddDialog extends DialogBox {

	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private SuggestBox suggestBox = new SuggestBox(oracle);

	private String selectedUser = null;

	private List<GroupResource> groups;

	private ListBox groupBox = new ListBox();

	private CheckBox read = new CheckBox();

	private CheckBox write = new CheckBox();

	private CheckBox modifyACL = new CheckBox();

	private final PermissionsList permList;

	boolean userAdd;

    private Pithos app;

	public PermissionsAddDialog(Pithos _app, List<GroupResource> _groups, PermissionsList _permList, boolean _userAdd) {
        app = _app;
		groups = _groups;
		userAdd = _userAdd;
		permList = _permList;
		
		groupBox.getElement().setId("addPermission.dropDown");
		
		suggestBox.getElement().setId("addPermission.textBox");
		
		read.getElement().setId("addPermission.read");
		
		write.getElement().setId("addPermission.write");
		
		modifyACL.getElement().setId("addpermission.modify");
		
		for (GroupResource group : _groups)
			groupBox.addItem(group.getName(), group.getName());
		final VerticalPanel panel = new VerticalPanel();
		final HorizontalPanel buttons = new HorizontalPanel();
		setWidget(panel);
		final FlexTable permTable = new FlexTable();
		permTable.setText(0, 0, "Users/Groups");
		permTable.setText(0, 1, "Read");
		permTable.setText(0, 2, "Write");
		permTable.setText(0, 3, "Modify Access");
		permTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 1, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 2, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 3, "props-toplabels");
		if (userAdd) {
			suggestBox.getTextBox().addFocusHandler(new FocusHandler() {

				@Override
				public void onFocus(FocusEvent event) {
					if (selectedUser != null && selectedUser.endsWith("@"))
						updateSuggestions();

				}
			});

			suggestBox.addKeyUpHandler(new KeyUpHandler() {

				@Override
				public void onKeyUp(KeyUpEvent event) {
					// Ignore the arrow keys.
					int keyCode = event.getNativeKeyCode();
					if (keyCode == KeyCodes.KEY_UP ||
							keyCode == KeyCodes.KEY_DOWN ||
							keyCode == KeyCodes.KEY_LEFT ||
							keyCode == KeyCodes.KEY_RIGHT)
						return;
					if (keyCode==KeyCodes.KEY_ESCAPE) {
						suggestBox.hideSuggestionList();
						return;
					}
					String text = suggestBox.getText().trim();
					// Avoid useless queries for keystrokes that do not modify
					// the text.
					if (text.equals(selectedUser))
						return;
					selectedUser = text;
					// Go to the server only if the user typed the @ character.
					if (selectedUser.endsWith("@"))
						updateSuggestions();
				}
			});
			permTable.setWidget(1, 0, suggestBox);
		} else
			permTable.setWidget(1, 0, groupBox);
		permTable.setWidget(1, 1, read);
		permTable.setWidget(1, 2, write);
		permTable.setWidget(1, 3, modifyACL);

		permTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		permTable.getFlexCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(1, 3, HasHorizontalAlignment.ALIGN_CENTER);
		panel.add(permTable);

		final Button ok = new Button("OK", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addPermission();
				hide();
			}
		});
		ok.getElement().setId("addPermission.button.ok");
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog
		// when the button is clicked.
		final Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		cancel.getElement().setId("addPermission.button.cancel");
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("pithos-TabPanelBottom");
		panel.add(buttons);
		panel.addStyleName("pithos-TabPanelBottom");
	}

	private void addPermission() {
		PermissionHolder perm = new PermissionHolder();
		if (userAdd) {
			selectedUser = suggestBox.getText();
			for(PermissionHolder p : permList.permissions)
				if (selectedUser.equals(p.getUser())){
					app.displayError("User already has access to the resource");
					return;
				}
			perm.setUser(selectedUser);
		} else {
			String groupId = groupBox.getValue(groupBox.getSelectedIndex());
			GroupResource selected = null;
			for (GroupResource g : groups)
				if (g.getName().equals(groupId))
					selected = g;
			if (selected == null)
				return;
			for(PermissionHolder p : permList.permissions)
				if (selected.getName().equals(p.getGroup())){
					app.displayError("Group already has access to the resource");
					return;
				}
			perm.setGroup(selected.getName());
		}
		boolean readValue = read.getValue();
		boolean writeValue = write.getValue();
		boolean modifyValue = modifyACL.getValue();

		perm.setRead(readValue);
		perm.setWrite(writeValue);
		perm.setModifyACL(modifyValue);
		permList.addPermission(perm);
		permList.updateTable();
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
			suggestBox.setFocus(true);
	}

	/**
	 * Update the list of suggestions.
	 */
	protected void updateSuggestions() {
		String query = selectedUser.substring(0, selectedUser.length()-1);
		GWT.log("Searching for " + query, null);

		GetCommand<UserSearchResource> eg = new GetCommand<UserSearchResource>(app, UserSearchResource.class,
					app.getApiPath() + "users/" + URL.encodeComponent(query), false, null) {

			@Override
			public void onComplete() {
				suggestBox.hideSuggestionList();
				oracle.clear();
				UserSearchResource s = getResult();
				for (UserResource user : s.getUsers()) {
					GWT.log("Found " + user.getUsername(), null);
					oracle.add(user.getUsername());
				}
				suggestBox.showSuggestionList();
			}

			@Override
			public void onError(Throwable t) {
				if(t instanceof RestException)
					app.displayError("Unable to perform search: "+((RestException)t).getHttpStatusText());
				else
					app.displayError("System error while searching for users: "+t.getMessage());
				GWT.log("", t);
				DisplayHelper.log(t.getMessage());
			}

		};
		DeferredCommand.addCommand(eg);
	}
}
