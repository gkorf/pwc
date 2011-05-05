/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.rest.GetCommand;
import gr.grnet.pithos.web.client.rest.resource.UserResource;
import gr.grnet.pithos.web.client.rest.resource.UserSearchResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;


/**
 * This command manages usernames and the corresponding user's Full Name
 * along with the HashMap collection in the GSS.java class
 *
 *
 */
public class GetUserCommand implements Command{

	/**
	 * User's username e.g johndoe@somewhere.com
	 */
	private String userName;

	public GetUserCommand(String _userName){
		userName = _userName;
	}

	@Override
	public void execute() {
		String path = GSS.get().getApiPath() + "users/" + userName; 
		GetCommand<UserSearchResource> gg = new GetCommand<UserSearchResource>(UserSearchResource.class,
					path, false ,null) {
			@Override
			public void onComplete() {
				final UserSearchResource result = getResult();
				for (UserResource user : result.getUsers()){
					String username = user.getUsername();
					String _userFullName = user.getName();
					GSS.get().putUserToMap(username, _userFullName);
				}
			}
			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				GSS.get().displayError("Unable to fetch user's full name from the given username " + userName);
			}
		};
		DeferredCommand.addCommand(gg);

	}

}
