/*
 * Copyright 2011-2013 GRNET S.A. All rights reserved.
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

package gr.grnet.pithos.web.client.othersharedtree;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import gr.grnet.pithos.web.client.Const;
import gr.grnet.pithos.web.client.FolderContextMenu;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.SharingUsers;
import gr.grnet.pithos.web.client.catalog.UpdateUserCatalogs;
import gr.grnet.pithos.web.client.catalog.UserCatalogs;
import gr.grnet.pithos.web.client.foldertree.AccountResource;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.othersharedtree.OtherSharedTreeView.Templates;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OtherSharedTreeViewModel implements TreeViewModel {

    protected Pithos app;

    final String dummy = "No shares by others yet";

    private Cell<Folder> folderCell = new AbstractCell<Folder>(ContextMenuEvent.getType().getName()) {

        @Override
        public void render(Context context, Folder folder, SafeHtmlBuilder safeHtmlBuilder) {
//            app.LOG("OtherSharedTreeViewModel::render(), folder=", folder);
            String html = AbstractImagePrototype.create(OtherSharedTreeView.images.folderYellow()).getHTML();
            safeHtmlBuilder.appendHtmlConstant(html).appendHtmlConstant("&nbsp;");
            safeHtmlBuilder.append(Templates.INSTANCE.nameSpan(folder.getName()));
        }

        @Override
        public void onBrowserEvent(Context context, com.google.gwt.dom.client.Element parent, final Folder folder, com.google.gwt.dom.client.NativeEvent event, ValueUpdater<Folder> valueUpdater) {
            app.LOG("OtherSharedTreeViewModel::render(), folder=", folder, ", parent=", parent.getString());
            if(event.getType().equals(ContextMenuEvent.getType().getName())) {
                final int x = event.getClientX();
                final int y = event.getClientY();
                OtherSharedTreeViewModel.this.selectionModel.setSelected(folder, true);
                app.scheduleFolderHeadCommand(folder, new Command() {

                    @Override
                    public void execute() {
                        FolderContextMenu menu = new FolderContextMenu(app, OtherSharedTreeView.images, app.getSelectedTree(), folder);
                        menu.setPopupPosition(x, y);
                        menu.show();
                    }
                });
            }
        }
    };

    protected ListDataProvider<String> userLevelDataProviderForIDs = new ListDataProvider<String>();

    protected Map<String, ListDataProvider<Folder>> userDataProviderMap = new HashMap<String, ListDataProvider<Folder>>();
    private Map<Folder, ListDataProvider<Folder>> dataProviderMap = new HashMap<Folder, ListDataProvider<Folder>>();

    protected SingleSelectionModel<Folder> selectionModel;

    public OtherSharedTreeViewModel(Pithos _app, SingleSelectionModel<Folder> selectionModel) {
        app = _app;
        this.selectionModel = selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if(value == null) {
            app.LOG("OtherSharedTreeViewModel::getNodeInfo(null), calling fetchSharingUsers(null)");
            fetchSharingUsers(null);
            app.LOG("OtherSharedTreeViewModel::getNodeInfo(null), called fetchSharingUsers(null)");
            return new DefaultNodeInfo<String>(userLevelDataProviderForIDs, new TextCell(new SafeHtmlRenderer<String>() {
                @Override
                public SafeHtml render(String object) {
                    final String displayName = app.getDisplayNameForUserID(object);
                    app.LOG("OtherSharedTreeViewModel::(getNodeInfo)render(String ", object, "), parameter is userID, displayName=", displayName);
                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
                    render(displayName, builder);
                    return builder.toSafeHtml();
                }

                @Override
                public void render(String object, SafeHtmlBuilder builder) {
                    if(!object.equals(dummy)) {
                        app.LOG("OtherSharedTreeViewModel::(getNodeInfo)render(String ", object, ") parameter is not [dummy]");
                        String html = AbstractImagePrototype.create(OtherSharedTreeView.images.myShared()).getHTML();
                        builder.appendHtmlConstant(html).appendHtmlConstant("&nbsp;");
                    }
                    builder.append(OtherSharedTreeView.Templates.INSTANCE.nameSpan(object));
                }
            }), null, null);
        }
        else if(value instanceof String) {
            app.LOG("OtherSharedTreeViewModel::getNodeInfo(String ", value, "), parameter is userID");
            final String userID = (String) value;

            if(userDataProviderMap.get(userID) == null) {
                userDataProviderMap.put(userID, new ListDataProvider<Folder>());
            }
            final ListDataProvider<Folder> userIDDataProvider = userDataProviderMap.get(userID);
            fetchSharedContainers(userID, userIDDataProvider, null);

            return new DefaultNodeInfo<Folder>(userIDDataProvider, folderCell, selectionModel, null);
        }
        else {
            app.LOG("OtherSharedTreeViewModel::getNodeInfo(Folder ", value, ")");
            final Folder f = (Folder) value;
            if(dataProviderMap.get(f) == null) {
                dataProviderMap.put(f, new ListDataProvider<Folder>());
            }
            final ListDataProvider<Folder> dataProvider = dataProviderMap.get(f);
            fetchFolder(f, dataProvider, false, null);
            return new DefaultNodeInfo<Folder>(dataProvider, folderCell, selectionModel, null);
        }
    }

    private void fetchSharingUsers(final Command callback) {
        app.LOG("OtherSharedTreeViewModel::fetchSharingUsers() callback=", callback);
        GetRequest<SharingUsers> getSharingUsers = new GetRequest<SharingUsers>(SharingUsers.class, Pithos.getStorageAPIURL(), "", "?format=json") {
            @Override
            public void onSuccess(final SharingUsers _result) {
                userLevelDataProviderForIDs.getList().clear();

                final List<String> userIDs = _result.getUserIDs();
                for(String userID : userIDs) {
                    app.LOG("OtherSharedTreeViewModel::fetchSharingUsers()::onSuccess() Sharing userID=", userID);
                }
                final List<String> userIDsWithUnknownDisplayNames = app.filterUserIDsWithUnknownDisplayName(userIDs);
//                for(String userID : userIDsWithUnknownDisplayNames) {
//                    app.LOG("OtherSharedTreeViewModel::fetchSharingUsers(): userID (with unknown name) =" + userID);
//                }

                userLevelDataProviderForIDs.getList().addAll(userIDs);
                if(userLevelDataProviderForIDs.getList().isEmpty()) {
                    userLevelDataProviderForIDs.getList().add(dummy);
                    app.LOG("OtherSharedTreeViewModel::fetchSharingUsers()::onSuccess() NO sharing users, adding [dummy]=\"", dummy, "\"");
                    return; // Only the dummy node is present, nothing to fetch from the server
                }

                if(userIDsWithUnknownDisplayNames.size() == 0) {
                    // All display names are known, so we proceed directly
                    fetchSharedContainers(userLevelDataProviderForIDs.getList().iterator(), callback);
                } else {
                    // First fetch unknown display names and then proceed
                    new UpdateUserCatalogs(app, userIDsWithUnknownDisplayNames) {
                        @Override
                        public void onSuccess(UserCatalogs requestedUserCatalogs, UserCatalogs updatedUserCatalogs) {
                            fetchSharedContainers(userLevelDataProviderForIDs.getList().iterator(), callback);
                        }

                        @Override
                        public void onError(Request request, Throwable t) {
                            super.onError(request, t);
                            app.setError(t);
                        }
                    }.scheduleDeferred();
                }
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error getting folder", t);
                app.setError(t);
                if(t instanceof RestException) {
                    app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                }
                else {
                    app.displayError("System error fetching folder: " + t.getMessage());
                }
            }

            @Override
            protected void onUnauthorized(Response response) {
                app.sessionExpired();
            }
        };
        getSharingUsers.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());
        Scheduler.get().scheduleDeferred(getSharingUsers);
    }

    protected void fetchSharedContainers(final Iterator<String> userIDsIterator, final Command callback) {
        app.LOG("OtherSharedTreeViewModel::fetchSharedContainers(), userIDsIterator=", userIDsIterator.hasNext(), ", callback=", callback);
        if(userIDsIterator.hasNext()) {
            String userID = userIDsIterator.next();
            if(userDataProviderMap.get(userID) == null) {
                userDataProviderMap.put(userID, new ListDataProvider<Folder>());
            }
            final ListDataProvider<Folder> dataProvider = userDataProviderMap.get(userID);
            fetchSharedContainers(userID, dataProvider, new Command() {

                @Override
                public void execute() {
                    fetchSharedContainers(userIDsIterator, callback);

                }
            });
        }
        else if(callback != null) {
            callback.execute();
        }
    }

    @Override
    public boolean isLeaf(Object o) {
        if(o == null) {
            boolean result =  userLevelDataProviderForIDs.getList().isEmpty();
//            app.LOG("isLeaf(null) ==> " + result);
            return result;
        }
        else if(o instanceof Folder) {
            Folder folder = (Folder) o;
            boolean result = folder.getSubfolders().isEmpty();
//            app.LOG("isLeaf(Folder "+folder+") ==> " + result);
            return result;
        }
        else {
            ListDataProvider<Folder> dp = userDataProviderMap.get(o);
            if(dp != null) {
                boolean result =  dp.getList().isEmpty();
//                app.LOG("isLeaf("+o.getClass().getName()+" "+o+") ==> " + result);
                return result;
            }
//            app.LOG("isLeaf("+o.getClass().getName()+" "+o+") ==> (true)");
            return true;
        }
    }

    private void fetchSharedContainers(final String userID, final ListDataProvider<Folder> dataProvider, final Command callback) {
        app.LOG("OtherSharedTreeViewModel::fetchSharedContainers(), userID=", userID, ", callback=", callback);
        GetRequest<AccountResource> getUserSharedContainers = new GetRequest<AccountResource>(AccountResource.class, Pithos.getStorageAPIURL(), userID, "?format=json") {

            @Override
            public void onSuccess(AccountResource _result) {
                final ListDataProvider<Folder> tempProvider = new ListDataProvider<Folder>();
                Iterator<Folder> folderIterator = _result.getContainers().iterator();
                fetchFolder(userID, folderIterator, tempProvider, new Command() {

                    @Override
                    public void execute() {
                        dataProvider.getList().clear();
                        dataProvider.getList().addAll(tempProvider.getList());
                        if(callback != null) {
                            callback.execute();
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error getting account", t);
                app.setError(t);
                if(t instanceof RestException) {
                    app.displayError("Error getting account: " + ((RestException) t).getHttpStatusText());
                }
                else {
                    app.displayError("System error fetching user data: " + t.getMessage());
                }
            }

            @Override
            protected void onUnauthorized(Response response) {
                app.sessionExpired();
            }
        };
        getUserSharedContainers.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());
        Scheduler.get().scheduleDeferred(getUserSharedContainers);
    }

    protected void fetchFolder(final String userID, final Iterator<Folder> folderIterator, final ListDataProvider<Folder> dataProvider, final Command callback) {
        app.LOG("OtherSharedTreeViewModel::fetchFolder(), userID=", userID, " folderIterator=", folderIterator.hasNext(), ", callback=", callback);
        if(folderIterator.hasNext()) {
            final Folder f = folderIterator.next();

            String path = "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + URL.encodeQueryString(f.getPrefix());
            GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, Pithos.getStorageAPIURL(), userID, path, f) {
                @Override
                public void onSuccess(Folder _result) {
                    dataProvider.getList().add(_result);
                    fetchFolder(userID, folderIterator, dataProvider, callback);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("Error getting folder", t);
                    app.setError(t);
                    if(t instanceof RestException) {
                        app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                    }
                    else {
                        app.displayError("System error fetching folder: " + t.getMessage());
                    }
                }

                @Override
                protected void onUnauthorized(Response response) {
                    app.sessionExpired();
                }
            };
            getFolder.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());
            Scheduler.get().scheduleDeferred(getFolder);
        }
        else if(callback != null) {
            callback.execute();
        }
    }

    public Folder getSelection() {
        return selectionModel.getSelectedObject();
    }

    public void updateFolder(Folder folder, boolean showfiles, Command callback) {
        app.LOG("OtherSharedTreeViewModel::updateFolder(), folder=", folder, ", showfiles=", showfiles, ", callback=", callback);
        if(dataProviderMap.get(folder) == null) {
            dataProviderMap.put(folder, new ListDataProvider<Folder>());
        }
        final ListDataProvider<Folder> dataProvider = dataProviderMap.get(folder);
        fetchFolder(folder, dataProvider, showfiles, callback);
    }

    public void fetchFolder(final Folder f, final ListDataProvider<Folder> dataProvider, final boolean showfiles, final Command callback) {
        app.LOG("OtherSharedTreeViewModel::fetchFolder(), folder=", f, ", showfiles=", showfiles, ", callback=", callback);
        String path = "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + URL.encodeQueryString(f.getPrefix());
        GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, Pithos.getStorageAPIURL(), f.getOwnerID(), path, f) {
            @Override
            public void onSuccess(final Folder _result) {
                if(showfiles) {
                    app.showFiles(_result);
                }
                Iterator<Folder> iter = _result.getSubfolders().iterator();
                fetchFolder(_result.getOwnerID(), iter, dataProvider, new Command() {
                    @Override
                    public void execute() {
                        dataProvider.getList().clear();
                        dataProvider.getList().addAll(_result.getSubfolders());
                        app.getOtherSharedTreeView().updateChildren(f);
                        if(callback != null) {
                            callback.execute();
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error getting folder", t);
                app.setError(t);
                if(t instanceof RestException) {
                    app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                }
                else {
                    app.displayError("System error fetching folder: " + t.getMessage());
                }
            }

            @Override
            protected void onUnauthorized(Response response) {
                app.sessionExpired();
            }
        };
        getFolder.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());
        Scheduler.get().scheduleDeferred(getFolder);
    }

    public void initialize(Command callback) {
        app.LOG("OtherSharedTreeViewModel::initialize(), callback=", callback);
        fetchSharingUsers(callback);
    }
}
