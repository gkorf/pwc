package gr.grnet.pithos.web.client;

import com.google.gwt.regexp.shared.RegExp;

/**
 * Gathers application-wide constants, like <code>X-Auth-Token</code>
 */
public class Const {
    public static final String X_AUTH_TOKEN = "X-Auth-Token";
    public static final String OTHER_PROPERTIES = "otherProperties";
    public static final String LOGIN_URL = "loginUrl";
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String PERCENT_100 = "100%";
    public static final String DATE_FORMAT_1 = "EEE, dd MMM yyyy HH:mm:ss";
    public static final String AUTH_COOKIE = "authCookie";
    public static final String X_COPY_FROM = "X-Copy-From";
    public static final String X_SOURCE_ACCOUNT = "X-Source-Account";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String X_MOVE_FROM = "X-Move-From";
    public static final String HOME_CONTAINER = "pithos";
    public static final String TRASH_CONTAINER = "trash";
    public static final RegExp EMAIL_REGEX = RegExp.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+[.][A-Z]{2,4}$", "i");
    public static final String X_ACCOUNT_GROUP_ = "X-Account-Group-";

    private Const() {}


}
