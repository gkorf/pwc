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

import gr.grnet.pithos.web.client.FilePropertiesDialog.Images;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.foldertree.Version;
import gr.grnet.pithos.web.client.rest.PostRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VersionsList extends Composite {

    Pithos app;

    File file;
    
    private List<Version> versions = null;
    
    private Images images;
    
    private VerticalPanel permPanel = new VerticalPanel();
    
    private FlexTable permTable = new FlexTable();

	FilePropertiesDialog container;

	public VersionsList(Pithos _app, FilePropertiesDialog aContainer, final Images theImages, File _file, List<Version> theVersions) {
        app = _app;
		images = theImages;
		container = aContainer;
		file = _file;
		versions = theVersions;
		permTable.setText(0, 0, "Version");
		permTable.setText(0, 1, "Date");
		permTable.setText(0, 2, "");
		permTable.setText(0, 3, "");
		permTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 1, "props-toplabels");
		permTable.getFlexCellFormatter().setColSpan(0, 1, 2);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		permPanel.add(permTable);
		permPanel.addStyleName("pithos-TabPanelBottom");
		permTable.addStyleName("pithos-permList");
		initWidget(permPanel);
		showVersionsTable();
	}

	public void showVersionsTable(){
		DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
		int i = 1;
		for (final Version v : versions) {
			HTML restoreVersion = new HTML("<a href='#' class='hidden-link info'><span>" + AbstractImagePrototype.create(images.restore()).getHTML() + "</span><div>Restore this Version</div></a>");
			restoreVersion.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					restoreVersion(v.getVersion());
				}
			});

			permTable.setHTML(i, 0, "<span>" + v.getVersion() + "</span>");
			permTable.setHTML(i, 1, "<span>" + formatter.format(v.getDate()) + "</span>");
			HTML downloadHtml = new HTML("<a class='hidden-link info' href='#'><span>" + AbstractImagePrototype.create(images.download()).getHTML()+"</span><div>View this Version</div></a>");
			downloadHtml.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					String fileUrl = app.getApiPath() + app.getUsername() + file.getUri() + "?X-Auth-Token=" + app.getToken() + "&version=" + v.getVersion();
					Window.open(fileUrl, "_BLANK", "");
				}
			});
			permTable.setWidget(i, 2, downloadHtml);
			permTable.setWidget(i, 3, restoreVersion);
			permTable.getFlexCellFormatter().setStyleName(i, 0, "props-labels");
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_CENTER);
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 1, HasHorizontalAlignment.ALIGN_CENTER);
			permTable.getFlexCellFormatter().setColSpan(i, 1, 2);
			i++;
		}
	}

	void restoreVersion(int version) {
		String path = file.getUri() + "?update=";
		PostRequest restoreVersion = new PostRequest(app.getApiPath(), file.getOwner(), path) {
			
			@Override
			public void onSuccess(Resource result) {
				container.hide();
			}
			
			@Override
			public void onError(Throwable t) {
                if (t instanceof RestException) {
                	if (((RestException) t).getHttpStatusCode() == Response.SC_NO_CONTENT)
                		onSuccess(null);
                	else
                		app.displayError("Unable to restore version: " + ((RestException) t).getHttpStatusText());
                }
                else
                    app.displayError("System error unable to restore versions: "+t.getMessage());
			}
		};
		restoreVersion.setHeader("X-Auth-Token", app.getToken());
		restoreVersion.setHeader("X-Source-Object", file.getUri());
		restoreVersion.setHeader("X-Source-Version", String.valueOf(version));
		restoreVersion.setHeader("Content-Range", "bytes 0-/*");
		Scheduler.get().scheduleDeferred(restoreVersion);
	}
}
