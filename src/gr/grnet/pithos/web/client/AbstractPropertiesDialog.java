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

import gr.grnet.pithos.web.client.rest.GetCommand;
import gr.grnet.pithos.web.client.rest.resource.TagsResource;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Abstract class, parent of all 'File properties' dialog boxes.
 *
 */
public abstract class AbstractPropertiesDialog extends DialogBox {

	protected static final String MULTIPLE_VALUES_TEXT = "(Multiple values)";

	/**
	 * Text box with the tags associated with the file
	 */
	protected TextBox tags = new TextBox();

	protected String initialTagText;

	/**
	 * A FlowPanel with all user tags
	 */
	protected FlowPanel allTagsContent;


	protected TabPanel inner = null;

    protected Pithos app;

	/**
	 * The widget's constructor.
	 *
	 */
	public AbstractPropertiesDialog(Pithos _app) {
        app = _app;
		// Enable IE selection for the dialog (must disable it upon closing it)
		Pithos.enableIESelection();

		setAnimationEnabled(true);

	}
	/**
	 * Retrieves all user tags from the server and updates the FlowPanel
	 */
	protected void updateTags() {
		GetCommand<TagsResource> tc = new GetCommand<TagsResource>(app, TagsResource.class, app.getCurrentUserResource().getTagsPath(),null) {

			@Override
			public void onComplete() {
				allTagsContent.clear();
				TagsResource tagr = getResult();
				List<String> userTags = tagr.getTags();
				Anchor tag = null;
				for(String usrTag : userTags){
					tag = new Anchor(usrTag.toString(), false);
					tag.addStyleName("pithos-tag");
					allTagsContent.add(tag);
					Label separator = new Label(", ");
					separator.addStyleName("pithos-tag");
					allTagsContent.add(separator);
					tag.addClickHandler( new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							String existing = tags.getText();
							if (MULTIPLE_VALUES_TEXT.equals(existing)) existing = "";
							String newTag = ((Anchor) event.getSource()).getText().trim();
							// insert the new tag only if it is not in the list
							// already
							if (existing.indexOf(newTag) == -1 && !existing.trim().endsWith(newTag))
								tags.setText(existing.trim()
											+ (existing.length() > 0 ? ", " : "")
											+ newTag);
						}
					});
				}
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				app.displayError("Unable to fetch user tags");
			}
		};
		DeferredCommand.addCommand(tc);

	}

	/**
	 * Accepts any change and updates the file
	 *
	 */
	protected abstract void accept();

	@Override
	@SuppressWarnings("fallthrough")
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
	    super.onPreviewNativeEvent(preview);

	    NativeEvent evt = preview.getNativeEvent();
	    if (evt.getType().equals("keydown"))
			// Use the popup's key preview hooks to close the dialog when either
			  // enter or escape is pressed.
			  switch (evt.getKeyCode()) {
			    case KeyCodes.KEY_ENTER:
			    	accept();
			    case KeyCodes.KEY_ESCAPE:
			      closeDialog();
			      break;
			  }
	  }



	public void selectTab(int _tab) {
		inner.selectTab(_tab);
	}


	/**
	 * Enables IE selection prevention and hides the dialog
	 * (we disable the prevention on creation of the dialog)
	 */
	public void closeDialog() {
		Pithos.preventIESelection();
		hide();
	}
}
