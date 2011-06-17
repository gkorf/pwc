/*
 * Copyright (c) 2011 Greek Research and Technology Network
 */
package gr.grnet.pithos.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

/**
 * The top panel, which contains the menu bar icons and the user name.
 */
public class TopPanel extends Composite {

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends ClientBundle, FileMenu.Images, EditMenu.Images,
			SettingsMenu.Images, FilePropertiesDialog.Images,
			HelpMenu.Images, LoadingIndicator.Images {

		@Source("gr/grnet/pithos/resources/exit.png")
		ImageResource exit();

		@Source("gr/grnet/pithos/resources/folder_blue.png")
		ImageResource folder();

		@Source("gr/grnet/pithos/resources/edit.png")
		ImageResource edit();

		@Source("gr/grnet/pithos/resources/edit_group.png")
		ImageResource group();

		@Source("gr/grnet/pithos/resources/configure.png")
		ImageResource configure();

		@Source("gr/grnet/pithos/resources/help.png")
		ImageResource help();

		@Source("gr/grnet/pithos/resources/pithos-logo.png")
		ImageResource gssLogo();

		@Source("gr/grnet/pithos/resources/grnet-logo.png")
		ImageResource grnetLogo();
	}

	/**
	 * The menu bar widget.
	 */
	private MenuBar menu;

	/**
	 * The file menu widget.
	 */
	private FileMenu fileMenu;

	/**
	 * The edit menu widget.
	 */
	private EditMenu editMenu;

	/**
	 * The settings menu widget.
	 */
	private SettingsMenu settingsMenu;

	/**
	 * The help menu widget.
	 */
	private HelpMenu helpMenu;

	/**
	 * A widget that displays a message indicating that communication with the
	 * server is underway.
	 */
	private LoadingIndicator loading;

	/**
	 * The constructor for the top panel.
	 *
	 * @param images the supplied images
	 */
	public TopPanel(Images images) {
		fileMenu = new FileMenu(images);
		editMenu = new EditMenu(images);
		settingsMenu = new SettingsMenu(images);
		helpMenu = new HelpMenu(images);
		loading = new LoadingIndicator(images);
        loading.hide();
		HorizontalPanel outer = new HorizontalPanel();

		outer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		menu = new MenuBar();
		menu.setWidth("100%");
		menu.setAutoOpen(false);
		menu.setAnimationEnabled(true);
		menu.setStyleName("toolbarmenu");

		Command quitCommand = new Command(){
			@Override
			public void execute() {
				QuitDialog dlg = new QuitDialog();
				dlg.center();
			}
		};
		MenuItem quitItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.exit()).getHTML() + "</td><td>Quit</td></tr></table>", true, quitCommand);
		quitItem.getElement().setId("topMenu.quit");
		
		MenuItem fileItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.folder()).getHTML() + "</td><td>File</td></tr></table>", true, new MenuBar(true)){
			@Override
			public MenuBar getSubMenu() {
				return fileMenu.createMenu();
			}
		};
		fileItem.getElement().setId("topMenu.file");
		
		MenuItem editItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.edit()).getHTML() + "</td><td>Edit</td></tr></table>", true, new MenuBar(true)){
			@Override
			public MenuBar getSubMenu() {
				return editMenu.createMenu();
			}
		};
		editItem.getElement().setId("topMenu.edit");
		
		MenuItem configureItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.configure()).getHTML() + "</td><td>Settings</td></tr></table>",
					true,settingsMenu.getContextMenu());
		configureItem.getElement().setId("topMenu.settings");
		
		MenuItem helpItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.help()).getHTML() + "</td><td>Help</td></tr></table>", true, new MenuBar(true)){
			@Override
			public MenuBar getSubMenu() {
				return helpMenu.createMenu();
			}
		};
		helpItem.getElement().setId("topMenu.help");
		
		menu.addItem(quitItem);
		menu.addItem(fileItem);
		menu.addItem(editItem);
		menu.addItem(configureItem);
		menu.addItem(helpItem);

		outer.setSpacing(2);
		outer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		outer.setCellVerticalAlignment(menu, HasVerticalAlignment.ALIGN_MIDDLE);
		outer.add(menu);
		outer.setStyleName("toolbar");

		outer.add(loading);

		HTML logos = new HTML("<table><tr><td><a href='http://pithos.grnet.gr/pithos' target='pithos'>" +	AbstractImagePrototype.create(images.gssLogo()).getHTML() +
					"</a><a href='http://www.grnet.gr/' " +	"target='grnet'>" +
					AbstractImagePrototype.create(images.grnetLogo()).getHTML()+"</a></td></tr></table>");
		outer.add(logos);

		outer.setCellHorizontalAlignment(logos, HasHorizontalAlignment.ALIGN_RIGHT);

		initWidget(outer);
	}


	/**
	 * Retrieve the loading.
	 *
	 * @return the loading
	 */
	public LoadingIndicator getLoading() {
		return loading;
	}

	/**
	 * Retrieve the fileMenu.
	 *
	 * @return the fileMenu
	 */
	FileMenu getFileMenu() {
		return fileMenu;
	}

	/**
	 * Retrieve the editMenu.
	 *
	 * @return the editMenu
	 */
	EditMenu getEditMenu() {
		return editMenu;
	}
}
