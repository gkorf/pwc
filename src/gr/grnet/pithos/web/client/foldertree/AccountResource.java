/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.foldertree;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    
    private Set<Folder> containers = new LinkedHashSet<Folder>();

    private Date currentLogin = null;

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

    @Override
    public String getLastModifiedSince() {
        return null;
    }

    public Set<Folder> getContainers() {
        return containers;
    }

    public Date getCurrentLogin() {
        return currentLogin;
    }

    public void setCurrentLogin(Date currentLogin) {
        this.currentLogin = currentLogin;
    }

    public void populate(Response response) {
        String header = response.getHeader("X-Account-Container-Count");
        if (header != null)
            numberOfContainers = Long.valueOf(header);

        header = response.getHeader("X-Account-Object-Count");
        if (header != null)
            numberOfObjects = Long.valueOf(header);

        header = response.getHeader("X-Account-Bytes-Used");
        if (header != null)
            bytesUsed = Long.valueOf(header);

        header = response.getHeader("X-Account-Bytes-Remaining");
        if (header != null)
            bytesRemaining = Long.valueOf(header);

        DateTimeFormat df = DateTimeFormat.getFormat(PredefinedFormat.RFC_2822);
        header = response.getHeader("X-Account-Last-Login");
        if (header != null)
            lastLogin = df.parse(header);

        header = response.getHeader("Last-Modified");
        if (header != null)
            lastModified = df.parse(header);

        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONArray array = json.isArray();
        if (array != null) {
            for (int i=0; i<array.size(); i++) {
                JSONObject o = array.get(i).isObject();
                if (o != null) {
                    Folder f = new Folder();
                    f.populate(o);
                    containers.add(f);
                }
            }
        }
    }

    public static AccountResource createFromResponse(Response response) {
        AccountResource a = new AccountResource();
        a.populate(response);
        return a;
    }

    private String getSize(Long size, Double division){
        Double res = Double.valueOf(size.toString())/division;
        NumberFormat nf = NumberFormat.getFormat("######.#");
        return nf.format(res);
    }

    public String getFileSizeAsString() {
        if (bytesUsed < 1024)
            return String.valueOf(bytesUsed) + " B";
        else if (bytesUsed < 1024*1024)
            return getSize(bytesUsed, 1024D) + " KB";
        else if (bytesUsed < 1024*1024*1024)
            return getSize(bytesUsed,(1024D*1024D)) + " MB";
        return getSize(bytesUsed , (1024D*1024D*1024D)) + " GB";
    }

    public String getQuotaLeftAsString() {
        if (bytesRemaining < 1024)
            return String.valueOf(bytesRemaining) + " B";
        else if (bytesRemaining < 1024 * 1024)
            return getSize(bytesRemaining, 1024D) + " KB";
        else if (bytesRemaining < 1024 * 1024 * 1024)
            return getSize(bytesRemaining,(1024D * 1024D)) + " MB";
        return getSize(bytesRemaining , (1024D * 1024D * 1024D)) + " GB";
    }
}
