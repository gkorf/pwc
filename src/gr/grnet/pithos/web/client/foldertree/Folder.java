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

        subfolders.clear(); //This is necessary in case we update a pre-existing Folder so that stale subfolders won't show up
        files.clear();
        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONArray array = json.isArray();
        if (array != null) {
            for (int i=0; i<array.size(); i++) {
                JSONObject o = array.get(i).isObject();
                if (o != null) {
                    String contentType = unmarshallString(o, "content_type");
                    if (o.containsKey("subdir") || (contentType != null && (contentType.startsWith("application/directory") || contentType.startsWith("application/folder")))) {
                        Folder f = new Folder();
                        f.populate(this, o, container);
                        subfolders.add(f);
                    }
                    else {
                        File file = new File();
                        file.populate(this, o, container);
                        files.add(file);
                    }
                }
            }
        }
    }

    public void populate(Folder parent, JSONObject o, String aContainer) {
        this.parent = parent;
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
            return (container + prefix).equals(o.getContainer() + o.getPrefix());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (container + prefix).hashCode();
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
}
