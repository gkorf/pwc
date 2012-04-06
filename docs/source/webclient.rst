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


Constructing the My Shared tree
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Constructing the Others' Shared tree
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Constructing the Groups tree
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

File sharing
^^^^^^^^^^^^

File uploading
^^^^^^^^^^^^^^

File Copy/Cut/Paste operations
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^