# Copyright 2011-2012 GRNET S.A. All rights reserved.
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

import json
import copy

from django.views.generic.simple import direct_to_template
from django.conf import settings as django_settings

from pithos_webclient import settings
from pithos_webclient.version import __version__

from synnefo.branding.utils import get_branding_dict


MEDIA_URL = getattr(settings, "PITHOS_WEB_CLIENT_MEDIA_URL",
                    getattr(django_settings, "MEDIA_URL", "/static/"))

URLS_CONFIG = {
    'STORAGE_VIEW_URL': settings.PITHOS_UI_URL.rstrip('/') + '/view/',
    'STORAGE_API_URL': settings.PITHOS_URL.rstrip('/') + '/',
    'USER_CATALOGS_API_URL': settings.USER_CATALOG_URL.rstrip('/') + '/',
    'loginUrl': settings.LOGIN_URL,
    'feedbackUrl': settings.FEEDBACK_URL
}


def index(request):
    branding_settings = get_branding_dict("")
    urls_config = copy.deepcopy(URLS_CONFIG)

    for key, value in urls_config.iteritems():
        urls_config[key] = json.dumps(value)

    for key, value in branding_settings.iteritems():
        branding_settings[key] = json.dumps(value)

    return direct_to_template(request, 'pithos_webclient/index.html', {
        'settings': settings,
        'MEDIA_URL': MEDIA_URL,
        'CLIENT_VERSION': __version__,
        'PITHOS_UI_CLOUDBAR_ACTIVE_SERVICE': settings.CLOUDBAR_ACTIVE_SERVICE,
        'branding_settings': branding_settings,
        'urls_config': URLS_CONFIG
    })
