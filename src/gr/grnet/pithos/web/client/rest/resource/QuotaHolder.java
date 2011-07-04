/*
 * Copyright 2011 GRNET S.A. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 *   1. Redistributions of source code must retain the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials
 *      provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY GRNET S.A. ``AS IS'' AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GRNET S.A OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and
 * documentation are those of the authors and should not be
 * interpreted as representing official policies, either expressed
 * or implied, of GRNET S.A.
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



	public long percentOfFreeSpace(){
		return (long) ((double)quotaLeftSize*100/(fileSize+quotaLeftSize)+0.5);
	}
}
