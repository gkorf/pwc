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

import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The 'Multiple file properties' dialog box implementation.
 *
 */
public class FilesPropertiesDialog extends AbstractPropertiesDialog {

//	private final TristateCheckBox versionedCheck;

	private final List<File> files;

	/**
	 * The widget's constructor.
	 *
	 * @param _files
	 */
	public FilesPropertiesDialog(Pithos _app, final List<File> _files) {
		super(_app);

		files = _files;
//		int versionedNum = 0;
//		for (File fr : files)
//			if (fr.isVersioned()) versionedNum++;
//		Boolean versioned = null;
//		if (versionedNum == 0)
//            versioned = false;
//		if (versionedNum == files.size())
//            versioned = true;

//		initialVersioned = versioned;
//		versionedCheck = new TristateCheckBox(versioned);

		// Set the dialog's caption.
		setText("Files properties");

		// Outer contains inner and buttons.
		final VerticalPanel outer = new VerticalPanel();
		final FocusPanel focusPanel = new FocusPanel(outer);
		// Inner contains generalPanel and permPanel.
		inner = new DecoratedTabPanel();
		inner.setAnimationEnabled(true);

		inner.add(createGeneralPanel(), "General");

        final VerticalPanel verPanel = new VerticalPanel();

//        final HorizontalPanel vPanel = new HorizontalPanel();
//        vPanel.setSpacing(8);
//        vPanel.addStyleName("pithos-TabPanelBottom");
//        vPanel.add(new Label("Versioned"));
//        vPanel.add(versionedCheck);
//
//        verPanel.add(vPanel);

		inner.add(verPanel, "Versions");
		inner.selectTab(0);
        outer.add(inner);

        final HorizontalPanel buttons = new HorizontalPanel();
		// Create the 'OK' button, along with a listener that hides the dialog
		// when the button is clicked.
		final Button ok = new Button("OK", new ClickHandler() {
			@Override
			public void onClick(@SuppressWarnings("unused") ClickEvent event) {
				accept();
				closeDialog();
			}
		});
		buttons.add(ok);
		buttons.setCellHorizontalAlignment(ok, HasHorizontalAlignment.ALIGN_CENTER);
		// Create the 'Cancel' button, along with a listener that hides the
		// dialog when the button is clicked.
		final Button cancel = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(@SuppressWarnings("unused") ClickEvent event) {
				closeDialog();
			}
		});
		buttons.add(cancel);
		buttons.setCellHorizontalAlignment(cancel, HasHorizontalAlignment.ALIGN_CENTER);
		buttons.setSpacing(8);
		buttons.addStyleName("pithos-TabPanelBottom");
		outer.add(buttons);
		outer.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
		outer.addStyleName("pithos-TabPanelBottom");

		focusPanel.setFocus(true);
		setWidget(outer);
	}

    private VerticalPanel createGeneralPanel() {
        VerticalPanel generalPanel = new VerticalPanel();

        final FlexTable generalTable = new FlexTable();
        generalTable.setText(0, 0, String.valueOf(files.size())+" files selected");
        generalTable.setText(1, 0, "Folder");
        generalTable.setText(2, 0, "Tags");
        Folder parent = files.get(0).getParent();
        if(parent != null)
            generalTable.setText(1, 1, parent.getName());
        else
            generalTable.setText(1, 1, "-");

		// Find if tags are identical
//		List<String> tagsList = files.get(0).getTags();
//		List<String> tagss;
//		for (int i=1; i<files.size(); i++) {
//			tagss = files.get(i).getTags();
//			if (tagsList.size() != tagss.size() || !tagsList.containsAll(tagss)) {
//				tagsList = null;
//				break;
//			}
//		}
//		// Get the tags.
//		StringBuffer tagsBuffer = new StringBuffer();
//		if (tagsList==null)
//			tagsBuffer.append(MULTIPLE_VALUES_TEXT);
//		else {
//			Iterator i = tagsList.iterator();
//			while (i.hasNext()) {
//				String tag = (String) i.next();
//				tagsBuffer.append(tag).append(", ");
//			}
//			if (tagsBuffer.length() > 1)
//				tagsBuffer.delete(tagsBuffer.length() - 2, tagsBuffer.length() - 1);
//		}
//		initialTagText = tagsBuffer.toString();
//		tags.setText(initialTagText);
//		tags.addFocusHandler(new FocusHandler() {
//			@Override
//			public void onFocus(FocusEvent event) {
//				if (MULTIPLE_VALUES_TEXT.equals(tags.getText()))
//					tags.setText("");
//			}
//		}
//		);
//
//		generalTable.setWidget(2, 1, tags);
		generalTable.getFlexCellFormatter().setStyleName(0, 0, "props-labels");
		generalTable.getFlexCellFormatter().setColSpan(0, 0, 2);
		generalTable.getFlexCellFormatter().setStyleName(1, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(2, 0, "props-labels");
		generalTable.getFlexCellFormatter().setStyleName(0, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(1, 1, "props-values");
		generalTable.getFlexCellFormatter().setStyleName(2, 1, "props-values");
		generalTable.setCellSpacing(4);

        generalPanel.add(generalTable);

		// Asynchronously retrieve the tags defined by this user.
//		DeferredCommand.addCommand(new Command() {
//
//			@Override
//			public void execute() {
//				updateTags();
//			}
//		});

		DisclosurePanel allTags = new DisclosurePanel("All tags");
		allTagsContent = new FlowPanel();
		allTags.setContent(allTagsContent);
		generalPanel.add(allTags);
		generalPanel.setSpacing(4);

        return generalPanel;
    }

	/**
	 * Accepts any change and updates the file
	 *
	 */
	@Override
	protected void accept() {
//		JSONObject json = new JSONObject();
//		if ( versionedCheck.getState()!=null && !versionedCheck.getState().equals(initialVersioned) )
//				json.put("versioned", JSONBoolean.getInstance(versionedCheck.getState()));
//
//		JSONArray taga = new JSONArray();
//		int i = 0;
//		String tagText = tags.getText();
//		if (!MULTIPLE_VALUES_TEXT.equals(tagText) && !initialTagText.equals(tagText)) {
//			String[] tagset = tagText.split(",");
//			for (String t : tagset) {
//				JSONString to = new JSONString(t);
//				taga.set(i, to);
//				i++;
//			}
//			json.put("tags", taga);
//		}
//		String jsonString = json.toString();
//		if(jsonString.equals("{}")){
//			GWT.log("NO CHANGES", null);
//			return;
//		}
//		final List<String> fileIds = new ArrayList<String>();
//		for(FileResource f : files)
//			fileIds.add(f.getUri()+"?update=");
//		MultiplePostCommand rt = new MultiplePostCommand(fileIds.toArray(new String[0]), jsonString, 200){
//
//			@Override
//			public void onComplete() {
//				app.getTreeView().refreshCurrentNode(false);
//			}
//
//			@Override
//			public void onError(String p, Throwable t) {
//				GWT.log("", t);
//				if(t instanceof RestException){
//					int statusCode = ((RestException)t).getHttpStatusCode();
//					if(statusCode == 405)
//						app.displayError("You don't have the necessary permissions");
//					else if(statusCode == 404)
//						app.displayError("File does not exist");
//					else if(statusCode == 409)
//						app.displayError("A file with the same name already exists");
//					else if(statusCode == 413)
//						app.displayError("Your quota has been exceeded");
//					else
//						app.displayError("Unable to modify file::"+((RestException)t).getHttpStatusText());
//				}
//				else
//					app.displayError("System error modifying file:"+t.getMessage());
//			}
//		};
//		DeferredCommand.addCommand(rt);
	}
}
