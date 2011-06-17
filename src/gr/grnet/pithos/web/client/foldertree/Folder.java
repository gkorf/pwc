/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.foldertree;

import java.util.ArrayList;
import java.util.List;

public class Folder {
    private String uri;

    private String name;

    private List<Folder> subfolders = new ArrayList<Folder>();

    public Folder(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public List<Folder> getSubfolders() {
        return subfolders;
    }

    public void setSubfolders(List<Folder> subfolders) {
        this.subfolders = subfolders;
    }
}
