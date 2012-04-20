/*
 * Copyright 2012 GRNET S.A. All rights reserved.
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

import gr.grnet.pithos.web.client.grouptree.GroupTreeView;
import gr.grnet.pithos.web.client.grouptree.GroupTreeView.Templates;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;

public class PithosDisclosurePanel extends Composite {

	public interface Style extends CssResource {
		String disclosurePanel();
		
		String header();
		
		String content();
	}
	
	public interface Resources extends ClientBundle {
		@Source("PithosDisclosurePanel.css")
		Style pithosDisclosurePanelCss();
		
		@Source("upArrow.png")
		ImageResource icon();
		
		@Source("upArrow.png")
		ImageResource open();
		
		@Source("downArrow.png")
		ImageResource closed();
	}
	
	DisclosurePanel panel;
	
	Resources resources;
	
	public PithosDisclosurePanel(final Resources _resources, final String title, boolean open) {
		resources = _resources;
		resources.pithosDisclosurePanelCss().ensureInjected();
		panel = new DisclosurePanel();
		panel.addStyleName(resources.pithosDisclosurePanelCss().disclosurePanel());
		panel.setHeader(createHeader(resources, title, open));
		panel.setOpen(open);
		panel.setAnimationEnabled(true);
		panel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
			
			@Override
			public void onOpen(OpenEvent<DisclosurePanel> event) {
				panel.setHeader(createHeader(resources, title, true));
			}
		});
		panel.addCloseHandler(new CloseHandler<DisclosurePanel>() {
			
			@Override
			public void onClose(CloseEvent<DisclosurePanel> event) {
				panel.setHeader(createHeader(resources, title, false));
			}
		});
		
		initWidget(panel);
	}
	
	HTML createHeader(Resources resources, String title, boolean open) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant(AbstractImagePrototype.create(resources.icon()).getHTML());
        sb.append(Templates.INSTANCE.nameSpan(title));
       	sb.appendHtmlConstant(AbstractImagePrototype.create(open ? resources.open() : resources.closed()).getHTML());
       	HTML header = new HTML(sb.toSafeHtml());
       	header.addStyleName(resources.pithosDisclosurePanelCss().header());
        return header;
	}
	
	public void add(IsWidget widget) {
		panel.add(widget);
		panel.getContent().removeStyleName("content");
		panel.getContent().addStyleName(resources.pithosDisclosurePanelCss().content());
	}
}
