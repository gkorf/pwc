#!/bin/sh

rsync --delete -avz ./out/artifacts/pithos_web_client_GWT/gr.grnet.pithos.web.Pithos/* root@pithos.synnefo.live:/usr/share/synnefo/static/pithos_webclient
