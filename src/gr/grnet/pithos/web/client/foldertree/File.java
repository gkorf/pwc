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

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import java.io.StringWriter;
import java.security.Key;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

    private Folder parent;

    private Set<String> tags = new HashSet<String>();

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

    public String getUri() {
        return "/" + container + "/" + path;
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

    public void populate(Folder parent, JSONObject o, String container) {
        this.parent = parent;
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

        for (String key : o.keySet())
            if (key.startsWith("x_object_meta_") && !key.equals("x_object_meta_trash"))
                tags.add(key.substring("x_object_meta_".length()).trim().toLowerCase());
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
        for (Header h : response.getHeaders()) {
            String header = h.getName();
            if (header.startsWith("X-Object-Meta-") && !header.equals("X-Object-Meta-Trash"))
                tags.add(header.substring("X-Object-Meta-".length()).trim().toLowerCase());

        }
        String header = response.getHeader("X-Object-Meta-Trash");
        if (header != null)
            inTrash = Boolean.valueOf(header);
        else
            inTrash = false;

        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONObject o = json.isObject();
    }

    public Folder getParent() {
        return parent;
    }

    public Set<String> getTags() {
        return tags;
    }
}
