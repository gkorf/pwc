/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest.resource;

import java.io.Serializable;


public class PermissionHolder implements Serializable{

	private String user;
	private String group;
	private boolean read;
	private boolean write;
	private boolean modifyACL;

	/**
	 * Retrieve the user.
	 *
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Modify the user.
	 *
	 * @param aUser the user to set
	 */
	public void setUser(String aUser) {
		user = aUser;
	}

	/**
	 * Retrieve the group.
	 *
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Modify the group.
	 *
	 * @param aGroup the group to set
	 */
	public void setGroup(String aGroup) {
		group = aGroup;
	}

	/**
	 * Retrieve the read.
	 *
	 * @return the read
	 */
	public boolean isRead() {
		return read;
	}

	/**
	 * Modify the read.
	 *
	 * @param aRead the read to set
	 */
	public void setRead(boolean aRead) {
		read = aRead;
	}

	/**
	 * Retrieve the write.
	 *
	 * @return the write
	 */
	public boolean isWrite() {
		return write;
	}

	/**
	 * Modify the write.
	 *
	 * @param aWrite the write to set
	 */
	public void setWrite(boolean aWrite) {
		write = aWrite;
	}

	/**
	 * Retrieve the modifyACL.
	 *
	 * @return the modifyACL
	 */
	public boolean isModifyACL() {
		return modifyACL;
	}

	/**
	 * Modify the modifyACL.
	 *
	 * @param aModifyACL the modifyACL to set
	 */
	public void setModifyACL(boolean aModifyACL) {
		modifyACL = aModifyACL;
	}

}
