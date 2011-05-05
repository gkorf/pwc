/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'about' dialog box.
 */
public class AboutDialog extends DialogBox {

	/**
	 * The widget constructor.
	 */
	public AboutDialog() {
		// Set the dialog's caption.
		Configuration conf = (Configuration) GWT.create(Configuration.class);
		String service = conf.serviceName();
		setText("About " + service);
		setAnimationEnabled(true);
		// A VerticalPanel that contains the 'about' label and the 'OK' button.
		final VerticalPanel outer = new VerticalPanel();

		// Create the 'about' text and set the style.
		final HTML text = new HTML("This is the Web client for the " + service +
					" service. You can use it to store, retrieve and share " +
					"files in the " + service + " server grid. <p>Pithos version: " +
					conf.version() + "<br> Service designed and implemented by " + "<a href='http://www.grnet.gr'>GRNET</a></p>");
		text.setStyleName("pithos-AboutText");
		outer.add(text);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		final Button confirm = new Button("Close", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
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
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}

}
