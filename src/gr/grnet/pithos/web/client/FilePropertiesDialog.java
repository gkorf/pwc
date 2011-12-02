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
import gr.grnet.pithos.web.client.foldertree.FileVersions;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.foldertree.Version;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.PostRequest;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.tagtree.Tag;

import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
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
public class FilePropertiesDialog extends AbstractPropertiesDialog {

	protected PermissionsList permList;

	protected CheckBox readForAll;

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

		@Source("gr/grnet/pithos/resources/db_update.png")
		ImageResource restore();

		@Source("gr/grnet/pithos/resources/folder_inbox.png")
		ImageResource download();
	}

	/**
	 * The widget that holds the name of the file.
	 */
	private TextBox name = new TextBox();

	final File file;

    Images images = GWT.create(Images.class);

	/**
	 * The widget's constructor.
	 */
	public FilePropertiesDialog(Pithos _app, File _file) {
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
		setText("File properties");
		setAnimationEnabled(true);
		setGlassEnabled(true);
		setStyleName("pithos-DialogBox");

		// Outer contains inner and buttons.
		final VerticalPanel outer = new VerticalPanel();
		outer.add(close);
		final FocusPanel focusPanel = new FocusPanel(outer);
		// Inner contains generalPanel and permPanel.
		inner = new DecoratedTabPanel();
		inner.setAnimationEnabled(true);
		inner.addStyleName("inner");
		inner.getDeckPanel().addStyleName("pithos-TabPanelBottom");


        inner.add(createGeneralPanel(), "General");

        inner.add(createSharingPanel(), "Sharing");

		fetchVersions();
			
        inner.selectTab(0);

        outer.add(inner);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		final Button ok = new Button("OK", new ClickHandler() {
			@Override
			public void onClick(@SuppressWarnings("unused") ClickEvent event) {
				accept();
				closeDialog();
			}
		});
		ok.addStyleName("button");

        outer.add(ok);
        outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);

        focusPanel.setFocus(true);
        setWidget(outer);
	}

    protected void fetchVersions() {
    	String path = file.getUri() + "?format=json&version=list";
    	GetRequest<FileVersions> getVersions = new GetRequest<FileVersions>(FileVersions.class, app.getApiPath(), file.getOwner(), path) {

			@Override
			public void onSuccess(FileVersions _result) {
		        inner.add(createVersionPanel(_result.getVersions()), "Versions");
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
                if (t instanceof RestException) {
                    app.displayError("Unable to fetch versions: " + ((RestException) t).getHttpStatusText());
                }
                else
                    app.displayError("System error unable to fetch versions: "+t.getMessage());
			}

			@Override
			protected void onUnauthorized(Response response) {
				app.sessionExpired();
			}
		};
		getVersions.setHeader("X-Auth-Token", app.getToken());
		Scheduler.get().scheduleDeferred(getVersions);
	}

	private VerticalPanel createGeneralPanel() {
        final VerticalPanel generalPanel = new VerticalPanel();
        final FlexTable generalTable = new FlexTable();
        generalTable.setText(0, 0, "Name");
        generalTable.setText(1, 0, "Folder");
        generalTable.setText(2, 0, "Owner");
        generalTable.setText(3, 0, "Last modified");
        generalTable.setText(4, 0, "Tags");

        name.setWidth("100%");
        name.setText(file.getName());
        generalTable.setWidget(0, 1, name);
        if(file.getParent() != null)
            generalTable.setText(1, 1, file.getParent().getName());
        else
            generalTable.setText(1, 1, "-");
        generalTable.setText(2, 1, file.getOwner());

        final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
        generalTable.setText(3, 1, file.getLastModified() != null ? formatter.format(file.getLastModified()) : "");

		StringBuffer tagsBuffer = new StringBuffer();
        for (String t : file.getTags())
			tagsBuffer.append(t).append(", ");
		if (tagsBuffer.length() > 1)
			tagsBuffer.delete(tagsBuffer.length() - 2, tagsBuffer.length() - 1);
		initialTagText = tagsBuffer.toString();
		tags.setWidth("100%");
		tags.setText(initialTagText);
		generalTable.setWidget(4, 1, tags);

        generalTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(2, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(3, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(4, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(0, 1, "props-values");
        generalTable.getFlexCellFormatter().setStyleName(1, 1, "props-values");
        generalTable.getFlexCellFormatter().setStyleName(2, 1, "props-values");
        generalTable.getFlexCellFormatter().setStyleName(3, 1, "props-values");
        generalTable.getFlexCellFormatter().setStyleName(4, 1, "props-values");
        generalTable.setCellSpacing(4);

        generalPanel.add(generalTable);

        DisclosurePanel allTags = new DisclosurePanel("All tags");
        allTagsContent = new FlowPanel();
        allTagsContent.setWidth("100%");
        for (Tag t : app.getAllTags()) {
            final Anchor tagAnchor = new Anchor(t.getName(), false);
            tagAnchor.addStyleName("pithos-tag");
            allTagsContent.add(tagAnchor);
            Label separator = new Label(", ");
            separator.addStyleName("pithos-tag");
            allTagsContent.add(separator);
            tagAnchor.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(@SuppressWarnings("unused") ClickEvent event) {
                    String existing = tags.getText().trim();
                    if (MULTIPLE_VALUES_TEXT.equals(existing))
                        existing = "";
                    String newTag = tagAnchor.getText().trim();
                    // insert the new tag only if it is not in the list
                    // already
                    if (existing.indexOf(newTag) == -1)
                        tags.setText(existing + (existing.length() > 0 ? ", " : "") + newTag);
                }
            });
        }
        allTags.setContent(allTagsContent);
        generalPanel.add(allTags);
        generalPanel.setSpacing(4);
        return generalPanel;
    }

    private VerticalPanel createSharingPanel() {
        VerticalPanel permPanel = new VerticalPanel();

        permList = new PermissionsList(images, file.getPermissions(), file.getOwner(), file.getInheritedPermissionsFrom() != null);
        permPanel.add(permList);

        if (file.getInheritedPermissionsFrom() == null) {
            HorizontalPanel permButtons = new HorizontalPanel();
            Button add = new Button("Add Group", new ClickHandler() {
                @Override
                public void onClick(@SuppressWarnings("unused") ClickEvent event) {
                    PermissionsAddDialog dlg = new PermissionsAddDialog(app, app.getAccount().getGroups(), permList, false);
                    dlg.center();
                    permList.updatePermissionTable();
                }
            });
            add.addStyleName("button");
            permButtons.add(add);
            permButtons.setCellHorizontalAlignment(add, HasHorizontalAlignment.ALIGN_CENTER);

            final Button addUser = new Button("Add User", new ClickHandler() {
                @Override
                public void onClick(@SuppressWarnings("unused") ClickEvent event) {
                    PermissionsAddDialog dlg = new PermissionsAddDialog(app, app.getAccount().getGroups(), permList, true);
                    dlg.center();
                    permList.updatePermissionTable();
                }
            });
            addUser.addStyleName("button");
            permButtons.add(addUser);
            permButtons.setCellHorizontalAlignment(addUser, HasHorizontalAlignment.ALIGN_CENTER);

            permButtons.setSpacing(8);
            permButtons.addStyleName("pithos-TabPanelBottom");
            permPanel.add(permButtons);
        }

        final Label readForAllNote = new Label("When this option is enabled, the file will be readable" +
                    " by everyone. By checking this option, you are certifying that you have the right to " +
                    "distribute this file and that it does not violate the Terms of Use.", true);
        readForAllNote.setVisible(false);
        readForAllNote.setStylePrimaryName("pithos-readForAllNote");

        readForAll = new CheckBox();
        readForAll.setValue(file.isPublished());
        readForAll.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(@SuppressWarnings("unused") ClickEvent event) {
                readForAllNote.setVisible(readForAll.getValue());
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

        if (file.isPublished()) {
            final HorizontalPanel pathPanel = new HorizontalPanel();
            pathPanel.setWidth("100%");
            pathPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
            pathPanel.add(new Label("Link"));
            pathPanel.setSpacing(8);
            pathPanel.addStyleName("pithos-TabPanelBottom");

            TextBox path = new TextBox();
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
        }

        return permPanel;
    }

    VerticalPanel createVersionPanel(List<Version> versions) {
        VerticalPanel versionPanel = new VerticalPanel();
        VersionsList verList = new VersionsList(app, this, images, file, versions);
        versionPanel.add(verList);
        return versionPanel;
    }

	/**
	 * Accepts any change and updates the file
	 *
	 */
	@Override
	protected void accept() {
		String newFilename = null;

		final Map<String, Boolean[]> perms = (permList.hasChanges() ? permList.getPermissions() : null);

		if (!name.getText().trim().equals(file.getName())) {
			newFilename = name.getText().trim();
		}

		//only update the read for all perm if the user is the owner
        Boolean published = null;
		if (readForAll.getValue() != file.isPublished())
			if (file.getOwner().equals(app.getUsername()))
                published = readForAll.getValue();
        final Boolean finalPublished = published;

        String[] tagset = null;
		if (!tags.getText().equals(initialTagText))
			tagset = tags.getText().trim().split(",");
        final String[] newTags = tagset;

        if (newFilename != null) {
            final String path = file.getParent().getUri() + "/" + newFilename;
            PutRequest updateFile = new PutRequest(app.getApiPath(), app.getUsername(), path) {
                @Override
                public void onSuccess(@SuppressWarnings("unused") Resource result) {
                    updateMetaData(app.getApiPath(), file.getOwner(), path + "?update=", newTags, finalPublished, perms);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
                    app.displayError("System error modifying file:" + t.getMessage());
                }

				@Override
				protected void onUnauthorized(Response response) {
					app.sessionExpired();
				}
            };
            updateFile.setHeader("X-Auth-Token", app.getToken());
            updateFile.setHeader("X-Move-From", URL.encodePathSegment(file.getUri()));
            updateFile.setHeader("Content-Type", file.getContentType());
            Scheduler.get().scheduleDeferred(updateFile);
        }
        else
            updateMetaData(app.getApiPath(), app.getUsername(), file.getUri() + "?update=", newTags, finalPublished, perms);
	}

	protected void updateMetaData(String api, String owner, String path, String[] newTags, Boolean published, Map<String, Boolean[]> newPermissions) {
        if (newTags != null || published != null || newPermissions != null) {
            PostRequest updateFile = new PostRequest(api, owner, path) {
                @Override
                public void onSuccess(@SuppressWarnings("unused") Resource result) {
                    app.updateFolder(file.getParent(), true, null);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
                    app.displayError("System error modifying file:" + t.getMessage());
                }

				@Override
				protected void onUnauthorized(Response response) {
					app.sessionExpired();
				}
            };
            updateFile.setHeader("X-Auth-Token", app.getToken());
            for (String t : file.getTags()) {
        		updateFile.setHeader("X-Object-Meta-" + URL.encodePathSegment(t.trim()), "~");
            }
            if (newTags != null)
                for (String t : newTags)
                	if (t.length() > 0)
                		updateFile.setHeader("X-Object-Meta-" + URL.encodePathSegment(t.trim()), "true");
            if (published != null)
                updateFile.setHeader("X-Object-Public", published.toString());
            if (newPermissions != null) {
                String readPermHeader = "read=";
                String writePermHeader = "write=";
                for (String u : newPermissions.keySet()) {
                    Boolean[] p = newPermissions.get(u);
                    if (p[0] != null && p[0])
                        readPermHeader += u + ",";
                    if (p[1] != null && p[1])
                        writePermHeader += u + ",";
                }
                if (readPermHeader.endsWith("="))
                    readPermHeader = "";
                else if (readPermHeader.endsWith(","))
                    readPermHeader = readPermHeader.substring(0, readPermHeader.length() - 1);
                if (writePermHeader.endsWith("="))
                    writePermHeader = "";
                else if (writePermHeader.endsWith(","))
                    writePermHeader = writePermHeader.substring(0, writePermHeader.length() - 1);
                String permHeader = readPermHeader +  ((readPermHeader.length()  > 0 && writePermHeader.length() > 0) ?  ";" : "") + writePermHeader;
                if (permHeader.length() == 0)
                    permHeader="~";
                else
                	permHeader = URL.encodePathSegment(permHeader);
                updateFile.setHeader("X-Object-Sharing", permHeader);
            }
            Scheduler.get().scheduleDeferred(updateFile);
        }
        else
            app.updateFolder(file.getParent(), true, null);
    }
}
