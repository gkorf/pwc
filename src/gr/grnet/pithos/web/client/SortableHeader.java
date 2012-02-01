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

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * A {@link Header} subclass that maintains sorting state and displays an icon
 * to indicate the sort direction.
 */
public class SortableHeader extends Header<String> {

  interface Template extends SafeHtmlTemplates {
    @Template("<div style=\"position:relative;cursor:hand;cursor:pointer;"
        + "padding-right:{0}px;\">{1}<div>{2}</div></div>")
    SafeHtml sorted(int imageWidth, SafeHtml arrow, String text);

    @Template("<div style=\"position:relative;cursor:hand;cursor:pointer;"
        + "padding-right:{0}px;\"><div style=\"position:absolute;display:none;"
        + "\"></div><div>{1}</div></div>")
    SafeHtml unsorted(int imageWidth, String text);
  }

  private static Template template;

  /**
   * Image resources.
   */
  public static interface Resources extends ClientBundle {

    ImageResource downArrow();

    ImageResource upArrow();
  }

  private static final Resources RESOURCES = GWT.create(Resources.class);
  private static final int IMAGE_WIDTH = 16;
  private static final SafeHtml DOWN_ARROW = makeImage(RESOURCES.downArrow());
  private static final SafeHtml UP_ARROW = makeImage(RESOURCES.upArrow());

  private static SafeHtml makeImage(ImageResource resource) {
    AbstractImagePrototype proto = AbstractImagePrototype.create(resource);
    String html = proto.getHTML().replace("style='",
        "style='position:absolute;right:0px;top:0px;");
    return SafeHtmlUtils.fromTrustedString(html);
  }

  private boolean reverseSort = false;
  private boolean sorted = false;
  private String text;

  SortableHeader(String text) {
    super(new ClickableTextCell());
    if (template == null) {
      template = GWT.create(Template.class);
    }
    this.text = text;
  }

  public boolean getReverseSort() {
    return reverseSort;
  }

  @Override
  public String getValue() {
    return text;
  }

  @Override
  public void render(Context context, SafeHtmlBuilder sb) {
    if (sorted) {
      sb.append(template.sorted(IMAGE_WIDTH, reverseSort ? DOWN_ARROW : UP_ARROW, text));
    } else {
      sb.append(template.unsorted(IMAGE_WIDTH, text));
    }
  }

  public void setReverseSort(boolean reverseSort) {
    this.reverseSort = reverseSort;
  }

  public void setSorted(boolean sorted) {
    this.sorted = sorted;
  }

  public void toggleReverseSort() {
    this.reverseSort = !this.reverseSort;
  }
}
