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

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.*;

/**
 * The panel that displays a status bar with quota information.
 */
public class StatusPanel extends Composite {

	/**
	 * The constructor of the status panel.
	 */
	public StatusPanel() {
		Dictionary otherProperties = Dictionary.getDictionary("otherProperties");
        final String SERVICE_NAME = otherProperties.get("SERVICE_NAME");
        final String SERVICE_URL = otherProperties.get("SERVICE_URL");
        final String COMPANY_NAME = otherProperties.get("COMPANY_NAME");
        final String COMPANY_URL = otherProperties.get("COMPANY_URL");
        final String COPYRIGHT_MESSAGE = otherProperties.get("COPYRIGHT_MESSAGE");
        final String SYNNEFO_JS_LIB_VERSION = otherProperties.get("SYNNEFO_JS_LIB_VERSION");

		HorizontalPanel outer = new HorizontalPanel();
		outer.setWidth("100%");
		outer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        VerticalPanel inner = new VerticalPanel();
        inner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		HorizontalPanel firstLine = new HorizontalPanel();
		firstLine.setSpacing(8);
        firstLine.add(new HTML("<a class='grnet-sign' href='" + COMPANY_URL + "'>" + COPYRIGHT_MESSAGE + "</a>"));
		inner.add(firstLine);

        HorizontalPanel secondLine = new HorizontalPanel();
        secondLine.add(new HTML(
            "<div class='software'>Powered by <a href='" + SERVICE_URL +
            "'>Synnefo</a> <span class='version'>v " + SYNNEFO_JS_LIB_VERSION + "</span></div>"));
        secondLine.addStyleName("software");
        inner.add(secondLine);
        outer.add(inner);
        outer.addStyleName("pithos-statusbar");

		initWidget(outer);
	}
}
