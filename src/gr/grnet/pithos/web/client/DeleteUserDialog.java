/*
 * Copyright 2008, 2009 Electronic Business Systems Ltd.
 *
 * This file is part of GSS.
 *
 * GSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GSS.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.MessagePanel.Images;
import gr.grnet.pithos.web.client.rest.DeleteCommand;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * @author kman
 *
 */
public class DeleteUserDialog extends DialogBox {

	/**
	 * The widget's constructor.
	 * @param images the supplied images
	 */
	public DeleteUserDialog(final Images images) {
		// Use this opportunity to set the dialog's caption.
		setText("Delete user");
		setAnimationEnabled(true);
		final GroupUserResource group = (GroupUserResource) GSS.get().getCurrentSelection();
		// Create a VerticalPanel to contain the 'about' label and the 'OK'
		// button.
		final VerticalPanel outer = new VerticalPanel();
		final HorizontalPanel buttons = new HorizontalPanel();

		// Create the 'about' text and set a style name so we can style it with
		// CSS.
		final HTML text = new HTML("<table><tr><td>" + AbstractImagePrototype.create(images.warn()).getHTML() + "</td><td>" + "Are you sure you want to remove user '" + group.getName() + "'?</td></tr></table>");
		text.setStyleName("pithos-warnMessage");
		outer.add(text);

		// Create the 'Quit' button, along with a listener that hides the dialog
		// when the button is clicked and quits the application.
		final Button ok = new Button("OK", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteUser();
				hide();
			}
		});
		ok.getElement().setId("deleteUser.button.ok");
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
		cancel.getElement().setId("confirmation.button.cancel");
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.setStyleName("pithos-warnMessage");
		outer.setStyleName("pithos-warnMessage");
		outer.add(buttons);
		outer.setCellHorizontalAlignment(text, HasHorizontalAlignment.ALIGN_CENTER);
		outer.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		setWidget(outer);
	}

	/**
	 * Generate an RPC request to delete a group.
	 *
	 * @param userId the ID of the current user
	 */
	private void deleteUser() {
		final TreeItem user = GSS.get().getGroups().getCurrent();
		final TreeItem group = user.getParentItem();
		if (group == null) {
			GSS.get().displayError("No user was selected!");
			return;
		}
		final GroupUserResource memberR = (GroupUserResource) user.getUserObject();
		DeleteCommand du = new DeleteCommand(memberR.getUri()){

			@Override
			public void onComplete() {
				GSS.get().getGroups().updateGroups();
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						GSS.get().displayError("You don't have the necessary permissions");
					else if(statusCode == 404)
						GSS.get().displayError("User not found");
					else
						GSS.get().displayError("Unable to delete user:"+((RestException)t).getHttpStatusText());
				}
				else
					GSS.get().displayError("System error unable to delete user:"+t.getMessage());
			}
		};
		DeferredCommand.addCommand(du);

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
				hide();
				deleteUser();
				break;
			case KeyCodes.KEY_ESCAPE:
				hide();
				break;
		}
	}

}

