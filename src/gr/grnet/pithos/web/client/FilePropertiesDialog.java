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
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.PostCommand;
import gr.grnet.pithos.web.client.rest.PostRequest;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import gr.grnet.pithos.web.client.tagtree.Tag;
import java.util.Iterator;

/**
 * The 'File properties' dialog box implementation.
 *
 */
public class FilePropertiesDialog extends AbstractPropertiesDialog {

	private PermissionsList permList;

	private CheckBox readForAll;

	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ClientBundle,MessagePanel.Images {

		@Source("gr/grnet/pithos/resources/edit_user.png")
		ImageResource permUser();

		@Source("gr/grnet/pithos/resources/groupevent.png")
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

	private final CheckBox versioned = new CheckBox();

	final File file;

	private String userFullName;

    private Pithos app;

	/**
	 * The widget's constructor.
	 */
	public FilePropertiesDialog(Pithos _app, File _file) {
        app = _app;
        file = _file;

		// Set the dialog's caption.
		setText("File properties");


//		permList = new PermissionsList(images, file.getPermissions(), file.getOwner());

		// Outer contains inner and buttons.
		final VerticalPanel outer = new VerticalPanel();
		final FocusPanel focusPanel = new FocusPanel(outer);
		// Inner contains generalPanel and permPanel.
		inner = new DecoratedTabPanel();
		inner.setAnimationEnabled(true);


        inner.add(createGeneralPanel(), "General");

        inner.add(createSharingPanel(), "Sharing");

        inner.add(createVersionPanel(), "Versions");

        inner.selectTab(0);

        outer.add(inner);

		final HorizontalPanel buttons = new HorizontalPanel();
		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		final Button ok = new Button("OK", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				accept();
				closeDialog();
			}
		});

		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		final Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				closeDialog();
			}
		});
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("pithos-TabPanelBottom");

        outer.add(buttons);
        outer.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
        outer.addStyleName("pithos-TabPanelBottom");

        focusPanel.setFocus(true);
        setWidget(outer);
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
        generalTable.setText(3, 1, formatter.format(file.getLastModified()));

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
                public void onClick(ClickEvent event) {
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
//
//        permList = new PermissionsList(images, file.getPermissions(), file.getOwner());
//        permPanel.add(permList);
//
//        HorizontalPanel permButtons = new HorizontalPanel();
//        Button add = new Button("Add Group", new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                PermissionsAddDialog dlg = new PermissionsAddDialog(groups, permList, false);
//                dlg.center();
//            }
//        });
//        permButtons.add(add);
//        permButtons.setCellHorizontalAlignment(add, HasHorizontalAlignment.ALIGN_CENTER);
//
//        final Button addUser = new Button("Add User", new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                PermissionsAddDialog dlg = new PermissionsAddDialog(groups, permList, true);
//                dlg.center();
//            }
//        });
//        permButtons.add(addUser);
//        permButtons.setCellHorizontalAlignment(addUser, HasHorizontalAlignment.ALIGN_CENTER);
//
//        permButtons.setSpacing(8);
//        permButtons.addStyleName("pithos-TabPanelBottom");
//        permPanel.add(permButtons);
//
//        final Label readForAllNote = new Label("When this option is enabled, the file will be readable" +
//                    " by everyone. By checking this option, you are certifying that you have the right to " +
//                    "distribute this file and that it does not violate the Terms of Use.", true);
//        readForAllNote.setVisible(false);
//        readForAllNote.setStylePrimaryName("pithos-readForAllNote");
//
//        readForAll = new CheckBox();
//        readForAll.setValue(file.isReadForAll());
//        readForAll.addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                readForAllNote.setVisible(readForAll.getValue());
//            }
//
//        });
//
//        // Only show the read for all permission if the user is the owner.
//        if (file.getOwner().equals(app.getUsername())) {
//            final HorizontalPanel permForAll = new HorizontalPanel();
//            permForAll.add(new Label("Public"));
//            permForAll.add(readForAll);
//            permForAll.setSpacing(8);
//            permForAll.addStyleName("pithos-TabPanelBottom");
//            permForAll.add(readForAllNote);
//            permPanel.add(permForAll);
//        }
//
//
//        final HorizontalPanel pathPanel = new HorizontalPanel();
//        pathPanel.setWidth("100%");
//        pathPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
//        pathPanel.add(new Label("Link"));
//        pathPanel.setSpacing(8);
//        pathPanel.addStyleName("pithos-TabPanelBottom");
//
//        TextBox path = new TextBox();
//        path.setWidth("100%");
//        path.addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                Pithos.enableIESelection();
//                ((TextBox) event.getSource()).selectAll();
//                Pithos.preventIESelection();
//            }
//        });
//        path.setText(file.getUri());
//        path.setTitle("Use this link for sharing the file via e-mail, IM, etc. (crtl-C/cmd-C to copy to system clipboard)");
//        path.setWidth("100%");
//        path.setReadOnly(true);
//        pathPanel.add(path);
//        permPanel.add(pathPanel);

        return permPanel;
    }

    private VerticalPanel createVersionPanel() {
        VerticalPanel versionPanel = new VerticalPanel();

//        VersionsList verList = new VersionsList(this, images, bodies);
//        versionPanel.add(verList);
//
//        HorizontalPanel vPanel = new HorizontalPanel();
//
//		vPanel.setSpacing(8);
//		vPanel.addStyleName("pithos-TabPanelBottom");
//		vPanel.add(new Label("Versioned"));
//
//		versioned.setValue(file.isVersioned());
//		vPanel.add(versioned);
//		versionPanel.add(vPanel);
//
//        HorizontalPanel vPanel2 = new HorizontalPanel();
//		vPanel2.setSpacing(8);
//		vPanel2.addStyleName("pithos-TabPanelBottom");
//
//        HTML removeAllVersion = new HTML("<span>Remove all previous versions?</span>");
//        vPanel2.add(removeAllVersion);
//
//		Button removeVersionsButton = new Button(AbstractImagePrototype.create(images.delete()).getHTML(), new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				ConfirmationDialog confirm = new ConfirmationDialog("Really " +
//						"remove all previous versions?", "Remove") {
//
//					@Override
//					public void cancel() {
//					}
//
//					@Override
//					public void confirm() {
//						FilePropertiesDialog.this.closeDialog();
//						removeAllOldVersions();
//					}
//
//				};
//				confirm.center();
//			}
//
//		});
//		vPanel2.add(removeVersionsButton);
//        if(!file.isVersioned())
//            vPanel2.setVisible(false);
//
//        versionPanel.add(vPanel2);

        return versionPanel;
    }

	/**
	 * Accepts any change and updates the file
	 *
	 */
	@Override
	protected void accept() {
		String newFilename = null;
//		permList.updatePermissionsAccordingToInput();
//		Set<PermissionHolder> perms = permList.getPermissions();
//		JSONObject json = new JSONObject();
		if (!name.getText().trim().equals(file.getName())) {
			newFilename = name.getText().trim();
//			json.put("name", new JSONString(newFilename));
		}
//		if (versioned.getValue() != file.isVersioned())
//			json.put("versioned", JSONBoolean.getInstance(versioned.getValue()));
		//only update the read for all perm if the user is the owner
//		if (readForAll.getValue() != file.isReadForAll())
//			if (file.getOwner().equals(Pithos.get().getCurrentUserResource().getUsername()))
//				json.put("readForAll", JSONBoolean.getInstance(readForAll.getValue()));
//		int i = 0;
//		if (permList.hasChanges()) {
//			GWT.log("Permissions change", null);
//			JSONArray perma = new JSONArray();
//
//			for (PermissionHolder p : perms) {
//				JSONObject po = new JSONObject();
//				if (p.getUser() != null)
//					po.put("user", new JSONString(p.getUser()));
//				if (p.getGroup() != null)
//					po.put("group", new JSONString(p.getGroup()));
//				po.put("read", JSONBoolean.getInstance(p.isRead()));
//				po.put("write", JSONBoolean.getInstance(p.isWrite()));
//				po.put("modifyACL", JSONBoolean.getInstance(p.isModifyACL()));
//				perma.set(i, po);
//				i++;
//			}
//			json.put("permissions", perma);
//		}
        String[] tagset = null;
		if (!tags.getText().equals(initialTagText))
			tagset = tags.getText().split(",");
//		String jsonString = json.toString();
//		if(jsonString.equals("{}")){
//			GWT.log("NO CHANGES", null);
//			return;
//		}
        final String[] newTags = tagset;

        if (newFilename != null) {
            final String path = app.getApiPath() + app.getUsername() + file.getParent().getUri() + "/" + newFilename;
            PutRequest updateFile = new PutRequest(path) {
                @Override
                public void onSuccess(Resource result) {
                    if (newTags != null)
                        updateMetaData(path + "?update=", newTags);
                    else
                        app.updateFolder(file.getParent());
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
                    app.displayError("System error modifying file:" + t.getMessage());
                }
            };
            updateFile.setHeader("X-Auth-Token", app.getToken());
            updateFile.setHeader("X-Move-From", file.getUri());
            updateFile.setHeader("Content-Type", file.getContentType());
            Scheduler.get().scheduleDeferred(updateFile);
        }
        else if (newTags != null)
            updateMetaData(app.getApiPath() + app.getUsername() + file.getUri() + "?update=", newTags);
	}

    private void updateMetaData(String path, String[] newTags) {
        PostRequest updateFile = new PostRequest(path) {
            @Override
            public void onSuccess(Resource result) {
                app.updateFolder(file.getParent());
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("", t);
                app.displayError("System error modifying file:" + t.getMessage());
            }
        };
        updateFile.setHeader("X-Auth-Token", app.getToken());
        for (String t : newTags)
            updateFile.setHeader("X-Object-Meta-" + t.trim(), "true");
        Scheduler.get().scheduleDeferred(updateFile);
    }

	private void removeAllOldVersions() {
		JSONObject json = new JSONObject();
		json.put("versioned", JSONBoolean.getInstance(false));
		GWT.log(json.toString(), null);
		PostCommand cf = new PostCommand(file.getUri() + "?update=", json.toString(), 200) {

			@Override
			public void onComplete() {
				toggleVersioned(true);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if (t instanceof RestException) {
					int statusCode = ((RestException) t).getHttpStatusCode();
					if (statusCode == 405)
						Pithos.get().displayError("You don't have the necessary permissions");
					else if (statusCode == 404)
						Pithos.get().displayError("User in permissions does not exist");
					else if (statusCode == 409)
						Pithos.get().displayError("A folder with the same name already exists");
					else if (statusCode == 413)
						Pithos.get().displayError("Your quota has been exceeded");
					else
						Pithos.get().displayError("Unable to modify file:" + ((RestException) t).getHttpStatusText());
				} else
					Pithos.get().displayError("System error moifying file:" + t.getMessage());
			}
		};
		DeferredCommand.addCommand(cf);
	}

	private void toggleVersioned(boolean versionedValue) {
		JSONObject json = new JSONObject();
		json.put("versioned", JSONBoolean.getInstance(versionedValue));
		GWT.log(json.toString(), null);
		PostCommand cf = new PostCommand(file.getUri() + "?update=", json.toString(), 200) {

			@Override
			public void onComplete() {
				Pithos.get().getTreeView().refreshCurrentNode(false);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if (t instanceof RestException) {
					int statusCode = ((RestException) t).getHttpStatusCode();
					if (statusCode == 405)
						Pithos.get().displayError("You don't have the necessary permissions");
					else if (statusCode == 404)
						Pithos.get().displayError("User in permissions does not exist");
					else if (statusCode == 409)
						Pithos.get().displayError("A folder with the same name already exists");
					else if (statusCode == 413)
						Pithos.get().displayError("Your quota has been exceeded");
					else
						Pithos.get().displayError("Unable to modify file:" + ((RestException) t).getHttpStatusText());
				} else
					Pithos.get().displayError("System error moifying file:" + t.getMessage());
			}
		};
		DeferredCommand.addCommand(cf);
	}

}
