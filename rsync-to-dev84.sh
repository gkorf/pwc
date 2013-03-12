#!/bin/sh

rsync --delete -avz ./out/artifacts/pithos_web_client_GWT/gr.grnet.pithos.web.Pithos/* dev84.dev.grnet.gr:pithos_webclient/
