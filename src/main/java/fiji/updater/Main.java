package fiji.updater;

import ij.IJ;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import net.imagej.updater.FilesCollection;
import net.imagej.updater.UpdateSite;

import org.scijava.log.StderrLogService;
import org.xml.sax.SAXException;

/**
 * Access the command-line interface of the ImageJ Updater
 *
 * @author Johannes Schindelin
 * @deprecated use ij-updater-core directly
 */
public class Main {
	public static void main(String[] args) {
		//new Adapter(false).runCommandLineUpdater(args);

try {
		FilesCollection files;
		UpdateSite siteOpenSPIM, siteMM;
		boolean hasMMNightly, hasOpenSPIM;
		String urlOpenSPIM, urlMM;

		urlOpenSPIM = "http://openspim.org/update/";
		urlMM = "http://sites.imagej.net/Micro-Manager-dev/";
		files = new FilesCollection(new StderrLogService(),
				new File(System.getProperty("user.home") + "/Desktop/Fiji.app"));
		files.read();
		siteOpenSPIM = files.getUpdateSite("OpenSPIM", true);
		hasOpenSPIM = siteOpenSPIM != null && siteOpenSPIM.isActive() && urlOpenSPIM.equals(siteOpenSPIM.getURL());
		siteMM = files.getUpdateSite("Micro-Manager-dev", true);
		hasMMNightly = siteMM != null && siteMM.isActive() && urlMM.equals(siteMM.getURL());
		if (!hasMMNightly) {
			if (siteMM != null) {
				siteMM.setActive(true);
				siteMM.setLastModified(-1);
			} else {
				siteMM = files.addUpdateSite("Micro-Manager-dev", urlMM, null, null, -1);
			}
			if (siteOpenSPIM == null) {
				siteOpenSPIM = files.addUpdateSite("OpenSPIM", urlOpenSPIM, null, null, -1);
			} else if (!hasOpenSPIM) {
				siteOpenSPIM.setActive(true);
			}
			if (siteMM.compareTo(siteOpenSPIM) > 0) {
				// OpenSPIM needs to be able to override Micro-Manager-dev
				files.removeUpdateSite("OpenSPIM");
				files.addUpdateSite(siteOpenSPIM);
			}
			files.write();
			IJ.run("Update...");
		}

}
catch (SAXException e) {}
catch (IOException e) {}
catch (TransformerConfigurationException e) {} catch (ParserConfigurationException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
	}
}
