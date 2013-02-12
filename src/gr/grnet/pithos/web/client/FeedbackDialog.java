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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.*;
import gr.grnet.pithos.web.client.rest.PostRequest;


/**
 * A dialog box that displays info about invitations
 */
public class FeedbackDialog extends DialogBox {

    Dictionary otherProperties = Dictionary.getDictionary("otherProperties");

    Pithos app;

    String appData;

    TextArea msg;

    /**
     * The widget constructor.
     */
    public FeedbackDialog(final Pithos _app, final String _appData) {
        app = _app;
        appData = _appData;

        // Set the dialog's caption.
        Anchor close = new Anchor("close");
        close.addStyleName("close");
        close.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        setText("Send feedback");
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
        msg = new TextArea();
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
                sendFeedback();
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
        if(evt.getType().equals("keydown")) {
            // Use the popup's key preview hooks to close the dialog when
            // either enter or escape is pressed.
            switch(evt.getKeyCode()) {
                case KeyCodes.KEY_ENTER:
                    sendFeedback();
                    hide();
                    break;
                case KeyCodes.KEY_ESCAPE:
                    hide();
                    break;
            }
        }
    }

    /**
     */
    void sendFeedback() {
        final String feedbackURL = otherProperties.get("feedbackUrl");
        PostRequest sendFeedback = new PostRequest("", "", feedbackURL, "feedback_msg=" + msg.getText() + "&feedback_data=" + appData + "&auth=" + app.getUserToken()) {

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
                app.displayError("Could not send feedback");
            }
        };
        sendFeedback.setHeader("X-Auth-Token", app.getUserToken());
        Scheduler.get().scheduleDeferred(sendFeedback);
    }
}
