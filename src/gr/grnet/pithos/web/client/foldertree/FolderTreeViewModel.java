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
import gwtquery.plugins.droppable.client.gwt.DragAndDropNodeInfo;

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
            final ListDataProvider<Folder> dataProvider = new ListDataProvider<Folder>();
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
        GSS app = GSS.get();
        String path = app.getApiPath() + app.getUsername() + "?format=json";

        GetRequest<AccountResource> getAccount = new GetRequest<AccountResource>(AccountResource.class, path) {
            @Override
            public void onSuccess(AccountResource result) {
                dataProvider.getList().clear();
                for (ContainerResource c : result.getContainers()) {
                    dataProvider.getList().add(new Folder(c.getName()));
                }
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
}
