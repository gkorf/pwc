/*
 * Copyright 2011-2012 GRNET S.A. All rights reserved.
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

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The panel that displays a status bar with quota information.
 */
public class StatusPanel extends Composite {

	/**
	 * The constructor of the status panel.
	 */
	public StatusPanel() {
		HorizontalPanel outer = new HorizontalPanel();
		outer.setWidth("100%");
		outer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        VerticalPanel inner = new VerticalPanel();
        inner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		HorizontalPanel firstLine = new HorizontalPanel();
		firstLine.setSpacing(8);
		firstLine.add(new Anchor("About"));
		firstLine.add(new HTML("|"));
        firstLine.add(new Anchor("Help"));
		firstLine.add(new HTML("|"));
        firstLine.add(new Anchor("Contact"));
		firstLine.add(new HTML("|"));
        firstLine.add(new Anchor("Terms"));
		firstLine.add(new HTML("|"));
        firstLine.add(new Anchor("Privacy"));
		inner.add(firstLine);

        HorizontalPanel secondLine = new HorizontalPanel();
        secondLine.add(new HTML("<a class='grnet-sign' href='http://www.grnet.gr'>Copyright (C) 2011-2012 Greek Research and Technology Network</a>"));
        inner.add(secondLine);
        outer.add(inner);
        outer.addStyleName("pithos-statusbar");

		initWidget(outer);
	}
}
