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
from pithos.api.services import pithos_services as vanilla_pithos_services
from astakosclient import astakos_services as vanilla_astakos_services
from synnefo.lib.services import get_public_endpoint, fill_endpoints

from copy import deepcopy

# Process Pithos settings. This code is shared between snf-pithos-app and
# snf-pithos-webclient since they share the PITHOS_ settings prefix for most
# of their settings.
BASE_URL = getattr(settings, 'PITHOS_BASE_URL',
                   "https://object-store.example.synnefo.org/pithos/")

BASE_HOST, BASE_PATH = parse_base_url(BASE_URL)

# Process Astakos settings
ASTAKOS_BASE_URL = getattr(settings, 'ASTAKOS_BASE_URL',
                           'https://accounts.example.synnefo.org/astakos/')
ASTAKOS_BASE_HOST, ASTAKOS_BASE_PATH = parse_base_url(ASTAKOS_BASE_URL)

pithos_services = deepcopy(vanilla_pithos_services)
fill_endpoints(pithos_services, BASE_URL)
PITHOS_PREFIX = get_path(pithos_services, 'pithos_object-store.prefix')
PUBLIC_PREFIX = get_path(pithos_services, 'pithos_public.prefix')
UI_PREFIX = get_path(pithos_services, 'pithos_ui.prefix')

astakos_services = deepcopy(vanilla_astakos_services)
fill_endpoints(astakos_services, ASTAKOS_BASE_URL)
CUSTOMIZE_ASTAKOS_SERVICES = \
        getattr(settings, 'PITHOS_CUSTOMIZE_ASTAKOS_SERVICES', ())
for path, value in CUSTOMIZE_ASTAKOS_SERVICES:
    set_path(astakos_services, path, value, createpath=True)

ASTAKOS_ACCOUNTS_PREFIX = get_path(astakos_services, 'astakos_account.prefix')
ASTAKOS_VIEWS_PREFIX = get_path(astakos_services, 'astakos_ui.prefix')
ASTAKOS_IDENTITY_PREFIX = get_path(astakos_services, 'astakos_identity.prefix')

BASE_ASTAKOS_PROXY_PATH = getattr(settings, 'PITHOS_BASE_ASTAKOS_PROXY_PATH',
                                  ASTAKOS_BASE_PATH)
BASE_ASTAKOS_PROXY_PATH = join_urls(BASE_PATH, BASE_ASTAKOS_PROXY_PATH)
BASE_ASTAKOS_PROXY_PATH = BASE_ASTAKOS_PROXY_PATH.strip('/')

PROXY_USER_SERVICES = getattr(settings, 'PITHOS_PROXY_USER_SERVICES', True)

if not BASE_PATH.startswith("/"):
    BASE_PATH = "/" + BASE_PATH

PITHOS_URL = get_public_endpoint(pithos_services, 'object-store', 'v1')
PITHOS_UI_URL = get_public_endpoint(pithos_services, 'pithos_ui', '')
ASTAKOS_UI_URL = get_public_endpoint(astakos_services, 'astakos_ui', '')

if PROXY_USER_SERVICES:
    ACCOUNT_URL = join_urls('/', BASE_ASTAKOS_PROXY_PATH,
                            ASTAKOS_ACCOUNTS_PREFIX, 'v1.0')
else:
    ACCOUNT_URL = get_public_endpoint(astakos_services, 'account', 'v1.0')


USER_CATALOG_URL = join_urls(ACCOUNT_URL, 'user_catalogs')
FEEDBACK_URL = join_urls(ACCOUNT_URL, 'feedback')
LOGIN_URL = join_urls(ASTAKOS_UI_URL, 'login?next=')

AUTH_COOKIE_NAME = getattr(settings, 'PITHOS_UI_AUTH_COOKIE_NAME',
                           '_pithos2_a')

CLOUDBAR_ACTIVE_SERVICE = getattr(
    settings,
    'PITHOS_UI_CLOUDBAR_ACTIVE_SERVICE',
    'pithos')
