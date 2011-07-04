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
