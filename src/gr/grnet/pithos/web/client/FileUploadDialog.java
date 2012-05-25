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

import gr.grnet.pithos.web.client.foldertree.Folder;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'File upload' dialog box implementation.
 */
public class FileUploadDialog extends DialogBox {

    public static final boolean DONE = true;

    Anchor close;
    
	/**
	 * The Form element that performs the file upload.
	 */
    protected final FormPanel form = new FormPanel();

	private final FileUpload upload = new FileUpload();

	private final Label filenameLabel = new Label();

    private final Label foldernameLabel = new Label();

    private Button submit;

	protected Folder folder;

    protected Pithos app;
    
    private boolean inProgress = false;

	/**
	 * The widget's constructor.
	 */
	public FileUploadDialog(Pithos _app) {
		app = _app;
		close = new Anchor("close");
		close.addStyleName("close");
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				close();
			}
		});
		// Set the dialog's caption.
		setText("File upload");
		setAnimationEnabled(true);
//		setGlassEnabled(true);
		setStyleName("pithos-DialogBox");
		setVisible(false);
		
		// Since we're going to add a FileUpload widget, we'll need to set the
		// form to use the POST method, and multipart MIME encoding.
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);

		// Create a panel to hold all of the form widgets.
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("470px");
		panel.add(close);
		form.setWidget(panel);

		VerticalPanel inner = new VerticalPanel();
		inner.addStyleName("inner");

        final Hidden auth = new Hidden("X-Auth-Token");
        inner.add(auth);
		upload.setName("X-Object-Data");
		upload.setVisible(false);
		filenameLabel.setText("");
		filenameLabel.setVisible(false);
		filenameLabel.setStyleName("props-labels");
		HorizontalPanel fileUploadPanel = new HorizontalPanel();
		fileUploadPanel.setVisible(false);
		fileUploadPanel.add(filenameLabel);
		fileUploadPanel.add(upload);
		Grid generalTable = new Grid(2, 2);
		generalTable.setText(0, 0, "Folder");
        generalTable.setWidget(0, 1, foldernameLabel);
		generalTable.setWidget(1, 1, fileUploadPanel);
		generalTable.getCellFormatter().setStyleName(0, 0, "props-labels");
        generalTable.getCellFormatter().setStyleName(0, 1, "props-values");
        generalTable.getCellFormatter().setVisible(1, 0, false);
		generalTable.setCellSpacing(4);

		inner.add(generalTable);

		FlowPanel uploader = new FlowPanel();
		uploader.getElement().setId("uploader");
		inner.add(uploader);
		
		panel.add(inner);
		panel.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);
		
		setWidget(form);
		
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				center();
				close();
			}
		});
	}

	private void refreshFolder() {
		if (app.getSelectedTree().equals(app.getFolderTreeView()))
			app.updateFolder(folder, true, new Command() {
				
				@Override
				public void execute() {
					app.updateStatistics();
				}
			}, true);
		else
			app.updateOtherSharedFolder(folder, true, null);
	}
	
	native void setupUpload(FileUploadDialog dlg, Pithos app, String token) /*-{
		var uploader = $wnd.$('#uploader').pluploadQueue();
		var createUploader = function() {
			$wnd.$("#uploader").pluploadQueue({
				// General settings
				runtimes : 'html5, flash, gears, silverlight, browserplus, html4',
				unique_names : true,
		
				// Flash settings
				flash_swf_url : 'plupload/js/plupload.flash.swf',
		
				// Silverlight settings
				silverlight_xap_url : 'plupload/js/plupload.silverlight.xap',
				
				multiple_queues: true,
				
				preinit: {
					Init: function(up, info) {
						if ($wnd.console && $wnd.console.log)
							$wnd.console.log("Init fired");
						up.settings.file_data_name = "X-Object-Data";				
					}
					
				},
				
				init: {
					FilesAdded: function(up, files) {
						var api = app.@gr.grnet.pithos.web.client.Pithos::getApiPath()();
						var folder = app.@gr.grnet.pithos.web.client.Pithos::getUploadFolder()();
						var owner = folder.@gr.grnet.pithos.web.client.foldertree.Folder::getOwner()();
						var uri = folder.@gr.grnet.pithos.web.client.foldertree.Folder::getUri()();
						var path = api + owner + uri;
						for (var j=0; j<files.length; j++)
							files[j].url = path + "/" + files[j].name;
						dlg.@gr.grnet.pithos.web.client.FileUploadDialog::setInProgress(Z)(true);
						up.start();
						app.@gr.grnet.pithos.web.client.Pithos::showUploadIndicator()();
						if (!dlg.@gr.grnet.pithos.web.client.FileUploadDialog::isVisible()())
							app.@gr.grnet.pithos.web.client.Pithos::showUploadAlert(I)(files.length);
					},
					
					FilesRemoved: function(up, files) {
						if (up.files.length == 0)
							dlg.@gr.grnet.pithos.web.client.FileUploadDialog::setInProgress(Z)(false);
						else
							dlg.@gr.grnet.pithos.web.client.FileUploadDialog::setInProgress(Z)(true);
					},
					
					BeforeUpload: function(up, file) {
						if ($wnd.console && $wnd.console.log)
							$wnd.console.log('About to upload ' + file.url);
						up.settings.url = file.url + + "?X-Auth-Token=" + encodeURIComponent(token);
					},
					
					UploadProgress: function(up, file) {
						$wnd.$('#upload_alert_progress_bar').css('width', up.total.percent + '%');
						$wnd.$('#upload_alert_percent').html(up.total.percent + '%');
					},
					
					FileUploaded: function(up, file, response) {
						if ($wnd.console && $wnd.console.log) {
							$wnd.console.log('File ' + file.name + ' uploaded');
							$wnd.console.log('Response: ' + response);
						}
						var folder = app.@gr.grnet.pithos.web.client.Pithos::getUploadFolder()();
						if (folder == file.folder)
							app.@gr.grnet.pithos.web.client.Pithos::updateUploadFolder()();
					},
					
					UploadComplete: function(up, files) {
						if ($wnd.console && $wnd.console.log) {
							$wnd.console.log('All files finished');
						}
						dlg.@gr.grnet.pithos.web.client.FileUploadDialog::setInProgress(Z)(false);
						dlg.@gr.grnet.pithos.web.client.FileUploadDialog::hideUploadIndicator()();
						app.@gr.grnet.pithos.web.client.Pithos::hideUploadAlert()();
						var uris = [];
						if (!dlg.@gr.grnet.pithos.web.client.FileUploadDialog::isVisible()())
							while (files.length > 0) {
								uris.push(files[0].url);
								up.removeFile(files[0]);
							}
						else
							for (var i=0; i<files.length; i++)
								uris.push(files[i].url);
						app.@gr.grnet.pithos.web.client.Pithos::updateUploadFolder(Lcom/google/gwt/core/client/JsArrayString;)(uris);
					},
					
					Error: function(up, error) {
						if ($wnd.console && $wnd.console.log)
							$wnd.console.log("Error occured:" + error);
					}
				}
			});
			return $wnd.$('#uploader').pluploadQueue();
		};
		
		if ($wnd.console && $wnd.console.log)
			$wnd.console.log(uploader);
		if (!uploader) {
			uploader = createUploader();
		}
		else {
			var dropElm = $wnd.document.getElementById('rightPanel');
			$wnd.plupload.removeAllEvents(dropElm, uploader.id);
			var removeAll = true;
			var files = uploader.files;
			if ($wnd.console && $wnd.console.log)
				$wnd.console.log('About to check ' + files.length + ' files');
			for (var i=0; i<files.length; i++) {
				var f = files[i];
				if (f.status != $wnd.plupload.DONE && f.status != $wnd.plupload.FAILED) {
					removeAll = false;
					break;
				}
			}
			if (removeAll) {
				if ($wnd.console && $wnd.console.log)
					$wnd.console.log('About to remove ' + files.length + ' files');
				uploader.destroy();
				uploader = createUploader();
				if ($wnd.console && $wnd.console.log)
					$wnd.console.log(uploader);
				dlg.@gr.grnet.pithos.web.client.FileUploadDialog::setInProgress(Z)(false);
			}
			else {
				dlg.@gr.grnet.pithos.web.client.FileUploadDialog::setInProgress(Z)(true);
			}
		}
	}-*/;
	
	native boolean isUploading()/*-{
		var uploader = $wnd.$("#uploader").pluploadQueue();
		var files = uploader.files;
		for (var i=0; i<files.length; i++) {
			var f = files[i];
			if (f.status == $wnd.plupload.UPLOADING) {
				return true;
			}
		}
		return false;
	}-*/;
	
	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		super.onPreviewNativeEvent(event);

		NativeEvent evt = event.getNativeEvent();
		if (evt.getType().equals("keydown"))
			// Use the popup's key preview hooks to close the dialog when
			// escape is pressed.
			switch (evt.getKeyCode()) {
				case KeyCodes.KEY_ESCAPE:
					close();
					break;
			}
	}

	public void setFolder(Folder folder) {
		this.folder = folder;
		foldernameLabel.setText(folder.getName());
	}

	@Override
	public void center() {
		app.hideUploadIndicator();
		setVisible(true);
		setModal(true);
		super.center();
		setupUpload(this, app, app.getToken());
		super.center();
	}
	
	private void hideUploadIndicator() {
		app.hideUploadIndicator();
	}
	
	void close() {
		setVisible(false);
		setModal(false);
		clearUploader();
		if (isUploading())
			app.showUploadIndicator();
		setGlobalDropArea();
	}

	private native void clearUploader() /*-{
		var uploader = $wnd.$("#uploader").pluploadQueue();
		var files = uploader.files;
		while (files.length > 0)
			uploader.removeFile(files[0]);
	}-*/;
	
	native void setGlobalDropArea() /*-{
		var uploader = $wnd.$("#uploader").pluploadQueue();
		if (uploader.runtime == 'html5') {
			uploader.settings.drop_element = 'rightPanel';
			uploader.trigger('PostInit');
		}
	}-*/;

	private void setInProgress(boolean _inProgress) {
		inProgress = _inProgress;
		if (inProgress)
			close.setText("hide");
		else
			close.setText("close");
	}
}
