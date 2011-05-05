/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'settings' menu implementation.
 */
public class SettingsMenu extends PopupPanel implements ClickHandler {

	/**
	 * The widget's images.
	 */
	private Images images;
	private final MenuBar contextMenu;
	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ClientBundle,MessagePanel.Images {

		/**
		 * Will bundle the file 'advancedsettings.png' residing in the package
		 * 'gr.grnet.pithos.web.resources'.
		 *
		 * @return the image prototype
		 */
		@Source("gr/grnet/pithos/resources/advancedsettings.png")
		ImageResource preferences();

		@Source("gr/grnet/pithos/resources/lock.png")
		ImageResource credentials();

	}

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public SettingsMenu(final Images newImages) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;

		Command userCredentialsCommand = new Command(){
			@Override
			public void execute() {
				CredentialsDialog dlg = new CredentialsDialog(newImages);
				dlg.center();
			}
		};
		contextMenu = new MenuBar(true);
//		contextMenu.addItem("<span>" + newImages.preferences().getHTML() + "&nbsp;Preferences</span>", true, cmd);
		MenuItem showCredentialsItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.credentials()).getHTML() + "&nbsp;Show Credentials</span>", true, userCredentialsCommand);
		showCredentialsItem.getElement().setId("topMenu.settingsMenu.showCredentials");
		contextMenu.addItem(showCredentialsItem);
		
		add(contextMenu);
		// setStyleName("toolbarPopup");
	}

	@Override
	public void onClick(final ClickEvent event) {
		final SettingsMenu menu = new SettingsMenu(images);
		final int left = event.getRelativeElement().getAbsoluteLeft();
		final int top = event.getRelativeElement().getAbsoluteTop() + event.getRelativeElement().getOffsetHeight();
		menu.setPopupPosition(left, top);

		menu.show();
	}


	/**
	 * Retrieve the contextMenu.
	 *
	 * @return the contextMenu
	 */
	public MenuBar getContextMenu() {
		contextMenu.setAutoOpen(false);
		return contextMenu;
	}


}
