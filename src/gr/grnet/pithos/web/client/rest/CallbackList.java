/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.rest;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;



public class CallbackList<T> implements AsyncCallback<T>{
	List<T> result = new ArrayList();
	boolean erroneous = false;

	@Override
	public void onFailure(Throwable arg0) {
		GWT.log("Error in callback list", arg0);
		erroneous = true;

	}


	@Override
	public void onSuccess(T arg0) {
		result.add(arg0);
	}



	/**
	 * Retrieve the result.
	 *
	 * @return the result
	 */
	public List<T> getResult() {
		return result;
	}



	/**
	 * Retrieve the erroneous.
	 *
	 * @return the erroneous
	 */
	public boolean isErroneous() {
		return erroneous;
	}





}
