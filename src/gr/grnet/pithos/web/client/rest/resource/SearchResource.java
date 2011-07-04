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
package gr.grnet.pithos.web.client.rest.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class SearchResource extends RestResource {
	int size;
	public SearchResource(String aUri) {
		super(aUri);
	}

	List<FileResource> files = new ArrayList<FileResource>();
	List<String> filePaths = new LinkedList<String>();

	/**
	 * Retrieve the files.
	 *
	 * @return the files
	 */
	public List<String> getFilePaths() {
		return filePaths;
	}

	/**
	 * Modify the files.
	 *
	 * @param newFilePaths the files to set
	 */
	public void setFilePaths(List<String> newFilePaths) {
		filePaths = newFilePaths;
	}

	/**
	 * Retrieve the files.
	 *
	 * @return the files
	 */
	public List<FileResource> getFiles() {
		return files;
	}

	/**
	 * Modify the files.
	 *
	 * @param newFiles the files to set
	 */
	public void setFiles(List<FileResource> newFiles) {
		files = newFiles;
	}

	@Override
	public void createFromJSON(String text) {
		JSONArray subs = JSONParser.parse(text).isArray();
		if (subs != null)
			for (int i = 0; i < subs.size(); i++) {
				JSONObject fo = subs.get(i).isObject();
				if (fo != null) {
					if(i==0&&unmarshallInt(fo, "length")!=-1){
						setSize(unmarshallInt(fo, "length"));
					}
					else{
					String fname = unmarshallString(fo, "name");
					String fowner = unmarshallString(fo, "owner");
					String fcontent = unmarshallString(fo, "content");
					String fpath = unmarshallString(fo, "path");
					Boolean fshared = unmarshallBoolean(fo,"shared");
					boolean fversioned = unmarshallBoolean(fo,"versioned");
					fpath = URL.decodeComponent(fpath);
					Integer fversion = null;
					if (fo.get("version") != null)
						fversion = new Integer(fo.get("version").toString());
					boolean fdeleted = unmarshallBoolean(fo, "deleted");
					Date fcreationDate = null;
					if (fo.get("creationDate") != null)
						fcreationDate = new Date(new Long(fo.get("creationDate").toString()));
					Date fmodificationDate = null;
					if (fo.get("modificationDate") != null)
						fmodificationDate = new Date(new Long(fo.get("modificationDate").toString()));
					String furi = unmarshallString(fo,"uri");
					Long fsize = 0L;
					if(fo.get("size") != null)
						fsize = new Long(fo.get("size").toString());
					filePaths.add(furi);
					FileResource fs = new FileResource(furi);
					fs.setName(fname);
					fs.setOwner(fowner);
					fs.setVersion(fversion);
					fs.setContentLength(fsize);
					fs.setDeleted(fdeleted);
					fs.setCreationDate(fcreationDate);
					fs.setModificationDate(fmodificationDate);
					fs.setContentType(fcontent);
					fs.setPath(fpath);
					fs.setShared(fshared);
					fs.setVersioned(fversioned);
					files.add(fs);
					}
				}
			}
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
	
	
	/**
	 * Retrieve the size.
	 *
	 * @return the size
	 */
	public int getSize() {
		return size;
	}
	
	
	/**
	 * Modify the size.
	 *
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}
}
