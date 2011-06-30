/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.foldertree;

import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Folder extends Resource {
    /*
     * The name of the folder. If the folder is a container this is its name. If it is a virtual folder this is the
     * last part of its path
     */
    private String name = null;

    private Date lastModified = null;

    private long bytesUsed = 0;

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

    public Folder() {};

    public Folder(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public long getBytesUsed() {
        return bytesUsed;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void populate(Response response) {
        String header = response.getHeader("Last-Modified");
        if (header != null)
            lastModified = DateTimeFormat.getFormat(PredefinedFormat.RFC_2822).parse(header);

        header = response.getHeader("X-Container-Bytes-Used");
        if (header != null)
            bytesUsed = Long.valueOf(header);

        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONArray array = json.isArray();
        if (array != null) {
            for (int i=0; i<array.size(); i++) {
                JSONObject o = array.get(i).isObject();
                if (o != null) {
                    String contentType = unmarshallString(o, "content_type");
                    if (o.containsKey("subdir") || (contentType != null && (contentType.startsWith("application/directory") || contentType.startsWith("application/folder")))) {
                        Folder f = new Folder();
                        f.populate(o, container);
                        subfolders.add(f);
                    }
                    else {
                        File file = new File();
                        file.populate(o, container);
                        files.add(file);
                    }
                }
            }
        }
    }

    public void populate(JSONObject o, String aContainer) {
        String path = null;
        if (o.containsKey("subdir")) {
            path = unmarshallString(o, "subdir");
        }
        else {
            path = unmarshallString(o, "name");
            lastModified = unmarshallDate(o, "last_modified");
        }
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        if (path.contains("/"))
            name = path.substring(path.lastIndexOf("/") + 1, path.length()); //strip the prefix
        else
            name = path;
        if (aContainer != null) {
            container = aContainer;
            prefix = path;
        }
        else {
            container = name;
            prefix = "";
        }
    }

    @Override
    public String getLastModifiedSince() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static Folder createFromResponse(Response response, Folder result) {
        Folder f = null;
        if (result == null)
            f = new Folder();
        else
            f = result;

        f.populate(response);
        return f;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Folder) {
            Folder o = (Folder) other;
            if (container != null)
                return prefix.equals(o.getPrefix()) && container.equals(o.getContainer());
            else
                return o.getContainer() == null && name.equals(o.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return prefix.hashCode() + name.hashCode();
    }

    public Set<File> getFiles() {
        return files;
    }
}
