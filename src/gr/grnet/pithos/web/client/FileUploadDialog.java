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

import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
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

	/**
	 * The widget's constructor.
	 */
	public FileUploadDialog() {
		Anchor close = new Anchor();
		close.addStyleName("close");
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		// Set the dialog's caption.
		setText("File upload");
		setAnimationEnabled(true);
		setGlassEnabled(true);
		setStyleName("pithos-DialogBox");
		// Since we're going to add a FileUpload widget, we'll need to set the
		// form to use the POST method, and multipart MIME encoding.
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);

		// Create a panel to hold all of the form widgets.
		VerticalPanel panel = new VerticalPanel();
		panel.add(close);
		form.setWidget(panel);

		VerticalPanel inner = new VerticalPanel();
		inner.addStyleName("inner");

        final Hidden auth = new Hidden("X-Auth-Token");
        inner.add(auth);
		upload.setName("X-Object-Data");
		filenameLabel.setText("");
		filenameLabel.setVisible(false);
		filenameLabel.setStyleName("props-labels");
		HorizontalPanel fileUploadPanel = new HorizontalPanel();
		fileUploadPanel.add(filenameLabel);
		fileUploadPanel.add(upload);
		Grid generalTable = new Grid(2, 2);
		generalTable.setText(0, 0, "Folder");
        generalTable.setWidget(0, 1, foldernameLabel);
		generalTable.setText(1, 0, "File");
		generalTable.setWidget(1, 1, fileUploadPanel);
		generalTable.getCellFormatter().setStyleName(0, 0, "props-labels");
        generalTable.getCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.setCellSpacing(4);

		inner.add(generalTable);

		// Create the 'upload' button, along with a listener that submits the
		// form.
		submit = new Button("Upload", new ClickHandler() {
			@Override
			public void onClick(@SuppressWarnings("unused") ClickEvent event) {
				prepareAndSubmit();
			}
		});
		submit.addStyleName("button");
		inner.add(submit);

		// Add an event handler to the form.
		form.addSubmitHandler(new SubmitHandler() {

			@Override
			public void onSubmit(@SuppressWarnings("unused") SubmitEvent event) {
                auth.setValue(app.getToken()); //This is done here because the app object is not available in the constructor
                Cookies.setCookie("X-Auth-Token", app.getToken(), null, "", "/", false);
			}
		});
		form.addSubmitCompleteHandler(new SubmitCompleteHandler() {

			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				// When the form submission is successfully completed, this
				// event is fired. Assuming the service returned a response
				// of type text/html, we can get the result text here (see
				// the FormPanel documentation for further explanation).
				String results = event.getResults();

				// Unfortunately the results are never empty, even in
				// the absense of errors, so we have to check for '<pre></pre>'.
				if (results != null && results.length() > 0 && !results.equalsIgnoreCase("<pre></pre>")) {
					GWT.log(results, null);
					app.displayError(results);
				}
				if (app.getSelectedTree().equals(app.getFolderTreeView()))
					app.updateFolder(folder, true, new Command() {
						
						@Override
						public void execute() {
							app.updateStatistics();
						}
					});
				else
					app.updateOtherSharedFolder(folder, true);
				hide();
			}
		});

		FlowPanel uploader = new FlowPanel();
		uploader.getElement().setId("uploader");
		inner.add(uploader);
		
		panel.add(inner);
		panel.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);
		
		
		Scheduler.get().scheduleDeferred(new Command() {
			
			@Override
			public void execute() {
				String path = app.getApiPath() + folder.getOwner() + folder.getUri();
				setupUpload(path, app.getToken());
			}
		});
		setWidget(form);
	}

	native void setupUpload(String path, String token) /*-{
		$wnd.$("#uploader").pluploadQueue({
			// General settings
			runtimes : 'html5, flash, gears, silverlight, browserplus',
			url : 'upload.php',
			max_file_size : '10mb',
			chunk_size : '1mb',
			unique_names : true,
	
			// Resize images on clientside if we can
			resize : {width : 320, height : 240, quality : 90},
	
			// Flash settings
			flash_swf_url : 'plupload/js/plupload.flash.swf',
	
			// Silverlight settings
			silverlight_xap_url : 'plupload/js/plupload.silverlight.xap',
			
			preinit: {
				UploadFile: function(up, file) {
					up.settings.url = path + "/" + file.name + "?X-Auth-Token=" + token;
					up.settings.file_data_name = "X-Object-Data";
				}
			}
		});
	
		// Client side form validation
		$wnd.$('form').submit(function(e) {
	        var uploader = $wnd.$('#uploader').pluploadQueue();
	
	        // Files in queue upload them first
	        if (uploader.files.length > 0) {
	            // When all files are uploaded submit form
	            uploader.bind('StateChanged', function() {
	                if (uploader.files.length === (uploader.total.uploaded + uploader.total.failed)) {
	                    $wnd.$('form')[0].submit();
	                }
	            });
	                
	            uploader.start();
	        } else {
	            alert('You must queue at least one file.');
	        }
	
	        return false;
	    });
	}-*/;
	
	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
		super.onPreviewNativeEvent(preview);

		NativeEvent evt = preview.getNativeEvent();
		if (evt.getType().equals("keydown"))
			// Use the popup's key preview hooks to close the dialog when either
			// enter or escape is pressed.
			switch (evt.getKeyCode()) {
				case KeyCodes.KEY_ENTER:
					prepareAndSubmit();
					break;
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}

	/**
	 * Make any last minute checks and start the upload.
	 */
	protected void prepareAndSubmit() {
        if (upload.getFilename().length() == 0) {
            app.displayError("You must select a file!");
            return;
        }
        final String fname = getFilename(upload.getFilename());
        String apath = app.getApiPath() + folder.getOwner() + folder.getUri() + "/" + fname + "?X-Auth-Token=" + app.getToken();
        form.setAction(apath);
        submit.setEnabled(false);
        upload.setVisible(false);
        filenameLabel.setText(fname);
        filenameLabel.setVisible(true);

		if (getFileForName(fname) == null) {
            form.submit();
		}
		else {
			// We are going to update an existing file, so show a confirmation dialog.
			ConfirmationDialog confirm = new ConfirmationDialog("Are you sure " +
					"you want to update " + fname + "?", "Update") {

				@Override
				public void cancel() {
					FileUploadDialog.this.hide();
				}

				@Override
				public void confirm() {
					form.submit();
				}

			};
			confirm.center();
		}
	}

    /**
	 * Returns the file name from a potential full path argument. Apparently IE
	 * insists on sending the full path name of a file when uploading, forcing
	 * us to trim the extra path info. Since this is only observed on Windows we
	 * get to check for a single path separator value.
	 *
	 * @param name the potentially full path name of a file
	 * @return the file name without extra path information
	 */
	protected String getFilename(String name) {
		int pathSepIndex = name.lastIndexOf("\\");
		if (pathSepIndex == -1) {
			pathSepIndex = name.lastIndexOf("/");
			if (pathSepIndex == -1)
				return name;
		}
		return name.substring(pathSepIndex + 1);
	}

	protected File getFileForName(String name){
		for (File f : folder.getFiles())
			if (f.getName().equals(name))
				return f;
		return null;
	}

    public void setApp(Pithos app) {
        this.app = app;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
        foldernameLabel.setText(folder.getName());
    }
}
