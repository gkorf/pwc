/*
 * Copyright 2007, 2008, 2009 Electronic Business Systems Ltd.
 *
 * This file is part of GSS.
 *
 * GSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GSS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gss_project.gss.web.client;

import java.io.Serializable;

/**
 * An exception that is thrown when an operation cannot be performed due to the
 * user having insufficient permissions.
 *
 * @author chstath
 */
public class InsufficientPermissionsException extends Exception implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The stored message that provides details about the problem.
	 */
	private String message;

	/**
	 *
	 */
	public InsufficientPermissionsException() {
	}

	/**
	 * @param newMessage
	 */
	public InsufficientPermissionsException(final String newMessage) {
		super(newMessage);
		message = newMessage;
	}

	/**
	 * @param cause
	 */
	public InsufficientPermissionsException(final Throwable cause) {
		super(cause);

	}

	/**
	 * @param newMessage
	 * @param cause
	 */
	public InsufficientPermissionsException(final String newMessage, final Throwable cause) {
		super(newMessage, cause);
		message = newMessage;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
