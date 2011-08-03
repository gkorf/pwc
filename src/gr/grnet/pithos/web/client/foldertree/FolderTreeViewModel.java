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

package gr.grnet.pithos.web.client.foldertree;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import gr.grnet.pithos.web.client.FolderContextMenu;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.foldertree.FolderTreeView.Templates;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FolderTreeViewModel implements TreeViewModel {

    private Pithos app;
    
    private Cell<Folder> folderCell = new AbstractCell<Folder>(ContextMenuEvent.getType().getName()) {

       @Override
        public void render(Context context, Folder folder, SafeHtmlBuilder safeHtmlBuilder) {
            String html;
            if (folder.isTrash())
                html = AbstractImagePrototype.create(FolderTreeView.images.emptyTrash()).getHTML();
            else
                html = AbstractImagePrototype.create(FolderTreeView.images.folderYellow()).getHTML();
            safeHtmlBuilder.appendHtmlConstant(html);
            safeHtmlBuilder.append(Templates.INSTANCE.nameSpan(folder.getName()));
        }

        @Override
        public void onBrowserEvent(Cell.Context context, com.google.gwt.dom.client.Element parent, Folder folder, com.google.gwt.dom.client.NativeEvent event, com.google.gwt.cell.client.ValueUpdater<Folder> valueUpdater) {
            if (event.getType().equals(ContextMenuEvent.getType().getName())) {
                FolderTreeViewModel.this.selectionModel.setSelected(folder, true);
                if (!folder.isTrash()) {
                    FolderContextMenu menu = new FolderContextMenu(app, FolderTreeView.images, folder);
                    menu.setPopupPosition(event.getClientX(), event.getClientY());
                    menu.show();
                }
            }
        }
    };

    private ListDataProvider<Folder> rootDataProvider = new ListDataProvider<Folder>();

    private Map<Folder, ListDataProvider<Folder>> dataProviderMap = new HashMap<Folder, ListDataProvider<Folder>>();

    private SingleSelectionModel<Folder> selectionModel;

    public FolderTreeViewModel(Pithos _app, SingleSelectionModel<Folder> selectionModel) {
        app = _app;
        this.selectionModel = selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            Folder f = new Folder("Loading ...");
            rootDataProvider.getList().add(f);
            return new DefaultNodeInfo<Folder>(rootDataProvider, folderCell, selectionModel, null);
        }
        else {
            final Folder f = (Folder) value;
            if (dataProviderMap.get(f) == null) {
                dataProviderMap.put(f, new ListDataProvider<Folder>());
            }
            final ListDataProvider<Folder> dataProvider = dataProviderMap.get(f);
            fetchFolder(f, dataProvider);
            return new DefaultNodeInfo<Folder>(dataProvider, folderCell, selectionModel, null);
        }
    }

    @Override
    public boolean isLeaf(Object o) {
        if (o instanceof Folder) {
            Folder f = (Folder) o;
            return f.getSubfolders().isEmpty();
        }
        return false;
    }

    private void fetchFolder(final Iterator<Folder> iter, final ListDataProvider<Folder> dataProvider, final Set<Folder> folders) {
        if (iter.hasNext()) {
            final Folder f = iter.next();

            String path = "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + f.getPrefix();
            GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, app.getApiPath(), app.getUsername(), path, f) {
                @Override
                public void onSuccess(Folder result) {
                    fetchFolder(iter, dataProvider, folders);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("Error getting folder", t);
                    if (t instanceof RestException)
                        app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                    else
                        app.displayError("System error fetching folder: " + t.getMessage());
                }
            };
            getFolder.setHeader("X-Auth-Token", app.getToken());
            Scheduler.get().scheduleDeferred(getFolder);
        }
        else {
            dataProvider.getList().clear();
            dataProvider.getList().addAll(folders);
            if (dataProvider.equals(rootDataProvider)) {
                selectionModel.setSelected(dataProvider.getList().get(0), true);
                Folder f = new Folder("Trash");
                f.setTrash(true);
                f.setContainer("trash");
                dataProvider.getList().add(f);
                app.updateTags();
            }
        }
    }

    public void initialize(AccountResource account) {
        Iterator<Folder> iter = account.getContainers().iterator();
        fetchFolder(iter, rootDataProvider, account.getContainers());
    }

    public Folder getSelection() {
        return selectionModel.getSelectedObject();
    }

    public void updateFolder(Folder folder) {
        if (dataProviderMap.get(folder) == null) {
            dataProviderMap.put(folder, new ListDataProvider<Folder>());
        }
        final ListDataProvider<Folder> dataProvider = dataProviderMap.get(folder);
        if (!folder.isTrash())
            fetchFolder(folder, dataProvider);
        else
            app.showFiles(folder);
    }

    public void fetchFolder(final Folder f, final ListDataProvider<Folder> dataProvider) {
        dataProvider.flush();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                String path = "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + f.getPrefix();
                GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, app.getApiPath(), app.getUsername(), path, f) {
                    @Override
                    public void onSuccess(Folder result) {
                        app.showFiles(result);
                        Iterator<Folder> iter = result.getSubfolders().iterator();
                        fetchFolder(iter, dataProvider, result.getSubfolders());
                    }

                    @Override
                    public void onError(Throwable t) {
                        GWT.log("Error getting folder", t);
                        if (t instanceof RestException)
                            app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                        else
                            app.displayError("System error fetching folder: " + t.getMessage());
                    }
                };
                getFolder.setHeader("X-Auth-Token", app.getToken());
                Scheduler.get().scheduleDeferred(getFolder);
            }
        });
    }
}
