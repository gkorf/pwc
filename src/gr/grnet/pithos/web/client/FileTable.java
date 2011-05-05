/*
 * Copyright (c) 2011 Greek Research and Technology Network
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
