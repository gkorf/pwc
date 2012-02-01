/*
 * Copyright 2011-2012 GRNET S.A. All rights reserved.
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

import gr.grnet.pithos.web.client.FolderContextMenu;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.SharingUsers;
import gr.grnet.pithos.web.client.foldertree.AccountResource;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.othersharedtree.OtherSharedTreeView.Templates;
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
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

public class OtherSharedTreeViewModel implements TreeViewModel {

	private static final String treeTitle = "Other 's shared";
    protected Pithos app;

    private Cell<Folder> folderCell = new AbstractCell<Folder>(ContextMenuEvent.getType().getName()) {

       @Override
        public void render(Context context, Folder folder, SafeHtmlBuilder safeHtmlBuilder) {
            String html = AbstractImagePrototype.create(OtherSharedTreeView.images.folderYellow()).getHTML();
            safeHtmlBuilder.appendHtmlConstant(html).appendHtmlConstant("&nbsp;");
            safeHtmlBuilder.append(Templates.INSTANCE.nameSpan(folder.getName()));
        }

        @Override
        public void onBrowserEvent(Context context, com.google.gwt.dom.client.Element parent, Folder folder, com.google.gwt.dom.client.NativeEvent event, ValueUpdater<Folder> valueUpdater) {
            if (event.getType().equals(ContextMenuEvent.getType().getName())) {
                OtherSharedTreeViewModel.this.selectionModel.setSelected(folder, true);
                FolderContextMenu menu = new FolderContextMenu(app, OtherSharedTreeView.images, app.getSelectedTree(), folder);
                menu.setPopupPosition(event.getClientX(), event.getClientY());
                menu.show();
            }
        }
    };

    private ListDataProvider<String> rootDataProvider = new ListDataProvider<String>();
    protected ListDataProvider<String> userLevelDataProvider = new ListDataProvider<String>();

    protected Map<String, ListDataProvider<Folder>> userDataProviderMap = new HashMap<String, ListDataProvider<Folder>>();
    private Map<Folder, ListDataProvider<Folder>> dataProviderMap = new HashMap<Folder, ListDataProvider<Folder>>();
    
    protected Map<String, Set<File>> sharedFiles = new HashMap<String, Set<File>>();

    protected SingleSelectionModel<Folder> selectionModel;

    public OtherSharedTreeViewModel(Pithos _app, SingleSelectionModel<Folder> selectionModel) {
        app = _app;
        this.selectionModel = selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            rootDataProvider.getList().add(treeTitle);
            final SingleSelectionModel<String> selectionModel2 = new SingleSelectionModel<String>();
            selectionModel2.addSelectionChangeHandler(new Handler() {

                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    if (selectionModel2.getSelectedObject() != null) {
                    	app.deselectOthers(app.getOtherSharedTreeView(), selectionModel2);
                    	app.applyPermissions(null);
                    	app.showFiles(new HashSet<File>());
                    }
                    app.showRelevantToolbarButtons();
                }
            });
            app.addSelectionModel(selectionModel2);
            return new DefaultNodeInfo<String>(rootDataProvider, new TextCell(new SafeHtmlRenderer<String>() {
                @Override
                public SafeHtml render(String object) {
                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
                    render(object, builder);
                    return builder.toSafeHtml();
                }

                @Override
                public void render(String object, SafeHtmlBuilder builder) {
                    String html = AbstractImagePrototype.create(OtherSharedTreeView.images.othersShared()).getHTML();
                    builder.appendHtmlConstant(html).appendHtmlConstant("&nbsp;");
                    builder.append(OtherSharedTreeView.Templates.INSTANCE.nameSpan(object));
                }
            }),  selectionModel2, null);
        }
        else if (value instanceof String) {
        	if (value.equals(treeTitle)) {
	        	fetchSharingUsers(null);
	            final SingleSelectionModel<String> selectionModel3 = new SingleSelectionModel<String>();
	            selectionModel3.addSelectionChangeHandler(new Handler() {

	                @Override
	                public void onSelectionChange(SelectionChangeEvent event) {
	                    if (selectionModel3.getSelectedObject() != null) {
	                    	app.deselectOthers(app.getOtherSharedTreeView(), selectionModel3);
	                    	app.applyPermissions(null);
	                    	String username = selectionModel3.getSelectedObject();
	            			if (userDataProviderMap.get(username) == null) {
	            				userDataProviderMap.put(username, new ListDataProvider<Folder>());
	            			}
	                    	fetchSharedFiles(username, userDataProviderMap.get(username));
	                    }
	                    app.showRelevantToolbarButtons();
	                }
	            });
	            app.addSelectionModel(selectionModel3);
	            return new DefaultNodeInfo<String>(userLevelDataProvider, new TextCell(new SafeHtmlRenderer<String>() {

					@Override
					public SafeHtml render(String object) {
	                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
	                    render(object, builder);
	                    return builder.toSafeHtml();
					}

					@Override
					public void render(String object, SafeHtmlBuilder builder) {
	                    String html = AbstractImagePrototype.create(OtherSharedTreeView.images.user()).getHTML();
	                    builder.appendHtmlConstant(html).appendHtmlConstant("&nbsp;");
	                    builder.append(OtherSharedTreeView.Templates.INSTANCE.nameSpan(object));
					}
				}), selectionModel3, null);
        	}
			String username = (String) value;
			if (userDataProviderMap.get(username) == null) {
				userDataProviderMap.put(username, new ListDataProvider<Folder>());
			}
			final ListDataProvider<Folder> dataProvider = userDataProviderMap.get(username);
			fetchSharedContainers(username, dataProvider, null);
			return new DefaultNodeInfo<Folder>(dataProvider, folderCell, selectionModel, null);
        }
        else {
            final Folder f = (Folder) value;
            if (dataProviderMap.get(f) == null) {
                dataProviderMap.put(f, new ListDataProvider<Folder>());
            }
            final ListDataProvider<Folder> dataProvider = dataProviderMap.get(f);
            fetchFolder(f, dataProvider, false);
            return new DefaultNodeInfo<Folder>(dataProvider, folderCell, selectionModel, null);
        }
    }

    private void fetchSharingUsers(final Command callback) {
        GetRequest<SharingUsers> getSharingUsers = new GetRequest<SharingUsers>(SharingUsers.class, app.getApiPath(), "", "?format=json") {
            @Override
            public void onSuccess(final SharingUsers _result) {
                userLevelDataProvider.getList().clear();
                userLevelDataProvider.getList().addAll(_result.getUsers());
                for (String name : _result.getUsers()) {
                	sharedFiles.put(name, new HashSet<File>());
                }
                Iterator<String> iter = _result.getUsers().iterator();
                fetchSharedContainers(iter, callback);
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error getting folder", t);
				app.setError(t);
                if (t instanceof RestException)
                    app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                else
                    app.displayError("System error fetching folder: " + t.getMessage());
            }

			@Override
			protected void onUnauthorized(Response response) {
				app.sessionExpired();
			}
        };
        getSharingUsers.setHeader("X-Auth-Token", app.getToken());
        Scheduler.get().scheduleDeferred(getSharingUsers);
	}

	protected void fetchSharedContainers(final Iterator<String> iter, final Command callback) {
		if (iter.hasNext()) {
			String username = iter.next();
			if (userDataProviderMap.get(username) == null) {
				userDataProviderMap.put(username, new ListDataProvider<Folder>());
			}
			final ListDataProvider<Folder> dataProvider = userDataProviderMap.get(username);
			fetchSharedContainers(username, dataProvider, new Command() {
				
				@Override
				public void execute() {
					fetchSharedContainers(iter, callback);
					
				}
			});
		}
		else
			if (callback != null)
				callback.execute();
	}

	@Override
    public boolean isLeaf(Object o) {
		if (o == null)
			return false;
		else if (o instanceof Folder) {
            Folder f = (Folder) o;
            return f.getSubfolders().isEmpty();
        }
		else {
			if (o.equals(treeTitle))
				return userLevelDataProvider.getList().isEmpty();
			ListDataProvider<Folder> dp = userDataProviderMap.get(o);
			if (dp != null)
				return dp.getList().isEmpty();
			return true;
		}
    }

	private void fetchSharedContainers(final String username, final ListDataProvider<Folder> dataProvider, final Command callback) {
		GetRequest<AccountResource> getUserSharedContainers = new GetRequest<AccountResource>(AccountResource.class, app.getApiPath(), username, "?format=json") {

			@Override
			public void onSuccess(AccountResource _result) {
		    	final ListDataProvider<Folder> tempProvider = new ListDataProvider<Folder>();
				Iterator<Folder> iter = _result.getContainers().iterator();
				fetchFolder(username, iter, tempProvider, new Command() {
					
					@Override
					public void execute() {
						dataProvider.getList().clear();
						dataProvider.getList().addAll(tempProvider.getList());
						if (callback != null)
							callback.execute();
					}
				});
			}

			@Override
			public void onError(Throwable t) {
                GWT.log("Error getting account", t);
				app.setError(t);
                if (t instanceof RestException)
                    app.displayError("Error getting account: " + ((RestException) t).getHttpStatusText());
                else
                    app.displayError("System error fetching user data: " + t.getMessage());
			}

			@Override
			protected void onUnauthorized(Response response) {
				app.sessionExpired();
			}
		};
		getUserSharedContainers.setHeader("X-Auth-Token", app.getToken());
		Scheduler.get().scheduleDeferred(getUserSharedContainers);
	}

	protected void fetchSharedFiles(final String username, final ListDataProvider<Folder> dataProvider) {
		GetRequest<AccountResource> getUserSharedContainers = new GetRequest<AccountResource>(AccountResource.class, app.getApiPath(), username, "?format=json") {

			@Override
			public void onSuccess(AccountResource _result) {
		    	final ListDataProvider<Folder> tempProvider = new ListDataProvider<Folder>();
				Iterator<Folder> iter = _result.getContainers().iterator();
				sharedFiles.get(username).clear();
				fetchFolder(username, iter, tempProvider, new Command() {
					
					@Override
					public void execute() {
						dataProvider.getList().clear();
						dataProvider.getList().addAll(tempProvider.getList());
						app.showFiles(sharedFiles.get(username));
					}
				});
			}

			@Override
			public void onError(Throwable t) {
                GWT.log("Error getting account", t);
				app.setError(t);
                if (t instanceof RestException)
                    app.displayError("Error getting account: " + ((RestException) t).getHttpStatusText());
                else
                    app.displayError("System error fetching user data: " + t.getMessage());
			}

			@Override
			protected void onUnauthorized(Response response) {
				app.sessionExpired();
			}
		};
		getUserSharedContainers.setHeader("X-Auth-Token", app.getToken());
		Scheduler.get().scheduleDeferred(getUserSharedContainers);
	}

	protected void fetchFolder(final String username, final Iterator<Folder> iter, final ListDataProvider<Folder> dataProvider, final Command callback) {
        if (iter.hasNext()) {
            final Folder f = iter.next();

            String path = "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + URL.encodeQueryString(f.getPrefix());
            GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, app.getApiPath(), username, path, f) {
                @Override
                public void onSuccess(Folder _result) {
                	if (!_result.isShared()) {
                		for (File file : _result.getFiles()) {
                			if (file.isShared())
                				sharedFiles.get(username).add(file);
                		}
	                	Iterator<Folder> iter2 = _result.getSubfolders().iterator();
	                	fetchFolder(username, iter2, dataProvider, new Command() {
							
							@Override
							public void execute() {
			                    fetchFolder(username, iter, dataProvider, callback);
							}
						});
                	}
                	else {
                		dataProvider.getList().add(_result);
	                    fetchFolder(username, iter, dataProvider, callback);
                	}
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("Error getting folder", t);
					app.setError(t);
                    if (t instanceof RestException)
                        app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                    else
                        app.displayError("System error fetching folder: " + t.getMessage());
                }

				@Override
				protected void onUnauthorized(Response response) {
					app.sessionExpired();
				}
            };
            getFolder.setHeader("X-Auth-Token", app.getToken());
            Scheduler.get().scheduleDeferred(getFolder);
        }
        else if (callback != null)
            callback.execute();
    }

    public Folder getSelection() {
        return selectionModel.getSelectedObject();
    }

    public void updateFolder(Folder folder, boolean showfiles) {
        if (dataProviderMap.get(folder) == null) {
            dataProviderMap.put(folder, new ListDataProvider<Folder>());
        }
        final ListDataProvider<Folder> dataProvider = dataProviderMap.get(folder);
        fetchFolder(folder, dataProvider, showfiles);
    }

    public void fetchFolder(final Folder f, final ListDataProvider<Folder> dataProvider, final boolean showfiles) {
        String path = "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + URL.encodeQueryString(f.getPrefix());
        GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, app.getApiPath(), f.getOwner(), path, f) {
            @Override
            public void onSuccess(final Folder _result) {
            	GWT.log(String.valueOf(_result.getFiles().size()));
                if (showfiles)
                    app.showFiles(_result);
                Iterator<Folder> iter = _result.getSubfolders().iterator();
                fetchFolder(_result.getOwner(), iter, dataProvider, new Command() {
                    @Override
                    public void execute() {
                        dataProvider.getList().clear();
                        dataProvider.getList().addAll(_result.getSubfolders());
                        app.getOtherSharedTreeView().updateChildren(f);
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error getting folder", t);
				app.setError(t);
                if (t instanceof RestException)
                    app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                else
                    app.displayError("System error fetching folder: " + t.getMessage());
            }

			@Override
			protected void onUnauthorized(Response response) {
				app.sessionExpired();
			}
        };
        getFolder.setHeader("X-Auth-Token", app.getToken());
        Scheduler.get().scheduleDeferred(getFolder);
    }
    
    public void initialize(Command callback) {
    	fetchSharingUsers(callback);
    }
}
