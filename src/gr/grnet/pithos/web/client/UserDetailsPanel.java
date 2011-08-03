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
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.rest.resource.UserResource;

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

    private Pithos app;

	/**
	 * The constructor of the user details panel.
	 */
	public UserDetailsPanel(Pithos _app) {
        app = _app;
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
		UserResource user = app.getCurrentUserResource();
		if (user == null)
			return !DONE;
		userInfoLabel.setHTML("<b>" + user.getName() + " \u0387 " + user.getUsername() + "</b>");
		app.putUserToMap(user.getUsername(), user.getName());
		return DONE;
	}

}
