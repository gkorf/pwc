/*
 * Copyright 2012-2013 GRNET S.A. All rights reserved.
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

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;

public class PithosDisclosurePanel extends Composite {

	public interface Style extends CssResource {
		String disclosurePanel();
		
		String header();
		
		String arrow();
		
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

        @Source("gr/grnet/pithos/resources/ajax-loader.gif")
        ImageResource ajaxLoader();
	}
	
	private final DisclosurePanel panel;
    private final Resources resources;
    private final String title;

    public PithosDisclosurePanel(final Resources _resources, final String title, boolean open) {
        this(_resources, title, false, open);
    }

	public PithosDisclosurePanel(final Resources _resources, final String title, boolean ajaxLoader, boolean open) {
        this.title = title;

		this.resources = _resources;
		resources.pithosDisclosurePanelCss().ensureInjected();
		this.panel = new DisclosurePanel();
		panel.addStyleName(resources.pithosDisclosurePanelCss().disclosurePanel());
		panel.setHeader(createHeader(resources, title, ajaxLoader, open));
		panel.setOpen(open);

        if(!ajaxLoader) {
            addOpenCloseHandlers();
        }

		initWidget(panel);
	}

    private void addOpenCloseHandlers() {
        panel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
            @Override
            public void onOpen(OpenEvent<DisclosurePanel> event) {
                panel.setHeader(createHeader(resources, title, false, true));
            }
        });
        panel.addCloseHandler(new CloseHandler<DisclosurePanel>() {

            @Override
            public void onClose(CloseEvent<DisclosurePanel> event) {
                panel.setHeader(createHeader(resources, title, false, false));
            }
        });
    }

    public void setLoaded(boolean open) {
        this.panel.setHeader(createHeader(resources, title, false, open));
        addOpenCloseHandlers();
    }
	
	Widget createHeader(Resources resources, String title, boolean ajaxLoader, boolean open) {
		HorizontalPanel header = new HorizontalPanel();
        
		Image img = new Image(resources.icon());
		header.add(img);
		header.setCellVerticalAlignment(img, HasVerticalAlignment.ALIGN_MIDDLE);
		header.setCellWidth(img, "32px");
		HTML titleHtml = new HTML(title);
		header.add(titleHtml);
		header.setCellVerticalAlignment(titleHtml, HasVerticalAlignment.ALIGN_MIDDLE);
		Image arrow = new Image(ajaxLoader ? resources.ajaxLoader() : open ? resources.open() : resources.closed());
		arrow.addStyleName(resources.pithosDisclosurePanelCss().arrow());
		header.add(arrow);
		header.setCellHorizontalAlignment(arrow, HasHorizontalAlignment.ALIGN_RIGHT);
		header.setCellVerticalAlignment(arrow, HasVerticalAlignment.ALIGN_MIDDLE);
		
		header.addStyleName(resources.pithosDisclosurePanelCss().header());
		return header;
	}
	
	public void setContent(Widget widget) {
		panel.setContent(widget);
		panel.getContent().removeStyleName("content");
		panel.getContent().addStyleName(resources.pithosDisclosurePanelCss().content());
	}
}
