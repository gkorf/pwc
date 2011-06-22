/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.rest.resource.UserResource;

import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * The panel that displays a status bar with quota information.
 */
public class UserDetailsPanel extends Composite {
	public static final boolean DONE = false;

	/**
	 * The label that displays user information.
	 */
	private HTML userInfoLabel;

	/**
	 * The constructor of the user details panel.
	 */
	public UserDetailsPanel() {
		final HorizontalPanel outer = new HorizontalPanel();
		outer.setSpacing(8);
		userInfoLabel = new HTML("&nbsp;");
		outer.add(userInfoLabel);
		outer.setCellHorizontalAlignment(userInfoLabel, HasHorizontalAlignment.ALIGN_RIGHT);
		outer.setStyleName("statusbar-inner");

		initWidget(outer);

//		DeferredCommand.addCommand(new IncrementalCommand() {
//
//			@Override
//			public boolean execute() {
//				return displayUserInfo();
//			}
//		});
	}

	/**
	 * Display the user information on the panel.
	 *
	 * @return true if the work has been carried out successfully
	 */
	protected boolean displayUserInfo() {
		UserResource user = GSS.get().getCurrentUserResource();
		if (user == null)
			return !DONE;
		userInfoLabel.setHTML("<b>" + user.getName() + " \u0387 " + user.getUsername() + "</b>");
		GSS.get().putUserToMap(user.getUsername(), user.getName());
		return DONE;
	}

}
