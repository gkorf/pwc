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

import gr.grnet.pithos.web.client.rest.MultipleGetCommand;
import gr.grnet.pithos.web.client.rest.MultipleGetCommand.Cached;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.TreeItem;


public class OtherUserResource extends RestResource{
	public OtherUserResource(String aUri) {
		super(aUri);
	}

	String username;
	List<String> filePaths = new LinkedList<String>();
	List<String> subfolderPaths = new LinkedList<String>();
	List<FolderResource> folders = new ArrayList<FolderResource>();
	List<FileResource> files = new ArrayList<FileResource>();

	private boolean filesExpanded=false;
	/**
	 * Retrieve the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Modify the username.
	 *
	 * @param aUsername the username to set
	 */
	public void setUsername(String aUsername) {
		username = aUsername;
	}

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
	 * @param newFiles the files to set
	 */
	public void setFilePaths(List<String> newFiles) {
		filePaths = newFiles;
	}

	/**
	 * Retrieve the subfolders.
	 *
	 * @return the subfolders
	 */
	public List<String> getSubfolderPaths() {
		return subfolderPaths;
	}

	/**
	 * Modify the subfolders.
	 *
	 * @param subfolders the subfolders to set
	 */
	public void setSubfolderPaths(List<String> subfolders) {
		subfolderPaths = subfolders;
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
						Boolean fshared = unmarshallBoolean(fo,"shared");
						boolean fversioned = unmarshallBoolean(fo,"versioned");
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
						String fpath = unmarshallString(fo, "path");
						fpath = URL.decodeComponent(fpath);
						FileResource fs = new FileResource(furi);
						fs.setName(fname);
						fs.setPath(fpath);
						fs.setOwner(fowner);
						fs.setVersion(fversion);
						fs.setContentLength(fsize);
						fs.setDeleted(fdeleted);
						fs.setCreationDate(fcreationDate);
						fs.setModificationDate(fmodificationDate);
						fs.setShared(fshared);
						fs.setVersioned(fversioned);
						fs.setContentType(fcontent);
						files.add(fs);
					}
				}
		}
	}

	@Override
	public String getName(){
		String[] names = uri.split("/");
		return names[names.length -1];
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}

	public MultipleGetCommand.Cached[] getFileCache(){
		if(getFilePaths().size() != getFiles().size()){
			GWT.log("MISMATCH IN PATH AND FILES SIZE", null);
			return null;
		}
		if(!filesExpanded)
			return null;
		MultipleGetCommand.Cached[] result = new MultipleGetCommand.Cached[getFilePaths().size()];
		for(int i=0; i<getFiles().size();i++){
			FileResource r = getFiles().get(i);
			Cached c = new Cached();
			c.cache=r;
			c.uri=r.uri;
			result[i] = c;
		}
		return result;
	}

	public void setFilesExpanded(boolean newFilesExpanded) {
		filesExpanded = newFilesExpanded;
	}

	@Override
	public String constructUri(TreeItem treeItem, String path){
		String constructedUri = "Files/others/"+ getName();
		return constructedUri;
	}
}
