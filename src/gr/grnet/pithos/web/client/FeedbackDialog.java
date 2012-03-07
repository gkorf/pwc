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

import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.PostRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * A dialog box that displays info about invitations
 */
public class FeedbackDialog extends DialogBox {

	Dictionary otherProperties = Dictionary.getDictionary("otherProperties");
	
	/**
	 * The widget constructor.
	 */
	public FeedbackDialog(final Pithos app, final String appData) {
		// Set the dialog's caption.
		Anchor close = new Anchor();
		close.addStyleName("close");
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		setText("Send feedback");
		setAnimationEnabled(true);
		setGlassEnabled(true);
		
		setStyleName("pithos-DialogBox");

		VerticalPanel outer = new VerticalPanel();
		outer.add(close);
		
		VerticalPanel inner = new VerticalPanel();
		inner.addStyleName("inner");
		// Create the text and set a style name so we can style it with CSS.
		HTML text = new HTML("Pithos+ is currently in alpha test and we would appreciate any<br>" + "kind of feedback. We welcome any suggestions, questions and<br>" + " bug reports you may have.");
		text.setStyleName("pithos-credentialsText");
		inner.add(text);
		FlexTable table = new FlexTable();
		table.setText(0, 0, "Please describe your problem here, provide as many details as possible");
		final TextArea msg = new TextArea();
		msg.setWidth("100%");
		msg.setHeight("100px");
		table.setWidget(1, 0, msg);

		table.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		table.getFlexCellFormatter().setStyleName(0, 1, "props-values");
		inner.add(table);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		Button confirm = new Button("Submit feedback", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				PostRequest sendFeedback = new PostRequest("", "", otherProperties.get("feedbackUrl"), "feedback-msg=" + msg.getText() + "&feedback-data=" + appData) {
					
					@Override
					protected void onUnauthorized(Response response) {
						app.sessionExpired();
					}
					
					@Override
					public void onSuccess(Resource result) {
						app.displayInformation("Feedback sent");
					}
					
					@Override
					public void onError(Throwable t) {
						GWT.log("", t);
					}
				};
				sendFeedback.setHeader("X-Auth-Token", app.getToken());
				Scheduler.get().scheduleDeferred(sendFeedback);
				hide();
			}
		});
		confirm.addStyleName("button");
		inner.add(confirm);
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
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}
}
