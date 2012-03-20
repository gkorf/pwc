from django.conf import settings

# !!!!!ATTENTION!!!!!
# loginUrl MUST end at "next=". You should not give the value of the next
# parameter. It will be determined automatically
LOGIN_URL = getattr(settings, 'PITHOS_UI_LOGIN_URL', 'https://accounts.okeanos.grnet.gr/im/login?next=')
FEEDBACK_URL = getattr(settings, 'PITHOS_UI_FEEDBACK_URL', 'https://accounts.okeanos.grnet.gr/im/feedback')
AUTH_COOKIE_NAME = getattr(settings, 'PITHOS_UI_AUTH_COOKIE_NAME', '_pithos2_a')

