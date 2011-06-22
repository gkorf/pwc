/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.foldertree;

import com.google.gwt.http.client.Header;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContainerResource extends Resource {
    /*
     * The name of the container
     */
    private String name;

    /*
     * The number of objects inside the container
     */
    private long count;

    /*
     * The total size of the objects inside the container
     */
    private long bytes;

    /*
     * The last object modification date
     */
    private Date lastModified;

    /*
     * The date the container was created
     */
    private Date created;

    private List<Folder> subfolders = new ArrayList<Folder>();

    @Override
    public String getLastModifiedSince() {
        return "";
    }

    public String getName() {
        return name;
    }

    public long getBytes() {
        return bytes;
    }

    public long getCount() {
        return count;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addSubfolder(Folder f) {
        subfolders.add(f);
    }

    public boolean hasSubfolders() {
        return !subfolders.isEmpty();
    }

    public List<Folder> getSubfolders() {
        return subfolders;
    }
}
