/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.clipboard;


public class Clipboard {
	public final static int CUT=1;
	public final static int COPY=2;
	private ClipboardItem item;

	/**
	 * Retrieve the item.
	 *
	 * @return the item
	 */
	public ClipboardItem getItem() {
		return item;
	}

	/**
	 * Modify the item.
	 *
	 * @param anItem the item to set
	 */
	public void setItem(ClipboardItem anItem) {
		item = anItem;
	}

	public boolean hasFolderOrFileItem(){
		if( item !=null )
			return item.isFileOrFolder();
		return false;
	}

	public boolean hasFileItem(){
		if( item !=null )
			return item.isFile();
		return false;
	}

	public boolean hasUserItem(){
		if( item !=null )
			return item.isUser();
		return false;
	}

	public boolean isEmpty(){
		return item == null;
	}

	public void clear(){
		item = null;
	}
}
