/*
 *  Copyright (c) 2011 Greek Research and Technology Network
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

	/**
	 * The widget's constructor.
	 *
	 */
	public AbstractPropertiesDialog() {

		// Enable IE selection for the dialog (must disable it upon closing it)
		GSS.enableIESelection();

		setAnimationEnabled(true);

	}
	/**
	 * Retrieves all user tags from the server and updates the FlowPanel
	 *
	 * @param userId
	 */
	protected void updateTags() {
		GetCommand<TagsResource> tc = new GetCommand<TagsResource>(TagsResource.class, GSS.get().getCurrentUserResource().getTagsPath(),null) {

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
				GSS.get().displayError("Unable to fetch user tags");
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
		GSS.preventIESelection();
		hide();
	}

}
