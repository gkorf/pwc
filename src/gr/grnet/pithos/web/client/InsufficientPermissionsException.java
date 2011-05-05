/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import java.io.Serializable;

/**
 * An exception that is thrown when an operation cannot be performed due to the
 * user having insufficient permissions.
 *
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
