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

package gr.grnet.pithos.web.client.tagtree;

import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Resource;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class Tag extends Resource {
    /*
     * The name of the tag.
     */
    private String name = null;

    private Set<File> files = new LinkedHashSet<File>();

    public Tag() {};

    public Tag(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

//    public void populate(Response response) {
//        String header = response.getHeader("Last-Modified");
//        if (header != null)
//            lastModified = DateTimeFormat.getFormat(PredefinedFormat.RFC_2822).parse(header);
//
//        header = response.getHeader("X-Container-Bytes-Used");
//        if (header != null)
//            bytesUsed = Long.valueOf(header);
//
//        subfolders.clear(); //This is necessary in case we update a pre-existing Tag so that stale subfolders won't show up
//        files.clear();
//        JSONValue json = JSONParser.parseStrict(response.getText());
//        JSONArray array = json.isArray();
//        if (array != null) {
//            for (int i=0; i<array.size(); i++) {
//                JSONObject o = array.get(i).isObject();
//                if (o != null) {
//                    String contentType = unmarshallString(o, "content_type");
//                    if (o.containsKey("subdir") || (contentType != null && (contentType.startsWith("application/directory") || contentType.startsWith("application/folder")))) {
//                        Tag f = new Tag();
//                        f.populate(this, o, container);
//                        subfolders.add(f);
//                    }
//                    else {
//                        File file = new File();
//                        file.populate(this, o, container);
//                        files.add(file);
//                    }
//                }
//            }
//        }
//    }
//
//    public void populate(Tag parent, JSONObject o, String aContainer) {
//        this.parent = parent;
//        String path = null;
//        if (o.containsKey("subdir")) {
//            path = unmarshallString(o, "subdir");
//        }
//        else {
//            path = unmarshallString(o, "name");
//            lastModified = unmarshallDate(o, "last_modified");
//        }
//        if (path.endsWith("/"))
//            path = path.substring(0, path.length() - 1);
//        if (path.contains("/"))
//            name = path.substring(path.lastIndexOf("/") + 1, path.length()); //strip the prefix
//        else
//            name = path;
//        if (aContainer != null) {
//            container = aContainer;
//            prefix = path;
//        }
//        else {
//            container = name;
//            prefix = "";
//        }
//    }
//
//    public static Tag createFromResponse(Response response, Tag result) {
//        Tag f = null;
//        if (result == null)
//            f = new Tag();
//        else
//            f = result;
//
//        f.populate(response);
//        return f;
//    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Tag) {
            Tag o = (Tag) other;
            return name.equals(o.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public Set<File> getFiles() {
        return files;
    }

	@Override
	public Date getLastModified() {
		return null;
	}
}
