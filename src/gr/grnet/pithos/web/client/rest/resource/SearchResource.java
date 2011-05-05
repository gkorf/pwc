/*
 *  Copyright (c) 2011 Greek Research and Technology Network
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
