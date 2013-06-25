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

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.*;

/**
 * The panel that displays a status bar with quota information.
 */
public class StatusPanel extends Composite {

    /**
     * The constructor of the status panel.
     */
    public StatusPanel() {
        final String SYNNEFO_VERSION = Pithos.getFromOtherPropertiesOrDefault("SYNNEFO_VERSION", "");

        HorizontalPanel outer = new HorizontalPanel();
        outer.setWidth("100%");
        outer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        VerticalPanel inner = new VerticalPanel();
        inner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        HorizontalPanel firstLine = new HorizontalPanel();
        firstLine.setSpacing(8);
        if(Pithos.isShowCopyrightMessage()) {
            final String COMPANY_URL = Pithos.getFromOtherPropertiesOrDefault("COMPANY_URL", "#");
            final String safeCompanyURL = UriUtils.encode(COMPANY_URL);
            final String COPYRIGHT_MESSAGE = Pithos.getFromOtherPropertiesOrDefault("COPYRIGHT_MESSAGE", "");
            final String safeCopyrightMessage = SafeHtmlUtils.htmlEscape(COPYRIGHT_MESSAGE);
            firstLine.add(new HTML("<a class='grnet-sign' href='" + safeCompanyURL + "'>" + safeCopyrightMessage + "</a>"));

            Pithos.LOG("Showing copyright message");
        }
        else {
            Pithos.LOG("Not showing copyright message");
        }
        inner.add(firstLine);

        HorizontalPanel secondLine = new HorizontalPanel();
        secondLine.add(new HTML(
            "<div class='software'>" +
                "Powered by <a href='http://synnefo.orgs'>Synnefo</a>" +
                "<span class='version'> v" + SYNNEFO_VERSION + "</span>" +
            "</div>"));
//        secondLine.addStyleName("software");
        inner.add(secondLine);
        outer.add(inner);
        outer.addStyleName("pithos-statusbar");

        initWidget(outer);
    }
}
