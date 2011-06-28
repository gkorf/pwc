/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.foldertree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.foldertree.FolderTreeView.FolderCell;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;
import java.util.Iterator;
import java.util.Set;

public class FolderTreeViewModel implements TreeViewModel {

    private ListDataProvider<Folder> rootDataProvider = new ListDataProvider<Folder>();

    private SingleSelectionModel<Folder> selectionModel;

    public FolderTreeViewModel(SingleSelectionModel<Folder> selectionModel) {
        this.selectionModel = selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            Folder f = new Folder("Loading ...");
            rootDataProvider.getList().add(f);
            return new DefaultNodeInfo<Folder>(rootDataProvider, new FolderCell(), selectionModel, null);
        }
        else {
            final Folder f = (Folder) value;
            final ListDataProvider<Folder> dataProvider = new ListDataProvider<Folder>();
            dataProvider.flush();
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    final GSS app = GSS.get();
                    String path = app.getApiPath() + app.getUsername() + "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + f.getPrefix();
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
                    getFolder.setHeader("X-Auth-Token", app.getToken());
                    Scheduler.get().scheduleDeferred(getFolder);
                }
            });
            return new DefaultNodeInfo<Folder>(dataProvider, new FolderCell(), selectionModel, null);
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

            GSS app = GSS.get();
            String path = app.getApiPath() + app.getUsername() + "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + f.getPrefix();
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
            getFolder.setHeader("X-Auth-Token", app.getToken());
            Scheduler.get().scheduleDeferred(getFolder);
        }
        else {
            dataProvider.getList().clear();
            dataProvider.getList().addAll(folders);
            if (dataProvider.equals(rootDataProvider))
                selectionModel.setSelected(dataProvider.getList().get(0), true);
        }
    }

    public void initialize(AccountResource account) {
        Iterator<Folder> iter = account.getContainers().iterator();
        fetchFolder(iter, rootDataProvider, account.getContainers());
    }

    public Folder getSelection() {
        return selectionModel.getSelectedObject();
    }
}
