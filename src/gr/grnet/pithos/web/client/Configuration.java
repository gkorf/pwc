/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import com.google.gwt.i18n.client.Constants;


/**
 * This interface contains bindings for the compile-time configurable
 * entities of the application.
 *
 */
public interface Configuration extends Constants {
	/**
	 * @return the name of the service
	 */
	@DefaultStringValue("GSS")
	String serviceName();

	/**
	 * @return the login URL
	 */
	@DefaultStringValue("/pithos/login")
	String loginUrl();

	/**
	 * @return the logout URL
	 */
	@DefaultStringValue("/pithos/login")
	String logoutUrl();

	/**
	 * @return the authentication cookie name
	 */
	@DefaultStringValue("_gss_a")
	String authCookie();

	/**
	 * @return the webdav cookie name
	 */
	@DefaultStringValue("_gss_wd")
	String webdavCookie();

	/**
	 * @return the separator string between username and token in the
	 * authentication cookie
	 */
	@DefaultStringValue("|")
	String cookieSeparator();

	/**
	 * @return the relative path of the API root URL
	 */
	@DefaultStringValue("rest/")
	String apiPath();

	/**
	 * @return the WebDAV URL
	 */
	@DefaultStringValue("/webdav/")
	String webdavUrl();

	/**
	 * @return the token TTL note
	 */
	@DefaultStringValue("")
	String tokenTTLNote();

	/**
	 * @return the version string
	 */
	@DefaultStringValue("")
	String version();

}
