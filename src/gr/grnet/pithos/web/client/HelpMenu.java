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
 * The 'Help' menu implementation.
 */
public class HelpMenu extends PopupPanel implements ClickHandler {

	/**
	 * The widget's images.
	 */
	private final Images images;

	private MenuBar contextMenu  = new MenuBar(true);

	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images extends ClientBundle{
		@Source("gr/grnet/pithos/resources/khelpcenter.png")
		ImageResource userGuide();

		@Source("gr/grnet/pithos/resources/linewidth.png")
		ImageResource terms();

		@Source("gr/grnet/pithos/resources/bell.png")
		ImageResource reportAbuse();

		@Source("gr/grnet/pithos/resources/bug.png")
		ImageResource reportBug();

		@Source("gr/grnet/pithos/resources/info.png")
		ImageResource about();

		@Source("gr/grnet/pithos/resources/edit_add.png")
		ImageResource upgradeQuota();
	}

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public HelpMenu(final Images newImages) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;
		createMenu();
		add(contextMenu);
	}

	@Override
	public void onClick(ClickEvent event) {
		HelpMenu menu = new HelpMenu(images);
		int left = event.getRelativeElement().getAbsoluteLeft();
		int top = event.getRelativeElement().getAbsoluteTop() + event.getRelativeElement().getOffsetHeight();
		menu.setPopupPosition(left, top);
		menu.show();
	}

	public MenuBar createMenu() {
		contextMenu.clearItems();
		contextMenu.setAutoOpen(false);
		Command hideCommand = new Command() {
			@Override
			public void execute() {
				hide();
			}
		};
		Command aboutCommand = new Command(){
			@Override
			public void execute() {
				AboutDialog dlg = new AboutDialog();
				dlg.center();
			}
		};
		MenuItem userGuideItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.userGuide()).getHTML() + "&nbsp;<a class='hidden-link' " +
					"href='/userguide/el' target='_blank'>User Guide</a></span>", true, hideCommand);
		contextMenu.addItem(userGuideItem);
		userGuideItem.getElement().setId("topMenu.help.userGuide");
		
		MenuItem termsItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.terms()).getHTML() + "&nbsp;<a class='hidden-link' " +
					"href='/terms' target='_blank'>Terms &amp; Conditions</a></span>", true, hideCommand);
		termsItem.getElement().setId("topMenu.help.terms");
		contextMenu.addItem(termsItem);
		
		MenuItem reportAbuseItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.reportAbuse()).getHTML() + "&nbsp;<a class='hidden-link' " +
					"href='/report-abuse' target='_blank'>Report abuse</a></span>", true, hideCommand);
		reportAbuseItem.getElement().setId("topMenu.help.reportAbuse");
		contextMenu.addItem(reportAbuseItem);
		
		MenuItem upgradeQuotaItem= new MenuItem("<span>" + AbstractImagePrototype.create(images.upgradeQuota()).getHTML() + "&nbsp;<a class='hidden-link' " +
					"href='/pithos/coupon' target='_blank'>Upgrade quota</a></span>", true, hideCommand);
		upgradeQuotaItem.getElement().setId("topMenu.help.upgradeQuota");
		contextMenu.addItem(upgradeQuotaItem);
		
		MenuItem reportBugItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.reportBug()).getHTML() + "&nbsp;<a class='hidden-link' " +
					"href='http://code.google.com/p/pithos/issues/list' target='_blank'>Report bug</a></span>", true, hideCommand);
		reportBugItem.getElement().setId("topMenu.help.reportBug");
		contextMenu.addItem(reportBugItem);
				
		MenuItem aboutItem = new MenuItem("<span>" + AbstractImagePrototype.create(images.about()).getHTML() + "&nbsp;About</span>", true, aboutCommand);
		aboutItem.getElement().setId("topMenu.help.about");
		contextMenu.addItem(aboutItem);
		return contextMenu;
	}

}
