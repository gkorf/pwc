/*
 *  Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.rest.MultiplePostCommand;
import gr.grnet.pithos.web.client.rest.PostCommand;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;

public class DnDFolderPopupMenu extends PopupPanel {

        public DnDFolderPopupMenu(final CellTreeView.Images newImages, final FolderResource target, final Object toCopy) {
                // The popup's constructor's argument is a boolean specifying that it
                // auto-close itself when the user clicks outside of it.
                super(true);
                setAnimationEnabled(true);
                // A dummy command that we will execute from unimplemented leaves.
                final Command cancelCmd = new Command() {

                        @Override
                        public void execute() {
                                hide();
                        }
                };

                final MenuBar contextMenu = new MenuBar(true);
                final CellTreeView folders = GSS.get().getTreeView();

                contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Move</span>", true, new Command() {

                                @Override
                                public void execute() {
                                        if (toCopy instanceof RestResourceWrapper){
                                                moveFolder(target, ((RestResourceWrapper) toCopy).getResource());
                                        }
                                        else if(toCopy instanceof List){
                                        	List<FileResource> files = GSS.get().getFileList().getSelectedFiles();
                                                moveFiles(target, files);
                                        }
                                        hide();
                                }

                        }).setVisible(target != null);

                contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new Command() {

                        @Override
                        public void execute() {
                                if (toCopy instanceof RestResourceWrapper)
                                        copyFolder(target, ((RestResourceWrapper) toCopy).getResource());
                                else if(toCopy instanceof List){
                                	List<FileResource> files = GSS.get().getFileList().getSelectedFiles();
                                    copyFiles(target, files);
                                }
                                hide();
                        }

                }).setVisible(target != null);

                contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.trash()).getHTML() + "&nbsp;Delete (Trash)</span>", true, new Command() {

                        @Override
                        public void execute() {
                        	GWT.log("EXECUTE TRASH:"+toCopy.getClass().getName());
                                if (toCopy instanceof RestResourceWrapper){
                                        trashFolder(((RestResourceWrapper) toCopy).getResource());
                                }
                                else if(toCopy instanceof List){
                                	List<FileResource> files = GSS.get().getFileList().getSelectedFiles();
                                    trashFiles(files);
                                }
                                hide();
                        }

                }).setVisible(target == null);
                contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Cancel</span>", true, cancelCmd);

                add(contextMenu);

        }

        private void copyFolder(final FolderResource target, FolderResource toCopy) {
                String atarget = target.getUri();
                atarget = atarget.endsWith("/") ? atarget : atarget + '/';
                atarget = atarget + toCopy.getName();
                PostCommand cf = new PostCommand(toCopy.getUri() + "?copy=" + atarget, "", 200) {

                        @Override
                        public void onComplete() {
                        		GSS.get().getTreeView().updateNodeChildren(new RestResourceWrapper(target));
								GSS.get().getStatusPanel().updateStats();
                        }

                        @Override
                        public void onError(Throwable t) {
                                GWT.log("", t);
                                if (t instanceof RestException) {
                                        int statusCode = ((RestException) t).getHttpStatusCode();
                                        if (statusCode == 405)
                                                GSS.get().displayError("You don't have the necessary permissions");

                                        else if (statusCode == 409)
                                                GSS.get().displayError("A folder with the same name already exists");
                                        else if (statusCode == 413)
                                                GSS.get().displayError("Your quota has been exceeded");
                                        else
                                                GSS.get().displayError("Unable to copy folder:" + ((RestException)t).getHttpStatusText());
                                } else
                                        GSS.get().displayError("System error copying folder:" + t.getMessage());
                        }
                };
                DeferredCommand.addCommand(cf);
        }

        private void moveFolder(final FolderResource target, final FolderResource toCopy) {
                String atarget = target.getUri();
                atarget = atarget.endsWith("/") ? atarget : atarget + '/';
                atarget = atarget + toCopy.getName();

                PostCommand cf = new PostCommand(toCopy.getUri() + "?move=" + atarget, "", 200) {

                        @Override
                        public void onComplete() {
                        	GWT.log("[MOVE]"+target.getUri()+"   "+ toCopy.getParentURI());
                        	GSS.get().getTreeView().updateNodeChildren(new RestResourceWrapper(target));
							GSS.get().getTreeView().updateNodeChildrenForRemove(toCopy.getParentURI());
							GSS.get().getStatusPanel().updateStats();
                        }

                        @Override
                        public void onError(Throwable t) {
                                GWT.log("", t);
                                if (t instanceof RestException) {
                                        int statusCode = ((RestException) t).getHttpStatusCode();
                                        if (statusCode == 405)
                                                GSS.get().displayError("You don't have the necessary permissions");

                                        else if (statusCode == 409)
                                                GSS.get().displayError("A folder with the same name already exists");
                                        else if (statusCode == 413)
                                                GSS.get().displayError("Your quota has been exceeded");
                                        else
                                                GSS.get().displayError("Unable to copy folder:" + ((RestException)t).getHttpStatusText());
                                } else
                                        GSS.get().displayError("System error copying folder:" + t.getMessage());
                        }
                };
                DeferredCommand.addCommand(cf);
        }

        private void copyFiles(final FolderResource ftarget, List<FileResource> files) {
                List<String> fileIds = new ArrayList<String>();
                String target = ftarget.getUri();
                target = target.endsWith("/") ? target : target + '/';
                for (FileResource fileResource : files) {
                        String fileTarget = target + URL.encodeComponent(fileResource.getName());
                        fileIds.add(fileResource.getUri() + "?copy=" + fileTarget);
                }
                int index = 0;
                executeCopyOrMoveFiles(index, fileIds);

        }

        private void moveFiles(final FolderResource ftarget, List<FileResource> files) {
                List<String> fileIds = new ArrayList<String>();
                String target = ftarget.getUri();
                target = target.endsWith("/") ? target : target + '/';
                for (FileResource fileResource : files) {
                        String fileTarget = target + URL.encodeComponent(fileResource.getName());
                        fileIds.add(fileResource.getUri() + "?move=" + fileTarget);
                }
                int index = 0;
                executeCopyOrMoveFiles(index, fileIds);

        }

        private void trashFolder(final FolderResource folder){
                PostCommand tot = new PostCommand(folder.getUri()+"?trash=","",200){

                        @Override
                        public void onComplete() {
                        	GSS.get().getTreeView().updateNodeChildrenForRemove(folder.getParentURI());
                        	GSS.get().getTreeView().updateTrashNode();
                                /*for(TreeItem item : items)
                                        GSS.get().getFolders().updateFolder((DnDTreeItem) item);
                                GSS.get().getFolders().update(GSS.get().getFolders().getTrashItem());
                               
                                GSS.get().showFileList(true);
                                */
                        }

                        @Override
                        public void onError(Throwable t) {
                                GWT.log("", t);
                                if(t instanceof RestException){
                                        int statusCode = ((RestException)t).getHttpStatusCode();
                                        if(statusCode == 405)
                                                GSS.get().displayError("You don't have the necessary permissions");
                                        else if(statusCode == 404)
                                                GSS.get().displayError("Folder does not exist");
                                        else
                                                GSS.get().displayError("Unable to trash folder:"+((RestException)t).getHttpStatusText());
                                }
                                else
                                        GSS.get().displayError("System error trashing folder:"+t.getMessage());
                        }
                };
                DeferredCommand.addCommand(tot);
        }

        private void trashFiles(List<FileResource> files){
                final List<String> fileIds = new ArrayList<String>();
                for(FileResource f : files)
                        fileIds.add(f.getUri()+"?trash=");
                MultiplePostCommand tot = new MultiplePostCommand(fileIds.toArray(new String[0]),200){

                        @Override
                        public void onComplete() {
                                GSS.get().showFileList(true);
                        }

                        @Override
                        public void onError(String p, Throwable t) {
                                GWT.log("", t);
                                if(t instanceof RestException){
                                        int statusCode = ((RestException)t).getHttpStatusCode();
                                        if(statusCode == 405)
                                                GSS.get().displayError("You don't have the necessary permissions");
                                        else if(statusCode == 404)
                                                GSS.get().displayError("File does not exist");
                                        else
                                                GSS.get().displayError("Unable to trash file:"+((RestException)t).getHttpStatusText());
                                }
                                else
                                        GSS.get().displayError("System error trashing file:"+t.getMessage());
                        }
                };
                DeferredCommand.addCommand(tot);
        }


        private void executeCopyOrMoveFiles(final int index, final List<String> paths) {
                if (index >= paths.size()) {
                        GSS.get().showFileList(true);
                        GSS.get().getStatusPanel().updateStats();
                        return;
                }
                PostCommand cf = new PostCommand(paths.get(index), "", 200) {

                        @Override
                        public void onComplete() {
                                executeCopyOrMoveFiles(index + 1, paths);
                        }

                        @Override
                        public void onError(Throwable t) {
                                GWT.log("", t);
                                if (t instanceof RestException) {
                                        int statusCode = ((RestException) t).getHttpStatusCode();
                                        if (statusCode == 405)
                                                GSS.get().displayError("You don't have the necessary permissions");
                                        else if (statusCode == 404)
                                                GSS.get().displayError("File not found");
                                        else if (statusCode == 409)
                                                GSS.get().displayError("A file with the same name already exists");
                                        else if (statusCode == 413)
                                                GSS.get().displayError("Your quota has been exceeded");
                                        else
                                                GSS.get().displayError("Unable to copy file:" + ((RestException)t).getHttpStatusText());
                                } else
                                        GSS.get().displayError("System error copying file:" + t.getMessage());

                        }
                };
                DeferredCommand.addCommand(cf);
        }

}