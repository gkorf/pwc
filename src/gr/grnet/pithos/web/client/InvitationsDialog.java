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

import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.PostRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * A dialog box that displays info about invitations
 */
public class InvitationsDialog extends DialogBox {

	private final String WIDTH_FIELD = "35em";
	private final String WIDTH_TEXT = "42em";

	Pithos app;
	TextBox name;
	TextBox emailBox;
	
	/**
	 * The widget constructor.
	 */
	public InvitationsDialog(Pithos _app, Invitations inv) {
		this.app = _app;
		Anchor close = new Anchor();
		close.addStyleName("close");
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		// Set the dialog's caption.
		setText("Invite people (" + inv.getInvitationsLeft() + " invitations left)");
		setAnimationEnabled(true);
		setGlassEnabled(true);
		setStyleName("pithos-DialogBox");
		VerticalPanel outer = new VerticalPanel();
		outer.add(close);

		VerticalPanel inner = new VerticalPanel();
		inner.addStyleName("inner");
		// Create the text and set a style name so we can style it with CSS.
		HTML text = new HTML("You have " + inv.getInvitationsLeft() + " invitations left in your account.");
		text.setStyleName("pithos-credentialsText");
		text.setWidth(WIDTH_TEXT);
		inner.add(text);
		
		FlexTable table = new FlexTable();
		if (inv.getInvitationsLeft() > 0) {
			table.setText(0, 0, "Name");
			table.setText(1, 0, "E-mail");
		}
		name = new TextBox();
		name.setWidth(WIDTH_FIELD);
		name.setVisible(inv.getInvitationsLeft() > 0);
		table.setWidget(0, 1, name);

		emailBox = new TextBox();
		emailBox.setWidth(WIDTH_FIELD);
		emailBox.setVisible(inv.getInvitationsLeft() > 0);
		table.setWidget(1, 1, emailBox);

		table.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		table.getFlexCellFormatter().setStyleName(0, 1, "props-values");
		table.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		table.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		inner.add(table);

		Button confirm = new Button("Send", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sendInvitation(emailBox.getText().trim(), name.getText().trim());
			}
		});
		confirm.addStyleName("button");
		confirm.setVisible(inv.getInvitationsLeft() > 0);
		inner.add(confirm);

		HTML sentLabel = new HTML("Sent invitations");
		sentLabel.addStyleName("pithos-sentInvitationsTitle");
		inner.add(sentLabel);
		
		FlexTable sentInvitationsTable = new FlexTable();
		Image accepted = new Image("images/invitation_accepted.png");
		accepted.setAltText("Invitation accepted");
		Image resend = new Image("images/resend.png");
		resend.setAltText("Resend invitation");
		
		HorizontalPanel legend = new HorizontalPanel();
		legend.add(new HTML("("));
		legend.add(accepted);
		legend.add(new HTML(" = Invitation has been accepted)"));
		legend.add(new HTML("("));
		legend.add(resend);
		legend.add(new HTML(" = Send invitation again)"));
		inner.add(legend);
		
		int row = 0;
		for (final Invitation i : inv.getSentInvitations()) {
			sentInvitationsTable.setText(row, 0, i.getRealname());
			sentInvitationsTable.setText(row, 1, i.getEmail());
			if (i.isAccepted())
				sentInvitationsTable.setWidget(row, 2, new Image("images/invitation_accepted.png"));
			else {
				Image img = new Image("images/resend.png");
				img.addStyleName("pithos-resendInvitation");
				img.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						sendInvitation(i.getEmail(), i.getRealname());
					}
				});
				sentInvitationsTable.setWidget(row, 2, img);
			}
			row++;
		}
		inner.add(sentInvitationsTable);
		
		outer.add(inner);
		outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);
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
					sendInvitation(emailBox.getText().trim(), name.getText().trim());
					break;
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}

	void sendInvitation(String email, String realname) {
		PostRequest sendInvitation = new PostRequest("/im/", "", "invite", "uniq=" + email + "&realname=" + realname) {
			
			@Override
			protected void onUnauthorized(Response response) {
				app.sessionExpired();
			}
			
			@Override
			public void onSuccess(Resource result) {
				app.displayInformation("Invitation sent");
			}
			
			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
			}
		};
		sendInvitation.setHeader("X-Auth-Token", app.getToken());
		Scheduler.get().scheduleDeferred(sendInvitation);
		hide();
	}
}
