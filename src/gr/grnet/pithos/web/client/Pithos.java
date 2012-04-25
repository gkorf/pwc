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
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.commands.UploadFileCommand;
import gr.grnet.pithos.web.client.foldertree.AccountResource;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.FolderTreeView;
import gr.grnet.pithos.web.client.foldertree.FolderTreeViewModel;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.grouptree.Group;
import gr.grnet.pithos.web.client.grouptree.GroupTreeView;
import gr.grnet.pithos.web.client.grouptree.GroupTreeViewModel;
import gr.grnet.pithos.web.client.mysharedtree.MysharedTreeView;
import gr.grnet.pithos.web.client.mysharedtree.MysharedTreeViewModel;
import gr.grnet.pithos.web.client.othersharedtree.OtherSharedTreeView;
import gr.grnet.pithos.web.client.othersharedtree.OtherSharedTreeViewModel;
import gr.grnet.pithos.web.client.rest.DeleteRequest;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.HeadRequest;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Pithos implements EntryPoint, ResizeHandler {

	public static final String HOME_CONTAINER = "pithos";

	public static final String TRASH_CONTAINER = "trash";

	public static final Configuration config = GWT.create(Configuration.class);
	
	/**
	 * Instantiate an application-level image bundle. This object will provide
	 * programmatic access to all the images needed by widgets.
	 */
	static Images images = (Images) GWT.create(Images.class);

    public String getUsername() {
        return username;
    }

    public void setAccount(AccountResource acct) {
        account = acct;
    }

    public AccountResource getAccount() {
        return account;
    }

    public void updateFolder(Folder f, boolean showfiles, Command callback, final boolean openParent) {
        folderTreeView.updateFolder(f, showfiles, callback, openParent);
    }

    public void updateGroupNode(Group group) {
        groupTreeView.updateGroupNode(group);
    }

    public void updateMySharedRoot() {
    	mysharedTreeView.updateRoot();
    }
    
    public void updateSharedFolder(Folder f, boolean showfiles) {
    	mysharedTreeView.updateFolder(f, showfiles);
    }
    
    public void updateOtherSharedFolder(Folder f, boolean showfiles) {
    	otherSharedTreeView.updateFolder(f, showfiles);
    }

    public MysharedTreeView getMySharedTreeView() {
        return mysharedTreeView;
    }

    /**
	 * An aggregate image bundle that pulls together all the images for this
	 * application into a single bundle.
	 */
	public interface Images extends TopPanel.Images, FileList.Images, ToolsMenu.Images {

		@Source("gr/grnet/pithos/resources/document.png")
		ImageResource folders();

		@Source("gr/grnet/pithos/resources/advancedsettings.png")
		@ImageOptions(width=32, height=32)
		ImageResource tools();
	}

	private Throwable error;
	
	/**
	 * The Application Clipboard implementation;
	 */
	private Clipboard clipboard = new Clipboard();

	/**
	 * The top panel that contains the menu bar.
	 */
	private TopPanel topPanel;

	/**
	 * The panel that contains the various system messages.
	 */
	private MessagePanel messagePanel = new MessagePanel(this, Pithos.images);

	/**
	 * The bottom panel that contains the status bar.
	 */
	private StatusPanel statusPanel = null;

	/**
	 * The file list widget.
	 */
	private FileList fileList;

	/**
	 * The tab panel that occupies the right side of the screen.
	 */
	private VerticalPanel inner = new VerticalPanel();


	/**
	 * The split panel that will contain the left and right panels.
	 */
	private HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();

	/**
	 * The currently selected item in the application, for use by the Edit menu
	 * commands. Potential types are Folder, File, User and Group.
	 */
	private Object currentSelection;

	public HashMap<String, String> userFullNameMap = new HashMap<String, String>();

    private String username = null;

    /**
     * The authentication token of the current user.
     */
    private String token;

    VerticalPanel trees;
    
    SingleSelectionModel<Folder> folderTreeSelectionModel;
    FolderTreeViewModel folderTreeViewModel;
    FolderTreeView folderTreeView;

    SingleSelectionModel<Folder> mysharedTreeSelectionModel;
    MysharedTreeViewModel mysharedTreeViewModel;
    MysharedTreeView mysharedTreeView = null;;

    protected SingleSelectionModel<Folder> otherSharedTreeSelectionModel;
    OtherSharedTreeViewModel otherSharedTreeViewModel;
    OtherSharedTreeView otherSharedTreeView = null;

    GroupTreeViewModel groupTreeViewModel;
    GroupTreeView groupTreeView;

    TreeView selectedTree;
    protected AccountResource account;
    
    Folder trash;
    
    List<Composite> treeViews = new ArrayList<Composite>();

    @SuppressWarnings("rawtypes") List<SingleSelectionModel> selectionModels = new ArrayList<SingleSelectionModel>();
    
    Button upload;
    
    private HTML usedBytes;
    
    private HTML totalBytes;
    
    private HTML usedPercent;
    
    private HTML numOfFiles;
    
    private Toolbar toolbar;
    
    private FileUploadDialog fileUploadDialog;
    
	@Override
	public void onModuleLoad() {
		if (parseUserCredentials())
            initialize();
	}

    private void initialize() {
    	boolean bareContent = Window.Location.getParameter("noframe") != null;
    	String contentWidth = bareContent ? "100%" : "75%";

    	VerticalPanel outer = new VerticalPanel();
        outer.setWidth("100%");
    	if (!bareContent) {
    		outer.addStyleName("pithos-outer");
    	}

        if (!bareContent) {
	        topPanel = new TopPanel(this, Pithos.images);
	        topPanel.setWidth("100%");
	        outer.add(topPanel);
	        outer.setCellHorizontalAlignment(topPanel, HasHorizontalAlignment.ALIGN_CENTER);
        }
        
        messagePanel.setVisible(false);
        outer.add(messagePanel);
        outer.setCellHorizontalAlignment(messagePanel, HasHorizontalAlignment.ALIGN_CENTER);
        outer.setCellVerticalAlignment(messagePanel, HasVerticalAlignment.ALIGN_MIDDLE);

        HorizontalPanel header = new HorizontalPanel();
        header.addStyleName("pithos-header");
        header.setWidth(contentWidth);
        if (bareContent)
        	header.addStyleName("pithos-header-noframe");
        upload = new Button("Upload", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	if (getSelection() != null)
            		new UploadFileCommand(Pithos.this, null, getSelection()).execute();
            }
        });
        upload.addStyleName("pithos-uploadButton");
        header.add(upload);
        header.setCellHorizontalAlignment(upload, HasHorizontalAlignment.ALIGN_LEFT);
        header.setCellVerticalAlignment(upload, HasVerticalAlignment.ALIGN_MIDDLE);

        toolbar = new Toolbar(this);
        header.add(toolbar);
        header.setCellHorizontalAlignment(toolbar, HasHorizontalAlignment.ALIGN_CENTER);
        header.setCellVerticalAlignment(toolbar, HasVerticalAlignment.ALIGN_MIDDLE);
        
        HorizontalPanel folderStatistics = new HorizontalPanel();
        folderStatistics.addStyleName("pithos-folderStatistics");
        numOfFiles = new HTML();
        folderStatistics.add(numOfFiles);
        folderStatistics.setCellVerticalAlignment(numOfFiles, HasVerticalAlignment.ALIGN_MIDDLE);
        HTML numOfFilesLabel = new HTML("&nbsp;Files");
        folderStatistics.add(numOfFilesLabel);
        folderStatistics.setCellVerticalAlignment(numOfFilesLabel, HasVerticalAlignment.ALIGN_MIDDLE);
        header.add(folderStatistics);
        header.setCellHorizontalAlignment(folderStatistics, HasHorizontalAlignment.ALIGN_RIGHT);
        header.setCellVerticalAlignment(folderStatistics, HasVerticalAlignment.ALIGN_MIDDLE);
        header.setCellWidth(folderStatistics, "40px");
        outer.add(header);
        outer.setCellHorizontalAlignment(header, HasHorizontalAlignment.ALIGN_CENTER);
        // Inner contains the various lists.nner
        inner.sinkEvents(Event.ONCONTEXTMENU);
        inner.setWidth("100%");

        folderTreeSelectionModel = new SingleSelectionModel<Folder>();
        folderTreeSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (folderTreeSelectionModel.getSelectedObject() != null) {
                    deselectOthers(folderTreeView, folderTreeSelectionModel);
                    applyPermissions(folderTreeSelectionModel.getSelectedObject());
                    Folder f = folderTreeSelectionModel.getSelectedObject();
            		updateFolder(f, true, new Command() {
            			
            			@Override
            			public void execute() {
            				updateStatistics();
            			}
            		}, true);
            		showRelevantToolbarButtons();
                }
				else {
					if (getSelectedTree().equals(folderTreeView))
						setSelectedTree(null);
					if (getSelectedTree() == null)
						showRelevantToolbarButtons();
				}
            }
        });
        selectionModels.add(folderTreeSelectionModel);

        folderTreeViewModel = new FolderTreeViewModel(this, folderTreeSelectionModel);
        folderTreeView = new FolderTreeView(folderTreeViewModel);
        treeViews.add(folderTreeView);
        
        fileList = new FileList(this, images, folderTreeView);
        inner.add(fileList);

        trees = new VerticalPanel();
        trees.setWidth("100%");
        
        trees.add(folderTreeView);
        
        HorizontalPanel separator = new HorizontalPanel();
        separator.addStyleName("pithos-statisticsSeparator");
        trees.add(separator);
        
        HorizontalPanel statistics = new HorizontalPanel();
	    statistics.addStyleName("pithos-statistics");
	    statistics.add(new HTML("Used:&nbsp;"));
	    usedBytes = new HTML();
	    statistics.add(usedBytes);
	    statistics.add(new HTML("&nbsp;of&nbsp;"));
	    totalBytes = new HTML();
	    statistics.add(totalBytes);
	    statistics.add(new HTML("&nbsp;("));
	    usedPercent = new HTML();
	    statistics.add(usedPercent);
	    statistics.add(new HTML(")"));
        trees.add(statistics);
        
        // Add the left and right panels to the split panel.
        splitPanel.setLeftWidget(trees);
        splitPanel.setRightWidget(inner);
        splitPanel.setSplitPosition("219px");
        splitPanel.setSize("100%", "100%");
        splitPanel.addStyleName("pithos-splitPanel");
        splitPanel.setWidth(contentWidth);
        outer.add(splitPanel);
        outer.setCellHorizontalAlignment(splitPanel, HasHorizontalAlignment.ALIGN_CENTER);

        if (!bareContent) {
	        statusPanel = new StatusPanel();
	        statusPanel.setWidth("100%");
	        outer.add(statusPanel);
	        outer.setCellHorizontalAlignment(statusPanel, HasHorizontalAlignment.ALIGN_CENTER);
        }
        else
        	splitPanel.addStyleName("pithos-splitPanel-noframe");

        // Hook the window resize event, so that we can adjust the UI.
        Window.addResizeHandler(this);
        // Clear out the window's built-in margin, because we want to take
        // advantage of the entire client area.
        Window.setMargin("0px");
        // Finally, add the outer panel to the RootPanel, so that it will be
        // displayed.
        RootPanel.get().add(outer);
        // Call the window resized handler to get the initial sizes setup. Doing
        // this in a deferred command causes it to occur after all widgets'
        // sizes have been computed by the browser.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                onWindowResized(Window.getClientHeight());
            }
        });

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                fetchAccount(new Command() {
					
					@Override
					public void execute() {
		                if (!account.hasHomeContainer())
		                    createHomeContainer(account, this);
		                else if (!account.hasTrashContainer())
		                	createTrashContainer(this);
		                else {
		                	for (Folder f : account.getContainers())
		                		if (f.getName().equals(Pithos.TRASH_CONTAINER)) {
		                			trash = f;
		                			break;
		                		}
		                    folderTreeViewModel.initialize(account, new Command() {
								
								@Override
								public void execute() {
				                    createMySharedTree();
								}
							});
		                    groupTreeViewModel = new GroupTreeViewModel(Pithos.this);
		                    groupTreeView = new GroupTreeView(groupTreeViewModel);
		                    treeViews.add(groupTreeView);
		                    trees.add(groupTreeView);
		                    showStatistics();
		                }
					}
				});
            }
        });
        
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
			
			@Override
			public boolean execute() {
				Folder f = getSelection();
				if (f != null) {
					if (getSelectedTree().equals(folderTreeView))
						updateFolder(f, true, null, false);
					else if (getSelectedTree().equals(mysharedTreeView))
						updateSharedFolder(f, true);
				}
				return true;
			}
		}, 3000);
    }

    public void applyPermissions(Folder f) {
    	if (f != null) {
    		if (f.isInTrash())
    			upload.setEnabled(false);
    		else {
		    	Boolean[] perms = f.getPermissions().get(username);
		    	if (f.getOwner().equals(username) || (perms != null && perms[1] != null && perms[1])) {
		    		upload.setEnabled(true);
		    	}
		    	else
		    		upload.setEnabled(false);
    		}
    	}
    	else
    		upload.setEnabled(false);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void deselectOthers(TreeView _selectedTree, SingleSelectionModel model) {
    	selectedTree = _selectedTree;
    	
    	for (Composite c : treeViews)
    		if (c.equals(selectedTree))
    			c.addStyleName("cellTreeWidget-selectedTree");
    		else
    			c.removeStyleName("cellTreeWidget-selectedTree");
    	
        for (SingleSelectionModel s : selectionModels)
            if (!s.equals(model) && s.getSelectedObject() != null)
                s.setSelected(s.getSelectedObject(), false);
    }

    public void showFiles(final Folder f) {
        Set<File> files = f.getFiles();
        showFiles(files);
    }

    public void showFiles(Set<File> files) {
        fileList.setFiles(new ArrayList<File>(files));
    }

    /**
	 * Parse and store the user credentials to the appropriate fields.
	 */
	private boolean parseUserCredentials() {
        Configuration conf = (Configuration) GWT.create(Configuration.class);
		Dictionary otherProperties = Dictionary.getDictionary("otherProperties");
        String cookie = otherProperties.get("authCookie");
        String auth = Cookies.getCookie(cookie);
        if (auth == null) {
            authenticateUser();
            return false;
        }
        if (auth.startsWith("\""))
        	auth = auth.substring(1);
        if (auth.endsWith("\""))
        	auth = auth.substring(0, auth.length() - 1);
		String[] authSplit = auth.split("\\" + conf.cookieSeparator(), 2);
		if (authSplit.length != 2) {
		    authenticateUser();
		    return false;
		}
		username = authSplit[0];
		token = authSplit[1];

        String gotoUrl = Window.Location.getParameter("goto");
		if (gotoUrl != null && gotoUrl.length() > 0) {
			Window.Location.assign(gotoUrl);
			return false;
		}
		return true;
    }

    /**
	 * Redirect the user to the login page for authentication.
	 */
	protected void authenticateUser() {
		Dictionary otherProperties = Dictionary.getDictionary("otherProperties");
        Window.Location.assign(otherProperties.get("loginUrl") + Window.Location.getHref());
	}

	protected void fetchAccount(final Command callback) {
        String path = "?format=json";

        GetRequest<AccountResource> getAccount = new GetRequest<AccountResource>(AccountResource.class, getApiPath(), username, path) {
            @Override
            public void onSuccess(AccountResource _result) {
                account = _result;
                if (callback != null)
                	callback.execute();
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error getting account", t);
				setError(t);
                if (t instanceof RestException)
                    displayError("Error getting account: " + ((RestException) t).getHttpStatusText());
                else
                    displayError("System error fetching user data: " + t.getMessage());
            }

			@Override
			protected void onUnauthorized(Response response) {
				sessionExpired();
			}
        };
        getAccount.setHeader("X-Auth-Token", token);
        Scheduler.get().scheduleDeferred(getAccount);
    }

    public void updateStatistics() {
    	HeadRequest<AccountResource> headAccount = new HeadRequest<AccountResource>(AccountResource.class, getApiPath(), username, "", account) {

			@Override
			public void onSuccess(AccountResource _result) {
				showStatistics();
			}

			@Override
			public void onError(Throwable t) {
                GWT.log("Error getting account", t);
				setError(t);
                if (t instanceof RestException)
                    displayError("Error getting account: " + ((RestException) t).getHttpStatusText());
                else
                    displayError("System error fetching user data: " + t.getMessage());
			}

			@Override
			protected void onUnauthorized(Response response) {
				sessionExpired();
			}
		};
		headAccount.setHeader("X-Auth-Token", token);
		Scheduler.get().scheduleDeferred(headAccount);
	}

	protected void showStatistics() {
    	usedBytes.setHTML(String.valueOf(account.getFileSizeAsString()));
    	totalBytes.setHTML(String.valueOf(account.getQuotaAsString()));
    	NumberFormat nf = NumberFormat.getPercentFormat();
    	usedPercent.setHTML(nf.format(account.getUsedPercentage()));
	}

	protected void createHomeContainer(final AccountResource _account, final Command callback) {
        String path = "/" + Pithos.HOME_CONTAINER;
        PutRequest createPithos = new PutRequest(getApiPath(), getUsername(), path) {
            @Override
            public void onSuccess(Resource result) {
            	if (!_account.hasTrashContainer())
            		createTrashContainer(callback);
            	else
            		fetchAccount(callback);
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error creating pithos", t);
				setError(t);
                if (t instanceof RestException)
                    displayError("Error creating pithos: " + ((RestException) t).getHttpStatusText());
                else
                    displayError("System error Error creating pithos: " + t.getMessage());
            }

			@Override
			protected void onUnauthorized(Response response) {
				sessionExpired();
			}
        };
        createPithos.setHeader("X-Auth-Token", getToken());
        Scheduler.get().scheduleDeferred(createPithos);
    }

    protected void createTrashContainer(final Command callback) {
        String path = "/" + Pithos.TRASH_CONTAINER;
        PutRequest createPithos = new PutRequest(getApiPath(), getUsername(), path) {
            @Override
            public void onSuccess(Resource result) {
           		fetchAccount(callback);
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error creating pithos", t);
				setError(t);
                if (t instanceof RestException)
                    displayError("Error creating pithos: " + ((RestException) t).getHttpStatusText());
                else
                    displayError("System error Error creating pithos: " + t.getMessage());
            }

			@Override
			protected void onUnauthorized(Response response) {
				sessionExpired();
			}
        };
        createPithos.setHeader("X-Auth-Token", getToken());
        Scheduler.get().scheduleDeferred(createPithos);
    }

    /**
	 * Creates an HTML fragment that places an image & caption together, for use
	 * in a group header.
	 *
	 * @param imageProto an image prototype for an image
	 * @param caption the group caption
	 * @return the header HTML fragment
	 */
	private String createHeaderHTML(AbstractImagePrototype imageProto, String caption) {
		String captionHTML = "<table class='caption' cellpadding='0' " 
		+ "cellspacing='0'>" + "<tr><td class='lcaption'>" + imageProto.getHTML() 
		+ "</td><td id =" + caption +" class='rcaption'><b style='white-space:nowrap'>&nbsp;" 
		+ caption + "</b></td></tr></table>";
		return captionHTML;
	}

	protected void onWindowResized(int height) {
		// Adjust the split panel to take up the available room in the window.
		int newHeight = height - splitPanel.getAbsoluteTop();
		if (newHeight < 1)
			newHeight = 1;
		splitPanel.setHeight("" + newHeight);
		inner.setHeight("" + newHeight);
	}

	@Override
	public void onResize(ResizeEvent event) {
		int height = event.getHeight();
		onWindowResized(height);
	}

	/**
	 * Display an error message.
	 *
	 * @param msg the message to display
	 */
	public void displayError(String msg) {
		messagePanel.displayError(msg);
	}

	/**
	 * Display a warning message.
	 *
	 * @param msg the message to display
	 */
	public void displayWarning(String msg) {
		messagePanel.displayWarning(msg);
	}

	/**
	 * Display an informational message.
	 *
	 * @param msg the message to display
	 */
	public void displayInformation(String msg) {
		messagePanel.displayInformation(msg);
	}

	/**
	 * Retrieve the fileList.
	 *
	 * @return the fileList
	 */
	public FileList getFileList() {
		return fileList;
	}

	/**
	 * Retrieve the topPanel.
	 *
	 * @return the topPanel
	 */
	TopPanel getTopPanel() {
		return topPanel;
	}

	/**
	 * Retrieve the clipboard.
	 *
	 * @return the clipboard
	 */
	public Clipboard getClipboard() {
		return clipboard;
	}

	public StatusPanel getStatusPanel() {
		return statusPanel;
	}

	public String getToken() {
		return token;
	}

	public static native void preventIESelection() /*-{
		$doc.body.onselectstart = function () { return false; };
	}-*/;

	public static native void enableIESelection() /*-{
		if ($doc.body.onselectstart != null)
		$doc.body.onselectstart = null;
	}-*/;

	/**
	 * @return the absolute path of the API root URL
	 */
	public String getApiPath() {
		Configuration conf = (Configuration) GWT.create(Configuration.class);
		return conf.apiPath();
	}

	/**
	 * History support for folder navigation
	 * adds a new browser history entry
	 *
	 * @param key
	 */
	public void updateHistory(String key){
//		Replace any whitespace of the initial string to "+"
//		String result = key.replaceAll("\\s","+");
//		Add a new browser history entry.
//		History.newItem(result);
		History.newItem(key);
	}

    public void deleteFolder(final Folder folder, final Command callback) {
        String path = getApiPath() + folder.getOwner() + "/" + folder.getContainer() + "?format=json&delimiter=/&prefix=" + URL.encodeQueryString(folder.getPrefix()) + "&t=" + System.currentTimeMillis();
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, path);
        builder.setHeader("X-Auth-Token", getToken());
        try {
            builder.sendRequest("", new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        JSONValue json = JSONParser.parseStrict(response.getText());
                        JSONArray array = json.isArray();
                        int i = 0;
                        if (array != null) {
                            deleteObject(folder, i, array, callback);
                        }
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                	setError(exception);
                    displayError("System error unable to delete folder: " + exception.getMessage());
                }
            });
        }
        catch (RequestException e) {
        }
    }

    void deleteObject(final Folder folder, final int i, final JSONArray array, final Command callback) {
        if (i < array.size()) {
            JSONObject o = array.get(i).isObject();
            if (o != null && !o.containsKey("subdir")) {
                JSONString name = o.get("name").isString();
                String path = "/" + folder.getContainer() + "/" + name.stringValue();
                DeleteRequest delete = new DeleteRequest(getApiPath(), folder.getOwner(), URL.encode(path)) {
                    @Override
                    public void onSuccess(Resource result) {
                        deleteObject(folder, i + 1, array, callback);
                    }

                    @Override
                    public void onError(Throwable t) {
                        GWT.log("", t);
						setError(t);
                        displayError("System error unable to delete folder: " + t.getMessage());
                    }

    				@Override
    				protected void onUnauthorized(Response response) {
    					sessionExpired();
    				}
                };
                delete.setHeader("X-Auth-Token", getToken());
                Scheduler.get().scheduleDeferred(delete);
            }
            else if (o != null) {
                String subdir = o.get("subdir").isString().stringValue();
                subdir = subdir.substring(0, subdir.length() - 1);
                String path = getApiPath() + getUsername() + "/" + folder.getContainer() + "?format=json&delimiter=/&prefix=" + URL.encodeQueryString(subdir) + "&t=" + System.currentTimeMillis();
                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, path);
                builder.setHeader("X-Auth-Token", getToken());
                try {
                    builder.sendRequest("", new RequestCallback() {
                        @Override
                        public void onResponseReceived(Request request, Response response) {
                            if (response.getStatusCode() == Response.SC_OK) {
                                JSONValue json = JSONParser.parseStrict(response.getText());
                                JSONArray array2 = json.isArray();
                                if (array2 != null) {
                                    int l = array.size();
                                    for (int j=0; j<array2.size(); j++) {
                                        array.set(l++, array2.get(j));
                                    }
                                }
                                deleteObject(folder, i + 1, array, callback);
                            }
                        }

                        @Override
                        public void onError(Request request, Throwable exception) {
                        	setError(exception);
                            displayError("System error unable to delete folder: " + exception.getMessage());
                        }
                    });
                }
                catch (RequestException e) {
                }
            }
        }
        else {
            String path = folder.getUri();
            DeleteRequest deleteFolder = new DeleteRequest(getApiPath(), getUsername(), URL.encode(path)) {
                @Override
                public void onSuccess(Resource result) {
                    updateFolder(folder.getParent(), true, new Command() {
						
						@Override
						public void execute() {
							folderTreeSelectionModel.setSelected(folder.getParent(), true);
							updateStatistics();
							if (callback != null)
								callback.execute();
						}
					}, true);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
					setError(t);
                    if (t instanceof RestException) {
                    	if (((RestException) t).getHttpStatusCode() != Response.SC_NOT_FOUND)
                    		displayError("Unable to delete folder: "+((RestException) t).getHttpStatusText());
                    	else
                    		onSuccess(null);
                    }
                    else
                        displayError("System error unable to delete folder: " + t.getMessage());
                }

				@Override
				protected void onUnauthorized(Response response) {
					sessionExpired();
				}
            };
            deleteFolder.setHeader("X-Auth-Token", getToken());
            Scheduler.get().scheduleDeferred(deleteFolder);
        }
    }

    public FolderTreeView getFolderTreeView() {
        return folderTreeView;
    }

    public void copyFiles(final Iterator<File> iter, final String targetUsername, final String targetUri, final Command callback) {
        if (iter.hasNext()) {
            File file = iter.next();
            String path = targetUri + "/" + file.getName();
            PutRequest copyFile = new PutRequest(getApiPath(), targetUsername, path) {
                @Override
                public void onSuccess(Resource result) {
                    copyFiles(iter, targetUsername, targetUri, callback);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
					setError(t);
                    if (t instanceof RestException) {
                        displayError("Unable to copy file: " + ((RestException) t).getHttpStatusText());
                    }
                    else
                        displayError("System error unable to copy file: "+t.getMessage());
                }

				@Override
				protected void onUnauthorized(Response response) {
					sessionExpired();
				}
            };
            copyFile.setHeader("X-Auth-Token", getToken());
            copyFile.setHeader("X-Copy-From", URL.encodePathSegment(file.getUri()));
            if (!file.getOwner().equals(targetUsername))
            	copyFile.setHeader("X-Source-Account", URL.encodePathSegment(file.getOwner()));
            copyFile.setHeader("Content-Type", file.getContentType());
            Scheduler.get().scheduleDeferred(copyFile);
        }
        else  if (callback != null) {
            callback.execute();
        }
    }

    public void copySubfolders(final Iterator<Folder> iter, final String targetUsername, final String targetUri, final Command callback) {
        if (iter.hasNext()) {
            final Folder f = iter.next();
            copyFolder(f, targetUsername, targetUri, new Command() {
				
				@Override
				public void execute() {
					copySubfolders(iter, targetUsername, targetUri, callback);
				}
			});
        }
        else  if (callback != null) {
            callback.execute();
        }
    }

    public void copyFolder(final Folder f, final String targetUsername, final String targetUri, final Command callback) {
        String path = targetUri + "/" + f.getName();
        PutRequest createFolder = new PutRequest(getApiPath(), targetUsername, path) {
            @Override
            public void onSuccess(Resource result) {
            	GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, getApiPath(), f.getOwner(), "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + URL.encodeQueryString(f.getPrefix()), f) {

					@Override
					public void onSuccess(final Folder _f) {
		                Iterator<File> iter = _f.getFiles().iterator();
		                copyFiles(iter, targetUsername, targetUri + "/" + _f.getName(), new Command() {
		                    @Override
		                    public void execute() {
		                        Iterator<Folder> iterf = _f.getSubfolders().iterator();
		                        copySubfolders(iterf, targetUsername, targetUri + "/" + _f.getName(), callback);
		                    }
		                });
					}

					@Override
					public void onError(Throwable t) {
		                GWT.log("", t);
						setError(t);
		                if (t instanceof RestException) {
		                    displayError("Unable to get folder: " + ((RestException) t).getHttpStatusText());
		                }
		                else
		                    displayError("System error getting folder: " + t.getMessage());
					}

					@Override
					protected void onUnauthorized(Response response) {
						sessionExpired();
					}
				};
				getFolder.setHeader("X-Auth-Token", getToken());
				Scheduler.get().scheduleDeferred(getFolder);
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("", t);
				setError(t);
               if (t instanceof RestException) {
                    displayError("Unable to create folder: " + ((RestException) t).getHttpStatusText());
                }
                else
                    displayError("System error creating folder: " + t.getMessage());
            }

			@Override
			protected void onUnauthorized(Response response) {
				sessionExpired();
			}
        };
        createFolder.setHeader("X-Auth-Token", getToken());
        createFolder.setHeader("Accept", "*/*");
        createFolder.setHeader("Content-Length", "0");
        createFolder.setHeader("Content-Type", "application/folder");
        Scheduler.get().scheduleDeferred(createFolder);
    }
    
    public void addSelectionModel(@SuppressWarnings("rawtypes") SingleSelectionModel model) {
    	selectionModels.add(model);
    }

	public OtherSharedTreeView getOtherSharedTreeView() {
		return otherSharedTreeView;
	}

	public void updateTrash(boolean showFiles, Command callback) {
		updateFolder(trash, showFiles, callback, true);
	}

	public void updateGroupsNode() {
		groupTreeView.updateGroupNode(null);
	}

	public void addGroup(String groupname) {
		Group newGroup = new Group(groupname);
		account.addGroup(newGroup);
		groupTreeView.updateGroupNode(null);
	}

	public void removeGroup(Group group) {
		account.removeGroup(group);
		updateGroupsNode();
	}

	public TreeView getSelectedTree() {
		return selectedTree;
	}
	
	public void setSelectedTree(TreeView selected) {
		selectedTree = selected;
	}

	public Folder getSelection() {
		if (selectedTree != null)
			return selectedTree.getSelection();
		return null;
	}

	public void showFolderStatistics(int folderFileCount) {
		numOfFiles.setHTML(String.valueOf(folderFileCount));
	}

	public GroupTreeView getGroupTreeView() {
		return groupTreeView;
	}

	public void sessionExpired() {
		new SessionExpiredDialog(this).center();
	}

	public void updateRootFolder(Command callback) {
		updateFolder(account.getPithos(), false, callback, true);
	}

	void createMySharedTree() {
		mysharedTreeSelectionModel = new SingleSelectionModel<Folder>();
		mysharedTreeSelectionModel.addSelectionChangeHandler(new Handler() {
		    @Override
		    public void onSelectionChange(SelectionChangeEvent event) {
		        if (mysharedTreeSelectionModel.getSelectedObject() != null) {
		            deselectOthers(mysharedTreeView, mysharedTreeSelectionModel);
		            upload.setEnabled(false);
		            updateSharedFolder(mysharedTreeSelectionModel.getSelectedObject(), true);
					showRelevantToolbarButtons();
		        }
				else {
					if (getSelectedTree().equals(mysharedTreeView))
						setSelectedTree(null);
					if (getSelectedTree() == null)
						showRelevantToolbarButtons();
				}
 		    }
		});
		selectionModels.add(mysharedTreeSelectionModel);
		mysharedTreeViewModel = new MysharedTreeViewModel(Pithos.this, mysharedTreeSelectionModel);
		mysharedTreeViewModel.initialize(new Command() {
			
			@Override
			public void execute() {
			    mysharedTreeView = new MysharedTreeView(mysharedTreeViewModel);
				trees.insert(mysharedTreeView, 3);
				treeViews.add(mysharedTreeView);
				createOtherSharedTree();
			}
		});
	}

	void createOtherSharedTree() {
		otherSharedTreeSelectionModel = new SingleSelectionModel<Folder>();
		otherSharedTreeSelectionModel.addSelectionChangeHandler(new Handler() {
		    @Override
		    public void onSelectionChange(SelectionChangeEvent event) {
		        if (otherSharedTreeSelectionModel.getSelectedObject() != null) {
		            deselectOthers(otherSharedTreeView, otherSharedTreeSelectionModel);
		            otherSharedTreeView.addStyleName("cellTreeWidget-selectedTree");
		            applyPermissions(otherSharedTreeSelectionModel.getSelectedObject());
		            updateOtherSharedFolder(otherSharedTreeSelectionModel.getSelectedObject(), true);
					showRelevantToolbarButtons();
		        }
				else {
					if (getSelectedTree().equals(otherSharedTreeView))
						setSelectedTree(null);
					if (getSelectedTree() == null)
						showRelevantToolbarButtons();
				}
 		    }
		});
		selectionModels.add(otherSharedTreeSelectionModel);
		otherSharedTreeViewModel = new OtherSharedTreeViewModel(Pithos.this, otherSharedTreeSelectionModel);
		otherSharedTreeViewModel.initialize(new Command() {
			
			@Override
			public void execute() {
			    otherSharedTreeView = new OtherSharedTreeView(otherSharedTreeViewModel);
				trees.insert(otherSharedTreeView, 3);
				treeViews.add(otherSharedTreeView);
			}
		});
	}

	public native void log1(String message)/*-{
		$wnd.console.log(message);
	}-*/;

	public String getErrorData() {
		if (error != null)
			return error.toString();
		return "";
	}
	
	public void setError(Throwable t) {
		error = t;
	}
	
	public void showRelevantToolbarButtons() {
		toolbar.showRelevantButtons();
	}

	public FileUploadDialog getFileUploadDialog() {
		if (fileUploadDialog == null)
			fileUploadDialog = new FileUploadDialog(this);
		return fileUploadDialog;
	}

	public void hideUploadIndicator() {
		upload.removeStyleName("pithos-uploadButton-loading");
		upload.setTitle("");
	}
	
	public void showUploadIndicator() {
		upload.addStyleName("pithos-uploadButton-loading");
		upload.setTitle("Upload in progress. Click for details.");
	}

	public void scheduleFolderHeadCommand(final Folder folder, final Command callback) {
		if (folder == null) {
			if (callback != null)
				callback.execute();
		}
		else {
			HeadRequest<Folder> headFolder = new HeadRequest<Folder>(Folder.class, getApiPath(), folder.getOwner(), folder.getUri(), folder) {
	
				@Override
				public void onSuccess(Folder _result) {
					if (callback != null)
						callback.execute();
				}
	
				@Override
				public void onError(Throwable t) {
			        if (t instanceof RestException) {
			        	if (((RestException) t).getHttpStatusCode() == Response.SC_NOT_FOUND) {
	                        final String path = folder.getUri();
	                        PutRequest newFolder = new PutRequest(getApiPath(), folder.getOwner(), path) {
	                            @Override
	                            public void onSuccess(Resource _result) {
	                            	scheduleFolderHeadCommand(folder, callback);
	                            }
	
	                            @Override
	                            public void onError(Throwable _t) {
	                                GWT.log("", _t);
	        						setError(_t);
	                                if(_t instanceof RestException){
	                                    displayError("Unable to create folder: " + ((RestException) _t).getHttpStatusText());
	                                }
	                                else
	                                    displayError("System error creating folder: " + _t.getMessage());
	                            }
	
	            				@Override
	            				protected void onUnauthorized(Response response) {
	            					sessionExpired();
	            				}
	                        };
	                        newFolder.setHeader("X-Auth-Token", getToken());
	                        newFolder.setHeader("Content-Type", "application/folder");
	                        newFolder.setHeader("Accept", "*/*");
	                        newFolder.setHeader("Content-Length", "0");
	                        Scheduler.get().scheduleDeferred(newFolder);
			        	}
			        	else
			        		displayError("Error heading folder: " + ((RestException) t).getHttpStatusText());
			        }
			        else
			            displayError("System error heading folder: " + t.getMessage());
	
			        GWT.log("Error heading folder", t);
					setError(t);
				}
	
				@Override
				protected void onUnauthorized(Response response) {
					sessionExpired();
				}
			};
			headFolder.setHeader("X-Auth-Token", getToken());
			Scheduler.get().scheduleDeferred(headFolder);
		}
	}

	public void scheduleFileHeadCommand(File f, final Command callback) {
		HeadRequest<File> headFile = new HeadRequest<File>(File.class, getApiPath(), f.getOwner(), f.getUri(), f) {

			@Override
			public void onSuccess(File _result) {
				if (callback != null)
					callback.execute();
			}

			@Override
			public void onError(Throwable t) {
		        GWT.log("Error heading file", t);
				setError(t);
		        if (t instanceof RestException)
		            displayError("Error heading file: " + ((RestException) t).getHttpStatusText());
		        else
		            displayError("System error heading file: " + t.getMessage());
			}

			@Override
			protected void onUnauthorized(Response response) {
				sessionExpired();
			}
		};
		headFile.setHeader("X-Auth-Token", getToken());
		Scheduler.get().scheduleDeferred(headFile);
	}
}
