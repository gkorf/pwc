/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.animation.FadeIn;
import gr.grnet.pithos.web.client.animation.FadeOut;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * A panel that displays various system messages.
 */
public class MessagePanel extends Composite {
	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images extends ClientBundle {
		@Source("gr/grnet/pithos/resources/messagebox_info.png")
		ImageResource info();

		@Source("gr/grnet/pithos/resources/messagebox_warning.png")
		ImageResource warn();

		@Source("gr/grnet/pithos/resources/messagebox_critical.png")
		ImageResource error();
	}

	/**
	 * The widget's images.
	 */
	public static Images images;

	/**
	 * The system message to be displayed.
	 */
	private HTML message = new HTML("&nbsp;");

	/**
	 * A link to clear the displayed message.
	 */
	private HTML clearMessageLink = new HTML("<a class='pithos-clearMessage' href='javascript:;'>Clear</a>");

	/**
	 * The panel that contains the messages.
	 */
	private HorizontalPanel inner = new HorizontalPanel();

	/**
	 * The panel that enables special effects for this widget.
	 */
	private SimplePanel simplePanel = new SimplePanel();

	/**
	 * The widget's constructor.
	 *
	 * @param newImages a bundle that provides the images for this widget
	 */
	public MessagePanel(final Images newImages) {
		images = newImages;
		buildPanel();
		simplePanel.setStyleName("effectPanel");
		inner.setStyleName("effectPanel-inner");
		DOM.setStyleAttribute(simplePanel.getElement(), "zoom", "1");
		simplePanel.add(inner);
		initWidget(simplePanel);
	}

	/**
	 * Build the panel that contains the icon, the message and the 'clear' link.
	 */
	private void buildPanel() {
		inner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		inner.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		inner.setSpacing(4);
		inner.add(message);
		inner.add(clearMessageLink);
		inner.setCellVerticalAlignment(message, HasVerticalAlignment.ALIGN_MIDDLE);
		clearMessageLink.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				FadeOut anim = new FadeOut(simplePanel){
					@Override
					protected void onComplete() {
						super.onComplete();
						hideMessage();
					}
				};
				anim.run(500);
			}
		});
	}

	/**
	 * Display an error message.
	 *
	 * @param msg the message to display
	 */
	public void displayError(final String msg) {
		GWT.log(msg, null);
		message = new HTML("<table class='pithos-errorMessage'><tr><td>" + AbstractImagePrototype.create(images.error()).getHTML() + "</td><td>" + msg + "</td></tr></table>");
		message.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				FadeOut anim = new FadeOut(simplePanel){

					@Override
					protected void onComplete() {
						super.onComplete();
						hideMessage();
					}
				};
				anim.run(500);
			}
		});
		buildPanel();
		setVisible(true);
		FadeIn anim = new FadeIn(simplePanel);
		anim.run(500);
	}

	/**
	 * Display a warning message.
	 *
	 * @param msg the message to display
	 */
	public void displayWarning(final String msg) {
		message = new HTML("<table class='pithos-warnMessage'><tr><td>" + AbstractImagePrototype.create(images.warn()).getHTML() + "</td><td>" + msg + "</td></tr></table>");
		message.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				FadeOut anim = new FadeOut(simplePanel){

					@Override
					protected void onComplete() {
						super.onComplete();
						hideMessage();
					}
				};
				anim.run(500);
			}
		});

		buildPanel();
		setVisible(true);
		FadeIn anim = new FadeIn(simplePanel);
		anim.run(500);
	}

	/**
	 * Display an informational message.
	 *
	 * @param msg the message to display
	 */
	public void displayInformation(final String msg) {
		message = new HTML("<table class='pithos-infoMessage'><tr><td>" + AbstractImagePrototype.create(images.info()).getHTML() + "</td><td>" + msg + "</td></tr></table>");
		message.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				FadeOut anim = new FadeOut(simplePanel){

					@Override
					protected void onComplete() {
						super.onComplete();
						hideMessage();
					}
				};
				anim.run(500);
			}
		});

		buildPanel();
		setVisible(true);
		FadeIn anim = new FadeIn(simplePanel);
		anim.run(500);
	}

	/**
	 * Clear the displayed message and hide the panel.
	 */
	public void hideMessage() {
		inner.clear();
		message = new HTML("&nbsp;");
		this.setVisible(false);
	}

}
