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
package gr.grnet.pithos.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import gr.grnet.pithos.web.client.catalog.UpdateUserCatalogs;
import gr.grnet.pithos.web.client.catalog.UserCatalogs;
import gr.grnet.pithos.web.client.commands.UploadFileCommand;
import gr.grnet.pithos.web.client.foldertree.*;
import gr.grnet.pithos.web.client.grouptree.Group;
import gr.grnet.pithos.web.client.grouptree.GroupTreeView;
import gr.grnet.pithos.web.client.grouptree.GroupTreeViewModel;
import gr.grnet.pithos.web.client.mysharedtree.MysharedTreeView;
import gr.grnet.pithos.web.client.mysharedtree.MysharedTreeViewModel;
import gr.grnet.pithos.web.client.othersharedtree.OtherSharedTreeView;
import gr.grnet.pithos.web.client.othersharedtree.OtherSharedTreeViewModel;
import gr.grnet.pithos.web.client.rest.*;
import org.apache.http.HttpStatus;

import java.util.*;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Pithos implements EntryPoint, ResizeHandler {
    private static final boolean IsLOGEnabled = false;
    public static final boolean IsDetailedHTTPLOGEnabled = true;
    public static final boolean IsFullResponseBodyLOGEnabled = true;
    private static final boolean EnableScheduledRefresh = true; // Make false only for debugging purposes.

    public static final Set<String> HTTPHeadersToIgnoreInLOG = new HashSet<String>();
    static {
        HTTPHeadersToIgnoreInLOG.add(Const.HTTP_HEADER_CONNECTION);
        HTTPHeadersToIgnoreInLOG.add(Const.HTTP_HEADER_DATE);
        HTTPHeadersToIgnoreInLOG.add(Const.HTTP_HEADER_KEEP_ALIVE);
        HTTPHeadersToIgnoreInLOG.add(Const.HTTP_HEADER_SERVER);
        HTTPHeadersToIgnoreInLOG.add(Const.HTTP_HEADER_VARY);
        HTTPHeadersToIgnoreInLOG.add(Const.IF_MODIFIED_SINCE);
    }

    public static final Configuration config = GWT.create(Configuration.class);
    public static final String CONFIG_API_PATH = config.apiPath();
    static {
        LOG("CONFIG_API_PATH = ", CONFIG_API_PATH);
    }

    public static final Dictionary otherProperties = Dictionary.getDictionary(Const.OTHER_PROPERTIES);
    public static String getFromOtherPropertiesOrDefault(String key, String def) {
        try {
            final String value = otherProperties.get(key);
            return value == null ? def : value;
        }
        catch(Exception e) {
            return def;
        }
    }

    public static String getFromOtherPropertiesOrNull(String key) {
        return getFromOtherPropertiesOrDefault(key, null);
    }

    private static final boolean SHOW_COPYRIGHT;
    static {
        final String valueStr = getFromOtherPropertiesOrDefault("SHOW_COPYRIGHT", "true").trim().toLowerCase();
        SHOW_COPYRIGHT = "true".equals(valueStr);
        LOG("SHOW_COPYRIGHT = '", valueStr, "' ==> ", SHOW_COPYRIGHT);
    }

    public static final String OTHERPROPS_STORAGE_API_URL = getFromOtherPropertiesOrNull("STORAGE_API_URL");
    public static final String OTHERPROPS_USER_CATALOGS_API_URL = getFromOtherPropertiesOrNull("USER_CATALOGS_API_URL");
    static {
        LOG("STORAGE_API_URL = ", OTHERPROPS_STORAGE_API_URL);
        LOG("USER_CATALOGS_API_URL = ", OTHERPROPS_USER_CATALOGS_API_URL);
    }

    public static final String STORAGE_API_URL;
    static {
        if(OTHERPROPS_STORAGE_API_URL != null) {
            STORAGE_API_URL = OTHERPROPS_STORAGE_API_URL;
        }
        else if(CONFIG_API_PATH != null) {
            STORAGE_API_URL = CONFIG_API_PATH;
        }
        else {
            throw new RuntimeException("Unknown STORAGE_API_URL");
        }

        LOG("Computed STORAGE_API_URL = ", STORAGE_API_URL);
    }

    public static final String STORAGE_VIEW_URL;
    static {
        final String viewURL = getFromOtherPropertiesOrNull("STORAGE_VIEW_URL");
        if(viewURL != null) {
            STORAGE_VIEW_URL = viewURL;
        }
        else {
            STORAGE_VIEW_URL = STORAGE_API_URL;
        }

        LOG("Computed STORAGE_VIEW_URL = ", STORAGE_VIEW_URL);
    }

    public static final String PUBLIC_LINK_VIEW_PREFIX = getFromOtherPropertiesOrDefault("PUBLIC_LINK_VIEW_PREFIX", "");

    public static final String USER_CATALOGS_API_URL;
    static {
        if(OTHERPROPS_USER_CATALOGS_API_URL != null) {
            USER_CATALOGS_API_URL = OTHERPROPS_USER_CATALOGS_API_URL;
        }
        else if(OTHERPROPS_STORAGE_API_URL != null) {
            throw new RuntimeException("STORAGE_API_URL is defined but USER_CATALOGS_API_URL is not");
        }
        else {
            // https://server.com/v1/ --> https://server.com
            String url = CONFIG_API_PATH;
            url = Helpers.stripTrailing(url, "/");
            url = Helpers.upToIncludingLastPart(url, "/");
            url = Helpers.stripTrailing(url, "/");
            url = url + "/user_catalogs";

            USER_CATALOGS_API_URL = url;

            LOG("Computed USER_CATALOGS_API_URL = ", USER_CATALOGS_API_URL);
        }
    }

    public interface Style extends CssResource {
        String commandAnchor();

        String statistics();

        @ClassName("gwt-HTML")
        String html();

        String uploadAlert();

        String uploadAlertLink();

        String uploadAlertProgress();

        String uploadAlertPercent();

        String uploadAlertClose();
    }

    public interface Resources extends ClientBundle {
        @Source("Pithos.css")
        Style pithosCss();

        @Source("gr/grnet/pithos/resources/close-popup.png")
        ImageResource closePopup();
    }

    public static Resources resources = GWT.create(Resources.class);

    /**
     * Instantiate an application-level image bundle. This object will provide
     * programmatic access to all the images needed by widgets.
     */
    static Images images = (Images) GWT.create(Images.class);

    public String getUserID() {
        return userID;
    }

    public UserCatalogs getUserCatalogs() {
        return userCatalogs;
    }

    public String getCurrentUserDisplayNameOrID() {
        final String displayName = userCatalogs.getDisplayName(getUserID());
        return displayName == null ? getUserID() : displayName;
    }

    public boolean hasDisplayNameForUserID(String userID) {
        return userCatalogs.getDisplayName(userID) != null;
    }

    public boolean hasIDForUserDisplayName(String userDisplayName) {
        return userCatalogs.getID(userDisplayName) != null;
    }

    public String getDisplayNameForUserID(String userID) {
        return userCatalogs.getDisplayName(userID);
    }

    public String getIDForUserDisplayName(String userDisplayName) {
        return userCatalogs.getID(userDisplayName);
    }

    public List<String> getDisplayNamesForUserIDs(List<String> userIDs) {
        if(userIDs == null) {
            userIDs = new ArrayList<String>();
        }
        final List<String> userDisplayNames = new ArrayList<String>();
        for(String userID : userIDs) {
            final String displayName = getDisplayNameForUserID(userID);
            userDisplayNames.add(displayName);
        }

        return userDisplayNames;
    }

    public List<String> filterUserIDsWithUnknownDisplayName(Collection<String> userIDs) {
        if(userIDs == null) {
            userIDs = new ArrayList<String>();
        }
        final List<String> filtered = new ArrayList<String>();
        for(String userID : userIDs) {
            if(!this.userCatalogs.hasID(userID)) {
                filtered.add(userID);
            }
        }
        return filtered;
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

    public void updateSharedFolder(Folder f, boolean showfiles, Command callback) {
        mysharedTreeView.updateFolder(f, showfiles, callback);
    }

    public void updateSharedFolder(Folder f, boolean showfiles) {
        updateSharedFolder(f, showfiles, null);
    }

    public void updateOtherSharedFolder(Folder f, boolean showfiles, Command callback) {
        otherSharedTreeView.updateFolder(f, showfiles, callback);
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
        @ImageOptions(width = 32, height = 32)
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
    StatusPanel statusPanel = null;

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

    /**
     * The ID that uniquely identifies the user in Pithos+.
     * Currently this is a UUID. It used to be the user's email.
     */
    private String userID = null;

    /**
     * Holds mappings from user UUIDs to emails and vice-versa.
     */
    private UserCatalogs userCatalogs = new UserCatalogs();

    /**
     * The authentication token of the current user.
     */
    private String userToken;

    VerticalPanel trees;

    SingleSelectionModel<Folder> folderTreeSelectionModel;
    FolderTreeViewModel folderTreeViewModel;
    FolderTreeView folderTreeView;

    SingleSelectionModel<Folder> mysharedTreeSelectionModel;
    MysharedTreeViewModel mysharedTreeViewModel;
    MysharedTreeView mysharedTreeView = null;

    protected SingleSelectionModel<Folder> otherSharedTreeSelectionModel;
    OtherSharedTreeViewModel otherSharedTreeViewModel;
    OtherSharedTreeView otherSharedTreeView = null;

    GroupTreeViewModel groupTreeViewModel;
    GroupTreeView groupTreeView;

    TreeView selectedTree;
    protected AccountResource account;

    Folder trash;

    List<Composite> treeViews = new ArrayList<Composite>();

    @SuppressWarnings("rawtypes")
    List<SingleSelectionModel> selectionModels = new ArrayList<SingleSelectionModel>();

    public Button upload;

    private HTML numOfFiles;

    private Toolbar toolbar;

    private FileUploadDialog fileUploadDialog = new FileUploadDialog(this);

    UploadAlert uploadAlert;

    Date lastModified;

    @Override
    public void onModuleLoad() {
        if(parseUserCredentials()) {
            initialize();
        }
    }

    static native void __ConsoleLog(String message) /*-{
      try { console.log(message); } catch (e) {}
    }-*/;

    public static void LOGError(Throwable error, StringBuilder sb) {
        if(!isLOGEnabled()) { return; }

        sb.append("\nException: [" + error.toString().replace("\n", "\n  ") + "]");
        Throwable cause = error.getCause();
        if(cause != null) {
            sb.append("\nCauses:\n");
            while(cause != null) {
                sb.append("  ");
                sb.append("[" + cause.toString().replace("\n", "\n  ")  + "]");
                sb.append("\n");
                cause = cause.getCause();
            }
        }
        else {
            sb.append("\n");
        }

        StackTraceElement[] stackTrace = error.getStackTrace();
        sb.append("Stack trace (" + stackTrace.length + " elements):\n");
        for(int i = 0; i < stackTrace.length; i++) {
            StackTraceElement errorElem = stackTrace[i];
            sb.append("  [" + i + "] ");
            sb.append(errorElem.toString());
            sb.append("\n");
        }
    }

    public static void LOGError(Throwable error) {
        if(!isLOGEnabled()) { return; }

        final StringBuilder sb = new StringBuilder();
        LOGError(error, sb);
        if(sb.length() > 0) {
            __ConsoleLog(sb.toString());
        }
    }

    public static boolean isLOGEnabled() {
        return IsLOGEnabled;
    }

    public static void LOG(Object ...args) {
        if(!isLOGEnabled()) { return; }

        final StringBuilder sb = new StringBuilder();
        for(Object arg : args) {
            if(arg instanceof Throwable) {
                LOGError((Throwable) arg, sb);
            }
            else {
                sb.append(arg);
            }
        }

        if(sb.length() > 0) {
            __ConsoleLog(sb.toString());
        }
    }

    private void initialize() {
        userCatalogs.updateWithIDAndName("*", "All Pithos users");

        lastModified = new Date(); //Initialize if-modified-since value with now.
        resources.pithosCss().ensureInjected();
        boolean bareContent = Window.Location.getParameter("noframe") != null;
        String contentWidth = bareContent ? Const.PERCENT_100 : Const.PERCENT_75;

        VerticalPanel outer = new VerticalPanel();
        outer.setWidth(Const.PERCENT_100);
        if(!bareContent) {
            outer.addStyleName("pithos-outer");
        }

        if(!bareContent) {
            topPanel = new TopPanel(this, Pithos.images);
            topPanel.setWidth(Const.PERCENT_100);
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
        if(bareContent) {
            header.addStyleName("pithos-header-noframe");
        }
        upload = new Button("Upload", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(getSelection() != null) {
                    new UploadFileCommand(Pithos.this, null, getSelection()).execute();
                }
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
        // Inner contains the various lists
        inner.sinkEvents(Event.ONCONTEXTMENU);
        inner.setWidth(Const.PERCENT_100);

        folderTreeSelectionModel = new SingleSelectionModel<Folder>();
        folderTreeSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if(folderTreeSelectionModel.getSelectedObject() != null) {
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
                    if(getSelectedTree().equals(folderTreeView)) {
                        setSelectedTree(null);
                    }
                    if(getSelectedTree() == null) {
                        showRelevantToolbarButtons();
                    }
                }
            }
        });
        selectionModels.add(folderTreeSelectionModel);

        folderTreeViewModel = new FolderTreeViewModel(this, folderTreeSelectionModel);
        folderTreeView = new FolderTreeView(folderTreeViewModel);
        treeViews.add(folderTreeView);

        fileList = new FileList(this, images);
        inner.add(fileList);

        trees = new VerticalPanel();
        trees.setWidth(Const.PERCENT_100);

        // Add the left and right panels to the split panel.
        splitPanel.setLeftWidget(trees);
        FlowPanel right = new FlowPanel();
        right.getElement().setId("rightPanel");
        right.add(inner);
        splitPanel.setRightWidget(right);
        splitPanel.setSplitPosition("219px");
        splitPanel.setSize(Const.PERCENT_100, Const.PERCENT_100);
        splitPanel.addStyleName("pithos-splitPanel");
        splitPanel.setWidth(contentWidth);
        outer.add(splitPanel);
        outer.setCellHorizontalAlignment(splitPanel, HasHorizontalAlignment.ALIGN_CENTER);

        if(!bareContent) {
            statusPanel = new StatusPanel();
            statusPanel.setWidth(Const.PERCENT_100);
            outer.add(statusPanel);
            outer.setCellHorizontalAlignment(statusPanel, HasHorizontalAlignment.ALIGN_CENTER);
        }
        else {
            splitPanel.addStyleName("pithos-splitPanel-noframe");
        }

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
        Scheduler.get().scheduleIncremental(new RepeatingCommand() {

            @Override
            public boolean execute() {
                if(!isCloudbarReady()) {
                    return true;
                }
                onWindowResized(Window.getClientHeight());
                return false;
            }
        });

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                LOG("Pithos::initialize() Calling Pithos::fetchAccount()");
                fetchAccount(new Command() {

                    @Override
                    public void execute() {
                        if(!account.hasHomeContainer()) {
                            createHomeContainer(account, this);
                        }
                        else if(!account.hasTrashContainer()) {
                            createTrashContainer(this);
                        }
                        else {
                            for(Folder f : account.getContainers()) {
                                if(f.getName().equals(Const.TRASH_CONTAINER)) {
                                    trash = f;
                                    break;
                                }
                            }
                            trees.add(folderTreeView);
                            folderTreeViewModel.initialize(account, new Command() {

                                @Override
                                public void execute() {
                                    createMySharedTree();
                                }
                            });

                            HorizontalPanel separator = new HorizontalPanel();
                            separator.addStyleName("pithos-statisticsSeparator");
                            separator.add(new HTML(""));
                            trees.add(separator);

                            groupTreeViewModel = new GroupTreeViewModel(Pithos.this);
                            groupTreeView = new GroupTreeView(groupTreeViewModel);
                            treeViews.add(groupTreeView);
                            trees.add(groupTreeView);
                            folderTreeView.showStatistics(account);
                        }
                    }
                });
            }
        });
    }

    public void scheduleRefresh() {
        if(!Pithos.EnableScheduledRefresh) { return; }

        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            @Override
            public boolean execute() {
                final Folder f = getSelection();
                if(f == null) {
                    return true;
                }

                HeadRequest<Folder> head = new HeadRequest<Folder>(Folder.class, getStorageAPIURL(), f.getOwnerID(), "/" + f.getContainer()) {

                    @Override
                    public void onSuccess(Folder _result) {
                        lastModified = new Date();
                        if(getSelectedTree().equals(folderTreeView)) {
                            updateFolder(f, true, new Command() {

                                @Override
                                public void execute() {
                                    scheduleRefresh();
                                }

                            }, false);
                        }
                        else if(getSelectedTree().equals(mysharedTreeView)) {
                            updateSharedFolder(f, true, new Command() {

                                @Override
                                public void execute() {
                                    scheduleRefresh();
                                }
                            });
                        }
                        else {
                            scheduleRefresh();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if(t instanceof RestException && ((RestException) t).getHttpStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
                            scheduleRefresh();
                        }
                        else if(retries >= MAX_RETRIES) {
                            LOG("Error heading folder. ", t);
                            setError(t);
                            if(t instanceof RestException) {
                                displayError("Error heading folder: " + ((RestException) t).getHttpStatusText());
                            }
                            else {
                                displayError("System error heading folder: " + t.getMessage());
                            }
                        }
                        else {//retry
                            LOG("Retry ", retries);
                            Scheduler.get().scheduleDeferred(this);
                        }
                    }

                    @Override
                    protected void onUnauthorized(Response response) {
                        if(retries >= MAX_RETRIES) {
                            sessionExpired();
                        }
                        else //retry
                        {
                            Scheduler.get().scheduleDeferred(this);
                        }
                    }
                };
                head.setHeader(Const.X_AUTH_TOKEN, getUserToken());
                head.setHeader(Const.IF_MODIFIED_SINCE, DateTimeFormat.getFormat(Const.DATE_FORMAT_1).format(lastModified, TimeZone.createTimeZone(0)) + " GMT");
                Scheduler.get().scheduleDeferred(head);

                return false;
            }
        }, 3000);
    }

    public void applyPermissions(Folder f) {
        if(f != null) {
            if(f.isInTrash()) {
                upload.setEnabled(false);
                disableUploadArea();
            }
            else {
                Boolean[] perms = f.getPermissions().get(userID);
                if(f.getOwnerID().equals(userID) || (perms != null && perms[1] != null && perms[1])) {
                    upload.setEnabled(true);
                    enableUploadArea();
                }
                else {
                    upload.setEnabled(false);
                    disableUploadArea();
                }
            }
        }
        else {
            upload.setEnabled(false);
            disableUploadArea();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void deselectOthers(TreeView _selectedTree, SingleSelectionModel model) {
        selectedTree = _selectedTree;

        for(SingleSelectionModel s : selectionModels) {
            if(!s.equals(model) && s.getSelectedObject() != null) {
                s.setSelected(s.getSelectedObject(), false);
            }
        }
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
        final String cookie = otherProperties.get(Const.AUTH_COOKIE);
        String auth = Cookies.getCookie(cookie);
        if(auth == null) {
            authenticateUser();
            return false;
        }
        if(auth.startsWith("\"")) {
            auth = auth.substring(1);
        }
        if(auth.endsWith("\"")) {
            auth = auth.substring(0, auth.length() - 1);
        }
        String[] authSplit = auth.split("\\" + config.cookieSeparator(), 2);
        if(authSplit.length != 2) {
            authenticateUser();
            return false;
        }
        this.userID = authSplit[0];
        this.userToken = authSplit[1];

        String gotoUrl = Window.Location.getParameter("goto");
        if(gotoUrl != null && gotoUrl.length() > 0) {
            Window.Location.assign(gotoUrl);
            return false;
        }
        return true;
    }

    /**
     * Redirect the user to the login page for authentication.
     */
    protected void authenticateUser() {
        Dictionary otherProperties = Dictionary.getDictionary(Const.OTHER_PROPERTIES);
        Window.Location.assign(otherProperties.get(Const.LOGIN_URL) + Window.Location.getHref());
    }

    public void fetchAccount(final Command callback) {
        String path = "?format=json";

        GetRequest<AccountResource> getAccount = new GetRequest<AccountResource>(AccountResource.class, getStorageAPIURL(), userID, path) {
            @Override
            public void onSuccess(AccountResource accountResource) {
                account = accountResource;
                if(callback != null) {
                    callback.execute();
                }

                final List<String> memberIDs = new ArrayList<String>();
                final List<Group> groups = account.getGroups();
                for(Group group : groups) {
                    memberIDs.addAll(group.getMemberIDs());
                }
                memberIDs.add(Pithos.this.getUserID());

                final List<String> theUnknown = Pithos.this.filterUserIDsWithUnknownDisplayName(memberIDs);
                // Initialize the user catalog
                new UpdateUserCatalogs(Pithos.this, theUnknown).scheduleDeferred();
                LOG("Called new UpdateUserCatalogs(Pithos.this, theUnknown).scheduleDeferred();");
            }

            @Override
            public void onError(Throwable t) {
                LOG("Error getting account", t);
                setError(t);
                if(t instanceof RestException) {
                    displayError("Error getting account: " + ((RestException) t).getHttpStatusText());
                }
                else {
                    displayError("System error fetching user data: " + t.getMessage());
                }
            }

            @Override
            protected void onUnauthorized(Response response) {
                sessionExpired();
            }
        };
        getAccount.setHeader(Const.X_AUTH_TOKEN, userToken);
        Scheduler.get().scheduleDeferred(getAccount);
    }

    public void updateStatistics() {
        HeadRequest<AccountResource> headAccount = new HeadRequest<AccountResource>(AccountResource.class, getStorageAPIURL(), userID, "", account) {

            @Override
            public void onSuccess(AccountResource _result) {
                folderTreeView.showStatistics(account);
            }

            @Override
            public void onError(Throwable t) {
                LOG("Error getting account", t);
                setError(t);
                if(t instanceof RestException) {
                    displayError("Error getting account: " + ((RestException) t).getHttpStatusText());
                }
                else {
                    displayError("System error fetching user data: " + t.getMessage());
                }
            }

            @Override
            protected void onUnauthorized(Response response) {
                sessionExpired();
            }
        };
        headAccount.setHeader(Const.X_AUTH_TOKEN, userToken);
        Scheduler.get().scheduleDeferred(headAccount);
    }

    protected void createHomeContainer(final AccountResource _account, final Command callback) {
        String path = "/" + Const.HOME_CONTAINER;
        PutRequest createPithos = new PutRequest(getStorageAPIURL(), getUserID(), path) {
            @Override
            public void onSuccess(Resource result) {
                if(!_account.hasTrashContainer()) {
                    createTrashContainer(callback);
                }
                else {
                    fetchAccount(callback);
                }
            }

            @Override
            public void onError(Throwable t) {
                LOG("Error creating pithos", t);
                setError(t);
                if(t instanceof RestException) {
                    displayError("Error creating pithos: " + ((RestException) t).getHttpStatusText());
                }
                else {
                    displayError("System error Error creating pithos: " + t.getMessage());
                }
            }

            @Override
            protected void onUnauthorized(Response response) {
                sessionExpired();
            }
        };
        createPithos.setHeader(Const.X_AUTH_TOKEN, getUserToken());
        Scheduler.get().scheduleDeferred(createPithos);
    }

    protected void createTrashContainer(final Command callback) {
        String path = "/" + Const.TRASH_CONTAINER;
        PutRequest createPithos = new PutRequest(getStorageAPIURL(), getUserID(), path) {
            @Override
            public void onSuccess(Resource result) {
                fetchAccount(callback);
            }

            @Override
            public void onError(Throwable t) {
                LOG("Error creating pithos", t);
                setError(t);
                if(t instanceof RestException) {
                    displayError("Error creating pithos: " + ((RestException) t).getHttpStatusText());
                }
                else {
                    displayError("System error Error creating pithos: " + t.getMessage());
                }
            }

            @Override
            protected void onUnauthorized(Response response) {
                sessionExpired();
            }
        };
        createPithos.setHeader(Const.X_AUTH_TOKEN, getUserToken());
        Scheduler.get().scheduleDeferred(createPithos);
    }

    /**
     * Creates an HTML fragment that places an image & caption together, for use
     * in a group header.
     *
     * @param imageProto an image prototype for an image
     * @param caption    the group caption
     * @return the header HTML fragment
     */
    private String createHeaderHTML(AbstractImagePrototype imageProto, String caption) {
        String captionHTML = "<table class='caption' cellpadding='0' "
            + "cellspacing='0'>" + "<tr><td class='lcaption'>" + imageProto.getHTML()
            + "</td><td id =" + caption + " class='rcaption'><b style='white-space:nowrap'>&nbsp;"
            + caption + "</b></td></tr></table>";
        return captionHTML;
    }

    protected void onWindowResized(int height) {
        // Adjust the split panel to take up the available room in the window.
        int newHeight = height - splitPanel.getAbsoluteTop() - 153;
        if(newHeight < 1) {
            newHeight = 1;
        }
        splitPanel.setHeight("" + newHeight);
        inner.setHeight("" + newHeight);
    }

    native boolean isCloudbarReady()/*-{
      if ($wnd.$("div.cloudbar") && $wnd.$("div.cloudbar").height() > 0)
        return true;
      return false;
    }-*/;

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
        onWindowResized(Window.getClientHeight());
    }

    /**
     * Display a warning message.
     *
     * @param msg the message to display
     */
    public void displayWarning(String msg) {
        messagePanel.displayWarning(msg);
        onWindowResized(Window.getClientHeight());
    }

    /**
     * Display an informational message.
     *
     * @param msg the message to display
     */
    public void displayInformation(String msg) {
        messagePanel.displayInformation(msg);
        onWindowResized(Window.getClientHeight());
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

    public String getUserToken() {
        return userToken;
    }

    public static native void preventIESelection() /*-{
      $doc.body.onselectstart = function () {
        return false;
      };
    }-*/;

    public static native void enableIESelection() /*-{
      if ($doc.body.onselectstart != null)
        $doc.body.onselectstart = null;
    }-*/;

    public static String getStorageAPIURL() {
        return STORAGE_API_URL;
    }

    public static String getStorageViewURL() {
        return STORAGE_VIEW_URL;
    }

    public static boolean isShowCopyrightMessage() {
        return SHOW_COPYRIGHT;
    }

    public static String getUserCatalogsURL() {
        return USER_CATALOGS_API_URL;
    }

    public static String getFileViewURL(File file) {
        return Pithos.getStorageViewURL() + file.getOwnerID() + file.getUri();
    }

    /**
     * History support for folder navigation
     * adds a new browser history entry
     *
     * @param key
     */
    public void updateHistory(String key) {
//		Replace any whitespace of the initial string to "+"
//		String result = key.replaceAll("\\s","+");
//		Add a new browser history entry.
//		History.newItem(result);
        History.newItem(key);
    }

    public void deleteFolder(final Folder folder, final Command callback) {
        final PleaseWaitPopup pwp = new PleaseWaitPopup();
        pwp.center();
        String path = "/" + folder.getContainer() + "/" + folder.getPrefix() + "?delimiter=/" + "&t=" + System.currentTimeMillis();
        DeleteRequest deleteFolder = new DeleteRequest(getStorageAPIURL(), folder.getOwnerID(), path) {

            @Override
            protected void onUnauthorized(Response response) {
                pwp.hide();
                sessionExpired();
            }

            @Override
            public void onSuccess(Resource result) {
                updateFolder(folder.getParent(), true, new Command() {

                    @Override
                    public void execute() {
                        folderTreeSelectionModel.setSelected(folder.getParent(), true);
                        updateStatistics();
                        if(callback != null) {
                            callback.execute();
                        }
                        pwp.hide();
                    }
                }, true);
            }

            @Override
            public void onError(Throwable t) {
                LOG(t);
                setError(t);
                if(t instanceof RestException) {
                    if(((RestException) t).getHttpStatusCode() != Response.SC_NOT_FOUND) {
                        displayError("Unable to delete folder: " + ((RestException) t).getHttpStatusText());
                    }
                    else {
                        onSuccess(null);
                    }
                }
                else {
                    displayError("System error unable to delete folder: " + t.getMessage());
                }
                pwp.hide();
            }
        };
        deleteFolder.setHeader(Const.X_AUTH_TOKEN, getUserToken());
        Scheduler.get().scheduleDeferred(deleteFolder);
    }

    public FolderTreeView getFolderTreeView() {
        return folderTreeView;
    }

    public void copyFiles(final Iterator<File> iter, final String targetUsername, final String targetUri, final Command callback) {
        if(iter.hasNext()) {
            File file = iter.next();
            String path = targetUri + "/" + file.getName();
            PutRequest copyFile = new PutRequest(getStorageAPIURL(), targetUsername, path) {
                @Override
                public void onSuccess(Resource result) {
                    copyFiles(iter, targetUsername, targetUri, callback);
                }

                @Override
                public void onError(Throwable t) {
                    LOG(t);
                    setError(t);
                    if(t instanceof RestException) {
                        displayError("Unable to copy file: " + ((RestException) t).getHttpStatusText());
                    }
                    else {
                        displayError("System error unable to copy file: " + t.getMessage());
                    }
                }

                @Override
                protected void onUnauthorized(Response response) {
                    sessionExpired();
                }
            };
            copyFile.setHeader(Const.X_AUTH_TOKEN, getUserToken());
            copyFile.setHeader(Const.X_COPY_FROM, URL.encodePathSegment(file.getUri()));
            if(!file.getOwnerID().equals(targetUsername)) {
                copyFile.setHeader(Const.X_SOURCE_ACCOUNT, URL.encodePathSegment(file.getOwnerID()));
            }
            copyFile.setHeader(Const.CONTENT_TYPE, file.getContentType());
            Scheduler.get().scheduleDeferred(copyFile);
        }
        else if(callback != null) {
            callback.execute();
        }
    }

    public void copyFolder(final Folder f, final String targetUsername, final String targetUri, boolean move, final Command callback) {
        String path = targetUri + "?delimiter=/";
        PutRequest copyFolder = new PutRequest(getStorageAPIURL(), targetUsername, path) {
            @Override
            public void onSuccess(Resource result) {
                if(callback != null) {
                    callback.execute();
                }
            }

            @Override
            public void onError(Throwable t) {
                LOG(t);
                setError(t);
                if(t instanceof RestException) {
                    displayError("Unable to copy folder: " + ((RestException) t).getHttpStatusText());
                }
                else {
                    displayError("System error copying folder: " + t.getMessage());
                }
            }

            @Override
            protected void onUnauthorized(Response response) {
                sessionExpired();
            }
        };
        copyFolder.setHeader(Const.X_AUTH_TOKEN, getUserToken());
        copyFolder.setHeader(Const.ACCEPT, "*/*");
        copyFolder.setHeader(Const.CONTENT_LENGTH, "0");
        copyFolder.setHeader(Const.CONTENT_TYPE, "application/directory");
        if(!f.getOwnerID().equals(targetUsername)) {
            copyFolder.setHeader(Const.X_SOURCE_ACCOUNT, f.getOwnerID());
        }
        if(move) {
            copyFolder.setHeader(Const.X_MOVE_FROM, URL.encodePathSegment(f.getUri()));
        }
        else {
            copyFolder.setHeader(Const.X_COPY_FROM, URL.encodePathSegment(f.getUri()));
        }
        Scheduler.get().scheduleDeferred(copyFolder);
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

    public Group addGroup(String groupname) {
        Group newGroup = new Group(groupname);
        account.addGroup(newGroup);
        groupTreeView.updateGroupNode(null);
        return newGroup;
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
        if(selectedTree != null) {
            return selectedTree.getSelection();
        }
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
        LOG("Pithos::createMySharedTree()");
        mysharedTreeSelectionModel = new SingleSelectionModel<Folder>();
        mysharedTreeSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if(mysharedTreeSelectionModel.getSelectedObject() != null) {
                    deselectOthers(mysharedTreeView, mysharedTreeSelectionModel);
                    upload.setEnabled(false);
                    disableUploadArea();
                    updateSharedFolder(mysharedTreeSelectionModel.getSelectedObject(), true);
                    showRelevantToolbarButtons();
                }
                else {
                    if(getSelectedTree().equals(mysharedTreeView)) {
                        setSelectedTree(null);
                    }
                    if(getSelectedTree() == null) {
                        showRelevantToolbarButtons();
                    }
                }
            }
        });
        selectionModels.add(mysharedTreeSelectionModel);
        mysharedTreeViewModel = new MysharedTreeViewModel(Pithos.this, mysharedTreeSelectionModel);
        mysharedTreeViewModel.initialize(new Command() {

            @Override
            public void execute() {
                mysharedTreeView = new MysharedTreeView(mysharedTreeViewModel);
                trees.insert(mysharedTreeView, 2);
                treeViews.add(mysharedTreeView);
                createOtherSharedTree();
            }
        });
    }

    void createOtherSharedTree() {
        LOG("Pithos::createOtherSharedTree()");
        otherSharedTreeSelectionModel = new SingleSelectionModel<Folder>();
        otherSharedTreeSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if(otherSharedTreeSelectionModel.getSelectedObject() != null) {
                    deselectOthers(otherSharedTreeView, otherSharedTreeSelectionModel);
                    applyPermissions(otherSharedTreeSelectionModel.getSelectedObject());
                    updateOtherSharedFolder(otherSharedTreeSelectionModel.getSelectedObject(), true, null);
                    showRelevantToolbarButtons();
                }
                else {
                    if(getSelectedTree().equals(otherSharedTreeView)) {
                        setSelectedTree(null);
                    }
                    if(getSelectedTree() == null) {
                        showRelevantToolbarButtons();
                    }
                }
            }
        });
        selectionModels.add(otherSharedTreeSelectionModel);
        otherSharedTreeViewModel = new OtherSharedTreeViewModel(Pithos.this, otherSharedTreeSelectionModel);
        // #3784 We show it empty...
        otherSharedTreeView = new OtherSharedTreeView(otherSharedTreeViewModel, true);
        trees.insert(otherSharedTreeView, 1);

        LOG("Pithos::createOtherSharedTree(), initializing otherSharedTreeViewModel with a callback");
        otherSharedTreeViewModel.initialize(new Command() {
            @Override
            public void execute() {
                // #3784 ... then remove the empty stuff and add a new view with the populated model
                trees.remove(otherSharedTreeView);

                otherSharedTreeView = new OtherSharedTreeView(otherSharedTreeViewModel, false);
                trees.insert(otherSharedTreeView, 1);
                treeViews.add(otherSharedTreeView);
                scheduleRefresh();
            }
        });
    }

    public String getErrorData() {
        final StringBuilder sb = new StringBuilder();
        final String NL = Const.NL;
        Throwable t = this.error;
        while(t != null) {
            sb.append(t.toString());
            sb.append(NL);
            StackTraceElement[] traces = t.getStackTrace();
            for(StackTraceElement trace : traces) {
                sb.append("  [");
                sb.append(trace.getClassName());
                sb.append("::");
                sb.append(trace.getMethodName());
                sb.append("() at ");
                sb.append(trace.getFileName());
                sb.append(":");
                sb.append(trace.getLineNumber());
                sb.append("]");
                sb.append(NL);
            }
            t = t.getCause();
        }

        return sb.toString();
    }

    public void setError(Throwable t) {
        error = t;
        LOG(t);
    }

    public void showRelevantToolbarButtons() {
        toolbar.showRelevantButtons();
    }

    public FileUploadDialog getFileUploadDialog() {
        if(fileUploadDialog == null) {
            fileUploadDialog = new FileUploadDialog(this);
        }
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
        if(folder == null) {
            if(callback != null) {
                callback.execute();
            }
        }
        else {
            HeadRequest<Folder> headFolder = new HeadRequest<Folder>(Folder.class, getStorageAPIURL(), folder.getOwnerID(), folder.getUri(), folder) {

                @Override
                public void onSuccess(Folder _result) {
                    if(callback != null) {
                        callback.execute();
                    }
                }

                @Override
                public void onError(Throwable t) {
                    if(t instanceof RestException) {
                        if(((RestException) t).getHttpStatusCode() == Response.SC_NOT_FOUND) {
                            final String path = folder.getUri();
                            PutRequest newFolder = new PutRequest(getStorageAPIURL(), folder.getOwnerID(), path) {
                                @Override
                                public void onSuccess(Resource _result) {
                                    scheduleFolderHeadCommand(folder, callback);
                                }

                                @Override
                                public void onError(Throwable _t) {
                                    setError(_t);
                                    if(_t instanceof RestException) {
                                        displayError("Unable to create folder: " + ((RestException) _t).getHttpStatusText());
                                    }
                                    else {
                                        displayError("System error creating folder: " + _t.getMessage());
                                    }
                                }

                                @Override
                                protected void onUnauthorized(Response response) {
                                    sessionExpired();
                                }
                            };
                            newFolder.setHeader(Const.X_AUTH_TOKEN, getUserToken());
                            newFolder.setHeader(Const.CONTENT_TYPE, "application/folder");
                            newFolder.setHeader(Const.ACCEPT, "*/*");
                            newFolder.setHeader(Const.CONTENT_LENGTH, "0");
                            Scheduler.get().scheduleDeferred(newFolder);
                        }
                        else if(((RestException) t).getHttpStatusCode() == Response.SC_FORBIDDEN) {
                            onSuccess(folder);
                        }
                        else {
                            displayError("Error heading folder: " + ((RestException) t).getHttpStatusText());
                        }
                    }
                    else {
                        displayError("System error heading folder: " + t.getMessage());
                    }

                    LOG("Error heading folder", t);
                    setError(t);
                }

                @Override
                protected void onUnauthorized(Response response) {
                    sessionExpired();
                }
            };
            headFolder.setHeader(Const.X_AUTH_TOKEN, getUserToken());
            Scheduler.get().scheduleDeferred(headFolder);
        }
    }

    public void scheduleFileHeadCommand(File f, final Command callback) {
        HeadRequest<File> headFile = new HeadRequest<File>(File.class, getStorageAPIURL(), f.getOwnerID(), f.getUri(), f) {

            @Override
            public void onSuccess(File _result) {
                if(callback != null) {
                    callback.execute();
                }
            }

            @Override
            public void onError(Throwable t) {
                LOG("Error heading file", t);
                setError(t);
                if(t instanceof RestException) {
                    displayError("Error heading file: " + ((RestException) t).getHttpStatusText());
                }
                else {
                    displayError("System error heading file: " + t.getMessage());
                }
            }

            @Override
            protected void onUnauthorized(Response response) {
                sessionExpired();
            }
        };
        headFile.setHeader(Const.X_AUTH_TOKEN, getUserToken());
        Scheduler.get().scheduleDeferred(headFile);
    }

    public boolean isMySharedSelected() {
        return getSelectedTree().equals(getMySharedTreeView());
    }

    private Folder getUploadFolder() {
        if(folderTreeView.equals(getSelectedTree()) || otherSharedTreeView.equals(getSelectedTree())) {
            return getSelection();
        }
        return null;
    }

    private void updateUploadFolder() {
        updateUploadFolder(null);
    }

    private void updateUploadFolder(final JsArrayString urls) {
        if(folderTreeView.equals(getSelectedTree()) || otherSharedTreeView.equals(getSelectedTree())) {
            Folder f = getSelection();
            if(getSelectedTree().equals(getFolderTreeView())) {
                updateFolder(f, true, new Command() {

                    @Override
                    public void execute() {
                        updateStatistics();
                        if(urls != null) {
                            selectUploadedFiles(urls);
                        }
                    }
                }, false);
            }
            else {
                updateOtherSharedFolder(f, true, null);
            }
        }
    }

    public native void disableUploadArea() /*-{
      var uploader = $wnd.$("#uploader").pluploadQueue();
      var dropElm = $wnd.document.getElementById('rightPanel');
      $wnd.plupload.removeAllEvents(dropElm, uploader.id);
    }-*/;

    public native void enableUploadArea() /*-{
      var uploader = $wnd.$("#uploader").pluploadQueue();
      var dropElm = $wnd.document.getElementById('rightPanel');
      $wnd.plupload.removeAllEvents(dropElm, uploader.id);
      if (uploader.runtime == 'html5') {
        uploader.settings.drop_element = 'rightPanel';
        uploader.trigger('PostInit');
      }
    }-*/;

    public void showUploadAlert(int nOfFiles) {
        if(uploadAlert == null) {
            uploadAlert = new UploadAlert(this, nOfFiles);
        }
        if(!uploadAlert.isShowing()) {
            uploadAlert.setPopupPositionAndShow(new PopupPanel.PositionCallback() {

                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    uploadAlert.setPopupPosition((Window.getClientWidth() - offsetWidth) / 2, statusPanel.getAbsoluteTop() - offsetHeight);
                }
            });
        }
        uploadAlert.setNumOfFiles(nOfFiles);
    }

    public void hideUploadAlert() {
        if(uploadAlert != null && uploadAlert.isShowing()) {
            uploadAlert.hide();
        }
    }

    public void selectUploadedFiles(JsArrayString urls) {
        List<String> selectedUrls = new ArrayList<String>();
        for(int i = 0; i < urls.length(); i++) {
            selectedUrls.add(urls.get(i));
        }
        fileList.selectByUrl(selectedUrls);
    }

    public void purgeContainer(final Folder container) {
        String path = "/" + container.getName() + "?delimiter=/";
        DeleteRequest delete = new DeleteRequest(getStorageAPIURL(), getUserID(), path) {

            @Override
            protected void onUnauthorized(Response response) {
                sessionExpired();
            }

            @Override
            public void onSuccess(Resource result) {
                updateFolder(container, true, null, true);
            }

            @Override
            public void onError(Throwable t) {
                LOG("Error deleting trash", t);
                setError(t);
                if(t instanceof RestException) {
                    displayError("Error deleting trash: " + ((RestException) t).getHttpStatusText());
                }
                else {
                    displayError("System error deleting trash: " + t.getMessage());
                }
            }
        };
        delete.setHeader(Const.X_AUTH_TOKEN, getUserToken());
        Scheduler.get().scheduleDeferred(delete);
    }
}
