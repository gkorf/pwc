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
import gr.grnet.pithos.web.client.SharingUsers;
import gr.grnet.pithos.web.client.foldertree.AccountResource;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.Group;
import gr.grnet.pithos.web.client.grouptree.GroupTreeView.Templates;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

public class GroupTreeViewModel implements TreeViewModel {

    protected Pithos app;

    private Cell<Group> groupCell = new AbstractCell<Group>(ContextMenuEvent.getType().getName()) {

		@Override
		public void render(Context context,	Group value, SafeHtmlBuilder sb) {
            String html = AbstractImagePrototype.create(GroupTreeView.images.group()).getHTML();
            sb.appendHtmlConstant(html);
            sb.append(Templates.INSTANCE.nameSpan(value.getName()));
		}
	};

    private Cell<String> userCell = new AbstractCell<String>(ContextMenuEvent.getType().getName()) {

		@Override
		public void render(Context context,	String value, SafeHtmlBuilder sb) {
            String html = AbstractImagePrototype.create(GroupTreeView.images.user()).getHTML();
            sb.appendHtmlConstant(html);
            sb.append(Templates.INSTANCE.nameSpan(value));
		}
	};

	private ListDataProvider<String> rootDataProvider = new ListDataProvider<String>();
    protected ListDataProvider<Group> groupsDataProvider = new ListDataProvider<Group>();

    protected Map<Group, ListDataProvider<String>> userDataProviderMap = new HashMap<Group, ListDataProvider<String>>();
    
    protected Map<String, Set<File>> sharedFiles = new HashMap<String, Set<File>>();

    private SingleSelectionModel<String> selectionModel;

    public GroupTreeViewModel(Pithos _app, SingleSelectionModel<String> selectionModel) {
        app = _app;
        this.selectionModel = selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            rootDataProvider.getList().add("Groups");
            return new DefaultNodeInfo<String>(rootDataProvider, new TextCell(new SafeHtmlRenderer<String>() {
                @Override
                public SafeHtml render(String object) {
                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
                    render(object, builder);
                    return builder.toSafeHtml();
                }

                @Override
                public void render(String object, SafeHtmlBuilder builder) {
                    String html = AbstractImagePrototype.create(GroupTreeView.images.groups()).getHTML();
                    builder.appendHtmlConstant(html);
                    builder.append(GroupTreeView.Templates.INSTANCE.nameSpan(object));
                }
                
            }),  null, null);
        }
        else if (value instanceof String) {
        	groupsDataProvider.getList().addAll(app.getAccount().getGroups());
            return new DefaultNodeInfo<Group>(groupsDataProvider, groupCell, null, null);
        }
        else { //Group
        	Group g = (Group) value;
			if (userDataProviderMap.get(g) == null) {
				userDataProviderMap.put(g, new ListDataProvider<String>());
			}
			final ListDataProvider<String> dataProvider = userDataProviderMap.get(g);
        	return new DefaultNodeInfo(dataProvider, userCell, null, null);
        }
    }

	@Override
    public boolean isLeaf(Object o) {
        if (o instanceof String && !o.equals("Groups"))
        	return true;
        return false;
    }
}
