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
