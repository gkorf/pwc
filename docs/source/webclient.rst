Pithos+ Web Client
==================

Introduction
------------
The Pithos+ Web client documentation contains information about installation and configuration of the web client along with information about the development of the client (development environment, tools, libraries, API usage etc).

Build instructions
------------------
Prerequisites: git, jdk 1.6, ant

First get the source from the git repository

git clone https://code.grnet.gr/git/pithos-web-client

Enter the newly created folder

cd pithos-web-client

and run ant

ant

Now go to the output folder

cd bin/www/gr.grnet.pithos.web.Pithos

This folder contains the "binaries" (html and javascript actually). Those files should be put somewhere to be served by the web server.
For deploying to pithos.dev.grnet.gr, upload everything to /var/www/pithos_web_client where they are served under /ui.

Important reminder: Due to Same-Origin-Policy the web client should be served under the same domain as the API.

Configuration
-------------

All configuration exists as javascript variables in index.html file. The things that need to be configured are:

loginUrl: The url that the client will redirect the user if she is not logged in (defailt: /im/login?next=). The next= parameter is determined automatically.

authCookie: The name of the authentication cookie that is set by the login process which is external to the web client (default: _pithos2_a).

feedbackUrl: The url that the client should use to POST feedback messages from the user when an error occurs (default: /im/feedback).

The CLOUDBAR_* set of variables is related to the bar that is displayed on the top of the web client 's page. This bar is used to navigate to other services of the cloud and also contains a menu for sendind feedback, inviting other people, logout etc. This bar is also externally configured and the web client just loads it as a script.

CLOUDBAR_ACTIVE_SERVICE: The service that we currently see (in case of the web client it is always pithos).

CLOUDBAR_LOCATION: The url that tha cloudbar script is loaded from (default: /static/im/cloudbar/).

CLOUDBAR_SERVICES: The url that is used to get the available service (default: /im/get_services).

CLOUDBAR_MENU: The url that the menu is loaded from (default: /im/get_menu).

Development environment
-----------------------
The development environment used is Eclipse Indigo with the Google Plugin for Eclipse but other environments can be used without problems. The libraries needed for development is

Google Web Toolkit (GWT) v.2.4.0 (http://developers.google.com/web-toolkit/)

The gwt-user.jar is needed in the classpath in order to compile the source. The gwt-dev.jar is needed to run the development mode server for debugging.

All dependencies are downloaded automatically by the ant build script (build.xml) in the dependencies folder if not already found there.

Building
^^^^^^^^
Building the project consists just of compiling the java source into javascript and html by the GWT compiler. This is done by the ant script (ant target "gwt-compile")

Debugging
^^^^^^^^^
Debugging is done using the GWT development server. The ant script has a special target (run-web-dev-mode) which starts the devmode server, but one can do the same thing from inside Eclipse. In order to use the devmode server, the GWT plugin is needed in the browser. This way the browser communicates with the devmode server and the developer can set breakpoints, examine variable values, evaluate expressions etc for the application that runs in the browser.

Technology and tools
--------------------
Pithos+ web client is a gwt application. It is written in Java and compiled to javascript that runs in the browser. More info about gwt can be found here http://developers.google.com/web-toolkit/

General architecture
--------------------

The web client does an adaptation of the container/object server-side data model to the more user-friendly folder/file data model. The client uses the API to retrieve info about the containers and objects from the server and displays them in a tree-like structure. It uses two special gwt widgets, CellTree (https://developers.google.com/web-toolkit/doc/latest/DevGuideUiCellWidgets#celltree) and CellTable (https://developers.google.com/web-toolkit/doc/latest/DevGuideUiCellTable) for the folder tree and filelist accordingly. The CellTree widget initiates calls to the API at the beginning and each time a subtree is expanded in order to fetch all info needed to display the subfolders. That way the datamodel is controlled by the widget.

Source code structure
---------------------
Java source code consists of the following packages:

gr.grnet.pithos.web.client: This is the root packages that containes the Pithos class which is the application entry point and various utility classes like menus, dialogs etc.

gr.grnet.pithos.web.client.foldertree: All classes related to the datamodel and tree widget that displays containers, folders and files.

gr.grnet.pithos.web.client.mysharedtree: All classes related to the datamodel and tree widget that displays the objects shared by me.

gr.grnet.pithos.web.client.othershared: All classes related to the datamodel and tree widget that displays the objects shared to me by other users

gr.grnet.pithos.web.client.grouptree: All classes related to the datamodel and tree widget that displays the groups and users used by the user in order to share files to groups of people more easily.

gr.grnet.pithos.web.client.commands: This packages contains the commands issued by the various menus (right-click menus and toolbar).

gr.grnet.pithos.web.client.rest: This package contains the classes that define the HTTP requests made to the API

gr.grnet.pithos.resources: This folder contains various images used as icons in the application 's UI. Those images are embeded in the javascript code by the GWT compiler for efficiency and therefore they are not served as static files.

gr.grnet.pithos.web.public: This folder contains the index.html page of the application and all related css and other files loaded by it (like the plupload scripts for multiple file uploading).

Authentication
--------------

Authentication is provided by an external service. Upon loading,, the web client checks for the existence of the authentication cookie named '_pithos2_a'. If the cookie is present then it is parsed for the username and authentication token. The format of the cookie content is

username|token

These username and token are used for every request to the server. If at any time, the client receives an HTTP 401 (Unauthorized) which means that the token has expired, then the user is informed and redirected to the login page.

If the auth cookie is not present in the first place then the user is immediately redirected to the login page.

The login page url is defined in the index.html file and it must end with a 'next=' url parameter. The value of the parameter will be determined automatically. If the parameter is not present then the login page will not be able to redirect back to the client after a successful login and the user will end up at her profile page.

API Usage
---------

Initialization
^^^^^^^^^^^^^^
Upon loading, the web client performs the following steps:

Ckeck if the user is authenticated (auth cookie present)

The application page is constructed

Requests the server for the user account and files. This is done in various stages. First a request is made for the user account data

GET /v1/username?format=json

If there is a container named 'pithos' and a container named 'trash' then it proceeds to the folder tree construction. Otherwise the missing containers will be created with requests

PUT /v1/username/pithos

PUT /v1/username/trash

Constructing the folder tree
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
The folder tree displays in a tree structure all the user 's containers (including trash). For each container a request is made

GET /v1/username/pithos?format=json&delimiter=/&prefix=

to get the first level of folders (either actual objects with application/folder content type or virtual prefixes). The home folder (pithos) is always displayed first, selected and expanded and the trash container is always last.

Due to the pithos container being programmatically selected and expanded at the beginning, additional requests 

GET /v1/username/pithos?format=json&delimiter=/&prefix=pithos_subfolder1

are made to the server to fetch details of the subfolders. We need to know if a subfolder has its own subfolders so that we display the cross sign next to it.

Constructing the "Shared by me" tree
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
My shared tree construction is a bit more complicated. All files that are shared individualy (not through sharing their parent folder) are displayed directly in the root of the tree (inside the 'Shared by me' folder).

All shared folders are displayed in tree structure under the root.

The above means that the client has to make a series of requests to collect all shared items and display them accordingly.

First a

GET /v1/username/container?format=json&delimiter=/&prefix=

is made for each container. If the container is shared it is added to the tree (under "Shared by me") and the client continues to the next container (this has to be re-visited because it was based on the fact that due to the permission inheritance the subfolders are also shared. Since the inheritance has been removed this is no longer valid).

If the container is not shared we have to go deeper to find possible shared subfolders and files. So we examine each file in the folder and if shared we add it in the "Shared by me" folder and we also do a nested iteration getting each subfolder

GET /v1/username/container?format=json&delimiter=/&prefix=subfolder

and this is done recursively until all shared folders have been collected.

Constructing the "Shared by others" tree
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
The "Shared by others" tree has the additional complication that we need to get the users that share objects with us and display them as a first level of subfolders.

GET /v1?format=json

For each of the users we do an additional

GET /v1/username?format=json

to get the containers shared by the user and for each container we do the same sequence of requests as in the "Shared by me" case. The difference here is that we don't need to check if the container/folder/file is shared because all requests with a different username always return only objects that are visible to the logged-on user.

Constructing the Groups tree
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The groups tree is contructed with the initial request for the user account data which returns the groups defined by the user along with their members.

Functionality
-------------
The web client provides functionality which is typical to a file manager like copy/cut/paste of individual files, create/rename/delete of folders, upload/rename/delete files etc. All such operations are done by using single calls to the API or combination of calls.

Folder operations
^^^^^^^^^^^^^^^^^
Folder creation
"""""""""""""""
is done by the FolderPropertiesDialog class with a request

PUT /v1/username/container/path/to/new/folder

Folder removal
""""""""""""""
is done by the DeleteFolderDialog class and is a bit more complicated because the files and subfolders must be deleted first. A request

GET /v1/username/container?format=json&delimiter=/&prefix=/path/to/folder

is made to retrieve all objects with names starting with the folder 's prefix. All objects are deleted with requests

DELETE /v1/username/container/path/to/folder/file

and if any of those objects is a marker object (folder) itself the same procedure is followed recursively. Finally a 

DELETE /v1/username/container/path/to/folder

is made to delete the initial folder.

Folder rename
"""""""""""""
is done by a 

PUT /v1/username/container/path/to/new/foldername

to create a folder with the new name, followed by recursive copy operations (see below about copy/move/paste) to move all folder 's children under the new one. Finally, a folder deletion is done as described earlier.

File Operations
^^^^^^^^^^^^^^^
File uploading
""""""""""""""
File uploading is done using the plupload http://www.plupload.com/ plugin.

File rename
"""""""""""
is done by FilePropertiesDialog class with a

PUT /v1/username/container/path/to/newfilename

with X-Move-From header containing the old path.

File delete
"""""""""""
is done by DeleteFileDialog class with a simple

DELETE /v1/username/container/path/to/file

File/Folder sharing
^^^^^^^^^^^^^^^^^^^
Sharing of file and folders with other users is done by setting permissions for other users or groups of users. The FolderPermissionsDialog/FilePermissionsDialog classes display the corresponding UI and perform the API calls. Permissions are set with

POST /v1/username/container/path/to/object

with the X-Object-Sharing containing the permissions (see Pithos+ development guide).

In the case of a virtual folder (no marker object) the above operation will return 404 (Not Found) and a PUT request is needed to create the marker and then repeat the POST.