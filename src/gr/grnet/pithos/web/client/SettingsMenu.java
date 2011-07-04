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
