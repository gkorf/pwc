/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest;


public class RestException extends Throwable {

	private int httpStatusCode;
    private String httpStatusText;
    private String text;

    public RestException() {
    }

    public RestException(String message) {
            super(message);
    }

    public RestException(Throwable innerException) {
            super(innerException);
    }

    public RestException(String message, Throwable innerException) {
            super(message, innerException);
    }

    public RestException(String aPath, int aStatusCode, String aStatusText, String aText) {
            super("HTTP error: " + aStatusCode+"\nPath:"+aPath + "\nStatus text:" + aStatusText + "\nText:" + aText);
            httpStatusCode = aStatusCode;
            httpStatusText = aStatusText;
            text = aText;
    }

    public int getHttpStatusCode() {
            return httpStatusCode;
    }

    public String getHttpStatusText() {
            return httpStatusText;
    }

    public String getText() {
            return text;
    }

}
