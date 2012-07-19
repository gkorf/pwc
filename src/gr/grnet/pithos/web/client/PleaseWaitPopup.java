package gr.grnet.pithos.web.client;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

public class PleaseWaitPopup extends PopupPanel {

	public PleaseWaitPopup() {
		setAnimationEnabled(true);
		setGlassEnabled(true);
		setModal(true);
		add(new Label("Please wait ..."));
	}
}
