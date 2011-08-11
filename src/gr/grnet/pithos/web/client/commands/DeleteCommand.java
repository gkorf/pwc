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

import gr.grnet.pithos.web.client.DeleteFileDialog;
import gr.grnet.pithos.web.client.DeleteFolderDialog;
import gr.grnet.pithos.web.client.MessagePanel.Images;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;

import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Delete selected object command
 *
 */
public class DeleteCommand implements Command{
	private PopupPanel containerPanel;
	final Images newImages;

    private Object resource;

    private Pithos app;
	/**
	 * @param _containerPanel
	 * @param _newImages the images of all the possible delete dialogs
	 */
	public DeleteCommand(Pithos _app, PopupPanel _containerPanel, Object resource, final Images _newImages ){
        app = _app;
		containerPanel = _containerPanel;
		newImages = _newImages;
        this.resource = resource;
	}

	@Override
	public void execute() {
        if (containerPanel != null)
    		containerPanel.hide();

        if (resource instanceof Folder) {
            DeleteFolderDialog dlg = new DeleteFolderDialog(app, newImages, (Folder) resource);
            dlg.center();
        } else if (resource instanceof List) {
            DeleteFileDialog dlg = new DeleteFileDialog(app, newImages, (List<File>) resource);
            dlg.center();
        }
	}
}
