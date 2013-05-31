/*
 * Copyright 2011-2013 GRNET S.A. All rights reserved.
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

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.*;
import gr.grnet.pithos.web.client.commands.AddUserCommand;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.grouptree.Group;

/**
 * The 'Purge Container' dialog box implementation.
 */
public class PurgeContainerDialog extends DialogBox {
    private final Pithos app;
    private final Folder container;

    private final CheckBox yesIAgree;

	private final VerticalPanel inner;

	public PurgeContainerDialog(Pithos app, Folder container) {
        this.app = app;
        this.container = container;
        this.yesIAgree =  new CheckBox("I want to purge all contents of " + container.getName());

        this.yesIAgree.setValue(false);

		Anchor close = new Anchor("close");
		close.addStyleName("close");
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		setGlassEnabled(true);
		setStyleName("pithos-DialogBox");

		// Enable IE selection for the dialog (must disable it upon closing it)
		Pithos.enableIESelection();

		// Use this opportunity to set the dialog's caption.
		setText(Const.PurgeContainer(container.getName()));

		// Outer contains inner and buttons
		VerticalPanel outer = new VerticalPanel();
		outer.add(close);
		// Inner contains generalPanel and permPanel
		inner = new VerticalPanel();
		inner.addStyleName("inner");

		VerticalPanel generalPanel = new VerticalPanel();
        FlexTable generalTable = new FlexTable();
        generalTable.setText(0, 0, "Are you sure?");

        generalTable.setWidget(0, 1, yesIAgree);

        generalTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
        generalTable.getFlexCellFormatter().setStyleName(0, 1, "props-values");
        generalTable.setCellSpacing(4);
        generalPanel.add(generalTable);
        inner.add(generalPanel);

        outer.add(inner);

		// Create the 'Create/Update' button, along with a listener that hides the dialog
		// when the button is clicked and quits the application.
		String okLabel = "Purge Container";
		final Button ok = new Button(okLabel, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
                purgeContainer();
				closeDialog();
			}
		});
		ok.addStyleName("button");
        ok.setEnabled(false);
        ok.setVisible(false);
		outer.add(ok);

        this.yesIAgree.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                ok.setVisible(event.getValue());
                ok.setEnabled(event.getValue());
            }
        });

        outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);

        setWidget(outer);
	}

	@Override
	public void center() {
		super.center();
        yesIAgree.setFocus(false);
	}

	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
		super.onPreviewNativeEvent(preview);

		NativeEvent evt = preview.getNativeEvent();
		if (evt.getType().equals(KeyDownEvent.getType().getName()))
			// Exit the dialog on Escape
			switch (evt.getKeyCode()) {
				case KeyCodes.KEY_ESCAPE:
					closeDialog();
					break;
			}
	}


	/**
	 * Enables IE selection prevention and hides the dialog
	 * (we disable the prevention on creation of the dialog)
	 */
	public void closeDialog() {
		Pithos.preventIESelection();
		hide();
	}

    private void purgeContainer() {
        app.purgeContainer(container);
    }
}
