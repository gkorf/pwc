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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.rest.HeadRequest;
import gr.grnet.pithos.web.client.rest.PostRequest;

import java.util.Map;

/**
 * The 'File properties' dialog box implementation.
 *
 */
public class FilePermissionsDialog extends AbstractPropertiesDialog {

	protected PermissionsList permList;

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

		@Source("gr/grnet/pithos/resources/delete.gif")
		ImageResource delete();
	}

	final File file;

    FileShareDialog.PrivateSharingImages images = GWT.create(FileShareDialog.PrivateSharingImages.class);

	/**
	 * The widget's constructor.
	 */
	public FilePermissionsDialog(Pithos _app, File _file) {
        super(_app);
        file = _file;

		Anchor close = new Anchor("close");
		close.addStyleName("close");
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		// Set the dialog's caption.
		setText("File permissions");
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

		final Button ok = new Button("OK", new ClickHandler() {
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

        permList = new PermissionsList(app, images, file.getPermissions(), file.getOwnerID(), false, new Command() {
			
			@Override
			public void execute() {
				accept();
			}
		});
        permPanel.add(permList);

        HorizontalPanel permButtons = new HorizontalPanel();
        final Button addUser = new Button("Add User", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PermissionsAddDialog dlg = new PermissionsAddDialog(app, app.getAccount().getGroups(), permList, true);
                dlg.center();
                permList.updatePermissionTable();
            }
        });
        addUser.addStyleName("button");
        permButtons.add(addUser);
        permButtons.setCellHorizontalAlignment(addUser, HasHorizontalAlignment.ALIGN_CENTER);

        Button add = new Button("Add Group", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	if (app.getAccount().getGroups().isEmpty()) {
                    new GroupCreateDialog(app, new Command() {
						
						@Override
						public void execute() {
			            	if (app.getAccount().getGroups().isEmpty())
			            		return;
			                PermissionsAddDialog dlg = new PermissionsAddDialog(app, app.getAccount().getGroups(), permList, false);
			                dlg.center();
			                permList.updatePermissionTable();
						}
					}).center();
            	}
            	else {
	                PermissionsAddDialog dlg = new PermissionsAddDialog(app, app.getAccount().getGroups(), permList, false);
	                dlg.center();
	                permList.updatePermissionTable();
            	}
            }
        });
        add.addStyleName("button");
        permButtons.add(add);
        permButtons.setCellHorizontalAlignment(add, HasHorizontalAlignment.ALIGN_CENTER);

        permButtons.setSpacing(8);
        permButtons.addStyleName("pithos-TabPanelBottom");
        permPanel.add(permButtons);

        final Label readForAllNote = new Label("When this option is enabled, the file will be readable" +
                    " by everyone. By checking this option, you are certifying that you have the right to " +
                    "distribute this file and that it does not violate the Terms of Use.", true);
        readForAllNote.setStylePrimaryName("pithos-readForAllNote");

        pathPanel = new HorizontalPanel();
        pathPanel.setVisible(false);
        pathPanel.setWidth(Const.PERCENT_100);
        pathPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        pathPanel.add(new Label("Link"));
        pathPanel.setSpacing(8);
        pathPanel.addStyleName("pithos-TabPanelBottom");

        path = new TextBox();
        path.setWidth(Const.PERCENT_100);
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
        path.setWidth(Const.PERCENT_100);
        path.setReadOnly(true);
        pathPanel.add(path);
        permPanel.add(pathPanel);

        Scheduler.get().scheduleDeferred(new Command() {
			
			@Override
			public void execute() {
				showLinkIfShared();
			}
		});
        return permPanel;
    }

    void showLinkIfShared() {
		if (file.isShared()) {
			UrlBuilder b = Window.Location.createUrlBuilder();
			b.setPath(Pithos.getStorageAPIURL() + file.getOwnerID() + file.getUri());
			path.setText(b.buildString());
	        pathPanel.setVisible(true);
		}
		else {
			pathPanel.setVisible(false);
		}
    }
	/**
	 * Accepts any change and updates the file
	 *
	 */
	@Override
	protected boolean accept() {
        updateMetaData(
            Pithos.getStorageAPIURL(),
            app.getUserID(),
            file.getUri() + Const.QUESTION_MARK_UPDATE_EQ,
            permList.getPermissions()
        );
        return true;
	}

	protected void updateMetaData(String api, String owner, final String path, final Map<String, Boolean[]> newPermissions) {
        if (newPermissions != null) {
            PostRequest updateFile = new PostRequest(api, owner, path) {
                @Override
                public void onSuccess(Resource result) {
                	HeadRequest<File> headFile = new HeadRequest<File>(File.class, Pithos.getStorageAPIURL(), file.getOwnerID(), path, file) {

						@Override
						public void onSuccess(File _result) {
							showLinkIfShared();
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
							app.setError(t);
		                    app.displayError("System error modifying file:" + t.getMessage());
						}

						@Override
						protected void onUnauthorized(Response response) {
							app.sessionExpired();
						}
					};
					headFile.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());
					Scheduler.get().scheduleDeferred(headFile);
                }

                @Override
                public void onError(Throwable t) {
					app.setError(t);
                    app.displayError("System error modifying file:" + t.getMessage());
                }

				@Override
				protected void onUnauthorized(Response response) {
					app.sessionExpired();
				}
            };
            updateFile.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());
            
            String readPermHeader = Const.READ_EQ;
            String writePermHeader = Const.WRITE_EQ;
            for (String u : newPermissions.keySet()) {
                Boolean[] p = newPermissions.get(u);
                if (p[0] != null && p[0]) {
                    readPermHeader += u + Const.COMMA;
                }
                if (p[1] != null && p[1]) {
                    writePermHeader += u + Const.COMMA;
                }
            }
            if (readPermHeader.endsWith(Const.EQ)) {
                readPermHeader = "";
            }
            else if (readPermHeader.endsWith(Const.COMMA)) {
                readPermHeader = readPermHeader.substring(0, readPermHeader.length() - 1);
            }
            if (writePermHeader.endsWith(Const.EQ)) {
                writePermHeader = "";
            }
            else if (writePermHeader.endsWith(Const.COMMA)) {
                writePermHeader = writePermHeader.substring(0, writePermHeader.length() - 1);
            }
            String permHeader = readPermHeader +
                ((readPermHeader.length()  > 0 && writePermHeader.length() > 0) ?  Const.SEMI : "") + writePermHeader;
            if (permHeader.length() == 0) {
                permHeader=Const.TILDE;
            }
            else {
            	permHeader = URL.encodePathSegment(permHeader);
            }
            updateFile.setHeader(Const.X_OBJECT_SHARING, permHeader);
            Scheduler.get().scheduleDeferred(updateFile);
        }
        else if (!app.isMySharedSelected()) {
            app.updateFolder(file.getParent(), true, new Command() {
				@Override
				public void execute() {
					if (file.isSharedOrPublished())
						app.updateMySharedRoot();
				}
			}, true);
        }
        else {
        	app.updateSharedFolder(file.getParent(), true);
        }
    }

	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
	    super.onPreviewNativeEvent(preview);

	    NativeEvent evt = preview.getNativeEvent();
	    if (evt.getType().equals(Const.EVENT_TYPE_KEYDOWN) &&
            evt.getKeyCode() == KeyCodes.KEY_ENTER) {

            closeDialog();
        }
	}
}
