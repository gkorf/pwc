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

package gr.grnet.pithos.web.client.foldertree;

import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.grouptree.Group;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * Created by IntelliJ IDEA. User: chstath Date: 5/19/11 Time: 2:55 PM To change this template use File | Settings |
 * File Templates.
 */
public class AccountResource extends Resource {

    private long numberOfContainers = 0;

    private long numberOfObjects = 0;

    private long bytesUsed = 0;

    private long bytesRemaining = 0;

    private Date lastLogin = null;

    private Date lastModified = null;
    
    private List<Folder> containers = new ArrayList<Folder>();

    private Date currentLogin = null;

    private List<Group> groups = new ArrayList<Group>();

    public long getBytesRemaining() {
        return bytesRemaining;
    }

    public void setBytesRemaining(long bytesRemaining) {
        this.bytesRemaining = bytesRemaining;
    }

    public long getBytesUsed() {
        return bytesUsed;
    }

    public void setBytesUsed(long bytesUsed) {
        this.bytesUsed = bytesUsed;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
	public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public long getNumberOfContainers() {
        return numberOfContainers;
    }

    public void setNumberOfContainers(long numberOfContainers) {
        this.numberOfContainers = numberOfContainers;
    }

    public long getNumberOfObjects() {
        return numberOfObjects;
    }

    public void setNumberOfObjects(long numberOfObjects) {
        this.numberOfObjects = numberOfObjects;
    }

    public List<Folder> getContainers() {
        return containers;
    }

    public Date getCurrentLogin() {
        return currentLogin;
    }

    public void setCurrentLogin(Date currentLogin) {
        this.currentLogin = currentLogin;
    }

    public void populate(String owner, Response response) {
        DateTimeFormat df = DateTimeFormat.getFormat(PredefinedFormat.RFC_2822);
        for (Header h : response.getHeaders()) {
        	if (h != null) {
		        String name = h.getName();
		        if (name.startsWith("X-Account-Group-")) {
		            String groupName = name.substring("X-Account-Group-".length()).trim().toLowerCase();
		            Group g = new Group(groupName);
		            String[] members = h.getValue().split(",");
		            for (String s : members)
		                g.addMember(s.trim());
		            groups.add(g);
		        }
		        else if (name.equals("X-Account-Container-Count")) {
		            numberOfContainers = Long.valueOf(h.getValue());
		        }
		        else if (name.equals("X-Account-Object-Count")) {
		            numberOfObjects = Long.valueOf(h.getValue());
		        }
		        else if (name.equals("X-Account-Bytes-Used")) {
		            bytesUsed = Long.valueOf(h.getValue());
		        }
		        else if (name.equals("X-Account-Bytes-Remaining")) {
		            bytesRemaining = Long.valueOf(h.getValue());
		        }
		        else if (name.equals("X-Account-Last-Login")) {
		            lastLogin = df.parse(h.getValue());
		        }
		        else if (name.equals("Last-Modified")) {
		            lastModified = df.parse(h.getValue());
		        }
        	}
        }

        if (response.getText() != null && response.getText().length() > 0) {
	        JSONValue json = JSONParser.parseStrict(response.getText());
	        JSONArray array = json.isArray();
	        if (array != null) {
	            for (int i=0; i<array.size(); i++) {
	                JSONObject o = array.get(i).isObject();
	                if (o != null) {
	                    Folder f = new Folder();
	                    f.populate(null, o, owner, null);
	                    containers.add(f);
	                }
	            }
	        }
        }
    }

    public static AccountResource createFromResponse(String owner, Response response, AccountResource result) {
    	AccountResource a;
    	if (result == null)
    		a = new AccountResource();
    	else
    		a = result;
        a.populate(owner, response);
        return a;
    }

    private String getSize(Long size, Double division){
        Double res = Double.valueOf(size.toString())/division;
        NumberFormat nf = NumberFormat.getFormat("######.#");
        return nf.format(res);
    }

    public String getFileSizeAsString() {
        if (bytesUsed < 1024)
            return String.valueOf(bytesUsed) + "B";
        else if (bytesUsed < 1024*1024)
            return getSize(bytesUsed, 1024D) + "KB";
        else if (bytesUsed < 1024*1024*1024)
            return getSize(bytesUsed,(1024D*1024D)) + "MB";
        return getSize(bytesUsed , (1024D*1024D*1024D)) + "GB";
    }

    public String getQuotaAsString() {
    	long quota = bytesUsed + bytesRemaining;
        if (quota < 1024)
            return String.valueOf(quota) + "B";
        else if (quota < 1024 * 1024)
            return getSize(quota, 1024D) + "KB";
        else if (quota < 1024 * 1024 * 1024)
            return getSize(quota,(1024D * 1024D)) + "MB";
        return getSize(quota , (1024D * 1024D * 1024D)) + "GB";
    }

    public List<Group> getGroups() {
        return groups;
    }
    
    public boolean hasHomeContainer() {
    	for (Folder f : containers)
    		if (f.getName().equals(Pithos.HOME_CONTAINER))
    			return true;
    	return false;
    }

    public boolean hasTrashContainer() {
    	for (Folder f : containers)
    		if (f.getName().equals(Pithos.TRASH_CONTAINER))
    			return true;
    	return false;
    }

	public void addGroup(Group newGroup) {
		groups.add(newGroup);
	}

	public void removeGroup(Group group) {
		groups.remove(group);
	}

	public Folder getTrash() {
		for (Folder c : containers) {
			if (c.getName().equals(Pithos.TRASH_CONTAINER))
				return c;
		}
		return null;
	}

	public double getUsedPercentage() {
		return ((double) bytesUsed) / (bytesUsed + bytesRemaining);
	}

	public Folder getPithos() {
		for (Folder f : containers)
			if (f.getName().equals(Pithos.HOME_CONTAINER))
				return f;
		return null;
	}
}
