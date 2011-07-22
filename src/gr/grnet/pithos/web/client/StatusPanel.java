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

import com.google.gwt.core.client.Scheduler;
import gr.grnet.pithos.web.client.foldertree.AccountResource;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import java.util.Date;

/**
 * The panel that displays a status bar with quota information.
 */
public class StatusPanel extends Composite {
	public static final boolean DONE = false;
	private HTML fileCountLabel = new HTML("");
	private HTML fileSizeLabel = new HTML("");
	private HTML quotaIcon = new HTML("");
	private HTML quotaLabel = new HTML("");
	private HTML lastLoginLabel = new HTML("");
	private HTML currentLoginLabel = new HTML("");
	private HTML currentlyShowingLabel = new HTML("");

	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images extends ClientBundle {

		@Source("gr/grnet/pithos/resources/windowlist.png")
		ImageResource totalFiles();

		@Source("gr/grnet/pithos/resources/database.png")
		ImageResource totalSize();

		@Source("gr/grnet/pithos/resources/redled.png")
		ImageResource redSize();

		@Source("gr/grnet/pithos/resources/greenled.png")
		ImageResource greenSize();

		@Source("gr/grnet/pithos/resources/yellowled.png")
		ImageResource yellowSize();

		@Source("gr/grnet/pithos/resources/xclock.png")
		ImageResource lastLogin();		
	}

	private final Images images;

	/**
	 * The constructor of the status panel.
	 *
	 * @param theImages the supplied images
	 */
	public StatusPanel(Images theImages) {
		images = theImages;
		HorizontalPanel outer = new HorizontalPanel();
		outer.setWidth("100%");
		outer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		HorizontalPanel left = new HorizontalPanel();
		left.setSpacing(8);
		HorizontalPanel middle = new HorizontalPanel();
		middle.setSpacing(8);
		HorizontalPanel right = new HorizontalPanel();
		right.setSpacing(8);
		outer.add(left);
		outer.add(middle);
		outer.add(right);
		left.add(new HTML("<b>Totals:</b> "));
		left.add(AbstractImagePrototype.create(images.totalFiles()).createImage());
		left.add(fileCountLabel);
		left.add(AbstractImagePrototype.create(images.totalSize()).createImage());
		left.add(fileSizeLabel);
		quotaIcon.setHTML(AbstractImagePrototype.create(images.greenSize()).getHTML());
		left.add(quotaIcon);
		left.add(quotaLabel);
		middle.add(AbstractImagePrototype.create(images.lastLogin()).createImage());
		middle.add(new HTML("<b>Last login:</b> "));
		middle.add(lastLoginLabel);
		middle.add(new HTML("<b>\u0387 Current session login:</b> "));
		middle.add(currentLoginLabel);
		right.add(currentlyShowingLabel);
		outer.setStyleName("statusbar-inner");
		left.setStyleName("statusbar-inner");
		middle.setStyleName("statusbar-inner");
		right.setStyleName("statusbar-inner");
		outer.setCellHorizontalAlignment(right, HasHorizontalAlignment.ALIGN_RIGHT);

		initWidget(outer);
	}

	/**
	 * Refresh the widget with the provided statistics.
	 */
	public void displayStats(AccountResource account) {
		if (account.getNumberOfObjects() == 1)
			fileCountLabel.setHTML("1 object");
		else
			fileCountLabel.setHTML(account.getNumberOfObjects() + " objects");
		fileSizeLabel.setHTML(account.getFileSizeAsString() + " used");
		long pc = (long) ((double) account.getBytesRemaining()/(account.getBytesRemaining() + account.getBytesUsed()) + 0.5);
		if (pc < 10) {
			quotaIcon.setHTML(AbstractImagePrototype.create(images.redSize()).getHTML());
			quotaLabel.setHTML(account.getQuotaLeftAsString() + " free");
		} else if(pc<20) {
			quotaIcon.setHTML(AbstractImagePrototype.create(images.yellowSize()).getHTML());
			quotaLabel.setHTML(account.getQuotaLeftAsString() +" free");
		} else {
			quotaIcon.setHTML(AbstractImagePrototype.create(images.greenSize()).getHTML());
			quotaLabel.setHTML(account.getQuotaLeftAsString() +" free");
		}
		final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");
        Date login = account.getLastLogin();
		lastLoginLabel.setHTML(login == null ? "" : formatter.format(login));

        login = account.getCurrentLogin();
		currentLoginLabel.setHTML(login == null ? "" : formatter.format(login));
	}

	/**
	 * Requests updated quota information from the server and refreshes
	 * the display.
	 */
    //TODO: This should not be done here
	public void updateStats() {
		final Pithos app = Pithos.get();
        GetRequest<AccountResource> getAccount = new GetRequest<AccountResource>(AccountResource.class, app.getApiPath(), app.getUsername(), "") {
            @Override
            public void onSuccess(AccountResource result) {
                displayStats(result);
            }

            @Override
            public void onError(Throwable t) {
                if(t instanceof RestException)
                    app.displayError("Unable to fetch quota:" +
                                ((RestException)t).getHttpStatusText());
                else
                    app.displayError("System error fetching quota:" +
                                t.getMessage());
                GWT.log("ERR", t);
            }
        };
        getAccount.setHeader("X-Auth-Token", app.getToken());
        Scheduler.get().scheduleDeferred(getAccount);
	}

	/**
	 * Displays the statistics for the current folder.
	 *
	 * @param text the statistics to display
	 */
	public void updateCurrentlyShowing(String text) {
		if (text == null)
			currentlyShowingLabel.setText("");
		else
			currentlyShowingLabel.setHTML(" <b>Showing:</b> " + text);
	}

}
