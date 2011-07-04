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
