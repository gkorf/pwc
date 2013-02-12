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

package gr.grnet.pithos.web.client.foldertree;

import gr.grnet.pithos.web.client.FolderContextMenu;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.foldertree.FolderTreeView.Templates;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

public class FolderTreeViewModel implements TreeViewModel {

	protected Pithos app;
    
    private Cell<Folder> folderCell = new AbstractCell<Folder>(ContextMenuEvent.getType().getName()) {

       @Override
        public void render(Context context, Folder folder, SafeHtmlBuilder safeHtmlBuilder) {
            String html;
            SafeHtml name;
        	if (folder.isHome()) {
        		html = AbstractImagePrototype.create(FolderTreeView.images.folderYellow()).getHTML();
        		name = Templates.INSTANCE.nameSpan(folder.getName());
        	}
        	else if (folder.isTrash()) {
        		html = AbstractImagePrototype.create(FolderTreeView.images.trash()).getHTML();
        		name = Templates.INSTANCE.nameSpan(folder.getName());
        	}
            else {
            	html = AbstractImagePrototype.create(folder.isShared() ? FolderTreeView.images.myShared() : FolderTreeView.images.folderYellow()).getHTML();
        		name = Templates.INSTANCE.nameSpan(folder.getName());
            }
            safeHtmlBuilder.appendHtmlConstant(html).appendHtmlConstant("&nbsp;");
            safeHtmlBuilder.append(name);
        }

        @Override
        public void onBrowserEvent(Cell.Context context, com.google.gwt.dom.client.Element parent, final Folder folder, com.google.gwt.dom.client.NativeEvent event, com.google.gwt.cell.client.ValueUpdater<Folder> valueUpdater) {
            if (event.getType().equals(ContextMenuEvent.getType().getName())) {
            	final int x = event.getClientX();
            	final int y = event.getClientY();
                FolderTreeViewModel.this.selectionModel.setSelected(folder, true);
                app.scheduleFolderHeadCommand(folder, new Command() {
					
					@Override
					public void execute() {
		                FolderContextMenu menu = new FolderContextMenu(app, FolderTreeView.images, app.getSelectedTree(), folder);
		                menu.setPopupPosition(x, y);
		                menu.show();
					}
				});
            }
        }
    };

    protected ListDataProvider<Folder> rootDataProvider = new ListDataProvider<Folder>();

    private Map<Folder, ListDataProvider<Folder>> dataProviderMap = new HashMap<Folder, ListDataProvider<Folder>>();

    protected SingleSelectionModel<Folder> selectionModel;

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
		final Folder f = (Folder) value;
		if (dataProviderMap.get(f) == null) {
		    dataProviderMap.put(f, new ListDataProvider<Folder>());
		}
		final ListDataProvider<Folder> dataProvider = dataProviderMap.get(f);
		//This prevents the loading indicator
		fetchFolder(f, dataProvider, false, null);
		return new DefaultNodeInfo<Folder>(dataProvider, folderCell, selectionModel, null);
    }

    @Override
    public boolean isLeaf(Object o) {
        if (o instanceof Folder) {
            Folder f = (Folder) o;
            return f.getSubfolders().isEmpty();
        }
        return false;
    }

	private native void log(String msg) /*-{
	$wnd.console.log(msg);
	}-*/;

    protected void fetchFolder(final Iterator<Folder> iter, final Command callback) {
        if (iter.hasNext()) {
            final Folder f = iter.next();

            String path = "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + URL.encodeQueryString(f.getPrefix());
            GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, app.getApiPath(), f.getOwnerID(), path, f) {
                @Override
                public void onSuccess(Folder _result) {
                    fetchFolder(iter, callback);
                }

                @Override
                public void onError(Throwable t) {
                	if (retries >= MAX_RETRIES) {
	                    GWT.log("Error getting folder", t);
						app.setError(t);
	                    if (t instanceof RestException)
	                        app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
	                    else
	                        app.displayError("System error fetching folder: " + t.getMessage());
                	}
                	else {//retry
                		GWT.log("Retry " + retries);
                		Scheduler.get().scheduleDeferred(this);
                	}
                }

				@Override
				protected void onUnauthorized(Response response) {
					if (retries >= MAX_RETRIES)
						app.sessionExpired();
	            	else //retry
	            		Scheduler.get().scheduleDeferred(this);
				}
            };
            getFolder.setHeader("X-Auth-Token", app.getUserToken());
            Scheduler.get().scheduleDeferred(getFolder);
        }
        else if (callback != null)
            callback.execute();
    }

    public void initialize(final AccountResource account, final Command callback) {
        Iterator<Folder> iter = account.getContainers().listIterator();
        fetchFolder(iter, new Command() {
            @Override
            public void execute() {
                rootDataProvider.getList().clear();
                Folder t = null;
                for (Folder c : account.getContainers()) {
                	if (c.isHome())
                    	rootDataProvider.getList().add(0, c); //Pithos is always first
                	else if (c.isTrash())
                		t = c; //Keep trash for adding in the end
                	else
                		rootDataProvider.getList().add(c);
                }
                if (t != null)
            		rootDataProvider.getList().add(t);
                selectionModel.setSelected(rootDataProvider.getList().get(0), true);
                if (callback != null)
                	callback.execute();
            }
        });
    }

    public Folder getSelection() {
        return selectionModel.getSelectedObject();
    }

    public void updateFolder(final Folder folder, boolean showfiles, final Command callback, final boolean openParent) {
        if (dataProviderMap.get(folder) == null) {
            dataProviderMap.put(folder, new ListDataProvider<Folder>());
        }
        final ListDataProvider<Folder> dataProvider = dataProviderMap.get(folder);
        fetchFolder(folder, dataProvider, showfiles, new Command() {
			
			@Override
			public void execute() {
				if (openParent)
					app.getFolderTreeView().openFolder(folder);
				if (callback != null)
					callback.execute();
			}
		});
    }

    public void fetchFolder(final Folder f, final ListDataProvider<Folder> dataProvider, final boolean showfiles, final Command callback) {
        String path = "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + URL.encodeQueryString(f.getPrefix());
        GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, app.getApiPath(), f.getOwnerID(), path, f) {
            @Override
            public void onSuccess(final Folder _result) {
                if (showfiles)
                    app.showFiles(_result);
                int defaultSize = app.getFolderTreeView().tree.getDefaultNodeSize();
                int size = _result.getSubfolders().size();
                if (size > defaultSize)
                	app.getFolderTreeView().tree.setDefaultNodeSize(size);
                Iterator<Folder> iter = new ArrayList<Folder>(_result.getSubfolders()).listIterator();
                fetchFolder(iter, new Command() {
                    @Override
                    public void execute() {
                        dataProvider.getList().clear();
                        dataProvider.getList().addAll(_result.getSubfolders());
                        if (callback != null)
                        	callback.execute();
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
            	if (retries >= MAX_RETRIES) {
	                GWT.log("Error getting folder", t);
					app.setError(t);
	                if (t instanceof RestException)
	                    app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
	                else
	                    app.displayError("System error fetching folder: " + t.getMessage());
            	}
            	else {//retry
            		GWT.log("Retry " + retries);
            		Scheduler.get().scheduleDeferred(this);
            	}
            }

			@Override
			protected void onUnauthorized(Response response) {
				if (retries >= MAX_RETRIES)
					app.sessionExpired();
            	else //retry
            		Scheduler.get().scheduleDeferred(this);
			}
        };
        getFolder.setHeader("X-Auth-Token", app.getUserToken());
        Scheduler.get().scheduleDeferred(getFolder);
    }
}
