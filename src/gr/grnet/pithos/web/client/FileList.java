/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */

package gr.grnet.pithos.web.client;

import static com.google.gwt.query.client.GQuery.$;

import gr.grnet.pithos.web.client.commands.UploadFileCommand;
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
import gr.grnet.pithos.web.client.rest.resource.UserSearchResource;
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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.GssSimplePager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
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

/**
 * A composite that displays the list of files in a particular folder.
 */
public class FileList extends Composite {
	ListDataProvider<FileResource> provider = new ListDataProvider<FileResource>();
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
	
	
	/**
	   * The styles applied to the table.
	   */
	  interface TableStyle extends CellTable.Style {
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
	
	DragStopEventHandler dragStop = new DragStopEventHandler() {
		
		@Override
		public void onDragStop(DragStopEvent event) {
			GWT.log("DRAG STOPPED");
			
		}
	};
	
	private static class ContactCell extends AbstractCell<gr.grnet.pithos.web.client.rest.resource.FileResource> {

	    /**
	     * The html of the image used for contacts.
	     * 
	     */
	    private final String imageHtml;

	    public ContactCell(ImageResource image) {
	      this.imageHtml = AbstractImagePrototype.create(image).getHTML();
	    }

	    

		

	    @Override
	    public void render(Context context, FileResource value, SafeHtmlBuilder sb) {
	      // Value can be null, so do a null check..
	      if (value == null) {
	        return;
	      }

          sb.append(Templates.INSTANCE.rendelContactCell(imageHtml, value.getName(), value.getFileSizeAsString()));
	    }


	  }
	/**
	 * Retrieve the celltable.
	 *
	 * @return the celltable
	 */
	public DragAndDropCellTable<FileResource> getCelltable() {
		return celltable;
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
	private List<FileResource> files;

	/**
	 * The widget's image bundle.
	 */
	private final Images images;
	
	private FileContextMenu menuShowing;
	private DragAndDropCellTable<FileResource> celltable;
	private final MultiSelectionModel<FileResource> selectionModel;
	private final List<SortableHeader> allHeaders = new ArrayList<SortableHeader>();
	SortableHeader nameHeader;
	GssSimplePager pagerBottom;
	GssSimplePager pagerTop;
	Button uploadButtonBottom;
	Button uploadButtonTop;
	/**
	 * Construct the file list widget. This entails setting up the widget
	 * layout, fetching the number of files in the current folder from the
	 * server and filling the local file cache of displayed files with data from
	 * the server, as well.
	 *
	 * @param _images
	 */
	public FileList(Images _images) {
		images = _images;
		DragAndDropCellTable.Resources resources = GWT.create(TableResources.class);
		ProvidesKey<FileResource> keyProvider = new ProvidesKey<FileResource>(){

			@Override
			public Object getKey(FileResource item) {
				return item.getUri();
			}
			
		};
		celltable = new DragAndDropCellTable<FileResource>(GSS.VISIBLE_FILE_COUNT,resources,keyProvider);
		
		DragAndDropColumn<FileResource, ImageResource> status = new DragAndDropColumn<FileResource, ImageResource>(new ImageResourceCell(){
			@Override
	          public boolean handlesSelection() {
	        	    return false;
	        	  }
		}) {
	          @Override
	          public ImageResource getValue(FileResource entity) {
	            return getFileIcon(entity);
	          }
	          
	       };
	    celltable.addColumn(status,"");
	    
	    initDragOperation(status);
		final DragAndDropColumn<FileResource,SafeHtml> nameColumn = new DragAndDropColumn<FileResource,SafeHtml>(new SafeHtmlCell()) {

			@Override
			public SafeHtml getValue(FileResource object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.append(Templates.INSTANCE.filenameSpan(object.getName()));
				if (object.getContentType().endsWith("png") || object.getContentType().endsWith("gif") || object.getContentType().endsWith("jpeg") ){
					sb.appendHtmlConstant("&nbsp;").append(Templates.INSTANCE.viewLink(GSS.get().getTopPanel().getFileMenu().getDownloadURL(object), object.getOwner() + " : " + object.getPath() + object.getName()));
				}
				
				return sb.toSafeHtml();
			}
			
		};
		initDragOperation(nameColumn);
		celltable.addColumn(nameColumn,nameHeader = new SortableHeader("Name"));
		allHeaders.add(nameHeader);
		//nameHeader.setSorted(true);
		//nameHeader.toggleReverseSort();
		nameHeader.setUpdater(new FileValueUpdater(nameHeader, "name"));
		celltable.redrawHeaders();
		
		
	    
	    
	    SortableHeader aheader;
	    DragAndDropColumn<FileResource,String> aColumn;
		celltable.addColumn(aColumn=new DragAndDropColumn<FileResource,String>(new TextCell()) {
			@Override
			public String getValue(FileResource object) {
				return GSS.get().findUserFullName(object.getOwner());
			}			
		},aheader = new SortableHeader("Owner"));
		initDragOperation(aColumn);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "owner"));
		celltable.addColumn(aColumn=new DragAndDropColumn<FileResource,String>(new TextCell()) {
			@Override
			public String getValue(FileResource object) {
				// TODO Auto-generated method stub
				return object.getPath();
			}			
		},aheader = new SortableHeader("Path"));
		initDragOperation(aColumn);
		allHeaders.add(aheader);
		
		aheader.setUpdater(new FileValueUpdater(aheader, "path"));	
		celltable.addColumn(aColumn=new DragAndDropColumn<FileResource,String>(new TextCell()) {
			@Override
			public String getValue(FileResource object) {
				if(object.isVersioned())
					return object.getVersion().toString();
				return "-";
			}			
		},aheader = new SortableHeader("Version"));
		initDragOperation(aColumn);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "version"));
		celltable.addColumn(aColumn=new DragAndDropColumn<FileResource,String>(new TextCell()) {
			@Override
			public String getValue(FileResource object) {
				// TODO Auto-generated method stub
				return object.getFileSizeAsString();
			}			
		},aheader = new SortableHeader("Size"));
		initDragOperation(aColumn);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "size"));	
		celltable.addColumn(aColumn=new DragAndDropColumn<FileResource,String>(new TextCell()) {
			@Override
			public String getValue(FileResource object) {
				return formatter.format(object.getModificationDate());
			}			
		},aheader = new SortableHeader("Last Modified"));
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "date"));
	       
		
		provider.addDataDisplay(celltable);
		celltable.addDragStopHandler(dragStop);
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
		pagerTop.setDisplay(celltable);	
		uploadButtonTop=new Button("<span id='topMenu.file.upload'>" + AbstractImagePrototype.create(images.fileUpdate()).getHTML() + "&nbsp;Upload</span>");
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
		celltable.setWidth("100%");
		vp.add(celltable);
		pagerBottom = new GssSimplePager(GssSimplePager.TextLocation.CENTER);
		pagerBottom.setDisplay(celltable);
		HorizontalPanel bottomPanel = new HorizontalPanel();
		bottomPanel.add(pagerBottom);
		uploadButtonBottom=new Button("<span id='topMenu.file.upload'>" + AbstractImagePrototype.create(images.fileUpdate()).getHTML() + "&nbsp;Upload</span>");
		uploadButtonBottom.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				new UploadFileCommand(null).execute();
			}
		});
		bottomPanel.add(uploadButtonBottom);
		vp.add(bottomPanel);
		vp.setCellWidth(celltable, "100%");
		
		initWidget(vp);
		pagerBottom.setVisible(false);
		pagerTop.setVisible(false);

		celltable.setStyleName("pithos-List");
		selectionModel = new MultiSelectionModel<FileResource>(keyProvider);
		

		 Handler selectionHandler = new SelectionChangeEvent.Handler() { 
             @Override 
             public void onSelectionChange(com.google.gwt.view.client.SelectionChangeEvent event) {
            	 if(getSelectedFiles().size()==1)
            		 GSS.get().setCurrentSelection(getSelectedFiles().get(0));
            	 else
            		 GSS.get().setCurrentSelection(getSelectedFiles());
             }
         };
         selectionModel.addSelectionChangeHandler(selectionHandler);
         
		celltable.setSelectionModel(selectionModel,GSSSelectionEventManager.<FileResource>createDefaultManager());
		celltable.setPageSize(GSS.VISIBLE_FILE_COUNT);
		
		//celltable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		Scheduler.get().scheduleIncremental(new RepeatingCommand() {

			@Override
			public boolean execute() {
				return fetchRootFolder();
			}
		});
		sinkEvents(Event.ONCONTEXTMENU);
		sinkEvents(Event.ONMOUSEUP);
		sinkEvents(Event.ONMOUSEDOWN);
		sinkEvents(Event.ONCLICK);
		sinkEvents(Event.ONKEYDOWN);
		sinkEvents(Event.ONDBLCLICK);
		GSS.preventIESelection();
	}
	
	//public native void fireClickEvent(Element element) /*-{
	  //  var evObj = $doc.createEvent('MouseEvents');
	    //evObj.initEvent('click', true, true);
	    //element.dispatchEvent(evObj);
  	//}-*/;
	
	
	
	 public List<FileResource> getSelectedFiles() {
         return new ArrayList<FileResource>(selectionModel.getSelectedSet());
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
					 FileResource value = context.getDraggableData();
				     if(!selectionModel.isSelected(value)){
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
				FileResource file = getSelectedFiles().get(0);
				String dateString = RestCommand.getDate();
				String resource = file.getUri().substring(app.getApiPath().length() - 1, file.getUri().length());
				String sig = app.getCurrentUserResource().getUsername() + " " +
						RestCommand.calculateSig("GET", dateString, resource,
						RestCommand.base64decode(app.getToken()));
				Window.open(file.getUri() + "?Authorization=" + URL.encodeComponent(sig) + "&Date=" + URL.encodeComponent(dateString), "_blank", "");
				event.preventDefault();
				return;
			}
		super.onBrowserEvent(event);
	}

	/**
	 * Retrieve the root folder for the current user.
	 *
	 * @return true if the retrieval was successful
	 */
	protected boolean fetchRootFolder() {
		UserResource user = GSS.get().getCurrentUserResource();
		if (user == null)
			return !DONE;
		// Update cache and clear selection.
		updateFileCache(true);
		return DONE;
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
		
		copyListAndContinue(files);
		for(FileResource f : files){
			folderTotalSize += f.getContentLength();
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
	private ImageResource getFileIcon(FileResource file) {
		String mimetype = file.getContentType();
		boolean shared = false;
		if(GSS.get().getTreeView().getSelection()!=null && (GSS.get().getTreeView().getSelection() instanceof OtherUserResource || GSS.get().getTreeView().getSelection() instanceof OthersFolderResource)){
			OtherUserResource otherUser = null;
			if(GSS.get().getTreeView().getSelection() instanceof OtherUserResource)
				otherUser = (OtherUserResource) GSS.get().getTreeView().getSelection();
			else if (GSS.get().getTreeView().getSelection() instanceof OthersFolderResource){
				otherUser = GSS.get().getTreeView().getOtherUserResourceOfOtherFolder((OthersFolderResource) GSS.get().getTreeView().getSelection());
			}
			if(otherUser ==null)
				shared=false;
			else{
				String uname = otherUser.getUsername();
				if(uname==null)
					uname = GSS.get().getTreeView().getOthers().getUsernameOfUri(otherUser.getUri());
				if(uname != null)
					shared = file.isShared();
			}
		}
		else
			shared = file.isShared();
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
	
	public void updateFileCache(boolean clearSelection){
		if(clearSelection){
			clearSelectedRows();
		}
		
		final RestResource folderItem = GSS.get().getTreeView().getSelection();
		// Validation.
		if (folderItem == null || folderItem.equals(GSS.get().getTreeView().getOthers())) {
			setFiles(new ArrayList<FileResource>());
			update(true);
			return;
		}
		else if (folderItem instanceof RestResourceWrapper) {
			setFiles(((RestResourceWrapper) folderItem).getResource().getFiles());
			update(true);
		}
		else if (folderItem instanceof SharedResource) {
			setFiles(((SharedResource) folderItem).getFiles());
			update(true);
		}
		else if (folderItem instanceof OtherUserResource) {
			setFiles(((OtherUserResource) folderItem).getFiles());
			update(true);
		}
		else if (folderItem instanceof TrashResource) {
			setFiles(((TrashResource) folderItem).getFiles());
			update(true);
		}
	}
	

	/**
	 * Fill the file cache with data.
	 */
	public void setFiles(final List<FileResource> _files) {
		if (_files.size() > 0 && ! (GSS.get().getTreeView().getSelection() instanceof TrashResource)) {
			files = new ArrayList<FileResource>();
			for (FileResource fres : _files)
				if (!fres.isDeleted())
					files.add(fres);
		}
		else
			files = _files;
		Collections.sort(files, new Comparator<FileResource>() {

			@Override
			public int compare(FileResource arg0, FileResource arg1) {
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
		//
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
		Iterator<FileResource> it = selectionModel.getSelectedSet().iterator();
		while(it.hasNext()){
			selectionModel.setSelected(it.next(),false);
		}
	}
	

	/**
	 *
	 */
	public void selectAllRows() {
		Iterator<FileResource> it = provider.getList().iterator();
		while(it.hasNext()){
			selectionModel.setSelected(it.next(),true);
		}


	}

	
	private void sortFiles(final String sortingProperty, final boolean sortingType){
		Collections.sort(files, new Comparator<FileResource>() {

            @Override
            public int compare(FileResource arg0, FileResource arg1) {
                    AbstractImagePrototype descPrototype = AbstractImagePrototype.create(images.desc());
                    AbstractImagePrototype ascPrototype = AbstractImagePrototype.create(images.asc());
                    if (sortingType){
                            if (sortingProperty.equals("version")) {
                                    return arg0.getVersion().compareTo(arg1.getVersion());
                            } else if (sortingProperty.equals("owner")) {
                                    return arg0.getOwner().compareTo(arg1.getOwner());
                            } else if (sortingProperty.equals("date")) {
                                    return arg0.getModificationDate().compareTo(arg1.getModificationDate());
                            } else if (sortingProperty.equals("size")) {
                                    return arg0.getContentLength().compareTo(arg1.getContentLength());
                            } else if (sortingProperty.equals("name")) {
                                    return arg0.getName().compareTo(arg1.getName());
                            } else if (sortingProperty.equals("path")) {
                                    return arg0.getUri().compareTo(arg1.getUri());
                            } else {
                                    return arg0.getName().compareTo(arg1.getName());
                            }
                    }
                    else if (sortingProperty.equals("version")) {
                            
                            return arg1.getVersion().compareTo(arg0.getVersion());
                    } else if (sortingProperty.equals("owner")) {
                            
                            return arg1.getOwner().compareTo(arg0.getOwner());
                    } else if (sortingProperty.equals("date")) {
                            
                            return arg1.getModificationDate().compareTo(arg0.getModificationDate());
                    } else if (sortingProperty.equals("size")) {
                            
                            return arg1.getContentLength().compareTo(arg0.getContentLength());
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
	 * Creates a new ArrayList<FileResources> from the given files ArrayList 
	 * in order that the input files remain untouched 
	 * and continues to find user's full names of each FileResource element
	 * in the new ArrayList
	 *    
	 * @param filesInput
	 */
	private void copyListAndContinue(List<FileResource> filesInput){
		List<FileResource> copiedFiles = new ArrayList<FileResource>();		
		for(FileResource file : filesInput) {
			copiedFiles.add(file);
		}
		handleFullNames(copiedFiles);
	}
	
	/**
	 * Examines whether or not the user's full name exists in the 
	 * userFullNameMap in the GSS.java for every element of the input list.
	 * If the user's full name does not exist in the map then a command is being made.  
	 * 
	 * @param filesInput
	 */
	private void handleFullNames(List<FileResource> filesInput){		
		if(filesInput.size() == 0){
			showCellTable();
			return;
		}		

		if(GSS.get().findUserFullName(filesInput.get(0).getOwner()) == null){
			findFullNameAndUpdate(filesInput);		
			return;
		}
				
		if(filesInput.size() >= 1){
			filesInput.remove(filesInput.get(0));
			if(filesInput.isEmpty()){
				showCellTable();				
			}else{
				handleFullNames(filesInput);
			}
		}		
	}
	
	/**
	 * Makes a command to search for full name from a given username. 
	 * Only after the completion of the command the celltable is shown
	 * or the search for the next full name continues.
	 *  
	 * @param filesInput
	 */
	private void findFullNameAndUpdate(final List<FileResource> filesInput){		
		String aUserName = filesInput.get(0).getOwner();
		String path = GSS.get().getApiPath() + "users/" + aUserName; 

		GetCommand<UserSearchResource> gg = new GetCommand<UserSearchResource>(UserSearchResource.class, path, false,null) {
			@Override
			public void onComplete() {
				final UserSearchResource result = getResult();
				for (UserResource user : result.getUsers()){
					String username = user.getUsername();
					String userFullName = user.getName();
					GSS.get().putUserToMap(username, userFullName);
					if(filesInput.size() >= 1){
						filesInput.remove(filesInput.get(0));
						if(filesInput.isEmpty()){
							showCellTable();
						}else{
							handleFullNames(filesInput);
						}												
					}									
				}
			}
			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				GSS.get().displayError("Unable to fetch user's full name from the given username " + filesInput.get(0).getOwner());
				if(filesInput.size() >= 1){
					filesInput.remove(filesInput.get(0));
					handleFullNames(filesInput);					
				}
			}
		};
		DeferredCommand.addCommand(gg);
	
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
		RestResource selectedItem = GSS.get().getTreeView().getSelection();
		boolean uploadVisible = !(selectedItem != null && (selectedItem instanceof TrashResource || selectedItem instanceof TrashFolderResource || selectedItem instanceof SharedResource || selectedItem instanceof OthersResource || selectedItem instanceof OtherUserResource));
		uploadButtonBottom.setVisible(uploadVisible&&files.size()>=GSS.VISIBLE_FILE_COUNT);
		uploadButtonTop.setVisible(uploadVisible&&files.size()>=GSS.VISIBLE_FILE_COUNT);
		provider.setList(files);
		
		provider.refresh();
		
		//celltable.redraw();
		celltable.redrawHeaders();		
	}

	
}
