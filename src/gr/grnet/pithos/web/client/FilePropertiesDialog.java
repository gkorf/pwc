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
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.PostRequest;
import gr.grnet.pithos.web.client.rest.PutRequest;

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
import com.google.gwt.user.client.ui.Image;
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
				accept();
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
        if(file.getParent() != null)
            generalTable.setText(1, 1, file.getParent().getName());
        else
            generalTable.setText(1, 1, "-");
        generalTable.setText(2, 1, file.getOwner());

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
        
		Image plus = new Image("images/plus.png");
		plus.addStyleName("pithos-addMetaImg");
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
		
		Image delete = new Image("images/delete.png");
		delete.addStyleName("pithos-metaDeleteImg");
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
	protected void accept() {
		String newFilename = null;

		if (!name.getText().trim().equals(file.getName())) {
			newFilename = name.getText().trim();
		}

        final Map<String, String> newMeta = new HashMap<String, String>();
        for (int row = 1; row < metaTable.getRowCount(); row++) {
        	String key = ((TextBox) metaTable.getWidget(row, 0)).getText().trim();
        	String value = ((TextBox) metaTable.getWidget(row, 1)).getText().trim();
        	if (key.length() > 0 && value.length() > 0)
        		newMeta.put(key, value);
        }

        if (newFilename != null) {
            final String path = file.getParent().getUri() + "/" + newFilename;
            PutRequest updateFile = new PutRequest(app.getApiPath(), app.getUsername(), path) {
                @Override
                public void onSuccess(Resource result) {
                    updateMetaData(app.getApiPath(), file.getOwner(), path + "?update=", newMeta);
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
            updateMetaData(app.getApiPath(), app.getUsername(), file.getUri() + "?update=", newMeta);
	}

	protected void updateMetaData(String api, String owner, String path, Map<String, String> newMeta) {
        if (newMeta != null) {
            PostRequest updateFile = new PostRequest(api, owner, path) {
                @Override
                public void onSuccess(Resource result) {
                    app.updateFolder(file.getParent(), true, new Command() {
						
						@Override
						public void execute() {
							app.updateMySharedRoot();
						}
					});
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
            
            for (String t : file.getMeta().keySet()) {
        		updateFile.setHeader("X-Object-Meta-" + URL.encodePathSegment(t.trim()), "~");
            }
            
            for (String key : newMeta.keySet())
                updateFile.setHeader("X-Object-Meta-" + URL.encodePathSegment(key.trim()), URL.encodePathSegment(newMeta.get(key)));
            
            Scheduler.get().scheduleDeferred(updateFile);
        }
        else
            app.updateFolder(file.getParent(), true, new Command() {
				
				@Override
				public void execute() {
					if (file.isShared())
						app.updateMySharedRoot();
				}
			});
    }
}
