/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client.animation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;


public class FadeOut extends Animation {
	Widget widget;

	int initialOpacity = 100;
	double currOpacity = 100;

	public FadeOut(Widget aWidget){
		widget = aWidget;
	}

	@Override
	protected void onUpdate(double progress) {
		if(currOpacity>0.0){
			progress = 1.0 - progress;
			currOpacity = initialOpacity*progress;
			DOM.setStyleAttribute(widget.getElement(), "opacity", ""+new Double(currOpacity/100d));
			//required for ie to work
			//Disabled because IE has bugs rendering non-opaque objects
			//int opacityToSet = new Double(currOpacity).intValue();
			//DOM.setStyleAttribute(widget.getElement(), "filter", "alpha(opacity="+currOpacity+")");
		}
	}

	/*
	protected void onComplete() {
		super.onComplete();
		hpanel.clear();
		html = new HTML("&nbsp;");
		hpanel.getParent().setVisible(false);
	}
	*/

}
