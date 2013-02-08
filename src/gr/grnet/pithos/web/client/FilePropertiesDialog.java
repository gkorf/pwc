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
import gr.grnet.pithos.web.client.rest.PostRequest;
import gr.grnet.pithos.web.client.rest.PutRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
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

	/**
	 * The widget that holds the name of the file.
	 */
	private TextBox name = new TextBox();

	final File file;

    FlexTable metaTable;
	/**
	 * The widget's constructor.
	 */
	public FilePropertiesDialog(Pithos _app, File _file) {
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
		setText("File properties");
		setGlassEnabled(true);
		setStyleName("pithos-DialogBox");

		// Outer contains inner and buttons.
		final VerticalPanel outer = new VerticalPanel();
		outer.add(close);
		// Inner contains generalPanel and permPanel.
		inner = new VerticalPanel();
		inner.addStyleName("inner");

        inner.add(createGeneralPanel());

        outer.add(inner);

		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		final Button ok = new Button("OK", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (accept())
					closeDialog();
			}
		});
		ok.addStyleName("button");

        outer.add(ok);
        outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);

        setWidget(outer);
	}

	private VerticalPanel createGeneralPanel() {
        final VerticalPanel generalPanel = new VerticalPanel();
        final FlexTable generalTable = new FlexTable();
        generalTable.setText(0, 0, "Name");
        generalTable.setText(1, 0, "Folder");
        generalTable.setText(2, 0, "Owner");
        generalTable.setText(3, 0, "Last modified");

        name.setWidth("100%");
        name.setText(file.getName());
        generalTable.setWidget(0, 1, name);
        if(file.getParent() != null) {
            generalTable.setText(1, 1, file.getParent().getName());
        }
        else {
            generalTable.setText(1, 1, "-");
        }

        final String ownerID = file.getOwnerID();
        final String displayName = app.getUserDisplayNameForID(ownerID);
        final String ownerDisplayName;
        if(displayName == null) {
            // FIXME: Get the actual display name and do not use the id
            ownerDisplayName = ownerID;
        }
        else {
            ownerDisplayName = displayName;
        }
        generalTable.setText(2, 1, ownerDisplayName);

        final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
        generalTable.setText(3, 1, file.getLastModified() != null ? formatter.format(file.getLastModified()) : "");

        generalTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(2, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(3, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(4, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(0, 1, "props-values");
        generalTable.getFlexCellFormatter().setStyleName(1, 1, "props-values");
        generalTable.getFlexCellFormatter().setStyleName(2, 1, "props-values");
        generalTable.getFlexCellFormatter().setStyleName(3, 1, "props-values");
        generalTable.setCellSpacing(4);

        generalPanel.add(generalTable);

        HorizontalPanel metaTitlePanel = new HorizontalPanel();
        metaTitlePanel.setSpacing(5);
        Label meta = new Label("Meta data");
        meta.addStyleName("pithos-metaTitle");
        metaTitlePanel.add(meta);
        
		Anchor plus = new Anchor("add");
		plus.addStyleName(Pithos.resources.pithosCss().commandAnchor());
		metaTitlePanel.add(plus);
		
		generalPanel.add(metaTitlePanel);
		
		metaTable = new FlexTable();
		metaTable.setCellSpacing(0);
		metaTable.setHTML(0, 0, "Name");
		metaTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		metaTable.setText(0, 1, "Value");
		metaTable.getFlexCellFormatter().setStyleName(0, 1, "props-labels");
		int rows = 1;
		for (String metaKey : file.getMeta().keySet()) {
			addFormLine(metaTable, rows++, metaKey, file.getMeta().get(metaKey));
		}
		if (rows == 1) //If no meta add an empty line
			addFormLine(metaTable, rows++, "", "");
		
		plus.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				addFormLine(metaTable, metaTable.getRowCount(), "", "");
			}
		});

		generalPanel.add(metaTable);
        generalPanel.setSpacing(4);
        return generalPanel;
    }

	void addFormLine(final FlexTable table, int row, String _name, String _value) {
		TextBox nameBox = new TextBox();
		nameBox.setText(_name);
		nameBox.addStyleName("pithos-metaName");
		table.setWidget(row, 0, nameBox);
		table.getFlexCellFormatter().setStyleName(1, 0, "props-values");

		TextBox valueBox = new TextBox();
		valueBox.setText(_value);
		valueBox.addStyleName("pithos-metaValue");
		table.setWidget(row, 1, valueBox);
		table.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		
		Anchor delete = new Anchor("remove");
		delete.addStyleName(Pithos.resources.pithosCss().commandAnchor());
		delete.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				int rowIndex = table.getCellForEvent(event).getRowIndex();
				table.removeRow(rowIndex);
			}
		});
		table.setWidget(row, 2, delete);
	}

	/**
	 * Accepts any change and updates the file
	 *
	 */
	@Override
	protected boolean accept() {
		String newFilename = null;

		if (!name.getText().trim().equals(file.getName())) {
			newFilename = name.getText().trim();
			if (newFilename.length() == 0)
				newFilename = null;
		}
		

        final Map<String, String> newMeta = new HashMap<String, String>();
        for (int row = 1; row < metaTable.getRowCount(); row++) {
        	String key = ((TextBox) metaTable.getWidget(row, 0)).getText().trim();
        	String value = ((TextBox) metaTable.getWidget(row, 1)).getText().trim();
        	if (key.length() > 0 && value.length() > 0)
        		newMeta.put(key, value);
        	else if ((key.length() > 0 && value.length() == 0) || (key.length() == 0 && value.length() > 0)) {
        		app.displayError("You have empty keys or values");
        		return false;
        	}
        }

        if (newFilename != null) {
            final String path = file.getParent().getUri() + "/" + newFilename;
            PutRequest updateFile = new PutRequest(app.getApiPath(), app.getUserID(), path) {
                @Override
                public void onSuccess(Resource result) {
                    updateMetaData(app.getApiPath(), file.getOwnerID(), path, newMeta);
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
            updateFile.setHeader("X-Auth-Token", app.getUserToken());
            updateFile.setHeader("X-Move-From", URL.encodePathSegment(file.getUri()));
            updateFile.setHeader("Content-Type", file.getContentType());
            for (String key : file.getMeta().keySet())
                updateFile.setHeader("X-Object-Meta-" + URL.encodePathSegment(key.trim()), URL.encodePathSegment(newMeta.get(key)));
            if (file.isPublished())
                updateFile.setHeader("X-Object-Public", "true");
            String readPermHeader = "read=";
            String writePermHeader = "write=";
            for (String u : file.getPermissions().keySet()) {
                Boolean[] p = file.getPermissions().get(u);
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

            Scheduler.get().scheduleDeferred(updateFile);
        }
        else
            updateMetaData(app.getApiPath(), app.getUserID(), file.getUri(), newMeta);
        return true;
	}

	protected void updateMetaData(final String api, final String owner, final String path, Map<String, String> newMeta) {
        if (newMeta != null) {
            PostRequest updateFile = new PostRequest(api, owner, path + "?update=") {
                @Override
                public void onSuccess(Resource result) {
                	if (!app.isMySharedSelected())
	                    app.updateFolder(file.getParent(), true, new Command() {
							
							@Override
							public void execute() {
								app.getFileList().selectByUrl(Arrays.asList(api + owner + path));
								app.updateMySharedRoot();
							}
						}, true);
                	else {
                		app.updateSharedFolder(file.getParent(), true, new Command() {
                			
							@Override
							public void execute() {
								app.getFileList().selectByUrl(Arrays.asList(api + owner + path));
							}
                		});
                	}
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
            updateFile.setHeader("X-Auth-Token", app.getUserToken());
            
            for (String t : file.getMeta().keySet()) {
        		updateFile.setHeader("X-Object-Meta-" + URL.encodePathSegment(t.trim()), "~");
            }
            
            for (String key : newMeta.keySet())
                updateFile.setHeader("X-Object-Meta-" + URL.encodePathSegment(key.trim()), URL.encodePathSegment(newMeta.get(key)));
            
            Scheduler.get().scheduleDeferred(updateFile);
        }
        else if (!app.isMySharedSelected())
            app.updateFolder(file.getParent(), true, new Command() {
				
				@Override
				public void execute() {
					app.getFileList().selectByUrl(Arrays.asList(api + owner + path));
					if (file.isSharedOrPublished())
						app.updateMySharedRoot();
				}
			}, true);
        else {
			app.getFileList().selectByUrl(Arrays.asList(api + owner + path));
        	app.updateSharedFolder(file.getParent(), true);
        }
    }
}
