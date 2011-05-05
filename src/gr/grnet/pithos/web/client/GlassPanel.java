/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;


public class GlassPanel extends Composite{

	public GlassPanel(){
		SimplePanel mySimplePanel = new SimplePanel();
	    initWidget(mySimplePanel);
	    setStyleName("gwt-GlassPanel");
	    setWidth("100%");
	    setHeight("100%");
	}
}
