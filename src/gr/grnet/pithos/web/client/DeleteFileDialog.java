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

import gr.grnet.pithos.web.client.MessagePanel.Images;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.DeleteRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'delete file' dialog box.
 */
public class DeleteFileDialog extends DialogBox {

    private List<File> files;

    protected Pithos app;

	/**
	 * The widget's constructor.
	 *
	 * @param images the supplied images
	 */
	public DeleteFileDialog(Pithos _app, Images images, List<File> _files) {
        app = _app;
        files = _files;
		Anchor close = new Anchor();
		close.addStyleName("close");
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		// Set the dialog's caption.
		setText("Confirmation");
		setAnimationEnabled(true);
		setGlassEnabled(true);
		setStyleName("pithos-DialogBox");
		// Create a VerticalPanel to contain the label and the buttons.
		VerticalPanel outer = new VerticalPanel();
		outer.add(close);
		
		VerticalPanel inner = new VerticalPanel();
		inner.addStyleName("inner");

		HTML text;
		if (files.size() == 1)
			text = new HTML("<table><tr><td>" + AbstractImagePrototype.create(images.warn()).getHTML() + "</td><td>" + "Are you sure you want to <b>permanently</b> delete file '" + files.get(0).getName() + "'?</td></tr></table>");
		else
			text = new HTML("<table><tr><td>" + AbstractImagePrototype.create(images.warn()).getHTML() + "</td><td>" + "Are you sure you want to <b>permanently</b> delete the selected files?</td></tr></table>");
		text.setStyleName("pithos-warnMessage");
		inner.add(text);

		// Create the 'Delete' button, along with a listener that hides the dialog
		// when the button is clicked and deletes the file.
		Button ok = new Button("Delete", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteFiles();
				hide();
			}
		});
		ok.addStyleName("button");
		inner.add(ok);
		inner.setCellHorizontalAlignment(text, HasHorizontalAlignment.ALIGN_CENTER);
		outer.add(inner);
		outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);
		setWidget(outer);
	}

	/**
	 * Generate an RPC request to delete a file.
	 */
	protected void deleteFiles() {
        Iterator<File> iter = files.iterator();
        deleteFile(iter);
    }

	protected void deleteFile(final Iterator<File> iter) {
        if (iter.hasNext()) {
            File f = iter.next();
            String path = f.getUri();
            DeleteRequest deleteFile = new DeleteRequest(app.getApiPath(), f.getOwner(), URL.encode(path)) {
                @Override
                public void onSuccess(Resource result) {
                    deleteFile(iter);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
					app.setError(t);
                    if (t instanceof RestException) {
                        app.displayError("Unable to delete file: " + ((RestException) t).getHttpStatusText());
                    }
                    else
                        app.displayError("System error unable to delete file: "+t.getMessage());
                }

				@Override
				protected void onUnauthorized(Response response) {
					app.sessionExpired();
				}
            };
            deleteFile.setHeader("X-Auth-Token", app.getToken());
            Scheduler.get().scheduleDeferred(deleteFile);
        }
        else {
            app.updateFolder(files.get(0).getParent(), true, new Command() {
				
				@Override
				public void execute() {
					app.updateStatistics();
				}
			}, true);
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
					deleteFiles();
					break;
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}



}
