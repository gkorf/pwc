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
import com.google.gwt.user.client.ui.TreeItem;


public class TrashResource extends RestResource{

	public TrashResource(String aUri) {
		super(aUri);
	}

	List<String> filePaths = new LinkedList<String>();
	List<String> subfolderPaths = new LinkedList<String>();
	List<FolderResource> folders = new ArrayList<FolderResource>();
	List<FileResource> files = new ArrayList<FileResource>();

	/**
	 * Retrieve the file paths.
	 *
	 * @return the file paths
	 */
	public List<String> getFilePaths() {
		return filePaths;
	}

	/**
	 * Modify the file paths.
	 *
	 * @param newFilePaths the file paths to set
	 */
	public void setFilePaths(List<String> newFilePaths) {
		filePaths = newFilePaths;
	}

	/**
	 * Retrieve the subfolder paths.
	 *
	 * @return the subfolder paths
	 */
	public List<String> getSubfolderPaths() {
		return subfolderPaths;
	}

	/**
	 * Modify the subfolder paths.
	 *
	 * @param newSubfolderPaths the subfolders to set
	 */
	public void setSubfolderPaths(List<String> newSubfolderPaths) {
		subfolderPaths = newSubfolderPaths;
	}

	/**
	 * Retrieve the folders.
	 *
	 * @return the folders
	 */
	public List<FolderResource> getFolders() {
		return folders;
	}

	/**
	 * Modify the folders.
	 *
	 * @param newFolders the folders to set
	 */
	public void setFolders(List<FolderResource> newFolders) {
		folders = newFolders;
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
		JSONObject json = (JSONObject) JSONParser.parse(text);
		if (json.get("folders") != null) {
			JSONArray subs = json.get("folders").isArray();
			if (subs != null)
				for (int i = 0; i < subs.size(); i++) {
					JSONObject so = subs.get(i).isObject();
					if (so != null) {
						String subUri = unmarshallString(so, "uri");
						String subName = unmarshallString(so, "name");
						subName = URL.decodeComponent(subName);
						if (subUri != null && subName != null) {
							if (!subUri.endsWith("/"))
								subUri = subUri + "/";
							FolderResource sub = new FolderResource(subUri);
							sub.setName(subName);
							sub.setNeedsExpanding(true);
							folders.add(sub);
							subfolderPaths.add(subUri);
						}
					}
				}
		}
		if (json.get("files") != null) {
			JSONArray subs = json.get("files").isArray();
			if (subs != null)
				for (int i = 0; i < subs.size(); i++) {
					JSONObject fo = subs.get(i).isObject();
					if (fo != null) {
						String fname = unmarshallString(fo, "name");
						String fowner = unmarshallString(fo, "owner");
						String fcontent = unmarshallString(fo, "content");
						Boolean fshared = unmarshallBoolean(fo, "shared");						
						Boolean fversioned = unmarshallBoolean(fo, "versioned");	
						String fpath = unmarshallString(fo, "path");						
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
						fs.setPath(fpath);
						fs.setCreationDate(fcreationDate);
						fs.setModificationDate(fmodificationDate);
						fs.setContentType(fcontent);
						fs.setShared(fshared);
						fs.setVersioned(fversioned);
						files.add(fs);
					}
				}
		}
	}

	public List<FolderResource> getTrashedFolders(){
		List<FolderResource> res = new ArrayList<FolderResource>();
		for(String s : subfolderPaths){
			String[] pathElements =  s.split("/");
			FolderResource tr = new FolderResource(s);
			tr.setName(URL.decodeComponent(pathElements[pathElements.length-1]));
			res.add(tr);
		}
		return res;
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}
	@Override
	public String constructUri(TreeItem treeItem, String path){
		String constructedUri = "Files/"+ getUri().substring(path.lastIndexOf("/")+1);
		if (!constructedUri.endsWith("/"))
			constructedUri += "/";
		return constructedUri;
	}
}
