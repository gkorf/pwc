Pithos+ Web Client
==================

Introduction
------------

Document Revisions
^^^^^^^^^^^^^^^^^^

=====================  =====================================
Revision               Description
=====================  =====================================
0.1 (Mar 17, 2012)     Initial release.
=====================  =====================================

Build instructions
------------------
prerequisites: git, jdk 1.6, ant

First get the source from the git repository

git clone https://code.grnet.gr/git/pithos-web-client

cd pithos-web-client

Edit the file runtime.properties and set the loginUrl and cloudbar properties to the correct values eg.

loginUrl=https://accounts.staging.okeanos.grnet.gr/im/login?next=https%3A//pithos.staging.okeanos.grnet.gr/ui

CLOUDBAR_ACTIVE_SERVICE = cloud;

CLOUDBAR_LOCATION = https://accounts.staging.okeanos.grnet.gr/static/im/cloudbar/

CLOUDBAR_SERVICES = https://accounts.staging.okeanos.grnet.gr/im/get_services

CLOUDBAR_MENU = https://accounts.staging.okeanos.grnet.gr/im/get_menu

Then run ant

ant

cd bin/www/gr.grnet.pithos.web.Pithos

This folder contains the "binaries" (html and javascript actually). Those files should be put somewhere to be served by the web server.
For deploying to pithos.dev.grnet.gr, upload everything to /var/www/pithos_web_client where they are served under /ui.

Important reminder: Due to Same-Origin-Policy the web client should be served under the same domain as the API.

Technology and tools
--------------------
Pithos+ web client is a gwt application. It is written in Java and compiled to javascript that runs in the browser.

General architecture
--------------------

Authentication
--------------

Authentication is provided by an external service. Upon loading the web client checks for the existence of the authentication cookie named '_pithos2_a'. If the cookie is present then it is parsed for the username and authentication token. The format of the cookie content is

username|token

These username and token are used for every request to the server. If at any time, the client receives an HTTP 401 (Unauthorized) which means that the token has expired, then the user is informed and redirected to the login page.

If the auth cookie is not present in the first place then the user is immediately redirected to the login page.

The login page url is defined in the runtime.properties file and it must end with a 'next=' url parameter. The value of the parameter will be determined automatically. If the parameter is not present then the login page will not be able to redirect back to the client after a successful login and the use will end up at her profile page.

API Usage
---------

Initialization
^^^^^^^^^^^^^^
Upon loading, the web client performs the followinf steps:

Ckeck if the user is authenticated (auth cookie present)

The application page is constructed

Requests the server for the user account and files. This is done in various stages. First a request is made for the user account data

GET /v1/username?format=json

If there is a container named 'pithos' and a container named 'trash' then it proceeds to the folder tree construction. Otherwise the missing containers will be created with a request

PUT /v1/username/pithos

PUT /v1/username/trash

Constructing the folder tree
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
The folder tree displays in a tree structure all the user 's containers (including trash). For each container a request is made

GET /v1/username/pithos?format=json&delimiter=/&prefix=

to get the first level of folders (either actual objects with application/folder content type or virtual prefixes). The home folder (pithos) is always displayed first, selected and expanded and the trash container is always last.

Due to the pithos container being programmatically selected and expanded at the beginning, additional requests 

GET /v1/username/pithos?format=json&delimiter=/&prefix=pithos_subfolder1

are made to server to fetch details of the subfolders. We need to know if a subfolder has its own subfolders so that we display the cross sign next to it.

Constructing the "Shared by me" tree
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
My shared tree construction is a bit more complicated. All files that are shared individualy (not through sharing their parent folder) are displayed directly in the root of the tree (inside the 'Shared by me' folder).

All shared folders are displayed in tree structure under the root.

The above means that the client has to make a series of requests to collect all shared items and display them accordingly.

First a

GET /v1/username/container?format=json&delimiter=/&prefix=

is made for each container. If the container is shared it is added to the tree (under "Shared by me") and the client continues to the next container (this has to be re-visited because it was based that due to the permission inheritance the subfolders are also shared. Since the inheritance has been removed this is no longer valid).

If the container is not shared we have to go deeper to find possible shared subfolders and files. So we examine each file in the folder and if shared we add it in the "Shared by me" folder and we also do a nested iteration getting each subfolder

GET /v1/username/container?format=json&delimiter=/&prefix=subfolder

and this is done recursively until all shared folders have been collected.

Constructing the "Shared by others" tree
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
The "Shared by others" tree has the additional complication that we need to get the users that share objects with us and display them as a first level of subfolders.

GET /v1?format=json

For each of the users we do an additional

GET /v1/username?format=json

to get the containers shared by the user and for each container we do the same sequence of requests as in the "Shared by me case". The difference no is that we don't need to check if the container/folder/file is shared because all requests with a different username always return only object that are visible to the logged-on user.

Constructing the Groups tree
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The groups tree is contructed with the initial request for the user account data which returns the groups defined be the user along with their members.

File sharing
^^^^^^^^^^^^

File uploading
^^^^^^^^^^^^^^
File uploading is done using the plupload http://www.plupload.com/ plugin.

File Copy/Cut/Paste operations
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^