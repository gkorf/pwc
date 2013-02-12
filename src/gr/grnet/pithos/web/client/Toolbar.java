/*
 * Copyright 2012-2013 GRNET S.A. All rights reserved.
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

import gr.grnet.pithos.web.client.commands.NewFolderCommand;
import gr.grnet.pithos.web.client.commands.PropertiesCommand;
import gr.grnet.pithos.web.client.foldertree.Folder;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class Toolbar extends Composite {

	Pithos app;
	
	Anchor newFolderButton;
	
	Anchor shareFolderButton;
	
	Anchor refreshButton;
	
	private Anchor toolsButton;
	
	ToolsMenu menu;
	
	public Toolbar(final Pithos _app) {
		app = _app;
        FlowPanel toolbar = new FlowPanel();
        toolbar.getElement().setId("toolbar");
        toolbar.addStyleName("clearfix");
        toolbar.getElement().getStyle().setDisplay(Display.BLOCK);

        newFolderButton = new Anchor("<span class='ico'></span><span class='title'>New folder</span>", true);
        newFolderButton.getElement().setId("newfolder-button");
        newFolderButton.addStyleName("pithos-toolbarItem");
        newFolderButton.setVisible(false);
        newFolderButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Folder folder = app.getSelectedTree().getSelection();
				if (folder != null) {
			        Boolean[] permissions = folder.getPermissions().get(app.getUserID());
			    	boolean canWrite = folder.getOwnerID().equals(app.getUserID()) || (permissions!= null && permissions[1] != null && permissions[1]);
			    	
			    	if (!folder.isInTrash() && canWrite)
			    		new NewFolderCommand(app, null, folder).execute();
				}
			}
		});
        toolbar.add(newFolderButton);

        shareFolderButton = new Anchor("<span class='ico'></span><span class='title'>Share folder</span>", true);
        shareFolderButton.getElement().setId("sharefolder-button");
        shareFolderButton.addStyleName("pithos-toolbarItem");
        shareFolderButton.setVisible(false);
        shareFolderButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Folder folder = app.getSelectedTree().getSelection();
				if (folder != null) {
			        Boolean[] permissions = folder.getPermissions().get(app.getUserID());
			    	boolean canWrite = folder.getOwnerID().equals(app.getUserID()) || (permissions!= null && permissions[1] != null && permissions[1]);
			    	boolean isFolderTreeSelected = app.getSelectedTree().equals(app.getFolderTreeView());
			    	
			    	if (!folder.isInTrash() && canWrite && isFolderTreeSelected && !folder.isContainer())
			    		new PropertiesCommand(app, null, folder, PropertiesCommand.PERMISSIONS).execute();
				}
			}
		});
        toolbar.add(shareFolderButton);

        refreshButton = new Anchor("<span class='ico'></span><span class='title'>Refresh</span>", true);
        refreshButton.getElement().setId("refresh-button");
        refreshButton.addStyleName("pithos-toolbarItem");
        refreshButton.setVisible(false);
        refreshButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
		    	boolean isFolderTreeSelected = app.getSelectedTree().equals(app.getFolderTreeView());
		    	boolean otherSharedTreeSelected = app.getSelectedTree().equals(app.getOtherSharedTreeView());
		    	Folder folder = app.getSelectedTree().getSelection();
		    	
		    	if (folder != null) {
		    		if (!app.isMySharedSelected()) {
			    		app.updateFolder(folder, true, new Command() {
			    			
			    			@Override
			    			public void execute() {
			    				app.updateStatistics();
			    			}
			    		}, true);
		    		}
		    		else
		    			app.updateSharedFolder(folder, true);
		    			
		    	}
			}
        });
        toolbar.add(refreshButton);

        toolsButton = new Anchor("<span class='ico'></span><span class='title'>More...</span>", true);
        toolsButton.getElement().setId("tools-button");
        toolsButton.addStyleName("pithos-toolbarItem");
        toolsButton.setVisible(false);
        toolsButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
                if (!menu.isEmpty()) {
		            menu.setPopupPosition(event.getClientX(), event.getClientY());
		            menu.show();
                }
			}
		});
        toolbar.add(toolsButton);
        
        initWidget(toolbar);
	}
	
	public void showRelevantButtons() {
		TreeView selectedTree = app.getSelectedTree();
		if (selectedTree != null) {
			final Folder folder = app.getSelectedTree().getSelection();
			if (folder != null) {
				app.scheduleFolderHeadCommand(folder, new Command() {
					
					@Override
					public void execute() {
				        Boolean[] permissions = folder.getPermissions().get(app.getUserID());
				    	boolean canWrite = folder.getOwnerID().equals(app.getUserID()) || (permissions!= null && permissions[1] != null && permissions[1]);
				    	boolean isFolderTreeSelected = app.getSelectedTree().equals(app.getFolderTreeView());
				    	boolean otherSharedTreeSelected = app.getSelectedTree().equals(app.getOtherSharedTreeView());
				    	
			    		refreshButton.setVisible(true);
				    	
				    	if (!folder.isInTrash() && canWrite) {
				    		if (isFolderTreeSelected || otherSharedTreeSelected)
				    			newFolderButton.setVisible(true);
				    		else
				    			newFolderButton.setVisible(false);
				    		if (isFolderTreeSelected && !folder.isContainer())
				    			shareFolderButton.setVisible(true);
				    		else
				    			shareFolderButton.setVisible(false);
				    	}
				    	else {
				    		newFolderButton.setVisible(false);
				    		shareFolderButton.setVisible(false);
				    	}
					}
				});
			}
			else {
				newFolderButton.setVisible(false);
				shareFolderButton.setVisible(false);
				refreshButton.setVisible(false);
			}
		}
		else {
			newFolderButton.setVisible(false);
			shareFolderButton.setVisible(false);
			refreshButton.setVisible(false);
		}

		if (selectedTree != null) {
	        menu = new ToolsMenu(app, Pithos.images, selectedTree, selectedTree.getSelection(), app.getFileList().getSelectedFiles());
	        if (!menu.isEmpty())
	        	toolsButton.setVisible(true);
	        else
	        	toolsButton.setVisible(false);
		}
    }
}
