/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client;

import static com.google.gwt.query.client.GQuery.$;

import gr.grnet.pithos.web.client.commands.UploadFileCommand;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.FolderTreeView;
import gr.grnet.pithos.web.client.rest.GetCommand;
import gr.grnet.pithos.web.client.rest.RestCommand;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.OtherUserResource;
import gr.grnet.pithos.web.client.rest.resource.OthersFolderResource;
import gr.grnet.pithos.web.client.rest.resource.OthersResource;
import gr.grnet.pithos.web.client.rest.resource.RestResource;
import gr.grnet.pithos.web.client.rest.resource.RestResourceWrapper;
import gr.grnet.pithos.web.client.rest.resource.SharedResource;
import gr.grnet.pithos.web.client.rest.resource.TrashFolderResource;
import gr.grnet.pithos.web.client.rest.resource.TrashResource;
import gr.grnet.pithos.web.client.rest.resource.UserResource;
import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.StopDragException;
import gwtquery.plugins.draggable.client.DraggableOptions.DragFunction;
import gwtquery.plugins.draggable.client.DraggableOptions.RevertOption;
import gwtquery.plugins.draggable.client.events.DragContext;
import gwtquery.plugins.draggable.client.events.DragStartEvent;
import gwtquery.plugins.draggable.client.events.DragStopEvent;
import gwtquery.plugins.draggable.client.events.DragStartEvent.DragStartEventHandler;
import gwtquery.plugins.draggable.client.events.DragStopEvent.DragStopEventHandler;
import gwtquery.plugins.droppable.client.gwt.DragAndDropCellTable;
import gwtquery.plugins.droppable.client.gwt.DragAndDropColumn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.GssSimplePager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import java.util.Set;

/**
 * A composite that displays the list of files in a particular folder.
 */
public class FileList extends Composite {

	ListDataProvider<File> provider = new ListDataProvider<File>();

    /**
       * The styles applied to the table.
       */
    interface TableStyle extends CellTable.Style {
    }

	interface TableResources extends DragAndDropCellTable.Resources {
	    @Source({CellTable.Style.DEFAULT_CSS, "GssCellTable.css"})
	    TableStyle cellTableStyle();
	}
	
	static interface Templates extends SafeHtmlTemplates {
	    Templates INSTANCE = GWT.create(Templates.class);

	    @Template("<div id='dragHelper' style='border:1px solid black; background-color:#ffffff; color:black; width:150px;z-index:100'></div>")
	    SafeHtml outerHelper();

        @Template("<span id='{0}'>{0}</span>")
        public SafeHtml filenameSpan(String filename);

        @Template("<a href='{0}' title='{1}' rel='lytebox[mnf]' onclick='myLytebox.start(this, false, false); return false;'>(view)</a>")
        public SafeHtml viewLink(String link, String title);

        @Template("<table><tr><td rowspan='3'>{0}</td><td style='font-size:95%;' id='{1}'>{1}</td></tr><tr><td>{2}</td></tr></table>")
        public SafeHtml rendelContactCell(String imageHtml, String name, String fileSize);

        @Template("<span id='{0}' class='{1}'>{2}</span>")
        public SafeHtml spanWithIdAndClass(String id, String cssClass, String content);
	}

    private String showingStats = "";

	private int startIndex = 0;

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	private final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");

	/**
	 * Specifies that the images available for this composite will be the ones
	 * available in FileContextMenu.
	 */
	public interface Images extends ClientBundle,FileContextMenu.Images, CellTreeView.Images {

		@Source("gr/grnet/pithos/resources/blank.gif")
		ImageResource blank();

		@Source("gr/grnet/pithos/resources/asc.png")
		ImageResource asc();

		@Source("gr/grnet/pithos/resources/desc.png")
		ImageResource desc();

		@Source("gr/grnet/pithos/resources/mimetypes/document_shared.png")
		ImageResource documentShared();

		@Source("gr/grnet/pithos/resources/mimetypes/kcmfontinst.png")
		ImageResource wordprocessor();

		@Source("gr/grnet/pithos/resources/mimetypes/log.png")
		ImageResource spreadsheet();

		@Source("gr/grnet/pithos/resources/mimetypes/kpresenter_kpr.png")
		ImageResource presentation();

		@Source("gr/grnet/pithos/resources/mimetypes/acroread.png")
		ImageResource pdf();

		@Source("gr/grnet/pithos/resources/mimetypes/image.png")
		ImageResource image();

		@Source("gr/grnet/pithos/resources/mimetypes/video2.png")
		ImageResource video();

		@Source("gr/grnet/pithos/resources/mimetypes/knotify.png")
		ImageResource audio();

		@Source("gr/grnet/pithos/resources/mimetypes/html.png")
		ImageResource html();

		@Source("gr/grnet/pithos/resources/mimetypes/txt.png")
		ImageResource txt();

		@Source("gr/grnet/pithos/resources/mimetypes/ark2.png")
		ImageResource zip();

		@Source("gr/grnet/pithos/resources/mimetypes/kcmfontinst_shared.png")
		ImageResource wordprocessorShared();

		@Source("gr/grnet/pithos/resources/mimetypes/log_shared.png")
		ImageResource spreadsheetShared();

		@Source("gr/grnet/pithos/resources/mimetypes/kpresenter_kpr_shared.png")
		ImageResource presentationShared();

		@Source("gr/grnet/pithos/resources/mimetypes/acroread_shared.png")
		ImageResource pdfShared();

		@Source("gr/grnet/pithos/resources/mimetypes/image_shared.png")
		ImageResource imageShared();

		@Source("gr/grnet/pithos/resources/mimetypes/video2_shared.png")
		ImageResource videoShared();

		@Source("gr/grnet/pithos/resources/mimetypes/knotify_shared.png")
		ImageResource audioShared();

		@Source("gr/grnet/pithos/resources/mimetypes/html_shared.png")
		ImageResource htmlShared();

		@Source("gr/grnet/pithos/resources/mimetypes/txt_shared.png")
		ImageResource txtShared();

		@Source("gr/grnet/pithos/resources/mimetypes/ark2_shared.png")
		ImageResource zipShared();

	}
	
	/**
	 * The number of files in this folder.
	 */
	int folderFileCount;

	/**
	 * Total folder size
	 */
	long folderTotalSize;

	/**
	 * A cache of the files in the list.
	 */
	private List<File> files;

	/**
	 * The widget's image bundle.
	 */
	private final Images images;
	
	private FileContextMenu menuShowing;

	private DragAndDropCellTable<File> celltable;

	private final MultiSelectionModel<File> selectionModel;

	private final List<SortableHeader> allHeaders = new ArrayList<SortableHeader>();

	SortableHeader nameHeader;

	GssSimplePager pagerBottom;

	GssSimplePager pagerTop;

	Button uploadButtonBottom;

	Button uploadButtonTop;

    FolderTreeView treeView;

    /**
	 * Construct the file list widget. This entails setting up the widget
	 * layout, fetching the number of files in the current folder from the
	 * server and filling the local file cache of displayed files with data from
	 * the server, as well.
	 *
	 * @param _images
	 */
	public FileList(Images _images, FolderTreeView treeView) {
		images = _images;
        this.treeView = treeView;

        DragAndDropCellTable.Resources resources = GWT.create(TableResources.class);

        ProvidesKey<File> keyProvider = new ProvidesKey<File>(){

			@Override
			public Object getKey(File item) {
				return item.getUri();
			}
		};

		celltable = new DragAndDropCellTable<File>(GSS.VISIBLE_FILE_COUNT, resources, keyProvider);
        celltable.setWidth("100%");
        celltable.setStyleName("pithos-List");

		DragAndDropColumn<File, ImageResource> status = new DragAndDropColumn<File, ImageResource>(new ImageResourceCell() {
		    @Override
	        public boolean handlesSelection() {
	            return false;
	        }
		})
        {
	         @Override
	         public ImageResource getValue(File entity) {
	             return getFileIcon(entity);
	         }
	    };
	    celltable.addColumn(status,"");
	    initDragOperation(status);

        final DragAndDropColumn<File,SafeHtml> nameColumn = new DragAndDropColumn<File,SafeHtml>(new SafeHtmlCell()) {

			@Override
			public SafeHtml getValue(File object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.append(Templates.INSTANCE.filenameSpan(object.getName()));
				if (object.getContentType().endsWith("png") || object.getContentType().endsWith("gif") || object.getContentType().endsWith("jpeg")) {
        			sb.appendHtmlConstant("&nbsp;")
                      .append(Templates.INSTANCE.viewLink(object.getUri(), object.getOwner() + " : " + object.getPath() + object.getName()));
				}
				
				return sb.toSafeHtml();
			}
			
		};
        initDragOperation(nameColumn);
        celltable.addColumn(nameColumn, nameHeader = new SortableHeader("Name"));
		allHeaders.add(nameHeader);
		nameHeader.setUpdater(new FileValueUpdater(nameHeader, "name"));

		celltable.redrawHeaders();
		
	    DragAndDropColumn<File,String> aColumn = new DragAndDropColumn<File, String>(new TextCell()) {
			@Override
			public String getValue(File object) {
				return object.getOwner();
			}
		};
        SortableHeader aheader = new SortableHeader("Owner");
		celltable.addColumn(aColumn, aheader);
		initDragOperation(aColumn);
		allHeaders.add(aheader);
        aheader.setUpdater(new FileValueUpdater(aheader, "owner"));

        aColumn = new DragAndDropColumn<File,String>(new TextCell()) {
			@Override
			public String getValue(File object) {
				return object.getPath();
			}
		};
        aheader = new SortableHeader("Path");
		celltable.addColumn(aColumn, aheader);
		initDragOperation(aColumn);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "path"));

        aColumn = new DragAndDropColumn<File,String>(new TextCell()) {
			@Override
			public String getValue(File object) {
    			return String.valueOf(object.getVersion());
			}
		};
        aheader = new SortableHeader("Version");
		celltable.addColumn(aColumn, aheader);
		initDragOperation(aColumn);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "version"));

        aColumn = new DragAndDropColumn<File,String>(new TextCell()) {
			@Override
			public String getValue(File object) {
				// TODO Auto-generated method stub
				return object.getSizeAsString();
			}
		};
        aheader = new SortableHeader("Size");
        celltable.addColumn(aColumn, aheader);
		initDragOperation(aColumn);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "size"));

        aColumn = new DragAndDropColumn<File,String>(new TextCell()) {
			@Override
			public String getValue(File object) {
				return formatter.format(object.getLastModified());
			}
		};
        aheader = new SortableHeader("Last Modified");
		celltable.addColumn(aColumn, aheader);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "date"));
	       
		provider.addDataDisplay(celltable);

		celltable.addDragStopHandler(new DragStopEventHandler() {

	    	@Override
		    public void onDragStop(DragStopEvent event) {
			    GWT.log("DRAG STOPPED");
		    }
	    });
		celltable.addDragStartHandler(new DragStartEventHandler() {

		    public void onDragStart(DragStartEvent event) {
		        FileResource value = event.getDraggableData();
		        
		        com.google.gwt.dom.client.Element helper = event.getHelper();
		        SafeHtmlBuilder sb = new SafeHtmlBuilder();
		        sb.appendHtmlConstant("<b>");
		        DisplayHelper.log(value.getName());
		        if(getSelectedFiles().size()==1)
		        	sb.appendEscaped(value.getName());
		        else
		        	sb.appendEscaped(getSelectedFiles().size()+" files");
		        sb.appendHtmlConstant("</b>");
		        helper.setInnerHTML(sb.toSafeHtml().asString());
		    }
		});

		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("100%");

		pagerTop = new GssSimplePager(GssSimplePager.TextLocation.CENTER);
        pagerTop.setVisible(false);
		pagerTop.setDisplay(celltable);
		uploadButtonTop = new Button("<span id='topMenu.file.upload'>" + AbstractImagePrototype.create(images.fileUpdate()).getHTML() + "&nbsp;Upload</span>");
		uploadButtonTop.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				new UploadFileCommand(null).execute();
			}
		});
		HorizontalPanel topPanel = new HorizontalPanel();
		topPanel.add(pagerTop);
		topPanel.add(uploadButtonTop);
		vp.add(topPanel);

        vp.add(celltable);

		pagerBottom = new GssSimplePager(GssSimplePager.TextLocation.CENTER);
        pagerBottom.setVisible(false);
		pagerBottom.setDisplay(celltable);
		uploadButtonBottom=new Button("<span id='topMenu.file.upload'>" + AbstractImagePrototype.create(images.fileUpdate()).getHTML() + "&nbsp;Upload</span>");
		uploadButtonBottom.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				new UploadFileCommand(null).execute();
			}
		});
        HorizontalPanel bottomPanel = new HorizontalPanel();
        bottomPanel.add(pagerBottom);
		bottomPanel.add(uploadButtonBottom);

		vp.add(bottomPanel);
		vp.setCellWidth(celltable, "100%");
		initWidget(vp);

		selectionModel = new MultiSelectionModel<File>(keyProvider);

		 Handler selectionHandler = new SelectionChangeEvent.Handler() {
             @Override 
             public void onSelectionChange(SelectionChangeEvent event) {
            	 if(getSelectedFiles().size() == 1)
            		 GSS.get().setCurrentSelection(getSelectedFiles().get(0));
            	 else
            		 GSS.get().setCurrentSelection(getSelectedFiles());
             }
         };
         selectionModel.addSelectionChangeHandler(selectionHandler);
         
		celltable.setSelectionModel(selectionModel, GSSSelectionEventManager.<File> createDefaultManager());
		celltable.setPageSize(GSS.VISIBLE_FILE_COUNT);
		
		sinkEvents(Event.ONCONTEXTMENU);
		sinkEvents(Event.ONMOUSEUP);
		sinkEvents(Event.ONMOUSEDOWN);
		sinkEvents(Event.ONCLICK);
		sinkEvents(Event.ONKEYDOWN);
		sinkEvents(Event.ONDBLCLICK);
		GSS.preventIESelection();
	}

	public List<File> getSelectedFiles() {
        return new ArrayList<File>(selectionModel.getSelectedSet());
	}
	
	private void initDragOperation(DragAndDropColumn<?, ?> column) {
        // retrieve draggableOptions on the column
		DraggableOptions draggableOptions = column.getDraggableOptions();
		// use template to construct the helper. The content of the div will be set
		// after
		draggableOptions.setHelper($(Templates.INSTANCE.outerHelper().asString()));
		//draggableOptions.setZIndex(100);
		// opacity of the helper
		draggableOptions.setAppendTo("body");
		//draggableOptions.setOpacity((float) 0.8);
		draggableOptions.setContainment("document");
		// cursor to use during the drag operation
		draggableOptions.setCursor(Cursor.MOVE);
		// set the revert option
		draggableOptions.setRevert(RevertOption.ON_INVALID_DROP);
		// prevents dragging when user click on the category drop-down list
		draggableOptions.setCancel("select");
	    draggableOptions.setOnBeforeDragStart(new DragFunction() {
			@Override
			public void f(DragContext context) {
		        File value = context.getDraggableData();
				if (!selectionModel.isSelected(value)) {
    		       	throw new StopDragException();
	    	    }
			}
		});
    }
	
	public void showContextMenu(Event event){
		menuShowing = new FileContextMenu(images, false, true);
		menuShowing=menuShowing.onEmptyEvent(event);
	}
	@Override
	public void onBrowserEvent(Event event) {
		
		if (files == null || files.size() == 0) {
			if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() == 0) {
				menuShowing = new FileContextMenu(images, false, true);
				menuShowing=menuShowing.onEmptyEvent(event);
				event.preventDefault();
				event.cancelBubble(true);
			}
			return;
		}
		if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() != 0) {
			GWT.log("*****GOING TO SHOW CONTEXT MENU ****", null);
			menuShowing =  new FileContextMenu(images, false, false);
			menuShowing=menuShowing.onEvent(event);
			event.cancelBubble(true);
			event.preventDefault();
		} else if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() == 0) {
			menuShowing = new FileContextMenu(images, false, true);
			menuShowing=menuShowing.onEmptyEvent(event);
			event.cancelBubble(true);
			event.preventDefault();
		} else if (DOM.eventGetType(event) == Event.ONDBLCLICK)
			if (getSelectedFiles().size() == 1) {
				GSS app = GSS.get();
				File file = getSelectedFiles().get(0);
				Window.open(file.getUri(), "_blank", "");
				event.preventDefault();
				return;
			}
		super.onBrowserEvent(event);
	}

	/**
	 * Update the display of the file list.
	 */
	void update(boolean sort) {
		int count = folderFileCount;
		int max = startIndex + GSS.VISIBLE_FILE_COUNT;
		if (max > count)
			max = count;
		folderTotalSize = 0;
		
		for(File f : files){
			folderTotalSize += f.getBytes();
		}
		if (folderFileCount == 0) {
			showingStats = "no files";
		} else if (folderFileCount < GSS.VISIBLE_FILE_COUNT) {
			if (folderFileCount == 1)
				showingStats = "1 file";
			else
				showingStats = folderFileCount + " files";
			showingStats += " (" + FileResource.getFileSizeAsString(folderTotalSize) + ")";
		} else {
			showingStats = "" + (startIndex + 1) + " - " + max + " of " + count + " files" + " (" + FileResource.getFileSizeAsString(folderTotalSize) + ")";
		}
		showCellTable();
		updateCurrentlyShowingStats();

	}

	/**
	 * Return the proper icon based on the MIME type of the file.
	 *
	 * @param file
	 * @return the icon
	 */
	private ImageResource getFileIcon(File file) {
		String mimetype = file.getContentType();
		boolean shared = file.isShared();
		if (mimetype == null)
			return shared ? images.documentShared() : images.document();
		mimetype = mimetype.toLowerCase();
		if (mimetype.startsWith("application/pdf"))
			return shared ? images.pdfShared() : images.pdf();
		else if (mimetype.endsWith("excel"))
			return shared ? images.spreadsheetShared() : images.spreadsheet();
		else if (mimetype.endsWith("msword"))
			return shared ? images.wordprocessorShared() : images.wordprocessor();
		else if (mimetype.endsWith("powerpoint"))
			return shared ? images.presentationShared() : images.presentation();
		else if (mimetype.startsWith("application/zip") ||
					mimetype.startsWith("application/gzip") ||
					mimetype.startsWith("application/x-gzip") ||
					mimetype.startsWith("application/x-tar") ||
					mimetype.startsWith("application/x-gtar"))
			return shared ? images.zipShared() : images.zip();
		else if (mimetype.startsWith("text/html"))
			return shared ? images.htmlShared() : images.html();
		else if (mimetype.startsWith("text/plain"))
			return shared ? images.txtShared() : images.txt();
		else if (mimetype.startsWith("image/"))
			return shared ? images.imageShared() : images.image();
		else if (mimetype.startsWith("video/"))
			return shared ? images.videoShared() : images.video();
		else if (mimetype.startsWith("audio/"))
			return shared ? images.audioShared() : images.audio();
		return shared ? images.documentShared() : images.document();
	}

	/**
	 * Update status panel with currently showing file stats.
	 */
	public void updateCurrentlyShowingStats() {
		GSS.get().getStatusPanel().updateCurrentlyShowing(showingStats);
	}
	
	/**
	 * Fill the file cache with data.
	 */
	public void setFiles(final List<File> _files) {
		files = new ArrayList<File>();
    	for (File fres : _files)
	    	if (!fres.isInTrash())
				files.add(fres);
		Collections.sort(files, new Comparator<File>() {

			@Override
			public int compare(File arg0, File arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}

		});
		folderFileCount = files.size();
		
		nameHeader.setSorted(true);
		nameHeader.toggleReverseSort();
		for (SortableHeader otherHeader : allHeaders) {
	        if (otherHeader != nameHeader) {
	            otherHeader.setSorted(false);
	            otherHeader.setReverseSort(true);
	        }
	    }

        if(files.size() > GSS.VISIBLE_FILE_COUNT){
            pagerBottom.setVisible(true);
            pagerTop.setVisible(true);
        }
        else{
            pagerTop.setVisible(false);
            pagerBottom.setVisible(false);
        }
        Folder selectedItem = treeView.getSelection();

        provider.setList(files);
        provider.refresh();
        celltable.redrawHeaders();
	}

	/**
	 * Does the list contains the requested filename
	 *
	 * @param fileName
	 * @return true/false
	 */
	public boolean contains(String fileName) {
		for (int i = 0; i < files.size(); i++)
			if (files.get(i).getName().equals(fileName))
				return true;
		return false;
	}

	public void clearSelectedRows() {
		Iterator<File> it = selectionModel.getSelectedSet().iterator();
		while(it.hasNext()){
			selectionModel.setSelected(it.next(),false);
		}
	}
	

	/**
	 *
	 */
	public void selectAllRows() {
		Iterator<File> it = provider.getList().iterator();
		while(it.hasNext()){
			selectionModel.setSelected(it.next(),true);
		}


	}

	
	private void sortFiles(final String sortingProperty, final boolean sortingType){
		Collections.sort(files, new Comparator<File>() {

            @Override
            public int compare(File arg0, File arg1) {
                    AbstractImagePrototype descPrototype = AbstractImagePrototype.create(images.desc());
                    AbstractImagePrototype ascPrototype = AbstractImagePrototype.create(images.asc());
                    if (sortingType){
                            if (sortingProperty.equals("version")) {
                                    return arg0.getVersion() - arg1.getVersion();
                            } else if (sortingProperty.equals("owner")) {
                                    return arg0.getOwner().compareTo(arg1.getOwner());
                            } else if (sortingProperty.equals("date")) {
                                    return arg0.getLastModified().compareTo(arg1.getLastModified());
                            } else if (sortingProperty.equals("size")) {
                                    return (int) (arg0.getBytes() - arg1.getBytes());
                            } else if (sortingProperty.equals("name")) {
                                    return arg0.getName().compareTo(arg1.getName());
                            } else if (sortingProperty.equals("path")) {
                                    return arg0.getUri().compareTo(arg1.getUri());
                            } else {
                                    return arg0.getName().compareTo(arg1.getName());
                            }
                    }
                    else if (sortingProperty.equals("version")) {
                            
                            return arg1.getVersion() - arg0.getVersion();
                    } else if (sortingProperty.equals("owner")) {
                            
                            return arg1.getOwner().compareTo(arg0.getOwner());
                    } else if (sortingProperty.equals("date")) {
                            
                            return arg1.getLastModified().compareTo(arg0.getLastModified());
                    } else if (sortingProperty.equals("size")) {
                            return (int) (arg1.getBytes() - arg0.getBytes());
                    } else if (sortingProperty.equals("name")) {
                            
                            return arg1.getName().compareTo(arg0.getName());
                    } else if (sortingProperty.equals("path")) {
                            
                            return arg1.getUri().compareTo(arg0.getUri());
                    } else {
                            
                            return arg1.getName().compareTo(arg0.getName());
                    }
            }

		});
	}
	
	final class FileValueUpdater implements ValueUpdater<String>{
		private String property;
		private SortableHeader header;
		/**
		 * 
		 */
		public FileValueUpdater(SortableHeader header,String property) {
			this.property=property;
			this.header=header;
		}
		@Override
		public void update(String value) {
			header.setSorted(true);
			header.toggleReverseSort();

	        for (SortableHeader otherHeader : allHeaders) {
	          if (otherHeader != header) {
	            otherHeader.setSorted(false);
	            otherHeader.setReverseSort(true);
	          }
	        }
	        celltable.redrawHeaders();
	        sortFiles(property, header.getReverseSort());
	        FileList.this.update(true);			
		}
		
	}

	/**
	 * Shows the files in the cellTable 
     */
	private void showCellTable(){
		if(files.size()>GSS.VISIBLE_FILE_COUNT){
			pagerBottom.setVisible(true);
			pagerTop.setVisible(true);
		}
		else{
			pagerTop.setVisible(false);
			pagerBottom.setVisible(false);
		}
		provider.setList(files);
		
		provider.refresh();
		
		//celltable.redraw();
		celltable.redrawHeaders();		
	}
}
