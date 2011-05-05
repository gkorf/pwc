/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.FileMenu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


public class ViewImageCommand implements Command {

	final FileMenu.Images newImages;

	private PopupPanel containerPanel;

	private String imageDownloadURL;

	private Label errorLabel = new Label();

	/**
	 * @param _containerPanel
	 * @param _newImages the images of all the possible delete dialogs
	 */
	public ViewImageCommand(PopupPanel _containerPanel, final FileMenu.Images _newImages, String _imageDownloadURL) {
		containerPanel = _containerPanel;
		newImages = _newImages;
		imageDownloadURL = _imageDownloadURL;
	}

	@Override
	public void execute() {
		containerPanel.hide();

		final Image image = new Image();
		// Hook up a load handler, so that we can be informed if the image
		// fails to load.
	    image.addLoadHandler(new LoadHandler() {

			@Override
			public void onLoad(LoadEvent event) {
				errorLabel.setText("");
			}
		});
		image.addErrorHandler(new ErrorHandler() {

			@Override
			public void onError(ErrorEvent event) {
				errorLabel.setText("An error occurred while loading.");
			}
		});
	    image.setUrl(imageDownloadURL);
	    final DialogBox imagePopup = new DialogBox(true, true);
	    imagePopup.setAnimationEnabled(true);
	    imagePopup.setText("Showing image in actual size");
	    VerticalPanel imageViewPanel = new VerticalPanel();
	    errorLabel.setText("loading image...");
	    imageViewPanel.add(errorLabel);
	    imageViewPanel.add(image);
	    imagePopup.setWidget(imageViewPanel);
	    image.setTitle("Click to close");
	    image.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
	    		imagePopup.hide();
	    	}
	    });
	    imagePopup.setPopupPosition(0, 0);
	    imagePopup.show();
	}
}
