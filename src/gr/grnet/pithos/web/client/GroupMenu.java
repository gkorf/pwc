/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.commands.NewGroupCommand;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'Group' menu implementation.
 */
public class GroupMenu extends PopupPanel implements ClickHandler {
	/**
	 * The widget's images.
	 */
	private Images images;
	private final MenuBar contextMenu;

	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ClientBundle {
		@Source("gr/grnet/pithos/resources/groupevent.png")
		ImageResource groupNew();

		@Source("gr/grnet/pithos/resources/view_text.png")
		ImageResource viewText();

	}

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public GroupMenu(final Images newImages) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;

		contextMenu = new MenuBar(true);
		MenuItem newGroupItem = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.groupNew()).getHTML() + "&nbsp;New Group</span>", true, new NewGroupCommand(this));
		newGroupItem.getElement().setId("topMenu.group.newGroup");
		contextMenu.addItem(newGroupItem);
		
		add(contextMenu);
	}

	@Override
	public void onClick(ClickEvent event) {
		GroupMenu menu = new GroupMenu(images);
		int left = event.getRelativeElement().getAbsoluteLeft();
		int top = event.getRelativeElement().getAbsoluteTop() + event.getRelativeElement().getOffsetHeight();
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
