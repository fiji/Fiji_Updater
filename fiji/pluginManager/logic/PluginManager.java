package fiji.pluginManager.logic;

import fiji.pluginManager.ui.IJProgress;
import fiji.pluginManager.ui.MainUserInterface;

import fiji.pluginManager.util.Util;

import ij.IJ;

import ij.plugin.PlugIn;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import javax.xml.parsers.ParserConfigurationException;

// TODO: scrap useless comment, rename package to fiji.updater and rename this
// class to "Main" or "Updater".
/*
 * Start up class of Plugin Manager Application:
 * Facade, Business logic, and overall-in-charge of providing the main user interface the
 * required list of PluginObjects that interface will use for display.
 */
public class PluginManager implements PlugIn {
	public static final String MAIN_URL = "http://pacific.mpi-cbg.de/uploads/incoming/plugins/";
	//public static final String MAIN_URL = "http://pacific.mpi-cbg.de/update/"; //TODO

	public static final String TXT_FILENAME = "current.txt";
	public static final String XML_LOCK = "db.xml.gz.lock";
	public static final String XML_COMPRESSED = "db.xml.gz";
	public static final String XML_FILENAME = "db.xml";
	public static final String XML_BACKUP = "db.bak";
	//public static final String UPDATE_DIRECTORY = "/update/";
	public static final String UPDATE_DIRECTORY = "/incoming/plugins/";

	// Key names for ij.Prefs for saved values ("cookies")
	// Note: ij.Prefs is only saved during shutdown of Fiji
	public static final String PREFS_XMLDATE = "fiji.updater.xmlDate";
	public static final String PREFS_USER = "fiji.updater.login";

	// Track when db.xml.gz was modified (Lock conflict purposes)
	private long lastModified;

	public void run(String arg) {
		// TODO: this should not be a thread
		new Thread() {
			public void run() {
				openPluginManager();
			}
		}.start();
	}

	// TODO: move more functionality into this class; the ui should be the ui only!!!
	public void openPluginManager() {
		// TODO: use ProgressPane in main window
		IJProgress progress = new IJProgress();
		progress.setTitle("Starting up Plugin Manager...");

		XMLFileDownloader downloader = new XMLFileDownloader();
		downloader.addProgress(progress);
		try {
			downloader.start();
			lastModified = downloader.getXMLLastModified();
			// TODO: it is a parser, not a reader.  And it should
			// be a static method.
			new XMLFileReader(downloader.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			new File(Util.prefix(PluginManager.XML_COMPRESSED))
					.deleteOnExit();
			IJ.error("Download/checksum failed: " + e);
			fallBackToOldUpdater();
			return;
		}

		PluginListBuilder pluginListBuilder =
			new PluginListBuilder(progress);
		pluginListBuilder.updateFromLocal();
		IJ.showStatus("");

		MainUserInterface main = new MainUserInterface(lastModified);
		main.setVisible(true);
	}

	protected void fallBackToOldUpdater() {
		try {
			UpdateFiji updateFiji = new UpdateFiji();
			updateFiji.hasGUI = true;
			updateFiji.exec(UpdateFiji.defaultURL);
		} catch (SecurityException se) {
			IJ.error("Security exception: " + se);
		}
	}
}
