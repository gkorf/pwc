/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client.foldertree;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import gr.grnet.pithos.web.client.FolderContextMenu;

public class FolderTreeView extends Composite {

    static interface BasicResources extends CellTree.Resources {

        @ImageOptions(flipRtl = true)
        @Source("cellTreeClosedItem.gif")
        ImageResource cellTreeClosedItem();

        @ImageOptions(flipRtl = true)
        @Source("cellTreeLoadingBasic.gif")
        ImageResource cellTreeLoading();

        @ImageOptions(flipRtl = true)
        @Source("cellTreeOpenItem.gif")
        ImageResource cellTreeOpenItem();

        @Source({"GssCellTreeBasic.css"})
        CellTree.Style cellTreeStyle();
    }

    static interface Images extends ClientBundle,Tree.Resources, FolderContextMenu.Images {

        @Source("gr/grnet/pithos/resources/folder_home.png")
        ImageResource home();

        @Source("gr/grnet/pithos/resources/folder_yellow.png")
        ImageResource folderYellow();
    }

    private static Images images = GWT.create(Images.class);

    static interface Templates extends SafeHtmlTemplates {
        Templates INSTANCE = GWT.create(Templates.class);

        @Template("<span>{0}</span>")
        public SafeHtml nameSpan(String name);
      }

    static class FolderCell extends AbstractCell<Folder> {

        @Override
        public void render(Context context, Folder folder, SafeHtmlBuilder safeHtmlBuilder) {
            String html = AbstractImagePrototype.create(images.folderYellow()).getHTML();
            safeHtmlBuilder.appendHtmlConstant(html);
            safeHtmlBuilder.append(Templates.INSTANCE.nameSpan(folder.getName()));
        }
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

        sinkEvents(Event.ONCONTEXTMENU);
        sinkEvents(Event.ONMOUSEUP);
        initWidget(tree);
    }

    public Folder getSelection() {
       return model.getSelection();
    }
}
