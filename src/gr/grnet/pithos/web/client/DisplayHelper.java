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

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;


/**
 * A helper class with static methods that manipulate display
 * widgets in various useful ways, not available in GWT.
 *
 */
public class DisplayHelper {

	/**
	 * A flag that denotes that no selection should be made while
	 * displaying the rows of a table.
	 */
	public static final int NO_SELECTION = -1;

	/**
	 * Clear any current selection in the specified ListBox.
	 */
	public static void clearSelections(ListBox listBox) {
		for (int i=0; i<listBox.getItemCount(); i++)
			if (listBox.isItemSelected(i))
				listBox.setItemSelected(i, false);
	}

	/**
	 * Select the item in the listBox whose value matches the provided
	 * value.
	 */
	public static void selectMatch(ListBox listBox, String value) {
		for (int i=0; i<listBox.getItemCount(); i++)
			if (listBox.getValue(i).equals(value))
				listBox.setItemSelected(i, true);
			else
				listBox.setItemSelected(i, false);
	}

	/**
	 * Select the items in the listBox whose value matches the provided
	 * value list. Every value that is matched in the listBox is removed
	 * from the value list, in order to let the caller know what values
	 * were not matched. Therefore the caller must be prepared for the
	 * value list to be modified.
	 *
	 * @param listBox the ListBox
	 * @param values the list of values to be selected
	 */
	public static void selectMultiMatch(ListBox listBox, List values) {
		for (int i=0; i<listBox.getItemCount(); i++)
			if (values.contains(listBox.getValue(i))) {
				listBox.setItemSelected(i, true);
				values.remove(listBox.getValue(i));
			} else
				listBox.setItemSelected(i, false);
	}

	public static native void log(String message) /*-{
		var logger = $wnd.console;
  		if (logger && logger.debug)
			logger.debug(message);
		else if (logger && logger.log)
			logger.log(message);
	}-*/;

	/**
	 * Make the specified row look like selected or not, according to the
	 * <code>selected</code> flag.
	 *
	 * @param row the row number in the list of entries (i.e. ignoring the header line)
	 * @param selected the flag that denotes whether the <code>styleName</code> should
	 * 				be added or removed
	 * @param styleName the name of the CSS style
	 */
	public static void styleRow(final FlexTable table, final int row, final boolean selected, String styleName) {
		if (row != -1)
			if (selected)
				table.getRowFormatter().addStyleName(row + 1, styleName);
			else
				table.getRowFormatter().removeStyleName(row + 1, styleName);
	}

	/**
	 * Select the specified row in the table. This entails modifying its style
	 * as well as the style of the previously selected row.
	 *
	 * @param table the FlexTable widget
	 * @param row the newly selected row
	 * @param previousRow the previously selected row
	 * @param styleName the name of the CSS style
	 * @return the newly selected row
	 */
	public static int selectRow(final FlexTable table, final int row, final int previousRow, String styleName) {
		// Reset the style of the previously selected row.
		styleRow(table, previousRow, false, styleName);
		// Select the row that was clicked.
		styleRow(table, row, true, styleName);
		return row;
	}
	/**
	 * The implementation of this trim method also checks for
	 * no brake space characters (nbsp) = '\00A0'
	 * and removes them
	 *
	 * @param input
	 * @return the new trimmed string without whitespace or no brake space
	 */
	public static native String trim(String input) /*-{
    if(input.length == 0)
    	return input;
	if((input[0]||input[input.length-1]) != '\u0020' && (input[0]||input[input.length-1]) != '\u00A0')
    	return input;
    var r1 = input.replace(/^(\s*)/, '');
    var r2 = r1.replace(/\s*$/, '');
    return r2;
  }-*/;

}
