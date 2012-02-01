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

package gr.grnet.pithos.web.client.foldertree;

import gr.grnet.pithos.web.client.FolderContextMenu;
import gr.grnet.pithos.web.client.TreeView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;

public class FolderTreeView extends Composite implements TreeView {

	public boolean isFolderOpen(Folder folder) {
        TreeNode root = ((CellTree) getWidget()).getRootTreeNode();
        return isFolderOpen(root, folder);
	}
	
	private boolean isFolderOpen(TreeNode node, Folder folder) {
        for (int i=0; i<node.getChildCount(); i++) {
            if (folder.equals(node.getChildValue(i))) {
                return node.isChildOpen(i);
            }
			if (node.isChildOpen(i)) {
			    TreeNode n = node.setChildOpen(i, true);
			    return isFolderOpen(n, folder);
			}
    	}
        return false;
	}
	
    public void openFolder(Folder folder) {
        TreeNode root = ((CellTree) getWidget()).getRootTreeNode();
        openFolder(root, folder);
    }

    private void openFolder(TreeNode node, Folder folder) {
        for (int i=0; i<node.getChildCount(); i++) {
            if (folder.equals(node.getChildValue(i))) {
            	node.setChildOpen(i, false, true);
            	node.setChildOpen(i, true, true);
            	break;
            }
			if (node.isChildOpen(i)) {
			    TreeNode n = node.setChildOpen(i, true);
			    openFolder(n, folder);
			    break;
			}
    	}
    }

    static interface BasicResources extends CellTree.Resources {

        @Override
		@ImageOptions(flipRtl = true)
        @Source("gr/grnet/pithos/web/client/cellTreeClosedItem.png")
        ImageResource cellTreeClosedItem();

        @Override
		@ImageOptions(flipRtl = true)
        @Source("gr/grnet/pithos/web/client/cellTreeLoadingBasic.gif")
        ImageResource cellTreeLoading();

        @Override
		@ImageOptions(flipRtl = true)
        @Source("gr/grnet/pithos/web/client/cellTreeOpenItem.png")
        ImageResource cellTreeOpenItem();

        @Override
		@Source({"gr/grnet/pithos/web/client/PithosCellTreeBasic.css"})
        CellTree.Style cellTreeStyle();
    }

    public static interface Images extends Tree.Resources, FolderContextMenu.Images {

        @Source("gr/grnet/pithos/resources/home22.png")
        ImageResource home();

        @Source("gr/grnet/pithos/resources/folder22.png")
        public ImageResource folderYellow();

        @Source("gr/grnet/pithos/resources/mimetypes/document.png")
        ImageResource document();

        @Source("gr/grnet/pithos/resources/othersshared.png")
        ImageResource othersShared();

        @Source("gr/grnet/pithos/resources/myshared22.png")
        ImageResource myShared();

        @Source("gr/grnet/pithos/resources/folder_user.png")
        ImageResource sharedFolder();

        @Source("gr/grnet/pithos/resources/trash.png")
        ImageResource trash();
    }

    static Images images = GWT.create(Images.class);

    static interface Templates extends SafeHtmlTemplates {
        public Templates INSTANCE = GWT.create(Templates.class);

        @Template("<span style='vertical-align: middle;'>{0}</span>")
        public SafeHtml nameSpan(String name);

        @Template("<span class='pithos-folderLabel'>{0}</span>")
        public SafeHtml imageSpan(String name);
    }

    private FolderTreeViewModel model;

    public FolderTreeView(FolderTreeViewModel viewModel) {
        this.model = viewModel;
        /*
         * Create the tree using the model. We use <code>null</code> as the default
         * value of the root node. The default value will be passed to
         * CustomTreeModel#getNodeInfo();
         */
        CellTree.Resources res = GWT.create(BasicResources.class);
        CellTree tree = new CellTree(model, null, res);
        tree.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

        initWidget(tree);
    }


    @Override
	public Folder getSelection() {
       return model.getSelection();
    }

    public void updateFolder(Folder folder, boolean showfiles, Command callback) {
        model.updateFolder(folder, showfiles, callback);
    }
}
