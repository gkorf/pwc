/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'loading' indicator widget implementation.
 */
public class LoadingIndicator extends Composite {
	public static final String DEFAULT_MESSAGE="Please Wait";
	
	HTML messageLabel;
	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ClientBundle {
		@Source("gr/grnet/pithos/resources/ajax-loader.gif")
		ImageResource loading();
	}

	/**
	 * The widget's constructor that creates a spinning indicator image.
	 */
	public LoadingIndicator(Images images) {
		VerticalPanel vp = new VerticalPanel();
		//vp.setHorizontalAlignment(HorizontalAlignmentConstant.CENTER);
		HTML inner = new HTML(AbstractImagePrototype.create(images.loading()).getHTML());
		vp.add(inner);
		vp.add(messageLabel = new HTML(DEFAULT_MESSAGE) );
		vp.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);
		vp.setCellHorizontalAlignment(messageLabel, HasHorizontalAlignment.ALIGN_CENTER);
		initWidget(vp);
	}
	
	
	/**
	 * Modify the message.
	 *
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		messageLabel.setHTML(message);
	}
	
	public void clearMessage(){
		setMessage(DEFAULT_MESSAGE);
	}
	
	public void show(String msg){
		if(msg==null)
			setMessage(DEFAULT_MESSAGE);
		else
			setMessage(msg);
		this.setVisible(true);
	}
	
	public void hide(){
		setMessage(DEFAULT_MESSAGE);
		this.setVisible(false);
	}
}
