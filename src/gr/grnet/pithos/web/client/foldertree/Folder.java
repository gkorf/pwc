/*
 * Copyright 2011-2012 GRNET S.A. All rights reserved.
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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class Folder extends Resource {
    /*
     * The name of the folder. If the folder is a container this is its name. If it is a virtual folder this is the
     * last part of its path
     */
    private String name = null;

    private Date lastModified = null;

    private long bytesUsed = 0;

    private Folder parent = null;
    
    private Set<Folder> subfolders = new LinkedHashSet<Folder>();
    /*
     * The name of the container that this folder belongs to. If this folder is container, this field equals name
     */
    private String container = null;

    /*
     * This is the full path of the folder (prefix is a misnomer but it was named so because this is used as a prefix=
     * parameter in the request that fetches its children). If the folder is a cointainer this is empty string
     */
    private String prefix = "";

    private Set<File> files = new LinkedHashSet<File>();

    private String owner;

    private Map<String, Boolean[]> permissions = new HashMap<String, Boolean[]>();

    private String inheritedPermissionsFrom;

    public Folder() {};

    public Folder(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    @Override
	public Date getLastModified() {
        return lastModified;
    }

    public long getBytesUsed() {
        return bytesUsed;
    }

    public Set<Folder> getSubfolders() {
        return subfolders;
    }

    public void setSubfolders(Set<Folder> subfolders) {
        this.subfolders = subfolders;
    }

    public String getContainer() {
        return container;
    }

    public String getPrefix() {
        return prefix;
    }

    private void parsePermissions(String rawPermissions) {
        String[] readwrite = rawPermissions.split(";");
        for (String s : readwrite) {
            String[] part = s.split("=");
            String perm = part[0].trim();
            String[] users = part[1].split(",");
            for (String u : users) {
                String user = u.trim();
                Boolean[] userPerm = permissions.get(u);
                if (userPerm == null) {
                    userPerm = new Boolean[2];
                    permissions.put(user, userPerm);
                }
                if (perm.equals("read")) {
                    userPerm[0] = Boolean.TRUE;
                }
                else if (perm.equals("write")) {
                    userPerm[1] = Boolean.TRUE;
                }
            }
        }
    }

    public void populate(String _owner, Response response) {
        this.owner = _owner;
        String header = response.getHeader("Last-Modified");
        if (header != null)
			try {
				lastModified = DateTimeFormat.getFormat(PredefinedFormat.RFC_2822).parse(header);
			} catch (IllegalArgumentException e) {
				GWT.log("Last-Modified will be set to null", e);
				lastModified = null;
			}

        header = response.getHeader("X-Container-Bytes-Used");
        if (header != null && header.length() > 0)
            bytesUsed = Long.valueOf(header);

        String rawPermissions = response.getHeader("X-Object-Sharing");
        if (rawPermissions != null && rawPermissions.length() > 0) {
            parsePermissions(URL.decodePathSegment(rawPermissions));
        }
        
        if (response.getText() == null || response.getText().isEmpty())
        	return;
        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONArray array = json.isArray();
        if (array != null) {
            subfolders.clear(); //This is necessary in case we update a pre-existing Folder so that stale subfolders won't show up
            files.clear();
            for (int i=0; i<array.size(); i++) {
                JSONObject o = array.get(i).isObject();
                if (o != null) {
                    String contentType = unmarshallString(o, "content_type");
                    if (o.containsKey("subdir") || (contentType != null && (contentType.startsWith("application/directory") || contentType.startsWith("application/folder")))) {
                        Folder f = new Folder();
                        f.populate(this, o, _owner, container);
                        if (f.getName().length() > 0)
                        	subfolders.add(f);
                    }
                    else {
                        File file = new File();
                        file.populate(this, o, _owner, container);
                        if (file.getName().length() > 0)
                        	files.add(file);
                    }
                }
            }
        }
    }

    public void populate(Folder _parent, JSONObject o, String _owner, String aContainer) {
        this.parent = _parent;
        String path = null;
        if (o.containsKey("subdir")) {
            path = unmarshallString(o, "subdir");
            if (path.endsWith("/")) { //Always true for "subdir"
                path = path.substring(0, path.length() - 1);
            }
            if (parent != null && parent.getPrefix().length() > 0)
            	name = path.substring(parent.getPrefix().length() + 1);
            else
            	name = path;
            if (name.equals("/"))
            	name = "";
        }
        else {
            path = unmarshallString(o, "name");
            lastModified = unmarshallDate(o, "last_modified");
            if (parent != null && parent.getPrefix().length() > 0)
            	name = path.substring(parent.getPrefix().length() + 1);
            else
            	name = path;
        }
        if (aContainer != null) {
            container = aContainer;
            prefix = path;
        }
        else {
            container = name;
            prefix = "";
        }
        this.owner = _owner;

        inheritedPermissionsFrom = unmarshallString(o, "x_object_shared_by");
        String rawPermissions = unmarshallString(o, "x_object_sharing");
        if (rawPermissions != null)
            parsePermissions(rawPermissions);
    }

    public static Folder createFromResponse(String owner, Response response, Folder result) {
        Folder f = null;
        if (result == null)
            f = new Folder();
        else
            f = result;

        f.populate(owner, response);
        return f;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Folder) {
            Folder o = (Folder) other;
            return owner.equals(o.getOwner()) && getUri().equals(o.getUri());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }

    public Set<File> getFiles() {
        return files;
    }

    public Folder getParent() {
        return parent;
    }

    public String getUri() {
        return "/" + container + (prefix.length() == 0 ? "" : "/" + prefix);
    }

    public boolean isContainer() {
        return parent == null;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getInheritedPermissionsFrom() {
        return inheritedPermissionsFrom;
    }

    public Map<String, Boolean[]> getPermissions() {
        return permissions;
    }

    public String getOwner() {
        return owner;
    }

	public boolean isShared() {
		return !permissions.isEmpty();
	}

	/**
	 * I am THE trash
	 * 
	 * @return
	 */
	public boolean isTrash() {
		return isContainer() && name.equals(Pithos.TRASH_CONTAINER);
	}
	
	/**
	 * I am IN THE trash
	 * 
	 * @return
	 */
	public boolean isInTrash() {
		return container.equals(Pithos.TRASH_CONTAINER);
	}

	public boolean isHome() {
		return isContainer() && name.equals(Pithos.HOME_CONTAINER);
	}

	public boolean contains(Folder folder) {
		for (Folder f : subfolders)
			if (f.equals(folder) || f.contains(folder))
				return true;
		return false;
	}
}
