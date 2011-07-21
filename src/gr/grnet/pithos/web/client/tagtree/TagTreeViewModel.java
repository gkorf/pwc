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

package gr.grnet.pithos.web.client.tagtree;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.foldertree.AccountResource;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.tagtree.TagTreeView.Templates;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TagTreeViewModel implements TreeViewModel {

    private Cell<Tag> tagCell = new AbstractCell<Tag>(ContextMenuEvent.getType().getName()) {

       @Override
        public void render(Context context, Tag tag, SafeHtmlBuilder safeHtmlBuilder) {
            String html = AbstractImagePrototype.create(TagTreeView.images.tag()).getHTML();
            safeHtmlBuilder.appendHtmlConstant(html);
            safeHtmlBuilder.append(Templates.INSTANCE.nameSpan(tag.getName()));
        }

        @Override
        public void onBrowserEvent(Context context, com.google.gwt.dom.client.Element parent, Tag tag, com.google.gwt.dom.client.NativeEvent event, com.google.gwt.cell.client.ValueUpdater<Tag> valueUpdater) {
            if (event.getType().equals(com.google.gwt.event.dom.client.ContextMenuEvent.getType().getName())) {
                TagTreeViewModel.this.selectionModel.setSelected(tag, true);
            }
        }
    };

    private ListDataProvider<String> rootDataProvider = new ListDataProvider<String>();
    private ListDataProvider<Tag> tagDataProvider = new ListDataProvider<Tag>();

    private SingleSelectionModel<Tag> selectionModel;

    public TagTreeViewModel(SingleSelectionModel<Tag> selectionModel) {
        this.selectionModel = selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            return new DefaultNodeInfo<String>(rootDataProvider, new TextCell(new SafeHtmlRenderer<String>() {
                @Override
                public SafeHtml render(String object) {
                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
                    render(object, builder);
                    return builder.toSafeHtml();
                }

                @Override
                public void render(String object, SafeHtmlBuilder builder) {
                    String html = AbstractImagePrototype.create(TagTreeView.images.tag()).getHTML();
                    builder.appendHtmlConstant(html);
                    builder.append(Templates.INSTANCE.nameSpan(object));
                }
            }), null, null);
        }
        else if (value instanceof String) {
            //fetchAllTags();
            return new DefaultNodeInfo<Tag>(tagDataProvider, tagCell, selectionModel, null);
        }
        else
            return null;
    }

    @Override
    public boolean isLeaf(Object o) {
        if (o == null)
            return false;
        if (o instanceof String)
            return tagDataProvider.getList().isEmpty();
        else
            return true;
    }

    private void fetchTags(final Iterator<Folder> iter) {
        final Pithos app = Pithos.get();
        if (iter.hasNext()) {
            final Folder f = iter.next();

            String path = app.getApiPath() + app.getUsername() + "/" + f.getUri() + "?format=json&delimiter=/&prefix=" + f.getPrefix();
//            GetRequest<Tag> getFolder = new GetRequest<Tag>(Tag.class, path, f) {
//                @Override
//                public void onSuccess(Tag result) {
//                    fetchFolder(iter, dataProvider, tags);
//                }
//
//                @Override
//                public void onError(Throwable t) {
//                    GWT.log("Error getting folder", t);
//                    if (t instanceof RestException)
//                        Pithos.get().displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
//                    else
//                        Pithos.get().displayError("System error fetching folder: " + t.getMessage());
//                }
//            };
//            getFolder.setHeader("X-Auth-Token", app.getToken());
//            Scheduler.get().scheduleDeferred(getFolder);
        }
    }
//
//    public void initialize(AccountResource account) {
//        Iterator<Tag> iter = account.getContainers().iterator();
//        fetchFolder(iter, rootDataProvider, account.getContainers());
//    }
//
    public Tag getSelection() {
        return selectionModel.getSelectedObject();
    }

    public void updateTag(Tag tag) {
        fetchTag(tag);
    }

    public void fetchTag(Tag t) {
        Pithos app = Pithos.get();
        AccountResource account = app.getAccount();
        Iterator<Folder> iter = account.getContainers().iterator();
        fetchTag(iter, t, new LinkedHashSet<File>());
    }

    private void fetchTag(final Iterator<Folder> iter, final Tag t, final Set<File> files) {
        final Pithos app = Pithos.get();
        if (iter.hasNext()) {
            Folder f = iter.next();
            String path = app.getApiPath() + app.getUsername() + f.getUri() + "?format=json&meta=" + t.getName();
            GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, path) {
                @Override
                public void onSuccess(Folder result) {
                    files.addAll(result.getFiles());
                    fetchTag(iter, t, files);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("Error getting folder", t);
                    if (t instanceof RestException)
                        Pithos.get().displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                    else
                        Pithos.get().displayError("System error fetching folder: " + t.getMessage());
                }
            };
            getFolder.setHeader("X-Auth-Token", app.getToken());
            Scheduler.get().scheduleDeferred(getFolder);
        }
        else {
            app.showFiles(files);
        }
    }

    public void initialize(List<Tag> allTags) {
        tagDataProvider.getList().addAll(allTags);
        rootDataProvider.getList().add("Tags");
    }
}
