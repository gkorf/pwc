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
import com.google.gwt.user.client.ui.Widget;


/**
 * A dialog box that displays info about invitations
 */
public class InvitationsDialog extends DialogBox {
	private static final int INV_PER_PAGE = 5;
	
	Pithos app;
	VerticalPanel messagesPanel;
	int rows = 0;
	FlexTable sentInvitationsTable;	
	/**
	 * The current displayed page of sent invitations 
	 */
	int currentPage = 0;
	HorizontalPanel pagerPanel;	
	/**
	 * The widget constructor.
	 */
	public InvitationsDialog(Pithos _app, final Invitations inv) {
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
		setText("Invite friends");
		setAnimationEnabled(true);
		setGlassEnabled(true);
		setStyleName("pithos-DialogBox");
		VerticalPanel outer = new VerticalPanel();
		outer.addStyleName("outer");
		outer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		VerticalPanel inner = new VerticalPanel();
		inner.addStyleName("inner");
		// Create the text and set a style name so we can style it with CSS.
		HTML text = new HTML("You have <span class='pithos-leftInvitationsNumber'>" + inv.getInvitationsLeft() + "</span> invitations left");
		text.addStyleName("pithos-invitationsLeft");
		inner.add(text);
		
		HorizontalPanel split = new HorizontalPanel();
		split.addStyleName("pithos-invitationsSplitPanel");

		VerticalPanel left = new VerticalPanel();
		left.addStyleName("pithos-sendInvitationsPanel");
		left.setVisible(inv.getInvitationsLeft() > 0);

		HorizontalPanel sendLabelPanel = new HorizontalPanel();
		HTML sendLabel = new HTML("Send new invitations");
		sendLabel.addStyleName("pithos-sendInvitationsTitle");
		sendLabelPanel.add(sendLabel);
		Image plus = new Image("images/plus.png");
		plus.addStyleName("pithos-addInvitationImg");
		sendLabelPanel.add(plus);
		left.add(sendLabelPanel);

		messagesPanel = new VerticalPanel();
		messagesPanel.setSpacing(5);
		left.add(messagesPanel);
		
		final FlexTable table = new FlexTable();
		table.setCellSpacing(0);
		if (inv.getInvitationsLeft() > 0) {
			table.setHTML(0, 0, "Name <span class='eg'>e.g. John Smith</span>");
			table.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
			table.setText(0, 1, "Email");
			table.getFlexCellFormatter().setStyleName(0, 1, "props-labels");
			rows = 1;
			addFormLine(table);
		}
		left.add(table);
		plus.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (rows == 0) {
					table.setHTML(0, 0, "Name <span class='eg'>e.g. John Smith</span>");
					table.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
					table.setText(0, 1, "Email");
					table.getFlexCellFormatter().setStyleName(0, 1, "props-labels");
					rows++;
				}
				addFormLine(table);
			}
		});

		Button send = new Button("send invitations", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int rowCount = table.getRowCount();
				
				for (int i = 1; i<rowCount; i++) {
					String name = ((TextBox) table.getWidget(i, 0)).getText().trim();
					String email = ((TextBox) table.getWidget(i, 1)).getText().trim();
					sendInvitation(email, name);
				}
			}
		});
		send.addStyleName("pithos-sendInvitationButton");
		send.setVisible(inv.getInvitationsLeft() > 0);
		left.add(send);
		
		split.add(left);
		split.setCellWidth(left, "50%");

		VerticalPanel right = new VerticalPanel();
		right.addStyleName("pithos-sentInvitationsPanel");
		
		HTML sentLabel = new HTML("Invitations sent");
		sentLabel.addStyleName("pithos-sentInvitationsTitle");
		right.add(sentLabel);
		
		sentInvitationsTable = new FlexTable();
		sentInvitationsTable.setCellSpacing(0);
		sentInvitationsTable.addStyleName("pithos-sentInvitationsTable");
		fillSentInvitationsTable(inv);
		right.add(sentInvitationsTable);
		
		pagerPanel = new HorizontalPanel();
		pagerPanel.setSpacing(5);
		Button prev = new Button("Prev");
		prev.addStyleName("pithos-pagerButton");
		prev.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (currentPage > 0) {
					pagerPanel.getWidget(currentPage + 1).removeStyleName("pithos-pagerButtonCurrent");
					currentPage--;
					fillSentInvitationsTable(inv);
					pagerPanel.getWidget(currentPage + 1).addStyleName("pithos-pagerButtonCurrent");
				}
			}
		});
		pagerPanel.add(prev);
		Button next = new Button("Next");
		next.addStyleName("pithos-pagerButton");
		next.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				int numOfInvs = inv.getSentInvitations().size();
				int numOfPages = numOfInvs / INV_PER_PAGE + (numOfInvs % INV_PER_PAGE == 0 ? 0 : 1);
				if (currentPage < numOfPages - 1) {
					pagerPanel.getWidget(currentPage + 1).removeStyleName("pithos-pagerButtonCurrent");
					currentPage++;
					pagerPanel.getWidget(currentPage + 1).addStyleName("pithos-pagerButtonCurrent");
					fillSentInvitationsTable(inv);
				}
			}
		});
		pagerPanel.add(next);
		right.add(pagerPanel);
		updatePagerPanel(inv);
		
		split.add(right);
		
		inner.add(split);
		
		outer.add(inner);
		outer.add(close);
		outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);
		setWidget(outer);
	}

	private void addFormLine(final FlexTable table) {
		table.setWidget(rows, 0, new TextBox());
		table.getFlexCellFormatter().setStyleName(1, 0, "props-values");

		table.setWidget(rows, 1, new TextBox());
		table.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		
		Image delete = new Image("images/delete.png");
		delete.addStyleName("pithos-invitationDeleteImg");
		delete.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				int rowIndex = table.getCellForEvent(event).getRowIndex();
				table.removeRow(rowIndex);
				if (rowIndex == 1 && table.getRowCount() > 1) {
					table.getCellFormatter().removeStyleName(rowIndex, 0, "pithos-invitationFormRow");
					table.getCellFormatter().removeStyleName(rowIndex, 1, "pithos-invitationFormRow");
					table.getCellFormatter().removeStyleName(rowIndex, 2, "pithos-invitationFormRow");
				}
				rows--;
			}
		});
		table.setWidget(rows, 2, delete);

		if (rows > 1) {
			table.getCellFormatter().addStyleName(rows, 0, "pithos-invitationFormRow");
			table.getCellFormatter().addStyleName(rows, 1, "pithos-invitationFormRow");
			table.getCellFormatter().addStyleName(rows, 2, "pithos-invitationFormRow");
		}
		rows++;
	}

	void sendInvitation(String email, final String realname) {
		if (realname == null || realname.length() == 0) {
			HTML msg = new HTML("Name cannot be empty");
			msg.addStyleName("pithos-invitationResponseError");
			messagesPanel.add(msg);
		}
		else if (email == null || email.length() == 0 || !email.contains("@") || 
				email.substring(email.indexOf("@")).length() < 3 || !email.substring(email.indexOf("@") + 2).contains(".")) {
			HTML msg = new HTML("Invalid email");
			msg.addStyleName("pithos-invitationResponseError");
			messagesPanel.add(msg);
		}
		else {
			PostRequest sendInvitation = new PostRequest("/im/", "", "invite", "uniq=" + email + "&realname=" + realname) {
				
				@Override
				protected void onUnauthorized(Response response) {
					app.sessionExpired();
				}
				
				@Override
				public void onSuccess(Resource result) {
					HTML msg = new HTML("Invitation to <span class='user'>" + realname + "</span> was sent.");
					msg.addStyleName("pithos-invitationResponse");
					messagesPanel.add(msg);
				}
				
				@Override
				public void onError(Throwable t) {
					GWT.log("", t);
				}
			};
			sendInvitation.setHeader("X-Auth-Token", app.getToken());
			Scheduler.get().scheduleDeferred(sendInvitation);
		}
	}
	
	void fillSentInvitationsTable(Invitations inv) {
		sentInvitationsTable.removeAllRows();
		int row = 0;
		for (int j=currentPage * INV_PER_PAGE + 0; j<inv.getSentInvitations().size() && j<(currentPage + 1)* INV_PER_PAGE; j++) {
			final Invitation i = inv.getSentInvitations().get(j);
			sentInvitationsTable.setText(row, 0, i.getRealname());
			if (i.isAccepted())
				sentInvitationsTable.setWidget(row, 1, new Image("images/invitation_accepted.png"));
			else {
				Image img = new Image("images/resend.png");
				img.addStyleName("pithos-resendInvitation");
				img.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						sendInvitation(i.getEmail(), i.getRealname());
					}
				});
				sentInvitationsTable.setWidget(row, 1, img);
			}
			sentInvitationsTable.getFlexCellFormatter().setRowSpan(row, 1, 2);
			sentInvitationsTable.getFlexCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_RIGHT);
			if (j < inv.getSentInvitations().size() - 1 && j < (currentPage + 1)* INV_PER_PAGE - 1)
				sentInvitationsTable.getFlexCellFormatter().addStyleName(row, 1, "pithos-invitedEmailBorder");
			row++;
			sentInvitationsTable.setText(row, 0, i.getEmail());
			sentInvitationsTable.getFlexCellFormatter().addStyleName(row, 0, "pithos-invitedEmail");
			if (j < inv.getSentInvitations().size() - 1 && j < (currentPage + 1)* INV_PER_PAGE - 1)
				sentInvitationsTable.getFlexCellFormatter().addStyleName(row, 0, "pithos-invitedEmailBorder");
			row++;
		}
	}
	
	void updatePagerPanel(final Invitations inv) {
		int numOfInvitations = inv.getSentInvitations().size();
		int numOfPages = numOfInvitations / INV_PER_PAGE + (numOfInvitations % INV_PER_PAGE == 0 ? 0 : 1);
		for (int i=0; i<numOfPages; i++) {
			final Button page = new Button(String.valueOf(i + 1));
			page.addStyleName("pithos-pagerButton");
			if (i == currentPage)
				page.addStyleName("pithos-pagerButtonCurrent");
				
			final int j = i;
			page.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					pagerPanel.getWidget(currentPage + 1).removeStyleName("pithos-pagerButtonCurrent");
					currentPage = j;
					fillSentInvitationsTable(inv);
					page.addStyleName("pithos-pagerButtonCurrent");
				}
			});
			pagerPanel.insert(page, i + 1);
		}
	}
}
