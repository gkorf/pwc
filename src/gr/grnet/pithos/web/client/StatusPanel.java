/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import gr.grnet.pithos.web.client.foldertree.AccountResource;
import gr.grnet.pithos.web.client.rest.GetCommand;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.QuotaHolder;
import gr.grnet.pithos.web.client.rest.resource.UserResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

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

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                AccountResource account = GSS.get().getAccount();
                displayStats(account);
            }
        });
	}

	/**
	 * Refresh the widget with the provided statistics.
	 */
	private void displayStats(AccountResource account) {
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
		lastLoginLabel.setHTML(formatter.format(account.getLastLogin()));
		currentLoginLabel.setHTML(formatter.format(account.getCurrentLogin()));
	}

	/**
	 * Requests updated quota information from the server and refreshes
	 * the display.
	 */
	public void updateStats() {
		final GSS app = GSS.get();
        String path = app.getApiPath() + app.getUsername();
        GetRequest<AccountResource> getAccount = new GetRequest<AccountResource>(AccountResource.class, path) {
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
