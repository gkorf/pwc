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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'Folder Context' menu implementation.
 */
public class UploadAlert extends PopupPanel {
	
	private HTML label = new HTML();
	
	/**
	 * The widget's constructor.
	 */
	public UploadAlert(final Pithos app, int _numOfFiles) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(false);
		addStyleName(Pithos.resources.pithosCss().uploadAlert());
		FlowPanel content = new FlowPanel();
		setNumOfFiles(_numOfFiles);
		content.add(label);
		Anchor a = new Anchor("Click for details");
		a.addStyleName(Pithos.resources.pithosCss().uploadAlertLink());
		a.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
		        app.getFileUploadDialog().center();
			}
		});
		content.add(a);
		Image close = new Image(Pithos.resources.closePopup());
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		close.addStyleName(Pithos.resources.pithosCss().uploadAlertClose());
		content.add(close);
		
		FlowPanel progress = new FlowPanel();
		progress.addStyleName("plupload_progress");
		progress.addStyleName(Pithos.resources.pithosCss().uploadAlertProgress());
		FlowPanel progress_container = new FlowPanel();
		progress_container.addStyleName("plupload_progress_container");
		progress.add(progress_container);
		FlowPanel progress_bar = new FlowPanel();
		progress_bar.getElement().setId("upload_alert_progress_bar");
		progress_bar.addStyleName("plupload_progress_bar");
		progress_container.add(progress_bar);
		content.add(progress);

		HTML percent = new HTML();
		percent.getElement().setId("upload_alert_percent");
		percent.addStyleName(Pithos.resources.pithosCss().uploadAlertPercent());
		content.add(percent);
		
		add(content);
	}
	
	public void setNumOfFiles(int n) {
		label.setText(String.valueOf(n) + " " + (n > 1 ? "files are" : "file is") + " being uploaded");
	}
}
