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

import gr.grnet.pithos.web.client.rest.PostCommand;
import gr.grnet.pithos.web.client.rest.RestException;

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
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * A dialog box that displays the user credentials for use in other client
 * applications, such as WebDAV clients.
 */
public class CredentialsDialog extends DialogBox {

	private final String WIDTH_FIELD = "35em";
	private final String WIDTH_TEXT = "42em";

	/**
	 * The 'confirm reset password' dialog box.
	 */
	private class ConfirmResetPasswordDialog extends DialogBox {

		/**
		 * The widget's constructor.
		 *
		 * @param images the supplied images
		 */
		private ConfirmResetPasswordDialog(MessagePanel.Images images) {
			// Set the dialog's caption.
			setText("Confirmation");
			setAnimationEnabled(true);
			// Create a VerticalPanel to contain the label and the buttons.
			VerticalPanel outer = new VerticalPanel();
			HorizontalPanel buttons = new HorizontalPanel();

			HTML text;
			text = new HTML("<table><tr><td>" +
					AbstractImagePrototype.create(images.warn()).getHTML() +
					"</td><td>" + "Are you sure you want to create a new " +
					"WebDAV password?</td></tr></table>");
			text.setStyleName("pithos-warnMessage");
			outer.add(text);

			// Create the 'Yes' button, along with a listener that hides the
			// dialog when the button is clicked and resets the password.
			Button ok = new Button("Yes", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					resetPassword(Pithos.get().getCurrentUserResource().getUri());
					hide();
				}
			});
			buttons.add(ok);
			buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
			// Create the 'No' button, along with a listener that hides the
			// dialog when the button is clicked.
			Button cancel = new Button("No", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
				}
			});
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

		@Override
		protected void onPreviewNativeEvent(NativePreviewEvent preview) {
			super.onPreviewNativeEvent(preview);
			NativeEvent evt = preview.getNativeEvent();
			if (evt.getType().equals("keydown"))
				// Use the popup's key preview hooks to close the dialog when either
				// enter or escape is pressed.
				switch (evt.getKeyCode()) {
					case KeyCodes.KEY_ENTER:
					case KeyCodes.KEY_ESCAPE:
						hide();
						break;
				}
		}

	}

	private class ReauthenticateDialog extends DialogBox {
		/**
		 * The widget constructor.
		 */
		public ReauthenticateDialog() {
			// Set the dialog's caption.
			setText("New Password Created");
			setAnimationEnabled(true);
			VerticalPanel outer = new VerticalPanel();

			// Create the text and set a style name so we can style it with CSS.
			HTML text = new HTML("<p>A new WebDAV password has been created." +
					"</p><p>You will now be redirected to the initial screen" +
					" for the changes to take effect. Choose \"Show " +
					"Credentials\" again afterwards to see the new password.</p>");
			text.setStyleName("pithos-AboutText");
			outer.add(text);

			// Create the 'OK' button, along with a listener that hides the
			// dialog when the button is clicked.
			Button confirm = new Button("Proceed", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					Pithos.get().authenticateUser();
					hide();
				}
			});
			outer.add(confirm);
			outer.setCellHorizontalAlignment(confirm, HasHorizontalAlignment.ALIGN_CENTER);
			outer.setSpacing(8);
			setWidget(outer);
		}

		@Override
		protected void onPreviewNativeEvent(NativePreviewEvent preview) {
			super.onPreviewNativeEvent(preview);
			NativeEvent evt = preview.getNativeEvent();
			if (evt.getType().equals("keydown"))
				// Use the popup's key preview hooks to close the dialog when
				// either enter or escape is pressed.
				switch (evt.getKeyCode()) {
					case KeyCodes.KEY_ENTER:
						Pithos.get().authenticateUser();
						hide();
						break;
					case KeyCodes.KEY_ESCAPE:
						hide();
						break;
				}
		}

	}

	/**
	 * The widget constructor.
	 */
	public CredentialsDialog(final MessagePanel.Images images) {
		// Set the dialog's caption.
		setText("User Credentials");
		setAnimationEnabled(true);
		// A VerticalPanel that contains the 'about' label and the 'OK' button.
		VerticalPanel outer = new VerticalPanel();
		Configuration conf = (Configuration) GWT.create(Configuration.class);
		String service = conf.serviceName();
		// Create the text and set a style name so we can style it with CSS.
		HTML text = new HTML("<p>These are the user credentials that are " +
				"required for interacting with " + service + ".");
		text.setStyleName("pithos-AboutText");
		text.setWidth(WIDTH_TEXT);
		outer.add(text);
		FlexTable table = new FlexTable();
		table.setText(0, 0, "Username");
		table.setText(1, 0, "Token");
		TextBox username = new TextBox();
		final Pithos app = Pithos.get();
		username.setText(app.getCurrentUserResource().getUsername());
		username.setReadOnly(true);
		username.setWidth(WIDTH_FIELD);
		username.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Pithos.enableIESelection();
				((TextBox) event.getSource()).selectAll();
				Pithos.preventIESelection();
			}

		});
		table.setWidget(0, 1, username);

		TextBox tokenBox = new TextBox();
		tokenBox.setText(app.getToken());
		tokenBox.setReadOnly(true);
		tokenBox.setWidth(WIDTH_FIELD);
		tokenBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Pithos.enableIESelection();
				((TextBox) event.getSource()).selectAll();
				Pithos.preventIESelection();
			}

		});
		table.setWidget(1, 1, tokenBox);

		table.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		table.getFlexCellFormatter().setStyleName(0, 1, "props-values");
		table.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		table.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		outer.add(table);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		Button confirm = new Button("Close", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		outer.add(confirm);
		outer.setCellHorizontalAlignment(confirm, HasHorizontalAlignment.ALIGN_CENTER);

		// Create the 'Reset password' button, along with a listener that hides
		// the dialog when the button is clicked.
		Button resetPassword = new Button("Reset Password", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ConfirmResetPasswordDialog dlg = new ConfirmResetPasswordDialog(images);
				dlg.center();
			}
		});
		outer.add(resetPassword);
		outer.setCellHorizontalAlignment(resetPassword, HasHorizontalAlignment.ALIGN_CENTER);

		outer.setSpacing(8);
		setWidget(outer);
	}

	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
		super.onPreviewNativeEvent(preview);
		NativeEvent evt = preview.getNativeEvent();
		if (evt.getType().equals("keydown"))
			// Use the popup's key preview hooks to close the dialog when
			// either enter or escape is pressed.
			switch (evt.getKeyCode()) {
				case KeyCodes.KEY_ENTER:
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}


	/**
	 * Generate an RPC request to reset WebDAV password.
	 *
	 */
	private void resetPassword(String userUri) {

		if (userUri == null || userUri.length() == 0) {
			Pithos.get().displayError("Empty user Uri!");
			return;
		}
		GWT.log("resetPassword(" + userUri + ")", null);
		PostCommand cg = new PostCommand(userUri + "?resetWebDAV", "", 200) {

			@Override
			public void onComplete() {
				ReauthenticateDialog dlg = new ReauthenticateDialog();
				dlg.center();
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						Pithos.get().displayError("You don't have the necessary" +
								" permissions");
					else if(statusCode == 404)
						Pithos.get().displayError("Resource does not exist");
					else
						Pithos.get().displayError("Unable to reset password:" +
									((RestException)t).getHttpStatusText());
				}
				else
					Pithos.get().displayError("System error resetting password:" +
								t.getMessage());
			}
		};
		DeferredCommand.addCommand(cg);
	}

}
