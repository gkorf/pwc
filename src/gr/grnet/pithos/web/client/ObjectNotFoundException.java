/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import java.io.Serializable;

/**
 * An exception thrown when a requested object was not found.
 *
 */
public class ObjectNotFoundException extends Exception implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The stored message that provides details about the problem.
	 */
	private String message;

	/**
	 * Default constructor
	 */
	public ObjectNotFoundException() {
		super();
	}

	/**
	 * Constructor from error message.
	 *
	 * @param newMessage The error message
	 */
	public ObjectNotFoundException(final String newMessage) {
		super(newMessage);
		message = newMessage;
	}

	/**
	 * Constructor from Throwable.
	 *
	 * @param cause The throwable that caused the exception
	 */
	public ObjectNotFoundException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor from error message and Throwable.
	 *
	 * @param newMessage The error message
	 * @param cause The throwable that caused the exception
	 */
	public ObjectNotFoundException(final String newMessage, final Throwable cause) {
		super(newMessage, cause);
		message = newMessage;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
