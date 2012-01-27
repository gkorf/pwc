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
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.grouptree.GroupTreeView.Templates;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

public class GroupTreeViewModel implements TreeViewModel {

    protected Pithos app;

    private ListDataProvider<String> rootDataProvider = new ListDataProvider<String>();
    
    private Cell<String> rootCell = new AbstractCell<String>(ContextMenuEvent.getType().getName()) {

		@Override
		public void render(Context context,	String value, SafeHtmlBuilder sb) {
            String html = AbstractImagePrototype.create(GroupTreeView.images.groups()).getHTML();
            sb.appendHtmlConstant(html).appendHtmlConstant("&nbsp;");
            sb.append(Templates.INSTANCE.nameSpan(value));
		}
		
        @Override
        public void onBrowserEvent(Cell.Context context, com.google.gwt.dom.client.Element parent, String s, com.google.gwt.dom.client.NativeEvent event, com.google.gwt.cell.client.ValueUpdater<String> valueUpdater) {
            GroupTreeViewModel.this.rootSelectionModel.setSelected(s, true);
            if (event.getType().equals(ContextMenuEvent.getType().getName())) {
                GroupContextMenu menu = new GroupContextMenu(app, GroupTreeView.images, null);
                menu.setPopupPosition(event.getClientX(), event.getClientY());
                menu.show();
            }
        }
	};

	private Cell<Group> groupCell = new AbstractCell<Group>(ContextMenuEvent.getType().getName()) {

		@Override
		public void render(Context context,	Group value, SafeHtmlBuilder sb) {
            String html = AbstractImagePrototype.create(GroupTreeView.images.group()).getHTML();
            sb.appendHtmlConstant(html).appendHtmlConstant("&nbsp;");
            sb.append(Templates.INSTANCE.nameSpan(value.getName()));
		}
		
        @Override
        public void onBrowserEvent(Cell.Context context, com.google.gwt.dom.client.Element parent, Group group, com.google.gwt.dom.client.NativeEvent event, com.google.gwt.cell.client.ValueUpdater<Group> valueUpdater) {
            GroupTreeViewModel.this.groupSelectionModel.setSelected(group, true);
            if (event.getType().equals(ContextMenuEvent.getType().getName())) {
                GroupContextMenu menu = new GroupContextMenu(app, GroupTreeView.images, group);
                menu.setPopupPosition(event.getClientX(), event.getClientY());
                menu.show();
            }
        }
	};

    private Cell<User> userCell = new AbstractCell<User>(ContextMenuEvent.getType().getName()) {

		@Override
		public void render(Context context,	User value, SafeHtmlBuilder sb) {
            String html = AbstractImagePrototype.create(GroupTreeView.images.user()).getHTML();
            sb.appendHtmlConstant(html).appendHtmlConstant("&nbsp;");
            sb.append(Templates.INSTANCE.nameSpan(value.getName()));
		}

        @Override
        public void onBrowserEvent(Cell.Context context, com.google.gwt.dom.client.Element parent, User user, com.google.gwt.dom.client.NativeEvent event, com.google.gwt.cell.client.ValueUpdater<User> valueUpdater) {
            GroupTreeViewModel.this.userSelectionModel.setSelected(user, true);
            if (event.getType().equals(ContextMenuEvent.getType().getName())) {
                UserContextMenu menu = new UserContextMenu(app, GroupTreeView.images, user);
                menu.setPopupPosition(event.getClientX(), event.getClientY());
                menu.show();
            }
        }
    };

    protected ListDataProvider<Group> groupsDataProvider = new ListDataProvider<Group>();

    protected Map<Group, ListDataProvider<User>> userDataProviderMap = new HashMap<Group, ListDataProvider<User>>();
    
    protected Map<String, Set<File>> sharedFiles = new HashMap<String, Set<File>>();

    SingleSelectionModel<String> rootSelectionModel;
    SingleSelectionModel<Group> groupSelectionModel;
    SingleSelectionModel<User> userSelectionModel;

    public GroupTreeViewModel(Pithos _app) {
        app = _app;
        rootSelectionModel = new SingleSelectionModel<String>();
    	app.addSelectionModel(rootSelectionModel);
    	rootSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				if (rootSelectionModel.getSelectedObject() != null) {
					app.deselectOthers(app.getGroupTreeView(), rootSelectionModel);
					app.showFiles(new HashSet<File>());
				}
			}
		});

    	groupSelectionModel = new SingleSelectionModel<Group>();
    	app.addSelectionModel(groupSelectionModel);
    	groupSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				if (groupSelectionModel.getSelectedObject() != null) {
					app.deselectOthers(app.getGroupTreeView(), groupSelectionModel);
					app.showFiles(new HashSet<File>());
				}
			}
		});

    	userSelectionModel = new SingleSelectionModel<User>();
    	app.addSelectionModel(userSelectionModel);
    	userSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				if (userSelectionModel.getSelectedObject() != null) {
					app.deselectOthers(app.getGroupTreeView(), userSelectionModel);
					app.showFiles(new HashSet<File>());
				}
			}
		});
}

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
        	rootDataProvider.getList().add("");
            return new DefaultNodeInfo<String>(rootDataProvider, rootCell,  rootSelectionModel, null);
        }
        else if (value instanceof String) {
        	groupsDataProvider.getList().clear();
        	groupsDataProvider.getList().addAll(app.getAccount().getGroups());
            return new DefaultNodeInfo<Group>(groupsDataProvider, groupCell, groupSelectionModel, null);
        }
        else { //Group
        	Group g = (Group) value;
			if (userDataProviderMap.get(g) == null) {
				userDataProviderMap.put(g, new ListDataProvider<User>());
			}
			final ListDataProvider<User> dataProvider = userDataProviderMap.get(g);
			dataProvider.getList().clear();
			for (String u : g.getMembers())
				dataProvider.getList().add(new User(u, g));
        	return new DefaultNodeInfo<User>(dataProvider, userCell, userSelectionModel, null);
        }
    }

	@Override
    public boolean isLeaf(Object o) {
        if (o instanceof String) {
       		return ((String) o).length() == 0 || app.getAccount().getGroups().isEmpty();
        }
        else if (o instanceof Group)
        	return ((Group) o).getMembers().isEmpty();
        else if (o != null)
        	return true;
        return false;
    }
	
	public void initialize() {
    	rootDataProvider.getList().clear();
    	rootDataProvider.getList().add("Groups");
	}

	public void updateGroupNode(Group group) {
		if (group == null) {
			groupsDataProvider.getList().clear();
			groupsDataProvider.getList().addAll(app.getAccount().getGroups());
		}
		else {
			if (userDataProviderMap.get(group) == null) {
				userDataProviderMap.put(group, new ListDataProvider<User>());
			}
			final ListDataProvider<User> dataProvider = userDataProviderMap.get(group);
			dataProvider.getList().clear();
			for (String u : group.getMembers())
				dataProvider.getList().add(new User(u, group));
		}
	}

	public Object getSelectedObject() {
		if (rootSelectionModel.getSelectedObject() != null)
			return rootSelectionModel.getSelectedObject();
		if (groupSelectionModel.getSelectedObject() != null)
			return groupSelectionModel.getSelectedObject();
		if (userSelectionModel.getSelectedObject() != null)
			return userSelectionModel.getSelectedObject();
		return null;
	}
}
