/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest.resource;

import java.io.Serializable;

import com.google.gwt.i18n.client.NumberFormat;


public class QuotaHolder implements Serializable{
	private Long fileCount = 0L;
	private Long fileSize = 0L;
	private Long quotaLeftSize = 0L;

	/**
	 * Retrieve the fileCount.
	 *
	 * @return the fileCount
	 */
	public Long getFileCount() {
		return fileCount;
	}

	/**
	 * Modify the fileCount.
	 *
	 * @param aFileCount the fileCount to set
	 */
	public void setFileCount(Long aFileCount) {
		fileCount = aFileCount;
	}

	/**
	 * Retrieve the fileSize.
	 *
	 * @return the fileSize
	 */
	public Long getFileSize() {
		return fileSize;
	}

	/**
	 * Modify the fileSize.
	 *
	 * @param aFileSize the fileSize to set
	 */
	public void setFileSize(Long aFileSize) {
		fileSize = aFileSize;
	}

	/**
	 * Retrieve the quotaLeftSize.
	 *
	 * @return the quotaLeftSize
	 */
	public Long getQuotaLeftSize() {
		return quotaLeftSize;
	}

	/**
	 * Modify the quotaLeftSize.
	 *
	 * @param aQuotaLeftSize the quotaLeftSize to set
	 */
	public void setQuotaLeftSize(Long aQuotaLeftSize) {
		quotaLeftSize = aQuotaLeftSize;
	}

	public String getFileSizeAsString() {
		if (fileSize < 1024)
			return String.valueOf(fileSize) + " B";
		else if (fileSize < 1024*1024)
			return getSize(fileSize, 1024D) + " KB";
		else if (fileSize < 1024*1024*1024)
			return getSize(fileSize,(1024D*1024D)) + " MB";
		return getSize(fileSize , (1024D*1024D*1024D)) + " GB";
	}

	public String getQuotaLeftAsString() {
		if (quotaLeftSize < 1024)
			return String.valueOf(quotaLeftSize) + " B";
		else if (quotaLeftSize < 1024*1024)
			return getSize(quotaLeftSize, 1024D) + " KB";
		else if (quotaLeftSize < 1024*1024*1024)
			return getSize(quotaLeftSize,(1024D*1024D)) + " MB";
		return getSize(quotaLeftSize , (1024D*1024D*1024D)) + " GB";
	}

	private String getSize(Long size, Double division){
		Double res = Double.valueOf(size.toString())/division;
		NumberFormat nf = NumberFormat.getFormat("######.#");
		return nf.format(res);
	}

	public long percentOfFreeSpace(){
		return (long) ((double)quotaLeftSize*100/(fileSize+quotaLeftSize)+0.5);
	}
}
