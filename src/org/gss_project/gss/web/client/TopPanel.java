/*
 * Copyright 2007, 2008, 2009 Electronic Business Systems Ltd.
 *
 * This file is part of GSS.
 *
 * GSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GSS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gss_project.gss.web.client;

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
			SettingsMenu.Images, GroupMenu.Images, FilePropertiesDialog.Images,
			HelpMenu.Images, LoadingIndicator.Images {

		@Source("org/gss_project/gss/resources/exit.png")
		ImageResource exit();

		@Source("org/gss_project/gss/resources/folder_blue.png")
		ImageResource folder();

		@Source("org/gss_project/gss/resources/edit.png")
		ImageResource edit();

		@Source("org/gss_project/gss/resources/edit_group.png")
		ImageResource group();

		@Source("org/gss_project/gss/resources/configure.png")
		ImageResource configure();

		@Source("org/gss_project/gss/resources/help.png")
		ImageResource help();

		@Source("org/gss_project/gss/resources/pithos-logo.png")
		ImageResource gssLogo();

		@Source("org/gss_project/gss/resources/grnet-logo.png")
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
	 * The group menu widget.
	 */
	private GroupMenu groupMenu;

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
		groupMenu = new GroupMenu(images);
		settingsMenu = new SettingsMenu(images);
		helpMenu = new HelpMenu(images);
		loading = new LoadingIndicator(images);
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
		
		MenuItem groupItem = new MenuItem("<table style='font-size: 100%'><tr><td>" +
					AbstractImagePrototype.create(images.group()).getHTML() + "</td><td>Group</td></tr></table>", true,
					groupMenu.getContextMenu());
		groupItem.getElement().setId("topMenu.group");
		
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
		menu.addItem(groupItem);
		menu.addItem(configureItem);
		menu.addItem(helpItem);

		outer.setSpacing(2);
		outer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		outer.setCellVerticalAlignment(menu, HasVerticalAlignment.ALIGN_MIDDLE);
		outer.add(menu);
		outer.setStyleName("toolbar");

		outer.add(loading);

		Configuration conf = (Configuration) GWT.create(Configuration.class);
        String path = Window.Location.getPath();
        String baseUrl = GWT.getModuleBaseURL();
        String homeUrl = baseUrl.substring(0, baseUrl.indexOf(path));
		HTML logos = new HTML("<table><tr><td><a href='" + homeUrl +
					"' target='gss'>" +	AbstractImagePrototype.create(images.gssLogo()).getHTML() +
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
