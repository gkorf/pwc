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

import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.rest.PostCommand;
import gr.grnet.pithos.web.client.rest.RestCommand;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FileResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.gears.client.Factory;
import com.google.gwt.gears.client.desktop.Desktop;
import com.google.gwt.gears.client.desktop.File;
import com.google.gwt.gears.client.desktop.OpenFilesHandler;
import com.google.gwt.gears.client.httprequest.HttpRequest;
import com.google.gwt.gears.client.httprequest.ProgressEvent;
import com.google.gwt.gears.client.httprequest.ProgressHandler;
import com.google.gwt.gears.client.httprequest.RequestCallback;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'File upload' dialog box implementation with Google Gears support.
 */
public class FileUploadGearsDialog extends FileUploadDialog {

	protected final Factory factory = Factory.getInstance();

	/**
	 * The array of files to upload.
	 */
	private File[] fileObjects;

	/**
	 * A list of files to upload, created from files array. Used to signal
	 * finished state when empty.
	 */
	protected List<File> selectedFiles = new ArrayList<File>();

	/**
	 * The list of progress bars for individual files.
	 */
	protected List<ProgressBar> progressBars = new ArrayList<ProgressBar>();

	private Button browse;

	private Button submit;

	private FlexTable generalTable;

	private Map<String, gr.grnet.pithos.web.client.foldertree.File> toRename;

	protected List<HttpRequest> requests = new ArrayList<HttpRequest>();
	
	private boolean canContinue = true;

    protected FileUploadGearsDialog() {
    }

	/**
	 * The widget's constructor.
	 */
	public FileUploadGearsDialog(GSS _app, Folder _folder) {
        this.folder = _folder;
        this.app = _app;
		// Set the dialog's caption.
		setText("File upload");
		setAnimationEnabled(true);
		// Create a panel to hold all of the dialog widgets.
		VerticalPanel panel = new VerticalPanel();
		final HTML info = new HTML("You may select one or more files to upload.");
		info.addStyleName("pithos-uploadNote");
		panel.add(info);
		// Add an informative label with the folder name.
		Object selection = GSS.get().getTreeView().getSelection();

		browse = new Button("Browse...");

		HorizontalPanel fileUploadPanel = new HorizontalPanel();
		fileUploadPanel.add(browse);

		generalTable = new FlexTable();
		generalTable.setText(0, 0, "Folder");
		generalTable.setText(1, 0, "File");
		generalTable.setText(0, 1, folder.getName());
		generalTable.setWidget(1, 1, fileUploadPanel);
		generalTable.getCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.setCellSpacing(4);

		panel.add(generalTable);

		// Create a panel to hold the buttons.
		HorizontalPanel buttons = new HorizontalPanel();

		submit = new Button("Upload");
		submit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				prepareAndSubmit();
			}
		});
		submit.setEnabled(false);
		buttons.add(submit);
		buttons.setCellHorizontalAlignment(submit, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				canContinue = false;				
				cancelUpload();				
				GSS.get().showFileList(true);
			}
		});
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("pithos-DialogBox");

		browse.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Desktop desktop = factory.createDesktop();
				desktop.openFiles(new OpenFilesHandler() {

					@Override
					public void onOpenFiles(OpenFilesEvent ofevent) {
						fileObjects = ofevent.getFiles();
						selectedFiles.addAll(Arrays.asList(fileObjects));
						for (int i = 0; i< selectedFiles.size(); i++) {
							generalTable.setText(i+1, 0, "File");
							generalTable.setText(i+1, 1, selectedFiles.get(i).getName());
							ProgressBar progress = new ProgressBar(20, 0);
							generalTable.setWidget(i+1, 2, progress);
							progressBars.add(progress);
							generalTable.getCellFormatter().setStyleName(i+1, 0, "props-labels");
							generalTable.getCellFormatter().setStyleName(i+1, 1, "props-values");
						}
						submit.setEnabled(true);
					}
				});
			}
		});

		panel.add(buttons);
		panel.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		panel.addStyleName("pithos-DialogBox");
		addStyleName("pithos-DialogBox");
		setWidget(panel);
	}

	/**
	 * Cancels the file upload.
	 */
	private void cancelUpload() {
		for (HttpRequest request: requests)
			request.abort();
		hide();		
	}

	@Override
	public void prepareAndSubmit() {
		GSS app = GSS.get();
		if (selectedFiles.size() == 0) {
			app.displayError("You must select a file!");
			hide();
			return;
		}
		submit.setEnabled(false);
		browse.setVisible(false);
		List<String> toUpdate = new ArrayList<String>();
		toRename = new HashMap<String, gr.grnet.pithos.web.client.foldertree.File>();
		for (File file: selectedFiles) {
			String fname = getFilename(file.getName());
			if (getFileForName(fname) == null) {
				// We are going to create a file, so we check to see if there is a
				// trashed file with the same name.
				gr.grnet.pithos.web.client.foldertree.File same = null;
				for (gr.grnet.pithos.web.client.foldertree.File fres : folder.getFiles())
					if (fres.isInTrash() && fres.getName().equals(fname))
						same = fres;
				// In that case add it to the list of files to rename.
				if (same != null)
					toRename.put(getBackupFilename(fname), same);
			} else
				// If we are updating a file add it to the list of files to update.
				toUpdate.add(fname);
		}

		if (!toUpdate.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			for (String name: toUpdate)
				sb.append(name).append("<br/>");
			// We are going to update existing files, so show a confirmation dialog.
			ConfirmationDialog confirm = new ConfirmationDialog("Are you sure " +
					"you want to update the following files?<br/><i>" + sb +
					"</i>", "Update") {

				@Override
				public void cancel() {
					hide();
				}

				@Override
				public void confirm() {
					confirmRename();
				}

			};
			confirm.center();
		} else
			confirmRename();
	}

	/**
	 * Confirm the renames of synonymous files already in the trash.
	 */
	private void confirmRename() {
		if (!toRename.isEmpty()) {
			StringBuffer sb = new StringBuffer();
			for (gr.grnet.pithos.web.client.foldertree.File file: toRename.values())
				sb.append(file.getName()).append("<br/>");
			ConfirmationDialog confirm = new ConfirmationDialog("Files " +
					"with the following names already exist in the trash. If" +
					" you continue,<br/>the trashed files will be renamed " +
					"automatically for you:<br/><i>" + sb + "</i>", "Continue") {

				@Override
				public void cancel() {
					hide();
				}

				@Override
				public void confirm() {
					updateTrashedFiles();
				}

			};
			confirm.center();
		} else
			uploadFiles();
	}

	/**
	 * Rename the conflicting trashed files with the supplied new names.
	 */
	private void updateTrashedFiles() {
		for (final String name: toRename.keySet()) {
			JSONObject json = new JSONObject();
			json.put("name", new JSONString(name));
			PostCommand cf = new PostCommand(toRename.get(name).getUri() + "?update=", json.toString(), 200) {

				@Override
				public void onComplete() {
					toRename.remove(name);
					uploadFiles();
				}

				@Override
				public void onError(Throwable t) {
					GSS app = GSS.get();
					GWT.log("", t);
					if (t instanceof RestException) {
						int statusCode = ((RestException) t).getHttpStatusCode();
						if (statusCode == 405)
							app.displayError("You don't have the necessary permissions");
						else if (statusCode == 404)
							app.displayError("User in permissions does not exist");
						else if (statusCode == 409)
							app.displayError("A file with the same name already exists");
						else if (statusCode == 413)
							app.displayError("Your quota has been exceeded");
						else
							app.displayError("Unable to modify file:" + ((RestException) t).getHttpStatusText());
					} else
						app.displayError("System error modifying file:" + t.getMessage());
				}

			};
			DeferredCommand.addCommand(cf);
		}
	}

	/**
	 * Checks if the renaming step for already trashed files is complete and
	 * starts file uploads.
	 */
	private void uploadFiles() {		
		if (!toRename.isEmpty()) return;
		if (canContinue){						
			doSend(selectedFiles);
		}
	}

	/**
	 * Perform the HTTP request to upload the specified file.
	 */
	protected void doSend(final List<File> filesRemaining) {
		final GSS app = GSS.get();
		HttpRequest request = factory.createHttpRequest();
		requests.add(request);
		String method = "PUT";

		String path;
		final String filename = getFilename(filesRemaining.get(0).getName());
		path = folder.getUri();
		if (!path.endsWith("/"))
			path = path + "/";
		path = path + encode(filename);

		String token = app.getToken();
		String resource = path.substring(app.getApiPath().length()-1, path.length());
		String date = RestCommand.getDate();
		String sig = RestCommand.calculateSig(method, date, resource, RestCommand.base64decode(token));
		request.open(method, path);
		request.setRequestHeader("X-GSS-Date", date);
		request.setRequestHeader("Authorization", app.getCurrentUserResource().getUsername() + " " + sig);
		request.setRequestHeader("Accept", "application/json; charset=utf-8");
		request.setCallback(new RequestCallback() {
			@Override
			public void onResponseReceived(HttpRequest req) {
				int state = req.getReadyState();
				if (state != 4) return;
				switch(req.getStatus()) {
					case 201: // Created falls through to updated.
					case 204:
						filesRemaining.remove(0);
						if(filesRemaining.isEmpty()){							
							finish();
							break;
						}						
						doSend(filesRemaining);				
						break;
					case 403:
						SessionExpiredDialog dlg = new SessionExpiredDialog();
						dlg.center();
						break;
					case 405:
						app.displayError("You don't have permission to " +
								"upload file " + filename);
						break;
					case 409:
						app.displayError("A folder with the name " + filename +
								" already exists at this level");
						break;
					case 413:
						app.displayError("There is not enough free space " +
								"available for uploading " + filename);
						break;
					default:
						app.displayError("Error uploading file " + filename +
									": " + req.getStatus());
				}
			}
		});
		request.getUpload().setProgressHandler(new ProgressHandler() {
			@Override
			public void onProgress(ProgressEvent event) {
				double pcnt = (double) event.getLoaded() / event.getTotal();
				progressBars.get(0).setProgress((int) Math.floor(pcnt * 100));
				if(pcnt*100 == 100)
					progressBars.remove(0);
			}
		});
		request.send(filesRemaining.get(0).getBlob());
	}

	/**
	 * Perform the final actions after the files are uploaded.
	 */
	protected void finish() {
		hide();
		//GSS.get().showFileList(true);
		GSS.get().getTreeView().updateNode(GSS.get().getTreeView().getSelection());//showFileList(true);
		GSS.get().getStatusPanel().updateStats();
	}

	/**
	 * Same as URL.encode, but also encode apostrophe since browsers aren't
	 * consistent about it (FF encodes, IE does not).
	 */
	protected String encode(String decodedURL) {
		String retv = decodedURL.replaceAll("@", "_"); // Replace bad character
		retv = URL.encodeComponent(retv);
		retv = retv.replaceAll("'", "%27");
		return retv;
	}

    protected String getBackupFilename(String filename) {
        List<gr.grnet.pithos.web.client.foldertree.File> filesInSameFolder = new ArrayList<gr.grnet.pithos.web.client.foldertree.File>();
        for (gr.grnet.pithos.web.client.foldertree.File deleted : folder.getFiles())
            if (deleted.isInTrash())
                filesInSameFolder.add(deleted);
        int i = 1;
        for (gr.grnet.pithos.web.client.foldertree.File same : filesInSameFolder)
            if (same.getName().startsWith(filename)) {
                String toCheck = same.getName().substring(filename.length(), same.getName().length());
                if (toCheck.startsWith(" ")) {
                    int test = -1;
                    try {
                        test = Integer.valueOf(toCheck.replace(" ", ""));
                    } catch (NumberFormatException e) {
                        // Do nothing since string is not a number.
                    }
                    if (test >= i)
                        i = test + 1;
                }
            }

        return filename + " " + i;
    }
}
