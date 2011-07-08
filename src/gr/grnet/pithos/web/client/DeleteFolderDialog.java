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
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Event;
import gr.grnet.pithos.web.client.MessagePanel.Images;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.DeleteCommand;
import gr.grnet.pithos.web.client.rest.DeleteRequest;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.RestRequestCallback;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;
import gr.grnet.pithos.web.client.rest.resource.TrashFolderResource;

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
import java.util.Iterator;
import java.util.List;

/**
 * The 'delete folder' dialog box.
 */
public class DeleteFolderDialog extends DialogBox {

    private GSS app;
    private Folder folder;
    
	/**
	 * The widget's constructor.
	 * @param images the supplied images
	 */
	public DeleteFolderDialog(GSS app, Images images, Folder folder) {
        this.app = app;
        this.folder = folder;
		// Set the dialog's caption.
		setText("Confirmation");
		setAnimationEnabled(true);
		// Create a VerticalPanel to contain the HTML label and the buttons.
		VerticalPanel outer = new VerticalPanel();
		HorizontalPanel buttons = new HorizontalPanel();

		HTML text = new HTML("<table><tr><td rowspan='2'>" + AbstractImagePrototype.create(images.warn()).getHTML() +
					"</td><td>" + "Are you sure you want to <b>permanently</b> delete folder '" + folder.getName() +
					"'?</td></tr></table>");
		text.setStyleName("pithos-warnMessage");
		outer.add(text);

		// Create the 'Delete' button, along with a listener that hides the dialog
		// when the button is clicked and deletes the folder.
		Button ok = new Button("Delete", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteFolder();
				hide();
			}
		});
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
	 * Generate an RPC request to delete a folder.
	 *
	 */
	private void deleteFolder() {
        String path = app.getApiPath() + app.getUsername() + "/" + folder.getContainer() + "?format=json&delimiter=/&prefix=" + folder.getPrefix();
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, path);
        builder.setHeader("If-Modified-Since", "0");
        builder.setHeader("X-Auth-Token", app.getToken());
        try {
            builder.sendRequest("", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        JSONValue json = JSONParser.parseStrict(response.getText());
                        JSONArray array = json.isArray();
                        int i = 0;
                        if (array != null) {
                            deleteObject(i, array);
                        }
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    GSS.get().displayError("System error unable to delete folder: " + exception.getMessage());
                }
            });
        }
        catch (RequestException e) {
        }
	}

    private void deleteObject(final int i, final JSONArray array) {
        if (i < array.size()) {
            JSONObject o = array.get(i).isObject();
            if (o != null && !o.containsKey("subdir")) {
                JSONString name = o.get("name").isString();
                String path = app.getApiPath() + app.getUsername() + "/" + folder.getContainer() + "/" + name.stringValue();
                DeleteRequest delete = new DeleteRequest(path) {
                    @Override
                    public void onSuccess(Resource result) {
                        deleteObject(i + 1, array);
                    }

                    @Override
                    public void onError(Throwable t) {
                        GWT.log("", t);
                        GSS.get().displayError("System error unable to delete folder: " + t.getMessage());
                    }
                };
                delete.setHeader("X-Auth-Token", app.getToken());
                Scheduler.get().scheduleDeferred(delete);
            }
            else {
                String subdir = o.get("subdir").isString().stringValue();
                subdir = subdir.substring(0, subdir.length() - 1);
                String path = app.getApiPath() + app.getUsername() + "/" + folder.getContainer() + "?format=json&delimiter=/&prefix=" + subdir;
                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, path);
                builder.setHeader("If-Modified-Since", "0");
                builder.setHeader("X-Auth-Token", app.getToken());
                try {
                    builder.sendRequest("", new RequestCallback() {
                        @Override
                        public void onResponseReceived(Request request, Response response) {
                            if (response.getStatusCode() == Response.SC_OK) {
                                JSONValue json = JSONParser.parseStrict(response.getText());
                                JSONArray array2 = json.isArray();
                                if (array2 != null) {
                                    int l = array.size();
                                    for (int j=0; j<array2.size(); j++) {
                                        array.set(l++, array2.get(j));
                                    }
                                }
                                deleteObject(i + 1, array);
                            }
                        }

                        @Override
                        public void onError(Request request, Throwable exception) {
                            GSS.get().displayError("System error unable to delete folder: " + exception.getMessage());
                        }
                    });
                }
                catch (RequestException e) {
                }
            }
        }
        else {
            String prefix = folder.getPrefix();
            String path = app.getApiPath() + app.getUsername() + "/" + folder.getContainer() + (prefix.length() == 0 ? "" : "/" + prefix);
            DeleteRequest deleteFolder = new DeleteRequest(path) {
                @Override
                public void onSuccess(Resource result) {
                    app.updateFolder(folder.getParent());
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
                    if (t instanceof RestException) {
                        app.displayError("Unable to delete folder: "+((RestException) t).getHttpStatusText());
                    }
                    else
                        GSS.get().displayError("System error unable to delete folder: " + t.getMessage());
                }
            };
            deleteFolder.setHeader("X-Auth-Token", app.getToken());
            Scheduler.get().scheduleDeferred(deleteFolder);
        }
    }

	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
		super.onPreviewNativeEvent(preview);

		NativeEvent evt = preview.getNativeEvent();
		if (evt.getType().equals(KeyDownEvent.getType().getName()))
			// Use the popup's key preview hooks to close the dialog when either
			// enter or escape is pressed.
			switch (evt.getKeyCode()) {
				case KeyCodes.KEY_ENTER:
					hide();
					deleteFolder();
					break;
				case KeyCodes.KEY_ESCAPE:
					hide();
					break;
			}
	}

}
