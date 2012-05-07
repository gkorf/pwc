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

import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.HeadRequest;
import gr.grnet.pithos.web.client.rest.PostRequest;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'File properties' dialog box implementation.
 *
 */
public class FilePublishDialog extends AbstractPropertiesDialog {

	protected CheckBox readForAll;
	
	private HorizontalPanel pathPanel;
	
	private TextBox path;
	
	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends MessagePanel.Images {

		@Source("gr/grnet/pithos/resources/edit_user.png")
		ImageResource permUser();

		@Source("gr/grnet/pithos/resources/groups22.png")
		ImageResource permGroup();

		@Source("gr/grnet/pithos/resources/editdelete.png")
		ImageResource delete();
	}

	final File file;

    Images images = GWT.create(Images.class);

	/**
	 * The widget's constructor.
	 */
	public FilePublishDialog(Pithos _app, File _file) {
        super(_app);
        file = _file;

		Anchor close = new Anchor();
		close.addStyleName("close");
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		// Set the dialog's caption.
		setText("Publish/Un-publish");
		setAnimationEnabled(true);
		setGlassEnabled(true);
		setStyleName("pithos-DialogBox");

		// Outer contains inner and buttons.
		final VerticalPanel outer = new VerticalPanel();
		outer.add(close);
		final FocusPanel focusPanel = new FocusPanel(outer);
		// Inner contains generalPanel and permPanel.
		inner = new VerticalPanel();
		inner.addStyleName("inner");

        inner.add(createSharingPanel());

        outer.add(inner);

		final Button ok = new Button("Close", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				closeDialog();
			}
		});
		ok.addStyleName("button");

        outer.add(ok);
        outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);

        focusPanel.setFocus(true);
        setWidget(outer);
	}

    private VerticalPanel createSharingPanel() {
        VerticalPanel permPanel = new VerticalPanel();

        final Label readForAllNote = new Label("When this option is enabled, the file will be readable" +
                    " by everyone. By checking this option, you are certifying that you have the right to " +
                    "distribute this file and that it does not violate the Terms of Use.", true);
        readForAllNote.setStylePrimaryName("pithos-readForAllNote");

        readForAll = new CheckBox();
        readForAll.setValue(file.isPublished());
        readForAll.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	accept();
            }
        });

        // Only show the read for all permission if the user is the owner.
        if (file.getOwner().equals(app.getUsername())) {
            final HorizontalPanel permForAll = new HorizontalPanel();
            permForAll.add(new Label("Public"));
            permForAll.add(readForAll);
            permForAll.setSpacing(8);
            permForAll.addStyleName("pithos-TabPanelBottom");
            permForAll.add(readForAllNote);
            permPanel.add(permForAll);
        }

        pathPanel = new HorizontalPanel();
        pathPanel.setVisible(false);
        pathPanel.setWidth("100%");
        pathPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        pathPanel.add(new Label("Link"));
        pathPanel.setSpacing(8);
        pathPanel.addStyleName("pithos-TabPanelBottom");

        path = new TextBox();
        path.setWidth("100%");
        path.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Pithos.enableIESelection();
                ((TextBox) event.getSource()).selectAll();
                Pithos.preventIESelection();
            }
        });
        path.setText(Window.Location.getHost() + file.getPublicUri());
        path.setTitle("Use this link for sharing the file via e-mail, IM, etc. (crtl-C/cmd-C to copy to system clipboard)");
        path.setWidth("100%");
        path.setReadOnly(true);
        pathPanel.add(path);
        permPanel.add(pathPanel);

        Scheduler.get().scheduleDeferred(new Command() {
			
			@Override
			public void execute() {
				showLinkIfPublished();
			}
		});
        return permPanel;
    }

    void showLinkIfPublished() {
		if (file.isPublished()) {
			UrlBuilder b = Window.Location.createUrlBuilder();
			b.setPath(file.getPublicUri());
			path.setText(b.buildString());
	        pathPanel.setVisible(true);
		}
		else {
			pathPanel.setVisible(false);
		}
    }
	/**
	 * Accepts any change and updates the file
	 * @return 
	 *
	 */
	@Override
	protected boolean accept() {
        Boolean published = null;
		if (readForAll.getValue() != file.isPublished())
			if (file.getOwner().equals(app.getUsername()))
                published = readForAll.getValue();
        updateMetaData(app.getApiPath(), app.getUsername(), file.getUri() + "?update=", published);
        return true;
	}

	protected void updateMetaData(String api, String owner, final String path, final Boolean published) {
        if (published != null) {
            PostRequest updateFile = new PostRequest(api, owner, path) {
                @Override
                public void onSuccess(Resource result) {
                	HeadRequest<File> headFile = new HeadRequest<File>(File.class, app.getApiPath(), file.getOwner(), path, file) {

						@Override
						public void onSuccess(File _result) {
							showLinkIfPublished();
							if (!app.isMySharedSelected())
			                    app.updateFolder(file.getParent(), true, new Command() {
									
									@Override
									public void execute() {
										app.updateMySharedRoot();
									}
								}, true);
							else
								app.updateSharedFolder(file.getParent(), true);
						}

						@Override
						public void onError(Throwable t) {
		                    GWT.log("", t);
							app.setError(t);
		                    app.displayError("System error modifying file:" + t.getMessage());
						}

						@Override
						protected void onUnauthorized(Response response) {
							app.sessionExpired();
						}
					};
					headFile.setHeader("X-Auth-Token", app.getToken());
					Scheduler.get().scheduleDeferred(headFile);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
					app.setError(t);
                    app.displayError("System error modifying file:" + t.getMessage());
                }

				@Override
				protected void onUnauthorized(Response response) {
					app.sessionExpired();
				}
            };
            updateFile.setHeader("X-Auth-Token", app.getToken());
            updateFile.setHeader("X-Object-Public", published.toString());
            Scheduler.get().scheduleDeferred(updateFile);
        }
        else if (!app.isMySharedSelected())
            app.updateFolder(file.getParent(), true, new Command() {
				
				@Override
				public void execute() {
					if (file.isSharedOrPublished())
						app.updateMySharedRoot();
				}
			}, true);
        else
        	app.updateSharedFolder(file.getParent(), true);
    }

	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
	    super.onPreviewNativeEvent(preview);

	    NativeEvent evt = preview.getNativeEvent();
	    if (evt.getType().equals("keydown") && evt.getKeyCode() == KeyCodes.KEY_ENTER)
			        closeDialog();
	}
}
