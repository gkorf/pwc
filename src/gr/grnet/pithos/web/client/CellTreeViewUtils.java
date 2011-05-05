/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.CellTreeView.RefreshHandler;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.TreeNode;


public class CellTreeViewUtils {
	CellTree tree;
	/**
	 * 
	 */
	public CellTreeViewUtils(CellTree tree) {
		this.tree = tree;
	}
	
	void refreshNodeContainingResource(RestResource r){
		TreeNode node = tree.getRootTreeNode();
		refreshNodeContainingResource(node,r);
	}
	
	void refreshNodeContainingResource(String  uri){
		TreeNode node = tree.getRootTreeNode();
		refreshNodeContainingResource(node,uri);
	}
	
	private void refreshNodeContainingResource(TreeNode node, RestResource resource){
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
			if(node.getChildValue(i).equals(resource)){
				if(node.getChildValue(i) instanceof RestResourceWrapper && ((RestResourceWrapper)node.getChildValue(i)).getResource().getFolders().size()==0)
					return;
				node.setChildOpen(i, false, true);
				node.setChildOpen(i, true, true);
				return;
			}
			else if(node.isChildOpen(i)){
				TreeNode n = node.setChildOpen(i, true);
				if(n!=null)
					refreshNodeContainingResource(n,resource);
			}
		}
		
	}
	
	void refreshNodeContainingResource(TreeNode node, String uri){
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
			if(node.isChildOpen(i)){
				if(node.getChildValue(i) instanceof RestResource && ((RestResource)node.getChildValue(i)).getUri().equals(uri)){
					if(node.getChildValue(i) instanceof RestResourceWrapper && ((RestResourceWrapper)node.getChildValue(i)).getResource().getFolders().size()==0)
						return;
					node.setChildOpen(i, false, true);
					node.setChildOpen(i, true, true);
					return;
				}
				else{
					TreeNode n = node.setChildOpen(i, true);
					if(n!=null)
						refreshNodeContainingResource(n,uri);
				}
			}
		}
	}
	public void openNodeContainingResource(RestResource resource){
		TreeNode node = tree.getRootTreeNode();
		openNodeContainingResource(node,resource);
	}
	private void openNodeContainingResource(TreeNode node, RestResource resource){
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
			
				if(node.getChildValue(i).equals(resource)){
					if(node.getChildValue(i) instanceof RestResourceWrapper && ((RestResourceWrapper)node.getChildValue(i)).getResource().getFolders().size()==0)
						return;
					node.setChildOpen(i, true, true);
					return;
				}
				else{
					if(node.isChildOpen(i)){
						TreeNode n = node.setChildOpen(i, true);
						if(n!=null)
							openNodeContainingResource(n,resource);
					}
				}
			
		}
	}
	
	public void openNodeContainingResource(RestResource resource, RefreshHandler handler){
		TreeNode node = tree.getRootTreeNode();
		openNodeContainingResource(node,resource,handler);
	}
	private void openNodeContainingResource(TreeNode node, RestResource resource, RefreshHandler handler){
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
				if(node.getChildValue(i).equals(resource)){
					if(node.getChildValue(i) instanceof RestResourceWrapper && ((RestResourceWrapper)node.getChildValue(i)).getResource().getFolders().size()==0)
						return;
					//node.setChildOpen(i, false, true);
					node.setChildOpen(i, true, true);
					handler.onRefresh();
					return;
				}
				else{
					if(node.isChildOpen(i)){
						TreeNode n = node.setChildOpen(i, true);
						if(n!=null)
							openNodeContainingResource(n,resource, handler);
					}
				}
			
		}
	}
	
	
	
	public boolean doesNodeContainsResource(TreeNode node, RestResource resource){
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
			if(node.isChildOpen(i)){
				if(node.getChildValue(i) instanceof RestResource && ((RestResource)node.getChildValue(i)).equals(resource)){
					return true;
				}
				else if(node.isChildOpen(i)){
					TreeNode n = node.setChildOpen(i, true);
					if(n!=null)
						return doesNodeContainsResource(n,resource);
				}
			}
		}
		return false;
	}
	
	public boolean doesNodeContainsResource(TreeNode node, String resource){
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
			
				if(node.getChildValue(i) instanceof RestResource && ((RestResource)node.getChildValue(i)).getUri().equals(resource)){
					return true;
				}
				else if(node.isChildOpen(i)){
					TreeNode n = node.setChildOpen(i, true);
					if(n!=null)
						return doesNodeContainsResource(n,resource);
				}
			
		}
		return false;
	}
	
	public TreeNode getNodeContainingResource(TreeNode node, RestResource resource){
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
			
				if(node.getChildValue(i) instanceof RestResource && ((RestResource)node.getChildValue(i)).getUri().equals(resource.getUri())){
					return node;
				}
				else if(node.isChildOpen(i)){
					TreeNode n = node.setChildOpen(i, true);
					if(n!=null)
						return getNodeContainingResource(n,resource);
				}
			
		}
		return null;
	}
	
	public TreeNode getNodeContainingResource(TreeNode node, String resource){
		if(node==null)
			return null;
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
			
				if(node.getChildValue(i) instanceof RestResource && ((RestResource)node.getChildValue(i)).getUri().equals(resource)){
					return node;
				}
				else if(node.isChildOpen(i)){
					TreeNode n = node.setChildOpen(i, true);
					if(n!=null){
						TreeNode result = getNodeContainingResource2(n,resource);
						if(result !=null)
							return result;
					}
				}
			
		}
		return null;
	}
	
	public TreeNode getNodeContainingResource2(TreeNode node, String resource){
		if(node==null)
			return null;
		int count = node.getChildCount();
		for(int i=0;i<count;i++){
				if(node.getChildValue(i) instanceof RestResource && ((RestResource)node.getChildValue(i)).getUri().equals(resource)){
					return node.setChildOpen(i, node.isChildOpen(i));
				}
				else if(node.isChildOpen(i)){
					TreeNode n = node.setChildOpen(i, true);
					if(n!=null){
						TreeNode result = getNodeContainingResource2(n,resource);
						if(result !=null)
							return result;
					}
				}
			
		}
		return null;
	}
	public boolean doesSharedNodeContainsResource( String resource){
		if(tree.getRootTreeNode().isChildOpen(2)){
			TreeNode node = tree.getRootTreeNode().setChildOpen(2, true);
			return doesNodeContainsResource(node, resource);
		}
		return false;
	}
	
	public boolean doesSharedNodeContainsResourceIn1stLevel( String resource){
		if(tree.getRootTreeNode().isChildOpen(2)){
			TreeNode node = tree.getRootTreeNode().setChildOpen(2, true);
			int count = node.getChildCount();
			for(int i=0;i<count;i++){
				
					if(node.getChildValue(i) instanceof RestResource && ((RestResource)node.getChildValue(i)).getUri().equals(resource)){
						return true;
					}
				
			}
			return false;
		}
		return false;
	}
	
	public boolean doesSharedNodeContainsResourceIn2ndLevel( String resource){
		if(tree.getRootTreeNode().isChildOpen(2)){
			TreeNode node = tree.getRootTreeNode().setChildOpen(2, true);
			int count = node.getChildCount();
			for(int i=0;i<count;i++){
				if(node.isChildOpen(i)){
					TreeNode child = node.setChildOpen(i, true);
					for(int j=0;j<child.getChildCount();j++){
						if(child.getChildValue(j) instanceof RestResource && ((RestResource)child.getChildValue(j)).getUri().equals(resource)){
							return true;
						}
					}
				}
					
				
			}
			return false;
		}
		return false;
	}
	
	public boolean doesRootNodeContainsResource( String resource){
		if(tree.getRootTreeNode().isChildOpen(0)){
			TreeNode node = tree.getRootTreeNode().setChildOpen(0, true);
			return doesNodeContainsResource(node, resource);
		}
		return false;
	}
	
	public boolean doesSharedNodeContainsResource( RestResource resource){
		if(tree.getRootTreeNode().isChildOpen(2)){
			TreeNode node = tree.getRootTreeNode().setChildOpen(2, true);
			return doesNodeContainsResource(node, resource);
		}
		return false;
	}
	
	public boolean doesRootNodeContainsResource( RestResource resource){
		if(tree.getRootTreeNode().isChildOpen(0)){
			TreeNode node = tree.getRootTreeNode().setChildOpen(0, true);
			return doesNodeContainsResource(node, resource);
		}
		return false;
	}
}
