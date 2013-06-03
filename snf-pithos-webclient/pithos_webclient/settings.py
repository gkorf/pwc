# Copyright 2013 GRNET S.A. All rights reserved.
#
# Redistribution and use in source and binary forms, with or
# without modification, are permitted provided that the following
# conditions are met:
#
#   1. Redistributions of source code must retain the above
#      copyright notice, this list of conditions and the following
#      disclaimer.
#
#   2. Redistributions in binary form must reproduce the above
#      copyright notice, this list of conditions and the following
#      disclaimer in the documentation and/or other materials
#      provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY GRNET S.A. ``AS IS'' AND ANY EXPRESS
# OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
# PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GRNET S.A OR
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
# USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
# AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
# ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#
# The views and conclusions contained in the software and
# documentation are those of the authors and should not be
# interpreted as representing official policies, either expressed
# or implied, of GRNET S.A.

from django.conf import settings
from synnefo.lib import join_urls, parse_base_url
from synnefo.util.keypath import get_path
from pithos_webclient.services import pithos_services
from astakosclient import astakos_services

from copy import deepcopy

# Process Pithos settings. This code is shared between snf-pithos-app and
# snf-pithos-webclient since they share the PITHOS_ settings prefix for most
# of their settings.

# Top-level URL for Pithos. Must set.
BASE_URL = getattr(settings, 'PITHOS_BASE_URL',
                   "https://object-store.example.synnefo.org/pithos/")

BASE_HOST, BASE_PATH = parse_base_url(BASE_URL)

# Process Astakos settings
ASTAKOS_BASE_URL = getattr(settings, 'ASTAKOS_BASE_URL',
                           'https://accounts.example.synnefo.org/astakos/')
ASTAKOS_BASE_HOST, ASTAKOS_BASE_PATH = parse_base_url(ASTAKOS_BASE_URL)

PITHOS_PREFIX = get_path(pithos_services, 'pithos_object-store.prefix')
PUBLIC_PREFIX = get_path(pithos_services, 'pithos_public.prefix')
UI_PREFIX = get_path(pithos_services, 'pithos_ui.prefix')

CUSTOMIZE_ASTAKOS_SERVICES = \
        getattr(settings, 'PITHOS_CUSTOMIZE_ASTAKOS_SERVICES', ())
for path, value in CUSTOMIZE_ASTAKOS_SERVICES:
    set_path(astakos_services, path, value, createpath=True)

ASTAKOS_ACCOUNTS_PREFIX = get_path(astakos_services, 'astakos_account.prefix')
ASTAKOS_VIEWS_PREFIX = get_path(astakos_services, 'astakos_ui.prefix')
ASTAKOS_KEYSTONE_PREFIX = get_path(astakos_services, 'astakos_keystone.prefix')

BASE_ASTAKOS_PROXY_PATH = getattr(settings, 'PITHOS_BASE_ASTAKOS_PROXY_PATH',
                                  ASTAKOS_BASE_PATH)

PROXY_USER_SERVICES = getattr(settings, 'PITHOS_PROXY_USER_SERVICES', True)

# Base settings set. Resolve webclient required settings
ASTAKOS_ACCOUNTS_URL = join_urls(ASTAKOS_BASE_URL, ASTAKOS_ACCOUNTS_PREFIX)
if PROXY_USER_SERVICES:
    ASTAKOS_ACCOUNTS_URL = join_urls('/', BASE_ASTAKOS_PROXY_PATH,
                                     ASTAKOS_ACCOUNTS_PREFIX)


if not BASE_PATH.startswith("/"):
    BASE_PATH = "/" + BASE_PATH

ACCOUNTS_URL = getattr(settings, 'PITHOS_UI_ACCOUNTS_URL',
                       join_urls(ASTAKOS_ACCOUNTS_URL))
USER_CATALOG_URL = getattr(settings, 'PITHOS_UI_USER_CATALOG_URL',
                           join_urls(ACCOUNTS_URL, 'user_catalogs'))
FEEDBACK_URL = getattr(settings, 'PITHOS_UI_FEEDBACK_URL',
                       join_urls(ACCOUNTS_URL, 'feedback'))
PITHOS_URL = getattr(settings, 'PITHOS_UI_PITHOS_URL',
                      join_urls(BASE_PATH, PITHOS_PREFIX, 'v1'))
AUTH_COOKIE_NAME = getattr(settings, 'PITHOS_UI_AUTH_COOKIE_NAME',
                           '_pithos2_a')

DEFAULT_LOGIN_URL = join_urls(ASTAKOS_BASE_URL, ASTAKOS_VIEWS_PREFIX, 'login',
                              '?next=')
LOGIN_URL = getattr(settings, 'PITHOS_UI_LOGIN_URL', DEFAULT_LOGIN_URL)
CLOUDBAR_ACTIVE_SERVICE = getattr(
    settings,
    'PITHOS_UI_CLOUDBAR_ACTIVE_SERVICE',
    'pithos')
