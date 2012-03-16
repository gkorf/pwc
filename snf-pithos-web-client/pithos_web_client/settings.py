from django.conf import settings

#!!!!!ATTENTION!!!!! loginUrl MUST end at "next=". You should not give the value of the next parameter. It will be determined automatically
LOGIN_URL = getattr(settings, 'PITHOS_UI_LOGIN_URL', '/im/login?next=')
FEEDBACK_URL = getattr(settings, 'PITHOS_UI_FEEDBACK_URL', '/im/feedback')

CLOUDBAR_ACTIVE_SERVICE = getattr(settings, 'PITHOS_UI_CLOUDBAR_ACTIVE_SERVICE', 'pithos')
CLOUDBAR_LOCATION = getattr(settings, 'CLOUDBAR_LOCATION', '/static/im/cloudbar/')
CLOUDBAR_COOKIE_NAME = getattr(settings, 'CLOUDBAR_COOKIE_NAME', '_pithos2_a')
CLOUDBAR_MENU_URL = getattr(settings, 'CLOUDBAR_MENU_URL', '/im/get_menu')
CLOUDBAR_SERVICES_URL = getattr(settings, 'CLOUDBAR_SERVICES_URL', '/im/get_services')
