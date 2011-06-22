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
import org.w3c.css.sac.ElementSelector;

public class Folder extends Resource {
    private String name = null;

    private Date lastModified = null;

    private long bytesUsed = 0;

    private Set<Folder> subfolders = new LinkedHashSet<Folder>();
    private String container = null;

    private String prefix = "";

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

    public void setContainer(String container) {
        this.container = container;
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
                    if (o.containsKey("subdir")) {
                        Folder f = new Folder();
                        f.populate(o);
                        f.setContainer(container == null ? name : container);
                        f.setPrefix(container == null ? f.getName() : prefix + "/" + f.getName());
                        subfolders.add(f);
                    }
                    else {
                        String contentType = unmarshallString(o, "content_type");
                        if (contentType != null && contentType.startsWith("application/directory")) {
                            Folder f = new Folder();
                            f.populate(o);
                            f.setContainer(container == null ? name : container);
                            f.setPrefix(container == null ? f.getName() : prefix + "/" + f.getName());
                            subfolders.add(f);
                        }
                        else {
                            // add file
                        }
                    }
                }
            }
        }
    }

    public void populate(JSONObject o) {
        if (o.containsKey("subdir")) {
            name = unmarshallString(o, "subdir");
            if (name.endsWith("/"))
                name = name.substring(0, name.length() - 1);
        }
        else {
            name = unmarshallString(o, "name");
            lastModified = unmarshallDate(o, "last_modified");
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
            return name.equals(o.getName()) && prefix.equals(o.getPrefix());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return prefix.hashCode() + name.hashCode();
    }
}
