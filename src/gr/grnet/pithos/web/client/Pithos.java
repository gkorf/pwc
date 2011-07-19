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
package gr.grnet.pithos.web.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Command;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import gr.grnet.pithos.web.client.commands.GetUserCommand;
import gr.grnet.pithos.web.client.foldertree.AccountResource;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.FolderTreeView;
import gr.grnet.pithos.web.client.foldertree.FolderTreeViewModel;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.DeleteRequest;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.PutRequest;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.OtherUserResource;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;
import gr.grnet.pithos.web.client.rest.resource.SharedResource;
import gr.grnet.pithos.web.client.rest.resource.TrashResource;
import gr.grnet.pithos.web.client.rest.resource.UserResource;

import gr.grnet.pithos.web.client.tagtree.Tag;
import gr.grnet.pithos.web.client.tagtree.TagTreeView;
import gr.grnet.pithos.web.client.tagtree.TagTreeViewModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.Set;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Pithos implements EntryPoint, ResizeHandler {

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	public static final int VISIBLE_FILE_COUNT = 25;

	/**
	 * Instantiate an application-level image bundle. This object will provide
	 * programmatic access to all the images needed by widgets.
	 */
	private static Images images = (Images) GWT.create(Images.class);

    public String getUsername() {
        return username;
    }

    public void setAccount(AccountResource acct) {
        account = acct;
    }

    public AccountResource getAccount() {
        return account;
    }

    public void updateFolder(Folder f) {
        folderTreeView.updateFolder(f);
    }

    public void updateTag(Tag t) {
        tagTreeView.updateTag(t);
    }

    public void updateTags() {
        tagTreeViewModel.initialize(account);
    }

    /**
	 * An aggregate image bundle that pulls together all the images for this
	 * application into a single bundle.
	 */
	public interface Images extends ClientBundle, TopPanel.Images, StatusPanel.Images, FileMenu.Images, EditMenu.Images, SettingsMenu.Images, FilePropertiesDialog.Images, MessagePanel.Images, FileList.Images, Search.Images, CellTreeView.Images {

		@Source("gr/grnet/pithos/resources/document.png")
		ImageResource folders();

		@Source("gr/grnet/pithos/resources/edit_group_22.png")
		ImageResource groups();

		@Source("gr/grnet/pithos/resources/search.png")
		ImageResource search();
	}

	/**
	 * The single Pithos instance.
	 */
	private static Pithos singleton;

	/**
	 * Gets the singleton Pithos instance.
	 *
	 * @return the Pithos object
	 */
	public static Pithos get() {
		if (Pithos.singleton == null)
			Pithos.singleton = new Pithos();
		return Pithos.singleton;
	}

	/**
	 * The Application Clipboard implementation;
	 */
	private Clipboard clipboard = new Clipboard();

	private UserResource currentUserResource;

	/**
	 * The top panel that contains the menu bar.
	 */
	private TopPanel topPanel;

	/**
	 * The panel that contains the various system messages.
	 */
	private MessagePanel messagePanel = new MessagePanel(Pithos.images);

	/**
	 * The bottom panel that contains the status bar.
	 */
	private StatusPanel statusPanel = null;

	/**
	 * The top right panel that displays the logged in user details
	 */
	private UserDetailsPanel userDetailsPanel = new UserDetailsPanel();

	/**
	 * The file list widget.
	 */
	private FileList fileList;

	/**
	 * The tab panel that occupies the right side of the screen.
	 */
	private TabPanel inner = new DecoratedTabPanel(){
		
//		public void onBrowserEvent(com.google.gwt.user.client.Event event) {
//			if (DOM.eventGetType(event) == Event.ONCONTEXTMENU){
//				if(isFileListShowing()){
//					getFileList().showContextMenu(event);
//				}
//			}
//		};
	};


	/**
	 * The split panel that will contain the left and right panels.
	 */
	private HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();

	/**
	 * The widget that displays the tree of folders.
	 */
	
	private CellTreeView treeView = null;
	/**
	 * The currently selected item in the application, for use by the Edit menu
	 * commands. Potential types are Folder, File, User and Group.
	 */
	private Object currentSelection;


	/**
	 * The WebDAV password of the current user
	 */
	private String webDAVPassword;

	public HashMap<String, String> userFullNameMap = new HashMap<String, String>();

    private String username = null;

    /**
     * The authentication token of the current user.
     */
    private String token;

    private SingleSelectionModel<Folder> folderTreeSelectionModel;
    private FolderTreeViewModel folderTreeViewModel;
    private FolderTreeView folderTreeView;

    private SingleSelectionModel<Tag> tagTreeSelectionModel;
    private TagTreeViewModel tagTreeViewModel;
    private TagTreeView tagTreeView;

    private AccountResource account;

	@Override
	public void onModuleLoad() {
		// Initialize the singleton before calling the constructors of the
		// various widgets that might call Pithos.get().
		singleton = this;
		if (parseUserCredentials())
            initialize();
	}

    private void initialize() {
        topPanel = new TopPanel(Pithos.images);
        topPanel.setWidth("100%");

        messagePanel.setWidth("100%");
        messagePanel.setVisible(false);


        // Inner contains the various lists.
        inner.sinkEvents(Event.ONCONTEXTMENU);
        inner.setAnimationEnabled(true);
        inner.getTabBar().addStyleName("pithos-MainTabBar");
        inner.getDeckPanel().addStyleName("pithos-MainTabPanelBottom");

        inner.setWidth("100%");

        inner.addSelectionHandler(new SelectionHandler<Integer>() {

            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                int tabIndex = event.getSelectedItem();
                switch (tabIndex) {
                    case 0:
                        fileList.updateCurrentlyShowingStats();
                        break;
                }
            }
        });

        folderTreeSelectionModel = new SingleSelectionModel<Folder>();
        folderTreeSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Folder f = folderTreeSelectionModel.getSelectedObject();
                updateFolder(f);
            }
        });

        folderTreeViewModel = new FolderTreeViewModel(folderTreeSelectionModel);
        folderTreeView = new FolderTreeView(folderTreeViewModel);

        fileList = new FileList(images, folderTreeView);
        inner.add(fileList, createHeaderHTML(AbstractImagePrototype.create(images.folders()), "Files"), true);

        tagTreeSelectionModel = new SingleSelectionModel<Tag>();
        tagTreeSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Tag t = tagTreeSelectionModel.getSelectedObject();
                updateTag(t);
            }
        });
        tagTreeViewModel = new TagTreeViewModel(tagTreeSelectionModel);
        tagTreeView = new TagTreeView(tagTreeViewModel);

        VerticalPanel trees = new VerticalPanel();
        trees.add(folderTreeView);
        trees.add(tagTreeView);
        // Add the left and right panels to the split panel.
        splitPanel.setLeftWidget(trees);
        splitPanel.setRightWidget(inner);
        splitPanel.setSplitPosition("25%");
        splitPanel.setSize("100%", "100%");
        splitPanel.addStyleName("pithos-splitPanel");

        // Create a dock panel that will contain the menu bar at the top,
        // the shortcuts to the left, the status bar at the bottom and the
        // right panel taking the rest.
        VerticalPanel outer = new VerticalPanel();
        outer.add(topPanel);
        outer.add(messagePanel);
        outer.add(splitPanel);
        statusPanel = new StatusPanel(Pithos.images);
        outer.add(statusPanel);
        outer.setWidth("100%");
        outer.setCellHorizontalAlignment(messagePanel, HasHorizontalAlignment.ALIGN_CENTER);

        outer.setSpacing(4);

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
                fetchAccount();
            }
        });
    }

    public void showFiles(Folder f) {
        inner.selectTab(0);
        if (f.isTrash()) {
            fileList.showTrash();
        }
        else
            fileList.showFiles();
        Set<File> files = f.getFiles();
        Iterator<File> iter = files.iterator();
        fetchFile(iter, files);
    }

    private void fetchFile(final Iterator<File> iter, final Set<File> files) {
        if (iter.hasNext()) {
            File file = iter.next();
            String path = getApiPath() + username + "/" + file.getContainer() + "/" + file.getPath() + "?format=json";
            GetRequest<File> getFile = new GetRequest<File>(File.class, path, file) {
                @Override
                public void onSuccess(File result) {
                    fetchFile(iter, files);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("Error getting file", t);
                    if (t instanceof RestException)
                        Pithos.get().displayError("Error getting file: " + ((RestException) t).getHttpStatusText());
                    else
                        Pithos.get().displayError("System error fetching file: " + t.getMessage());
                }
            };
            getFile.setHeader("X-Auth-Token", "0000");
            Scheduler.get().scheduleDeferred(getFile);
        }
        else
            fileList.setFiles(new ArrayList<File>(files));
    }

    /**
	 * Parse and store the user credentials to the appropriate fields.
	 */
	private boolean parseUserCredentials() {
		Configuration conf = (Configuration) GWT.create(Configuration.class);
		String cookie = conf.authCookie();
		String auth = Cookies.getCookie(cookie);
		if (auth == null) {
			authenticateUser();
            return false;
        }
        else {
            String[] authSplit = auth.split("\\" + conf.cookieSeparator(), 2);
            if (authSplit.length != 2) {
                authenticateUser();
                return false;
            }
            else {
                username = authSplit[0];
                token = authSplit[1];
                return true;
            }
        }
	}

    /**
	 * Redirect the user to the login page for authentication.
	 */
	protected void authenticateUser() {
		Configuration conf = (Configuration) GWT.create(Configuration.class);

//        Window.Location.assign(GWT.getModuleBaseURL() + conf.loginUrl() + "?next=" + Window.Location.getHref());
        Cookies.setCookie(conf.authCookie(), "test" + conf.cookieSeparator() + "0000");
        Window.Location.assign(GWT.getModuleBaseURL() + "Pithos.html");
	}

    private void fetchAccount() {
        String path = getApiPath() + username + "?format=json";

        GetRequest<AccountResource> getAccount = new GetRequest<AccountResource>(AccountResource.class, path) {
            @Override
            public void onSuccess(AccountResource result) {
                account = result;
                statusPanel.displayStats(account);
                folderTreeViewModel.initialize(account);
                inner.selectTab(0);
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error getting account", t);
                if (t instanceof RestException)
                    Pithos.get().displayError("Error getting account: " + ((RestException) t).getHttpStatusText());
                else
                    Pithos.get().displayError("System error fetching user data: " + t.getMessage());
            }
        };
        getAccount.setHeader("X-Auth-Token", token);
        Scheduler.get().scheduleDeferred(getAccount);
    }

	/**
	 * Clear the cookie and redirect the user to the logout page.
	 */
	void logout() {
		Configuration conf = (Configuration) GWT.create(Configuration.class);
		String cookie = conf.authCookie();
		String domain = Window.Location.getHostName();
		String path = Window.Location.getPath();
		Cookies.setCookie(cookie, "", null, domain, path, false);
        String baseUrl = GWT.getModuleBaseURL();
        String homeUrl = baseUrl.substring(0, baseUrl.indexOf(path));
		Window.Location.assign(homeUrl + conf.logoutUrl());
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

	private void onWindowResized(int height) {
		// Adjust the split panel to take up the available room in the window.
		int newHeight = height - splitPanel.getAbsoluteTop() - 44;
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

	public boolean isFileListShowing() {
		int tab = inner.getTabBar().getSelectedTab();
		if (tab == 0)
			return true;
		return false;
	}

	public boolean isSearchResultsShowing() {
		int tab = inner.getTabBar().getSelectedTab();
		if (tab == 2)
			return true;
		return false;
	}

	/**
	 * Make the file list visible.
	 *
	 * @param update
	 */
	public void showFileList(boolean update) {
		if(update){
			getTreeView().refreshCurrentNode(true);
		}
		else{
			RestResource currentFolder = getTreeView().getSelection();
			if(currentFolder!=null){
				showFileList(currentFolder);
		}
		}

	}
	
	public void showFileList(RestResource r) {
		showFileList(r,true);
	}
	
	public void showFileList(RestResource r, boolean clearSelection) {
		RestResource currentFolder = r;
		if(currentFolder!=null){
			List<FileResource> files = null;
			if (currentFolder instanceof RestResourceWrapper) {
				RestResourceWrapper folder = (RestResourceWrapper) currentFolder;
				files = folder.getResource().getFiles();
			}
		}
		inner.selectTab(0);
	}

	/**
	 * Display the 'loading' indicator.
	 */
	public void showLoadingIndicator(String message, String path) {
		if(path!=null){
			String[] split = path.split("/");
			message = message +" "+URL.decode(split[split.length-1]);
		}
		topPanel.getLoading().show(message);
	}

	/**
	 * Hide the 'loading' indicator.
	 */
	public void hideLoadingIndicator() {
		topPanel.getLoading().hide();
	}

	/**
	 * A native JavaScript method to reach out to the browser's window and
	 * invoke its resizeTo() method.
	 *
	 * @param x the new width
	 * @param y the new height
	 */
	public static native void resizeTo(int x, int y) /*-{
		$wnd.resizeTo(x,y);
	}-*/;

	/**
	 * A helper method that returns true if the user's list is currently visible
	 * and false if it is hidden.
	 *
	 * @return true if the user list is visible
	 */
	public boolean isUserListVisible() {
		return inner.getTabBar().getSelectedTab() == 1;
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
	 * Retrieve the folders.
	 *
	 * @return the folders
	 
	public Folders getFolders() {
		return folders;
	}*/

	/**
	 * Retrieve the currentSelection.
	 *
	 * @return the currentSelection
	 */
	public Object getCurrentSelection() {
		return currentSelection;
	}

	/**
	 * Modify the currentSelection.
	 *
	 * @param newCurrentSelection the currentSelection to set
	 */
	public void setCurrentSelection(Object newCurrentSelection) {
		currentSelection = newCurrentSelection;
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

	/**
	 * Retrieve the userDetailsPanel.
	 *
	 * @return the userDetailsPanel
	 */
	public UserDetailsPanel getUserDetailsPanel() {
		return userDetailsPanel;
	}

	

	public String getToken() {
		return token;
	}

	public String getWebDAVPassword() {
		return webDAVPassword;
	}

	/**
	 * Retrieve the currentUserResource.
	 *
	 * @return the currentUserResource
	 */
	public UserResource getCurrentUserResource() {
		return currentUserResource;
	}

	/**
	 * Modify the currentUserResource.
	 *
	 * @param newUser the new currentUserResource
	 */
	public void setCurrentUserResource(UserResource newUser) {
		currentUserResource = newUser;
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
	 * Convert server date to local time according to browser timezone
	 * and format it according to localized pattern.
	 * Time is always formatted to 24hr format.
	 * NB: This assumes that server runs in UTC timezone. Otherwise
	 * we would need to adjust for server time offset as well.
	 *
	 * @param date
	 * @return String
	 */
	public static String formatLocalDateTime(Date date) {
		Date convertedDate = new Date(date.getTime() - date.getTimezoneOffset());
		final DateTimeFormat dateFormatter = DateTimeFormat.getShortDateFormat();
		final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm");
		String datePart = dateFormatter.format(convertedDate);
		String timePart = timeFormatter.format(convertedDate);
		return datePart + " " + timePart;
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

	/**
	 * This method examines the token input and add a "/" at the end in case it's omitted.
	 * This happens only in Files/trash/, Files/shared/, Files/others.
	 *
	 * @param tokenInput
	 * @return the formated token with a "/" at the end or the same tokenInput parameter
	 */

	private String handleSpecialFolderNames(String tokenInput){
		List<String> pathsToCheck = Arrays.asList("Files/trash", "Files/shared", "Files/others");
		if(pathsToCheck.contains(tokenInput))
			return tokenInput + "/";
		return tokenInput;

	}

	/**
	 * Reject illegal resource names, like '.' or '..' or slashes '/'.
	 */
	static boolean isValidResourceName(String name) {
		if (".".equals(name) ||	"..".equals(name) || name.contains("/"))
			return false;
		return true;
	}

	public void putUserToMap(String _userName, String _userFullName){
		userFullNameMap.put(_userName, _userFullName);
	}

	public String findUserFullName(String _userName){
		return userFullNameMap.get(_userName);
	}
	public String getUserFullName(String _userName) {
		
        if (Pithos.get().findUserFullName(_userName) == null)
                //if there is no userFullName found then the map fills with the given _userName,
                //so userFullName = _userName
                Pithos.get().putUserToMap(_userName, _userName);
        else if(Pithos.get().findUserFullName(_userName).indexOf('@') != -1){
                //if the userFullName = _userName the GetUserCommand updates the userFullName in the map
                GetUserCommand guc = new GetUserCommand(_userName);
                guc.execute();
        }
        return Pithos.get().findUserFullName(_userName);
	}
	/**
	 * Retrieve the treeView.
	 *
	 * @return the treeView
	 */
	public CellTreeView getTreeView() {
		return treeView;
	}
	
	public void onResourceUpdate(RestResource resource,boolean clearSelection){
		if(resource instanceof RestResourceWrapper || resource instanceof OtherUserResource || resource instanceof TrashResource || resource instanceof SharedResource){
			if(getTreeView().getSelection()!=null&&getTreeView().getSelection().getUri().equals(resource.getUri()))
				showFileList(resource,clearSelection);
		}
		
	}

    public void deleteFolder(final Folder folder) {
        String path = getApiPath() + getUsername() + "/" + folder.getContainer() + "?format=json&delimiter=/&prefix=" + folder.getPrefix();
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, path);
        builder.setHeader("If-Modified-Since", "0");
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
                            deleteObject(folder, i, array);
                        }
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Pithos.get().displayError("System error unable to delete folder: " + exception.getMessage());
                }
            });
        }
        catch (RequestException e) {
        }
    }

    public void deleteObject(final Folder folder, final int i, final JSONArray array) {
        if (i < array.size()) {
            JSONObject o = array.get(i).isObject();
            if (o != null && !o.containsKey("subdir")) {
                JSONString name = o.get("name").isString();
                String path = getApiPath() + getUsername() + "/" + folder.getContainer() + "/" + name.stringValue();
                DeleteRequest delete = new DeleteRequest(path) {
                    @Override
                    public void onSuccess(Resource result) {
                        deleteObject(folder, i + 1, array);
                    }

                    @Override
                    public void onError(Throwable t) {
                        GWT.log("", t);
                        Pithos.get().displayError("System error unable to delete folder: " + t.getMessage());
                    }
                };
                delete.setHeader("X-Auth-Token", getToken());
                Scheduler.get().scheduleDeferred(delete);
            }
            else {
                String subdir = o.get("subdir").isString().stringValue();
                subdir = subdir.substring(0, subdir.length() - 1);
                String path = getApiPath() + getUsername() + "/" + folder.getContainer() + "?format=json&delimiter=/&prefix=" + subdir;
                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, path);
                builder.setHeader("If-Modified-Since", "0");
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
                                deleteObject(folder, i + 1, array);
                            }
                        }

                        @Override
                        public void onError(Request request, Throwable exception) {
                            Pithos.get().displayError("System error unable to delete folder: " + exception.getMessage());
                        }
                    });
                }
                catch (RequestException e) {
                }
            }
        }
        else {
            String prefix = folder.getPrefix();
            String path = getApiPath() + getUsername() + "/" + folder.getContainer() + (prefix.length() == 0 ? "" : "/" + prefix);
            DeleteRequest deleteFolder = new DeleteRequest(path) {
                @Override
                public void onSuccess(Resource result) {
                    updateFolder(folder.getParent());
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
                    if (t instanceof RestException) {
                        displayError("Unable to delete folder: "+((RestException) t).getHttpStatusText());
                    }
                    else
                        Pithos.get().displayError("System error unable to delete folder: " + t.getMessage());
                }
            };
            deleteFolder.setHeader("X-Auth-Token", getToken());
            Scheduler.get().scheduleDeferred(deleteFolder);
        }
    }

    public FolderTreeView getFolderTreeView() {
        return folderTreeView;
    }

    public void copyFiles(final Iterator<File> iter, final String targetUri, final Command callback) {
        if (iter.hasNext()) {
            File file = iter.next();
            String path = getApiPath() + getUsername() + targetUri + "/" + file.getName();
            PutRequest copyFile = new PutRequest(path) {
                @Override
                public void onSuccess(Resource result) {
                    copyFiles(iter, targetUri, callback);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
                    if (t instanceof RestException) {
                        Pithos.get().displayError("Unable to copy file: " + ((RestException) t).getHttpStatusText());
                    }
                    else
                        Pithos.get().displayError("System error unable to copy file: "+t.getMessage());
                }
            };
            copyFile.setHeader("X-Auth-Token", getToken());
            copyFile.setHeader("X-Copy-From", file.getUri());
            Scheduler.get().scheduleDeferred(copyFile);
        }
        else  if (callback != null) {
            callback.execute();
        }
    }

    public void copySubfolders(final Iterator<Folder> iter, final String targetUri, final Command callback) {
        if (iter.hasNext()) {
            final Folder f = iter.next();
            copyFolder(f, targetUri, callback);
        }
        else  if (callback != null) {
            callback.execute();
        }
    }

    public void copyFolder(final Folder f, final String targetUri, final Command callback) {
        String path = getApiPath() + getUsername() + targetUri + "/" + f.getName();
        PutRequest createFolder = new PutRequest(path) {
            @Override
            public void onSuccess(Resource result) {
                Iterator<File> iter = f.getFiles().iterator();
                copyFiles(iter, targetUri + "/" + f.getName(), new Command() {
                    @Override
                    public void execute() {
                        Iterator<Folder> iterf = f.getSubfolders().iterator();
                        copySubfolders(iterf, targetUri + "/" + f.getName(), new Command() {
                            @Override
                            public void execute() {
                                callback.execute();
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("", t);
                if (t instanceof RestException) {
                    displayError("Unable to create folder:" + ((RestException) t).getHttpStatusText());
                }
                else
                    displayError("System error creating folder:" + t.getMessage());
            }
        };
        createFolder.setHeader("X-Auth-Token", getToken());
        createFolder.setHeader("Accept", "*/*");
        createFolder.setHeader("Content-Length", "0");
        createFolder.setHeader("Content-Type", "application/folder");
        Scheduler.get().scheduleDeferred(createFolder);
    }
}
