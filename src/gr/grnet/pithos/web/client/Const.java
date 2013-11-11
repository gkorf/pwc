package gr.grnet.pithos.web.client;

import com.google.gwt.regexp.shared.RegExp;

/**
 * Gathers application-wide constants, like <code>X-Auth-Token</code>
 */
public final class Const {
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String HTTP_HEADER_SERVER = "Server";
    public static final String HTTP_HEADER_CONNECTION = "Connection";
    public static final String HTTP_HEADER_KEEP_ALIVE = "Keep-Alive";
    public static final String HTTP_HEADER_DATE = "Date";
    public static final String HTTP_HEADER_VARY = "Vary";

    public static final String X_AUTH_TOKEN = "X-Auth-Token";
    public static final String X_OBJECT_SHARING = "X-Object-Sharing";
    public static final String X_OBJECT_PUBLIC = "X-Object-Public";
    public static final String X_COPY_FROM = "X-Copy-From";
    public static final String X_SOURCE_ACCOUNT = "X-Source-Account";
    public static final String X_MOVE_FROM = "X-Move-From";

    public static final String X_ACCOUNT_GROUP_ = "X-Account-Group-";

    public static final String OTHER_PROPERTIES = "otherProperties";
    public static final String LOGIN_URL = "loginUrl";

    public static final String PERCENT_100 = "100%";
    public static final String PERCENT_75 = "75%";

    public static final String DATE_FORMAT_1 = "EEE, dd MMM yyyy HH:mm:ss";
    public static final String AUTH_COOKIE = "authCookie";

    public static final RegExp EMAIL_REGEX = RegExp.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+[.][A-Z]{2,4}$", "i");

    public static final String HOME_CONTAINER = "pithos";
    public static final String TRASH_CONTAINER = "trash";

    public static final String EQ = "=";
    public static final String COMMA = ",";
    public static final String TILDE = "~";
    public static final String SEMI = ";";
    public static final String COLON = ":";
    public static final String QUESTION_MARK = "?";
    public static final String AMPERSAND = "&";

    public static final String READ_EQ = "read=";
    public static final String WRITE_EQ = "write=";
    public static final String UPDATE_EQ = "update=";
    public static final String QUESTION_MARK_UPDATE_EQ = QUESTION_MARK + UPDATE_EQ;

    public static final String TXT_USER = "User";
    public static final String TXT_GROUP = "Group";
    public static final String TXT_ADD_USER = "Add User";
    public static final String TXT_ADD_GROUP = "Add Group";
    public static final String TXT_SHARE_FILE = "Share File";
    public static final String TXT_SHARE_FOLDER = "Share Folder";
    public static final String TXT_PRIVATE_LINK = "Private Link";
    public static final String TXT_PUBLIC_LINK = "Public Link";
    public static final String TXT_SHARED_WITH_ME = "Shared with me";

    public static final String HTML_NBSP = "&nbsp;";

    public static final String NL = "\n";

    public static final String EVENT_TYPE_KEYDOWN = "keydown";

    private Const() {}


    public static String PurgeContainer(String name) {
        return "Purge Container [" + name + "]";
    }

    public static String inSpan(String html) {
        return "<span>" + html + "</span>";
    }

    public static String inSpan(String html0, String html1, String ...html) {
        final int knownLength = html0.length() + html1.length();
        final int estimatedLength = knownLength * html.length;
        final StringBuilder sb = new StringBuilder(estimatedLength);
        sb.append(html0);
        sb.append(html1);
        for(String s : html) {
            sb.append(s);
        }

        return inSpan(sb.toString());
    }
}
