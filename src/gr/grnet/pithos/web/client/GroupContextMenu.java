/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.Groups.Images;
import gr.grnet.pithos.web.client.commands.CopyCommand;
import gr.grnet.pithos.web.client.commands.DeleteUserOrGroupCommand;
import gr.grnet.pithos.web.client.commands.NewGroupCommand;
import gr.grnet.pithos.web.client.commands.NewUserCommand;
import gr.grnet.pithos.web.client.commands.PasteCommand;
import gr.grnet.pithos.web.client.rest.resource.GroupResource;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TreeItem;


public class GroupContextMenu extends PopupPanel {

	/**
	 * The widget's images.
	 */
	private final Images images;
	private MenuItem copy;
	private MenuItem paste;
	private MenuItem newGroup;
	private MenuItem addUser;
	private MenuItem delete;
	public GroupContextMenu(final Images newImages) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		images=newImages;
		setAnimationEnabled(true);
		final MenuBar contextMenu = new MenuBar(true);
		newGroup = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.groupNew()).getHTML() + "&nbsp;New Group</span>", true, new NewGroupCommand(this));
		newGroup.getElement().setId("groupContextMenu.newGroup");
		contextMenu.addItem(newGroup);
		
		addUser = new MenuItem("<span>" + AbstractImagePrototype.create(newImages.groupNew()).getHTML() + "&nbsp;Add User</span>", true, new NewUserCommand(this));
		addUser.getElement().setId("groupContextMenu.addUser");
		contextMenu.addItem(addUser);
				
		copy = new MenuItem("<span id=groupContextMenu.copyUser>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy User</span>", true, new CopyCommand(this));
		copy.getElement().setId("groupContextMenu.copyUser");
		contextMenu.addItem(copy);
		
		paste = new MenuItem("<span id=groupContextMenu.pasteUser>" + AbstractImagePrototype.create(newImages.paste()).getHTML() + "&nbsp;Paste User</span>", true, new PasteCommand(this));
		paste.getElement().setId("groupContextMenu.pasteUser");
		contextMenu.addItem(paste);
		
		delete = new MenuItem("<span id=groupContextMenu.delete>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Delete</span>", true, new DeleteUserOrGroupCommand(this,images));
		delete.getElement().setId("groupContextMenu.delete");
		contextMenu.addItem(delete);
		
		add(contextMenu);

	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.PopupPanel#show()
	 */
	@Override
	public void show() {
		TreeItem current = GSS.get().getGroups().getCurrent();
		if(current==null){
			copy.setVisible(false);
			paste.setVisible(false);
			addUser.setVisible(false);
			delete.setVisible(false);
		}
		else{
			newGroup.setVisible(false);
			if(current.getUserObject() instanceof GroupUserResource && GSS.get().getCurrentSelection() instanceof GroupUserResource)
				copy.setVisible(true);
			else
				copy.setVisible(false);
			if(current.getUserObject() instanceof GroupResource && GSS.get().getCurrentSelection() instanceof GroupResource && GSS.get().getClipboard().hasUserItem())
				paste.setVisible(true);
			else
				paste.setVisible(false);
		}
		super.show();
	}

}
