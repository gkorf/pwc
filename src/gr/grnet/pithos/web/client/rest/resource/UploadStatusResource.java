/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest.resource;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;


public class UploadStatusResource extends RestResource{
	long bytesTransferred;
	long fileSize;

	public UploadStatusResource(String aUri) {
		super(aUri);
	}

	/**
	 * Retrieve the bytesTransferred.
	 *
	 * @return the bytesTransferred
	 */
	public long getBytesTransferred() {
		return bytesTransferred;
	}

	/**
	 * Modify the bytesTransferred.
	 *
	 * @param newBytesTransferred the bytesTransferred to set
	 */
	public void setBytesTransferred(long newBytesTransferred) {
		bytesTransferred = newBytesTransferred;
	}

	/**
	 * Retrieve the fileSize.
	 *
	 * @return the fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * Modify the fileSize.
	 *
	 * @param newFileSize the fileSize to set
	 */
	public void setFileSize(long newFileSize) {
		fileSize = newFileSize;
	}

	public int percent(){
		return new Long(bytesTransferred * 100 / fileSize).intValue();
	}

	@Override
	public void createFromJSON(String text) {
		JSONObject json = (JSONObject) JSONParser.parse(text);
		if(json.get("bytesTotal") != null)
			fileSize = new Long(json.get("bytesTotal").toString());
		if(json.get("bytesUploaded") != null)
			bytesTransferred = new Long(json.get("bytesUploaded").toString());

	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
}
