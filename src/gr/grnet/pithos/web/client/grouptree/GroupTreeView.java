/*
 * Copyright 2011-2013 GRNET S.A. All rights reserved.
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

package gr.grnet.pithos.web.client.grouptree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import gr.grnet.pithos.web.client.FolderContextMenu;
import gr.grnet.pithos.web.client.PithosDisclosurePanel;
import gr.grnet.pithos.web.client.TreeView;
import gr.grnet.pithos.web.client.foldertree.Folder;

public class GroupTreeView extends Composite implements TreeView {

    public void updateChildren(Group group) {
        TreeNode root = tree.getRootTreeNode();
        if (group != null)
        	updateChildren(root, group);
        else if (root.getChildCount() > 0) {
        	root.setChildOpen(0, false, true);
        	root.setChildOpen(0, true, true);
        }
    }

    private void updateChildren(TreeNode node, Group group) {
        for (int i=0; i<node.getChildCount(); i++) {
            if (group.equals(node.getChildValue(i))) {
                node.setChildOpen(i, false, true);
                node.setChildOpen(i, true, true);
            }
            else {
                if (node.isChildOpen(i)) {
                	TreeNode n = node.setChildOpen(i, true);
                	updateChildren(n, group);
                }	
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
        
        @Source("gr/grnet/pithos/web/client/cellTreeLoadingBasic.gif")
        @ImageOptions(repeatStyle=RepeatStyle.None)
        ImageResource cellTreeLoadingBasic();
    }

    public static interface Images extends Tree.Resources, FolderContextMenu.Images {

        @Source("gr/grnet/pithos/resources/groups22.png")
        ImageResource groups();

        @Source("gr/grnet/pithos/resources/groups22.png")
        public ImageResource group();

        @Source("gr/grnet/pithos/resources/edit_user.png")
        ImageResource user();
    }

    static Images images = GWT.create(Images.class);
    
    interface Style extends gr.grnet.pithos.web.client.PithosDisclosurePanel.Style {
    	@Override
		String header();
    }
 
    interface Resources extends gr.grnet.pithos.web.client.PithosDisclosurePanel.Resources {
		@Override
		@Source("PithosGroupDisclosurePanel.css")
		Style pithosDisclosurePanelCss();

		@Override
		@Source("gr/grnet/pithos/resources/groups22.png")
    	ImageResource icon();
    }

    public static interface Templates extends SafeHtmlTemplates {
        public Templates INSTANCE = GWT.create(Templates.class);

        @Template("<span style='margin-left:5px;'>{0}</span>")
        public SafeHtml nameSpan(String name);
      }

    private GroupTreeViewModel model;
    
    private CellTree tree;

    public GroupTreeView(GroupTreeViewModel viewModel) {
        this.model = viewModel;

        PithosDisclosurePanel panel = new PithosDisclosurePanel((Resources) GWT.create(Resources.class), "Groups", false, false);
        /*
         * Create the tree using the model. We use <code>null</code> as the default
         * value of the root node. The default value will be passed to
         * CustomTreeModel#getNodeInfo();
         */
        CellTree.Resources res = GWT.create(BasicResources.class);
        tree = new CellTree(model, null, res);
        tree.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

        panel.setContent(tree);
        
        initWidget(panel);
    }

    public void updateGroupNode(Group group) {
    	model.updateGroupNode(group);
    	updateChildren(group);
    }

	@Override
	public Folder getSelection() {
		return null;
	}

	public Object getSelected() {
		return model.getSelectedObject();
	}
}
