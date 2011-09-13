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
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;

public class GroupTreeViewModel implements TreeViewModel {

    protected Pithos app;

    private ListDataProvider<String> rootDataProvider = new ListDataProvider<String>();
    
    private Cell<String> rootCell = new AbstractCell<String>(ContextMenuEvent.getType().getName()) {

		@Override
		public void render(@SuppressWarnings("unused") Context context,	String value, SafeHtmlBuilder sb) {
            String html = AbstractImagePrototype.create(GroupTreeView.images.groups()).getHTML();
            sb.appendHtmlConstant(html);
            sb.append(Templates.INSTANCE.nameSpan(value));
		}
		
        @Override
        public void onBrowserEvent(@SuppressWarnings("unused") Cell.Context context, @SuppressWarnings("unused") com.google.gwt.dom.client.Element parent, String s, com.google.gwt.dom.client.NativeEvent event, @SuppressWarnings("unused") com.google.gwt.cell.client.ValueUpdater<String> valueUpdater) {
            if (event.getType().equals(ContextMenuEvent.getType().getName())) {
                GroupContextMenu menu = new GroupContextMenu(app, GroupTreeView.images, null);
                menu.setPopupPosition(event.getClientX(), event.getClientY());
                menu.show();
            }
        }
	};

	private Cell<Group> groupCell = new AbstractCell<Group>(ContextMenuEvent.getType().getName()) {

		@Override
		public void render(@SuppressWarnings("unused") Context context,	Group value, SafeHtmlBuilder sb) {
            String html = AbstractImagePrototype.create(GroupTreeView.images.group()).getHTML();
            sb.appendHtmlConstant(html);
            sb.append(Templates.INSTANCE.nameSpan(value.getName()));
		}
		
        @Override
        public void onBrowserEvent(@SuppressWarnings("unused") Cell.Context context, @SuppressWarnings("unused") com.google.gwt.dom.client.Element parent, Group group, com.google.gwt.dom.client.NativeEvent event, @SuppressWarnings("unused") com.google.gwt.cell.client.ValueUpdater<Group> valueUpdater) {
            if (event.getType().equals(ContextMenuEvent.getType().getName())) {
                GroupContextMenu menu = new GroupContextMenu(app, GroupTreeView.images, group);
                menu.setPopupPosition(event.getClientX(), event.getClientY());
                menu.show();
            }
        }
	};

    private Cell<User> userCell = new AbstractCell<User>(ContextMenuEvent.getType().getName()) {

		@Override
		public void render(@SuppressWarnings("unused") Context context,	User value, SafeHtmlBuilder sb) {
            String html = AbstractImagePrototype.create(GroupTreeView.images.user()).getHTML();
            sb.appendHtmlConstant(html);
            sb.append(Templates.INSTANCE.nameSpan(value.getName()));
		}

        @Override
        public void onBrowserEvent(@SuppressWarnings("unused") Cell.Context context, @SuppressWarnings("unused") com.google.gwt.dom.client.Element parent, User user, com.google.gwt.dom.client.NativeEvent event, @SuppressWarnings("unused") com.google.gwt.cell.client.ValueUpdater<User> valueUpdater) {
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

    public GroupTreeViewModel(Pithos _app) {
        app = _app;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
        	rootDataProvider.getList().add("");
            return new DefaultNodeInfo<String>(rootDataProvider, rootCell,  null, null);
        }
        else if (value instanceof String) {
        	groupsDataProvider.getList().addAll(app.getAccount().getGroups());
            return new DefaultNodeInfo<Group>(groupsDataProvider, groupCell, null, null);
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
        	return new DefaultNodeInfo<User>(dataProvider, userCell, null, null);
        }
    }

	@Override
    public boolean isLeaf(Object o) {
        if (o instanceof String) {
       		return ((String) o).length() == 0 || app.getAccount().getGroups().isEmpty();
        }
        else if (o instanceof Group)
        	return ((Group) o).getMembers().isEmpty();
        else
        	return true;
    }
	
	public void initialize() {
    	rootDataProvider.getList().clear();
    	rootDataProvider.getList().add("Groups");
	}
}
