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
package gr.grnet.pithos.web.client.clipboard;

import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;

import java.io.Serializable;
import java.util.List;


public class ClipboardItem implements Serializable{
	private int operation;
	private FileResource file;
	private List<FileResource> files;
	private RestResourceWrapper folderResource;
	private GroupUserResource user;

	public ClipboardItem(int anOperation, List<FileResource> theFiles){
		operation = anOperation;
		files = theFiles;
	}

	public ClipboardItem(int anOperation, FileResource aFile){
		operation = anOperation;
		file = aFile;
	}

	public ClipboardItem(int anOperation, RestResourceWrapper folder){
		operation = anOperation;
		folderResource = folder;
	}
	public ClipboardItem(int anOperation, GroupUserResource aUser){
		operation = anOperation;
		user = aUser;
	}

	public ClipboardItem(GroupUserResource aUser){
		operation = Clipboard.COPY;
		user = aUser;
	}

	public ClipboardItem(List<FileResource> theFiles){
		operation = Clipboard.COPY;
		files = theFiles;
	}

	public ClipboardItem(FileResource aFile){
		operation = Clipboard.COPY;
		file = aFile;
	}

	public ClipboardItem(RestResourceWrapper folder){
		operation = Clipboard.COPY;
		folderResource = folder;
	}

	/**
	 * Retrieve the user.
	 *
	 * @return the user
	 */
	public GroupUserResource getUser() {
		return user;
	}

	/**
	 * Retrieve the operation.
	 *
	 * @return the operation
	 */
	public int getOperation() {
		return operation;
	}

	/**
	 * Retrieve the file.
	 *
	 * @return the file
	 */
	public FileResource getFile() {
		return file;
	}

	/**
	 * Retrieve the files.
	 *
	 * @return the files
	 */
	public List<FileResource> getFiles() {
		return files;
	}

	/**
	 * checks whether the clipboard item is a file or folder
	 */
	public boolean isFileOrFolder(){
		if(file !=null || files != null || folderResource != null)
			return true;
		return false;
	}

	/**
	 * checks whether the clipboard item is a file (or files)
	 */
	public boolean isFile() {
		if(file !=null || files != null)
			return true;
		return false;
	}

	public boolean isUser(){
		if( user!=null  )
			return true;
		return false;
	}

	/**
	 * Retrieve the folderResource.
	 *
	 * @return the folderResource
	 */
	public RestResourceWrapper getRestResourceWrapper() {
		return folderResource;
	}
}
