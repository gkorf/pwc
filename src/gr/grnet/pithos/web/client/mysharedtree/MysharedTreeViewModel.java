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

package gr.grnet.pithos.web.client.mysharedtree;

import gr.grnet.pithos.web.client.FolderContextMenu;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.FolderTreeView;
import gr.grnet.pithos.web.client.mysharedtree.MysharedTreeView.Templates;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

public class MysharedTreeViewModel implements TreeViewModel {

    protected Pithos app;

    private Cell<Folder> folderCell = new AbstractCell<Folder>() {

       @Override
        public void render(Context context, Folder folder, SafeHtmlBuilder safeHtmlBuilder) {
            String html = AbstractImagePrototype.create(MysharedTreeView.images.folderYellow()).getHTML();
            safeHtmlBuilder.appendHtmlConstant(html).appendHtmlConstant("&nbsp;");
            safeHtmlBuilder.append(Templates.INSTANCE.nameSpan(folder.getName()));
        }

        @Override
        public void onBrowserEvent(Context context, com.google.gwt.dom.client.Element parent, final Folder folder, com.google.gwt.dom.client.NativeEvent event, ValueUpdater<Folder> valueUpdater) {
            if (event.getType().equals(ContextMenuEvent.getType().getName())) {
            	final int x = event.getClientX();
            	final int y = event.getClientY();
                MysharedTreeViewModel.this.selectionModel.setSelected(folder, true);
                app.scheduleFolderHeadCommand(folder, new Command() {
					
					@Override
					public void execute() {
		                FolderContextMenu menu = new FolderContextMenu(app, MysharedTreeView.images, app.getSelectedTree(), folder);
		                menu.setPopupPosition(x, y);
		                menu.show();
					}
				});
            }
        }
    };

    protected ListDataProvider<Folder> firstLevelDataProvider = new ListDataProvider<Folder>();

    private Map<Folder, ListDataProvider<Folder>> dataProviderMap = new HashMap<Folder, ListDataProvider<Folder>>();
    
    protected Set<File> sharedFiles = new HashSet<File>();

    protected SingleSelectionModel<Folder> selectionModel;

    public MysharedTreeViewModel(Pithos _app, SingleSelectionModel<Folder> selectionModel) {
        app = _app;
        this.selectionModel = selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            ListDataProvider<String> rootDataProvider = new ListDataProvider<String>();
            rootDataProvider.getList().add("Shared by me");
            final SingleSelectionModel<String> selectionModel2 = new SingleSelectionModel<String>();
            selectionModel2.addSelectionChangeHandler(new Handler() {

                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    if (selectionModel2.getSelectedObject() != null) {
                    	app.deselectOthers(app.getMySharedTreeView(), selectionModel2);
                    	app.applyPermissions(null);
                    	fetchSharedFiles(true);
    					app.showRelevantToolbarButtons();
                    }
    				else {
    					if (app.getSelectedTree().equals(app.getMySharedTreeView()) && app.getMySharedTreeView().getSelection() == null)
    						app.setSelectedTree(null);
    					if (app.getSelectedTree() == null)
    						app.showRelevantToolbarButtons();
    				}
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
                    String html = AbstractImagePrototype.create(MysharedTreeView.images.myShared()).getHTML();
                    builder.appendHtmlConstant(html).appendHtmlConstant("&nbsp;");
                    builder.append(MysharedTreeView.Templates.INSTANCE.nameSpan(object));
                }
            }),  selectionModel2, null);
        }
        else if (value instanceof String) {
        	fetchSharedContainers(null);
            return new DefaultNodeInfo<Folder>(firstLevelDataProvider, folderCell, selectionModel, null);
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

	protected void fetchSharedFiles(final boolean showFiles) {
		sharedFiles.clear();
    	final List<Folder> containers = app.getAccount().getContainers();
    	final ListDataProvider<Folder> tempProvider = new ListDataProvider<Folder>();
    	Iterator<Folder> iter = containers.iterator();
    	fetchFolder(iter, tempProvider, new Command() {
			
			@Override
			public void execute() {
				firstLevelDataProvider.getList().clear();
				firstLevelDataProvider.getList().addAll(tempProvider.getList());
				if (showFiles)
					app.showFiles(sharedFiles);
			}
		});
	}

	private void fetchSharedContainers(final Command callback) {
    	final List<Folder> containers = app.getAccount().getContainers();
    	final ListDataProvider<Folder> tempProvider = new ListDataProvider<Folder>();
    	Iterator<Folder> iter = containers.listIterator();
    	fetchFolder(iter, tempProvider, new Command() {
			
			@Override
			public void execute() {
				firstLevelDataProvider.getList().clear();
				firstLevelDataProvider.getList().addAll(tempProvider.getList());
				if (callback != null)
					callback.execute();
			}
		});
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
			return firstLevelDataProvider.getList().isEmpty();
		}
    }
	
	private native void log(String msg) /*-{
		$wnd.console.log(msg);
	}-*/;

    protected void fetchFolder(final Iterator<Folder> iter, final ListDataProvider<Folder> dataProvider, final Command callback) {
        if (iter.hasNext()) {
            final Folder f = iter.next();

            String path = "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + URL.encodeQueryString(f.getPrefix());
            GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, app.getApiPath(), f.getOwner(), path, f) {
                @Override
                public void onSuccess(Folder _result) {
                	if (!_result.isShared()) {
                		for (File file : _result.getFiles()) {
                			if (file.isSharedOrPublished())
                				sharedFiles.add(file);
                		}
	                	Iterator<Folder> iter2 = new ArrayList<Folder>(result.getSubfolders()).listIterator();
	                	fetchFolder(iter2, dataProvider, new Command() {
							
							@Override
							public void execute() {
			                    fetchFolder(iter, dataProvider, callback);
							}
						});
                	}
                	else {
                		dataProvider.getList().add(_result);
	                    fetchFolder(iter, dataProvider, callback);
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
                if (showfiles)
                    app.showFiles(_result);
                Iterator<Folder> iter = _result.getSubfolders().iterator();
                fetchFolder(iter, dataProvider, new Command() {
                    @Override
                    public void execute() {
                        dataProvider.getList().clear();
                   		dataProvider.getList().addAll(_result.getSubfolders());
                        app.getMySharedTreeView().updateChildren(f);
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
		fetchSharedContainers(callback);
	}
}
