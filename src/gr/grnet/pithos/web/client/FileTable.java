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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;


public class FileTable extends Grid{

	/**
	 *
	 */
	public FileTable() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param rows
	 * @param columns
	 */
	public FileTable(int rows, int columns) {
		super(rows, columns);
		// TODO Auto-generated constructor stub
	}

	public int getRowForEvent2(Event event) {
	    Element td = getEventTargetCell(event);
	    if (td == null)
			return -1;

	    Element tr = DOM.getParent(td);
	    Element body = DOM.getParent(tr);
	    int row = DOM.getChildIndex(body, tr);
	    return row;
	  }
	
	public static void copyRow(FileTable sourceTable, FileTable targetTable, int sourceRow, int targetRow) {
		targetTable.insertRow(targetRow);
		for (int col = 0; col < sourceTable.getCellCount(sourceRow); col++) {
			HTML html = new HTML(sourceTable.getHTML(sourceRow, col));
			targetTable.setWidget(targetRow, col, html);
		}
		copyRowStyle(sourceTable, targetTable, sourceRow, targetRow);
	}

	private static void copyRowStyle(FileTable sourceTable, FileTable targetTable, int sourceRow, int targetRow) {
		String rowStyle = sourceTable.getRowFormatter().getStyleName(sourceRow);
		targetTable.getRowFormatter().setStyleName(targetRow, rowStyle);
	}

	public static int getWidgetRow(Widget widget, FileTable table) {
	    for (int row = 0; row < table.getRowCount(); row++)
			for (int col = 0; col < table.getCellCount(row); col++) {
			    Widget w = table.getWidget(row, col);
			    if (w == widget)
					return row;
			  }
	    throw new RuntimeException("Unable to determine widget row");
	  }

}
