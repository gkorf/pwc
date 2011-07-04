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
package gr.grnet.pithos.web.client.commands;

import gr.grnet.pithos.web.client.GSS;
import gr.grnet.pithos.web.client.clipboard.Clipboard;
import gr.grnet.pithos.web.client.clipboard.ClipboardItem;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.GroupUserResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Command for cutting a file, folder or user to GSS Clipboard
 *
 */
public class CutCommand implements Command{
	private PopupPanel containerPanel;

	public CutCommand( PopupPanel _containerPanel ){
		containerPanel = _containerPanel;
	}

	@Override
	public void execute() {
		containerPanel.hide();
		Object selection = GSS.get().getCurrentSelection();
		if (selection == null)
			return;
		GWT.log("selection: " + selection.toString(), null);
		if (selection instanceof RestResourceWrapper) {
			ClipboardItem clipboardItem = new ClipboardItem(Clipboard.CUT, (RestResourceWrapper) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		} else if (selection instanceof FileResource) {
			ClipboardItem clipboardItem = new ClipboardItem(Clipboard.CUT, (FileResource) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		} else if (selection instanceof GroupUserResource) {
			ClipboardItem clipboardItem = new ClipboardItem(Clipboard.CUT, (GroupUserResource) selection);
			GSS.get().getClipboard().setItem(clipboardItem);
		}
		else if (selection instanceof List){
			 ClipboardItem clipboardItem = new ClipboardItem(Clipboard.CUT, (List<FileResource>) selection);
			 GSS.get().getClipboard().setItem(clipboardItem);
		 }
	}

}
