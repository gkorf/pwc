/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest.resource;


public class RestResourceWrapper extends RestResource{
	FolderResource resource;
	
	public RestResourceWrapper(FolderResource resource) {
		super(resource.getUri());
		this.resource=resource;
	}
	
	
	/**
	 * Modify the resource.
	 *
	 * @param resource the resource to set
	 */
	public void setResource(FolderResource resource) {
		this.resource = resource;
	}
	/**
	 * Retrieve the resource.
	 *
	 * @return the resource
	 */
	public FolderResource getResource() {
		return resource;
	}
	
	@Override
	public void createFromJSON(String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLastModifiedSince() {
		// TODO Auto-generated method stub
		return null;
	}

}
