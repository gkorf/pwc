/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.foldertree;

import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import java.util.Date;

public class File extends Resource {
    private String name;

    private String hash;

    private int version;

    private long bytes;

    private String contentType;

    private Date lastModified;

    private String modifiedBy;

    private Date versionTimestamp;

    private String path;

    private String owner;

    private boolean inTrash;

    private String container;

    public String getContentType() {
        return contentType;
    }

    public String getHash() {
        return hash;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public Date getVersionTimestamp() {
        return versionTimestamp;
    }

    @Override
    public String getLastModifiedSince() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUri() {
        return path + "/" + name;
    }

    public String getOwner() {
        return owner;
    }

    public String getPath() {
        return path;
    }

    public long getBytes() {
        return bytes;
    }

    public String getSizeAsString() {
        NumberFormat nf = NumberFormat.getFormat("######.#");
        if (bytes < 1024)
            return String.valueOf(bytes) + " B";
        else if (bytes < 1024 * 1024)
            return nf.format(Double.valueOf(bytes)/(1024)) + " KB";
        else if (bytes < 1024 * 1024 * 1024)
            return nf.format(Double.valueOf(bytes)/(1024 * 1024)) + " MB";
        return nf.format(Double.valueOf(bytes)/(1024 * 1024 * 1024)) + " GB";
    }

    public boolean isShared() {
        return false;
    }

    public boolean isInTrash() {
        return inTrash;
    }

    public void populate(JSONObject o, String container) {
        path = unmarshallString(o, "name");
        if (path.contains("/"))
            name = path.substring(path.lastIndexOf("/") + 1, path.length()); //strip the prefix
        else
            name = path;
        hash = unmarshallString(o, "hash");
        bytes = unmarshallLong(o, "bytes");
        version = unmarshallInt(o, "version");
        contentType = unmarshallString(o, "content_type");
        lastModified = unmarshallDate(o, "last_modified");
        modifiedBy = unmarshallString(o, "modified_by");
        versionTimestamp = unmarshallDate(o, "version_timestamp");
        this.container = container;
    }

    public boolean equals(Object other) {
        if (other instanceof File) {
            File o = (File) other;
            return name.equals(o.getName());
        }
        return false;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String getContainer() {
        return container;
    }

    public static File createFromResponse(Response response, File result) {
        result.populate(response);
        return result;
    }

    private void populate(Response response) {
        String header = response.getHeader("X-Object-Meta-Trash");
        if (header != null)
            inTrash = Boolean.valueOf(header);
        else
            inTrash = false;

        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONObject o = json.isObject();
    }
}
