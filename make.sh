#!/bin/bash

ant
rm -rf snf-pithos-web-client/pithos_web_client/static/pithos_web_client/*
cp -r bin/www/gr.grnet.pithos.web.Pithos/* snf-pithos-web-client/pithos_web_client/static/pithos_web_client
rm -rf snf-pithos-web-client/pithos_web_client/templates/pithos_web_client/index.html
cp bin/www/gr.grnet.pithos.web.Pithos/index.html snf-pithos-web-client/pithos_web_client/templates/pithos_web_client/index.html
