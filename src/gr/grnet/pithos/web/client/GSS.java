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
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import gr.grnet.pithos.web.client.clipboard.Clipboard;
import gr.grnet.pithos.web.client.commands.GetUserCommand;
import gr.grnet.pithos.web.client.foldertree.AccountResource;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.FolderTreeView;
import gr.grnet.pithos.web.client.foldertree.FolderTreeViewModel;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.OtherUserResource;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;
import gr.grnet.pithos.web.client.rest.resource.SharedResource;
import gr.grnet.pithos.web.client.rest.resource.TrashResource;
import gr.grnet.pithos.web.client.rest.resource.UserResource;

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
import com.google.gwt.user.client.DOM;
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
public class GSS implements EntryPoint, ResizeHandler {

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
	 * The single GSS instance.
	 */
	private static GSS singleton;

	/**
	 * Gets the singleton GSS instance.
	 *
	 * @return the GSS object
	 */
	public static GSS get() {
		if (GSS.singleton == null)
			GSS.singleton = new GSS();
		return GSS.singleton;
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
	private MessagePanel messagePanel = new MessagePanel(GSS.images);

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

    private AccountResource account;

	@Override
	public void onModuleLoad() {
		// Initialize the singleton before calling the constructors of the
		// various widgets that might call GSS.get().
		singleton = this;
		if (parseUserCredentials())
            initialize();
	}

    private void initialize() {
        topPanel = new TopPanel(GSS.images);
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
                showFiles(f);
            }
        });

        folderTreeViewModel = new FolderTreeViewModel(folderTreeSelectionModel);
        folderTreeView = new FolderTreeView(folderTreeViewModel);

        fileList = new FileList(images, folderTreeView);
        inner.add(fileList, createHeaderHTML(AbstractImagePrototype.create(images.folders()), "Files"), true);

        // Add the left and right panels to the split panel.
        splitPanel.setLeftWidget(folderTreeView);
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
        statusPanel = new StatusPanel(GSS.images);
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

    private void showFiles(Folder f) {
        inner.selectTab(0);
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
                        GSS.get().displayError("Error getting file: " + ((RestException) t).getHttpStatusText());
                    else
                        GSS.get().displayError("System error fetching file: " + t.getMessage());
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
        Window.Location.assign(GWT.getModuleBaseURL() + "GSS.html");
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
                    GSS.get().displayError("Error getting account: " + ((RestException) t).getHttpStatusText());
                else
                    GSS.get().displayError("System error fetching user data: " + t.getMessage());
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
		
        if (GSS.get().findUserFullName(_userName) == null)
                //if there is no userFullName found then the map fills with the given _userName,
                //so userFullName = _userName
                GSS.get().putUserToMap(_userName, _userName);
        else if(GSS.get().findUserFullName(_userName).indexOf('@') != -1){
                //if the userFullName = _userName the GetUserCommand updates the userFullName in the map
                GetUserCommand guc = new GetUserCommand(_userName);
                guc.execute();
        }
        return GSS.get().findUserFullName(_userName);
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
}
