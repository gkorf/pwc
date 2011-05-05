/*
 *  Copyright (c) 2011 Greek Research and Technology Network
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

	public ClipboardItem(){}

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
	 * Modify the user.
	 *
	 * @param aUser the user to set
	 */
	public void setUser(GroupUserResource aUser) {
		user = aUser;
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
	 * Modify the operation.
	 *
	 * @param anOperation the operation to set
	 */
	public void setOperation(int anOperation) {
		operation = anOperation;
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
	 * Modify the file.
	 *
	 * @param aFile the file to set
	 */
	public void setFile(FileResource aFile) {
		file = aFile;
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

	/**
	 * Modify the folderResource.
	 *
	 * @param aFolder the folderResource to set
	 */
	public void setRestResourceWrapper(RestResourceWrapper aFolder) {
		folderResource = aFolder;
	}
}
