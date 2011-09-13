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
package gr.grnet.pithos.web.client.grouptree;

import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.commands.AddUserCommand;
import gr.grnet.pithos.web.client.commands.CreateGroupCommand;
import gr.grnet.pithos.web.client.commands.DeleteGroupCommand;
import gr.grnet.pithos.web.client.grouptree.GroupTreeView.Images;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The 'Folder Context' menu implementation.
 */
public class GroupContextMenu extends PopupPanel {

	/**
	 * The widget's images.
	 */
	private final GroupTreeView.Images images;

	/**
	 * The widget's constructor.
	 *
	 * @param newImages the image bundle passed on by the parent object
	 */
	public GroupContextMenu(Pithos app, final Images newImages, Group group) {
		// The popup's constructor's argument is a boolean specifying that it
		// auto-close itself when the user clicks outside of it.
		super(true);
		setAnimationEnabled(true);
		images = newImages;
        MenuBar contextMenu = new MenuBar(true);
        
        if (group != null) {
	        MenuItem addUser = new MenuItem("<span>" + AbstractImagePrototype.create(images.user()).getHTML() + "&nbsp;Add User</span>", true, new AddUserCommand(app, this, group));
	        contextMenu.addItem(addUser);
	
	        MenuItem deleteGroup = new MenuItem("<span>" + AbstractImagePrototype.create(images.delete()).getHTML() + "&nbsp;Delete Group</span>", true, new DeleteGroupCommand(app, this, group));
	        contextMenu.addItem(deleteGroup);
        }
        else {
	        MenuItem createGroup = new MenuItem("<span>" + AbstractImagePrototype.create(images.group()).getHTML() + "&nbsp;Create Group</span>", true, new CreateGroupCommand(app, this));
	        contextMenu.addItem(createGroup);
        }

        add(contextMenu);
	}
}
