/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.foldertree;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.foldertree.FolderTreeView.FolderCell;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gwtquery.plugins.droppable.client.gwt.DragAndDropNodeInfo;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FolderTreeViewModel implements TreeViewModel {

    private SingleSelectionModel<Folder> selectionModel;

    public FolderTreeViewModel(SingleSelectionModel<Folder> selectionModel) {
        this.selectionModel = selectionModel;
    }
    
    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            final ListDataProvider<Folder> dataProvider = new ListDataProvider<Folder>();
            Folder f = new Folder("Loading ...");
            dataProvider.getList().add(f);
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    fetchAccount(dataProvider);
                }
            });
            return new DragAndDropNodeInfo<Folder>(dataProvider, new FolderCell(), selectionModel, null);
        }
        else {
            final Folder f = (Folder) value;
            final ListDataProvider<Folder> dataProvider = new ListDataProvider<Folder>();
            dataProvider.getList().addAll(f.getSubfolders());
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    final GSS app = GSS.get();
                    String container = f.getContainer() == null ? f.getName() : f.getContainer();
                    String prefix = f.getContainer() == null ? "" : f.getPrefix();
                    String path = app.getApiPath() + app.getUsername() + "/" + container + "?format=json&delimiter=/&prefix=" + prefix;
                    GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, path, f) {
                        @Override
                        public void onSuccess(Folder result) {
                            Iterator<Folder> iter = result.getSubfolders().iterator();
                            fetchFolder(iter, dataProvider, result.getSubfolders());
                        }

                        @Override
                        public void onError(Throwable t) {
                            GWT.log("Error getting folder", t);
                            if (t instanceof RestException)
                                GSS.get().displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                            else
                                GSS.get().displayError("System error fetching folder: " + t.getMessage());
                        }
                    };
                    Scheduler.get().scheduleDeferred(getFolder);
                }
            });
            return new DragAndDropNodeInfo<Folder>(dataProvider, new FolderCell(), selectionModel, null);
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

    public void fetchAccount(final ListDataProvider<Folder> dataProvider) {
        final GSS app = GSS.get();
        String path = app.getApiPath() + app.getUsername() + "?format=json";

        GetRequest<AccountResource> getAccount = new GetRequest<AccountResource>(AccountResource.class, path) {
            @Override
            public void onSuccess(AccountResource result) {
                app.setAccount(result);
                Iterator<Folder> iter = result.getContainers().iterator();
                fetchFolder(iter, dataProvider, result.getContainers());
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error getting account", t);
                if (t instanceof RestException)
                    GSS.get().displayError("Error getting account: " + ((RestException) t).getHttpStatusText());
                else
                    GSS.get().displayError("System error fetching user data: " + t.getMessage());
            }
        };

        Scheduler.get().scheduleDeferred(getAccount);
    }

    private void fetchFolder(final Iterator<Folder> iter, final ListDataProvider<Folder> dataProvider, final Set<Folder> folders) {
        if (iter.hasNext()) {
            final Folder f = iter.next();

            GSS app = GSS.get();
            String container = f.getContainer() == null ? f.getName() : f.getContainer();
            String prefix = f.getContainer() == null ? "" : f.getPrefix();
            String path = app.getApiPath() + app.getUsername() + "/" + container + "?format=json&delimiter=/&prefix=" + prefix;
            GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, path, f) {
                @Override
                public void onSuccess(Folder result) {
                    fetchFolder(iter, dataProvider, folders);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("Error getting folder", t);
                    if (t instanceof RestException)
                        GSS.get().displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                    else
                        GSS.get().displayError("System error fetching folder: " + t.getMessage());
                }
            };
            Scheduler.get().scheduleDeferred(getFolder);
        }
        else {
            dataProvider.getList().clear();
            dataProvider.getList().addAll(folders);
        }
    }
}
