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
import gr.grnet.pithos.web.client.foldertree.FileVersions;
import gr.grnet.pithos.web.client.foldertree.Version;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'File properties' dialog box implementation.
 *
 */
public class FileVersionsDialog extends AbstractPropertiesDialog {

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

	final File file;

    Images images = GWT.create(Images.class);

	/**
	 * The widget's constructor.
	 */
	public FileVersionsDialog(Pithos _app, File _file) {
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
		setText("File versions");
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

        focusPanel.setFocus(true);
        setWidget(outer);
	}

	void doCenter() {
		super.center();
	}
	
	@Override
	public void center() {
		fetchVersions();
	}

    protected void fetchVersions() {
    	String path = file.getUri() + "?format=json&version=list";
    	GetRequest<FileVersions> getVersions = new GetRequest<FileVersions>(FileVersions.class, app.getApiPath(), file.getOwner(), path) {

			@Override
			public void onSuccess(FileVersions _result) {
		        inner.add(createVersionPanel(_result.getVersions()));
				doCenter();
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				app.setError(t);
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

    VerticalPanel createVersionPanel(List<Version> versions) {
        VerticalPanel versionPanel = new VerticalPanel();
        VersionsList verList = new VersionsList(app, this, images, file, versions);
        versionPanel.add(verList);
        return versionPanel;
    }

	/**
	 * Accepts any change and updates the file
	 * @return 
	 *
	 */
	@Override
	protected boolean accept() {
        app.updateFolder(file.getParent(), true, new Command() {
			
			@Override
			public void execute() {
				if (file.isSharedOrPublished())
					app.updateMySharedRoot();
			}
		}, true);
        
        return true;
	}
}
