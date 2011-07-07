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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.Response;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.GetCommand;
import gr.grnet.pithos.web.client.rest.PostCommand;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestCommand;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;
import gr.grnet.pithos.web.client.rest.resource.UploadStatusResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import javax.xml.transform.Templates;

/**
 * The 'File upload' dialog box implementation.
 */
public class FileUploadDialog extends DialogBox {

    public static final boolean DONE = true;

	/**
	 * The Form element that performs the file upload.
	 */
	private final FormPanel form = new FormPanel();

	private final FileUpload upload = new FileUpload();

	private final Label filenameLabel = new Label();

    private final Label foldernameLabel = new Label();

    private Button submit;

	protected Folder folder;

    protected GSS app;

	/**
	 * The widget's constructor.
	 */
	public FileUploadDialog() {
		// Set the dialog's caption.
		setText("File upload");
		setAnimationEnabled(true);
		// Since we're going to add a FileUpload widget, we'll need to set the
		// form to use the POST method, and multipart MIME encoding.
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);

		// Create a panel to hold all of the form widgets.
		VerticalPanel panel = new VerticalPanel();
		form.setWidget(panel);
		final HTML info = new HTML("You may select a file to upload. Install" +
				" <a href='http://gears.google.com/' target='_blank'>Google " +
				"Gears</a><br> for uploading multiple files simultaneously.");
		info.addStyleName("pithos-uploadNote");
		panel.add(info);

        final Hidden auth = new Hidden("X-Auth-Token", "");
        panel.add(auth);
		upload.setName("file");
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

		panel.add(generalTable);

		// Create a panel to hold the buttons.
		HorizontalPanel buttons = new HorizontalPanel();

		// Create the 'upload' button, along with a listener that submits the
		// form.
		submit = new Button("Upload", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				prepareAndSubmit();
			}
		});
		buttons.add(submit);
		buttons.setCellHorizontalAlignment(submit, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		final Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("pithos-DialogBox");
        panel.add(buttons);
        panel.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);

		// Add an event handler to the form.
		form.addSubmitHandler(new SubmitHandler() {

			@Override
			public void onSubmit(SubmitEvent event) {
                auth.setValue(app.getToken()); //This is done here because the app object is not available in the constructor
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
				if (results != null && !results.equalsIgnoreCase("<pre></pre>")) {
					GWT.log(results, null);
					app.displayError(results);
				}
                app.updateFolder(folder);
				hide();
			}
		});


		panel.addStyleName("pithos-DialogBox");
		addStyleName("pithos-DialogBox");
		setWidget(form);
	}

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
        String apath = app.getApiPath() + app.getUsername() + folder.getUri() + "/" + fname;
        form.setAction(apath);
        submit.setEnabled(false);
        upload.setVisible(false);
        filenameLabel.setText(fname);
        filenameLabel.setVisible(true);

		if (getFileForName(fname) == null) {
			doUpload(apath);
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

    private void doUpload(String path) {
        PutRequest createFile = new PutRequest(path) {
            @Override
            public void onSuccess(Resource result) {
                form.submit();
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("", t);
                if (t instanceof RestException) {
                    app.displayError("Unable to create file:" + ((RestException) t).getHttpStatusText());
                }
                else
                    app.displayError("System error creating file:" + t.getMessage());
            }
        };
        createFile.setHeader("X-Auth-Token", app.getToken());
        createFile.setHeader("Content-Length", "0");
        Scheduler.get().scheduleDeferred(createFile);
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
			if (!f.isInTrash() && f.getName().equals(name))
				return f;
		return null;
	}

    public void setApp(GSS app) {
        this.app = app;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
        foldernameLabel.setText(folder.getName());
    }
}
