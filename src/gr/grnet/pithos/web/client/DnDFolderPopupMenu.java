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
import gr.grnet.pithos.web.client.rest.MultiplePostCommand;
import gr.grnet.pithos.web.client.rest.PostCommand;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FolderResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;

public class DnDFolderPopupMenu extends PopupPanel {

    private Pithos app;

    public DnDFolderPopupMenu(Pithos _app, final CellTreeView.Images newImages, final Folder target, final Object toCopy) {
        // The popup's constructor's argument is a boolean specifying that it
        // auto-close itself when the user clicks outside of it.
        super(true);
        app = _app;
        setAnimationEnabled(true);
        // A dummy command that we will execute from unimplemented leaves.
        final Command cancelCmd = new Command() {

            @Override
            public void execute() {
                hide();
            }
        };

        final MenuBar contextMenu = new MenuBar(true);
        final CellTreeView folders = app.getTreeView();

        contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.cut()).getHTML() + "&nbsp;Move</span>", true, new Command() {

            @Override
            public void execute() {
                if (toCopy instanceof Folder) {
                    moveFolder(target, (Folder) toCopy);
                }
                else if (toCopy instanceof List) {
                    List<File> files = app.getFileList().getSelectedFiles();
                    moveFiles(target, files);
                }
                hide();
            }
        }).setVisible(target != null);

        contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.copy()).getHTML() + "&nbsp;Copy</span>", true, new Command() {

            @Override
            public void execute() {
                if (toCopy instanceof Folder)
                    copyFolder(target, (Folder) toCopy);
                else if (toCopy instanceof List) {
                    List<File> files = app.getFileList().getSelectedFiles();
                    copyFiles(target, files);
                }
                hide();
            }
        }).setVisible(target != null);

        contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.trash()).getHTML() + "&nbsp;Delete (Trash)</span>", true, new Command() {

            @Override
            public void execute() {
                GWT.log("EXECUTE TRASH:" + toCopy.getClass().getName());
                if (toCopy instanceof RestResourceWrapper) {
                    trashFolder(((RestResourceWrapper) toCopy).getResource());
                }
                else if (toCopy instanceof List) {
                    List<File> files = app.getFileList().getSelectedFiles();
                    trashFiles(files);
                }
                hide();
            }
        }).setVisible(target == null);
        contextMenu.addItem("<span>" + AbstractImagePrototype.create(newImages.delete()).getHTML() + "&nbsp;Cancel</span>", true, cancelCmd);

        add(contextMenu);
    }

    private void copyFolder(final Folder target, Folder toCopy) {
//        String atarget = target.getUri();
//        atarget = atarget.endsWith("/") ? atarget : atarget + '/';
//        atarget = atarget + toCopy.getName();
//        PostCommand cf = new PostCommand(toCopy.getUri() + "?copy=" + atarget, "", 200) {
//
//            @Override
//            public void onComplete() {
//                app.getTreeView().updateNodeChildren(new RestResourceWrapper(target));
//                app.getStatusPanel().updateStats();
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                GWT.log("", t);
//                if (t instanceof RestException) {
//                    int statusCode = ((RestException) t).getHttpStatusCode();
//                    if (statusCode == 405)
//                        app.displayError("You don't have the necessary permissions");
//
//                    else if (statusCode == 409)
//                        app.displayError("A folder with the same name already exists");
//                    else if (statusCode == 413)
//                        app.displayError("Your quota has been exceeded");
//                    else
//                        app.displayError("Unable to copy folder:" + ((RestException) t).getHttpStatusText());
//                }
//                else
//                    app.displayError("System error copying folder:" + t.getMessage());
//            }
//        };
//        DeferredCommand.addCommand(cf);
    }

    private void moveFolder(final Folder target, final Folder toCopy) {
//        String atarget = target.getUri();
//        atarget = atarget.endsWith("/") ? atarget : atarget + '/';
//        atarget = atarget + toCopy.getName();
//
//        PostCommand cf = new PostCommand(toCopy.getUri() + "?move=" + atarget, "", 200) {
//
//            @Override
//            public void onComplete() {
//                GWT.log("[MOVE]" + target.getUri() + "   " + toCopy.getParentURI());
//                app.getTreeView().updateNodeChildren(new RestResourceWrapper(target));
//                app.getTreeView().updateNodeChildrenForRemove(toCopy.getParentURI());
//                app.getStatusPanel().updateStats();
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                GWT.log("", t);
//                if (t instanceof RestException) {
//                    int statusCode = ((RestException) t).getHttpStatusCode();
//                    if (statusCode == 405)
//                        app.displayError("You don't have the necessary permissions");
//
//                    else if (statusCode == 409)
//                        app.displayError("A folder with the same name already exists");
//                    else if (statusCode == 413)
//                        app.displayError("Your quota has been exceeded");
//                    else
//                        app.displayError("Unable to copy folder:" + ((RestException) t).getHttpStatusText());
//                }
//                else
//                    app.displayError("System error copying folder:" + t.getMessage());
//            }
//        };
//        DeferredCommand.addCommand(cf);
    }

    private void copyFiles(final Folder ftarget, List<File> files) {
//        List<String> fileIds = new ArrayList<String>();
//        String target = ftarget.getUri();
//        target = target.endsWith("/") ? target : target + '/';
//        for (File file : files) {
//            String fileTarget = target + URL.encodeComponent(file.getName());
//            fileIds.add(file.getUri() + "?copy=" + fileTarget);
//        }
//        int index = 0;
//        executeCopyOrMoveFiles(index, fileIds);
    }

    private void moveFiles(final Folder ftarget, List<File> files) {
//        List<String> fileIds = new ArrayList<String>();
//        String target = ftarget.getUri();
//        target = target.endsWith("/") ? target : target + '/';
//        for (File file : files) {
//            String fileTarget = target + URL.encodeComponent(file.getName());
//            fileIds.add(file.getUri() + "?move=" + fileTarget);
//        }
//        int index = 0;
//        executeCopyOrMoveFiles(index, fileIds);
    }

    private void trashFolder(final FolderResource folder) {
        PostCommand tot = new PostCommand(app, folder.getUri() + "?trash=", "", 200) {

            @Override
            public void onComplete() {
                app.getTreeView().updateNodeChildrenForRemove(folder.getParentURI());
                app.getTreeView().updateTrashNode();
                /*for(TreeItem item : items)
                        app.getFolders().updateFolder((DnDTreeItem) item);
                app.getFolders().update(app.getFolders().getTrashItem());

                app.showFileList(true);
                */
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("", t);
                if (t instanceof RestException) {
                    int statusCode = ((RestException) t).getHttpStatusCode();
                    if (statusCode == 405)
                        app.displayError("You don't have the necessary permissions");
                    else if (statusCode == 404)
                        app.displayError("Folder does not exist");
                    else
                        app.displayError("Unable to trash folder:" + ((RestException) t).getHttpStatusText());
                }
                else
                    app.displayError("System error trashing folder:" + t.getMessage());
            }
        };
        DeferredCommand.addCommand(tot);
    }

    private void trashFiles(List<File> files) {
        final List<String> fileIds = new ArrayList<String>();
        for (File f : files)
            fileIds.add(f.getUri() + "?trash=");
        MultiplePostCommand tot = new MultiplePostCommand(app, fileIds.toArray(new String[0]), 200) {

            @Override
            public void onComplete() {
                app.showFileList(true);
            }

            @Override
            public void onError(String p, Throwable t) {
                GWT.log("", t);
                if (t instanceof RestException) {
                    int statusCode = ((RestException) t).getHttpStatusCode();
                    if (statusCode == 405)
                        app.displayError("You don't have the necessary permissions");
                    else if (statusCode == 404)
                        app.displayError("File does not exist");
                    else
                        app.displayError("Unable to trash file:" + ((RestException) t).getHttpStatusText());
                }
                else
                    app.displayError("System error trashing file:" + t.getMessage());
            }
        };
        DeferredCommand.addCommand(tot);
    }

    private void executeCopyOrMoveFiles(final int index, final List<String> paths) {
        if (index >= paths.size()) {
            app.showFileList(true);
            app.getStatusPanel().updateStats();
            return;
        }
        PostCommand cf = new PostCommand(app, paths.get(index), "", 200) {

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
                        app.displayError("You don't have the necessary permissions");
                    else if (statusCode == 404)
                        app.displayError("File not found");
                    else if (statusCode == 409)
                        app.displayError("A file with the same name already exists");
                    else if (statusCode == 413)
                        app.displayError("Your quota has been exceeded");
                    else
                        app.displayError("Unable to copy file:" + ((RestException) t).getHttpStatusText());
                }
                else
                    app.displayError("System error copying file:" + t.getMessage());
            }
        };
        DeferredCommand.addCommand(cf);
    }
}