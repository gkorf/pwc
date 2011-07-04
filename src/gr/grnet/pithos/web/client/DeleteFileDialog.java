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

import gr.grnet.pithos.web.client.MessagePanel.Images;
import gr.grnet.pithos.web.client.rest.DeleteCommand;
import gr.grnet.pithos.web.client.rest.MultipleDeleteCommand;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FileResource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'delete file' dialog box.
 */
public class DeleteFileDialog extends DialogBox {

	/**
	 * The widget's constructor.
	 *
	 * @param images the supplied images
	 */
	public DeleteFileDialog(Images images) {
		// Set the dialog's caption.
		setText("Confirmation");
		setAnimationEnabled(true);
		Object selection = GSS.get().getCurrentSelection();
		// Create a VerticalPanel to contain the label and the buttons.
		VerticalPanel outer = new VerticalPanel();
		HorizontalPanel buttons = new HorizontalPanel();

		HTML text;
		if (selection instanceof FileResource)
			text = new HTML("<table><tr><td>" + AbstractImagePrototype.create(images.warn()).getHTML() + "</td><td>" + "Are you sure you want to <b>permanently</b> delete file '" + ((FileResource) selection).getName() + "'?</td></tr></table>");
		else
			text = new HTML("<table><tr><td>" + AbstractImagePrototype.create(images.warn()).getHTML() + "</td><td>" + "Are you sure you want to <b>permanently</b> delete the selected files?</td></tr></table>");
		text.setStyleName("pithos-warnMessage");
		outer.add(text);

		// Create the 'Delete' button, along with a listener that hides the dialog
		// when the button is clicked and deletes the file.
		Button ok = new Button("Delete", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteFile();
				hide();
			}
		});
		ok.getElement().setId("confirmation.ok");
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		cancel.getElement().setId("confirmation.cancel");
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.setStyleName("pithos-warnMessage");
		outer.setStyleName("pithos-warnMessage");
		outer.add(buttons);
		outer.setCellHorizontalAlignment(text, HasHorizontalAlignment.ALIGN_CENTER);
		outer.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		setWidget(outer);
	}

	/**
	 * Generate an RPC request to delete a file.
	 *
	 * @param userId the ID of the current user
	 */
	private void deleteFile() {
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null) {
			GSS.get().displayError("No file was selected");
			return;
		}
		if (selection instanceof FileResource) {
			FileResource file = (FileResource) selection;

			DeleteCommand df = new DeleteCommand(file.getUri()){

				@Override
				public void onComplete() {
					GSS.get().getTreeView().updateNode(GSS.get().getTreeView().getSelection());
					GSS.get().getStatusPanel().updateStats();
				}

				@Override
				public void onError(Throwable t) {
					GWT.log("", t);
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						if(statusCode == 405)
							GSS.get().displayError("You don't have the necessary permissions");
						else if(statusCode == 404)
							GSS.get().displayError("File not found");
						else
							GSS.get().displayError("Unable to delete file: "+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error unable to delete file: "+t.getMessage());
				}
			};

			DeferredCommand.addCommand(df);
		}
		else if(selection instanceof List){
			List<FileResource> files = (List<FileResource>) selection;
			List<String> fileIds = new ArrayList<String>();
			for(FileResource f : files)
				fileIds.add(f.getUri());

			MultipleDeleteCommand ed = new MultipleDeleteCommand(fileIds.toArray(new String[0])){

				@Override
				public void onComplete() {
					GSS.get().getTreeView().updateNode(GSS.get().getTreeView().getSelection());
				}

				@Override
				public void onError(Throwable t) {
					GWT.log("", t);
					GSS.get().getTreeView().updateNode(GSS.get().getTreeView().getSelection());
				}

				@Override
				public void onError(String path, Throwable t) {
					GWT.log("", t);
					if(t instanceof RestException){
						int statusCode = ((RestException)t).getHttpStatusCode();
						if(statusCode == 405)
							GSS.get().displayError("You don't have the necessary permissions");
						else if(statusCode == 404)
							GSS.get().displayError("File not found");
						else
							GSS.get().displayError("Unable to delete file:"+((RestException)t).getHttpStatusText());
					}
					else
						GSS.get().displayError("System error unable to delete file:"+t.getMessage());

				}
			};

			DeferredCommand.addCommand(ed);
		}
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
					hide();
					deleteFile();
					break;
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}



}
