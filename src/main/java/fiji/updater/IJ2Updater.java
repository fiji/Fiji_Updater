package fiji.updater;

import ij.IJ;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import net.imagej.ui.swing.updater.ImageJUpdater;
import net.imagej.updater.PromptUserToUpdate;
import net.imagej.updater.UpToDate;
import net.imagej.updater.UpToDate.Result;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.xml.sax.SAXException;

/**
 * Simple adapter for ImageJ2's updater.
 * <p>
 * This class exists merely to avoid problems loading the {@link Updater} and
 * the {@link UptodateCheck} when the ImageJ2 updater is not actually in the
 * class path (in which case they want to fall back to doing different things,
 * which is impossible if they cannot be loaded...).
 * </p>
 * 
 * @author Johannes Schindelin
 */
class IJ2Updater {
	static void newCheck() throws IOException, ParserConfigurationException,
			SAXException {

		final Result result = UpToDate.check();
		switch (result) {
		case UP_TO_DATE:
		case OFFLINE:
		case REMIND_LATER:
		case CHECK_TURNED_OFF:
		case UPDATES_MANAGED_DIFFERENTLY:
		case DEVELOPER:
			return;
		case UPDATEABLE:
			final Context context = (Context) IJ.runPlugIn(
					Context.class.getName(), "");
			final CommandService commandService = context
					.getService(CommandService.class);
			try {
				commandService.run(PromptUserToUpdate.class, true);
			}
			catch (final Throwable t) {
				t.printStackTrace();
				try {
					@SuppressWarnings("unchecked")
					final Class<? extends Command> obsolete = (Class<? extends Command>)
						Class.forName("net.imagej.updater.UpdatesAvailable");
					commandService.run(obsolete, true);
				}
				catch (final Throwable t2) {
					t2.printStackTrace();
				}
			}
			break;
		case PROXY_NEEDS_AUTHENTICATION:
			throw new RuntimeException(
					"TODO: authenticate proxy with the configured user/pass pair");
		case READ_ONLY:
			final String message = "Your ImageJ installation cannot be updated because it is read-only";
			IJ.showMessage(message);
			IJ.showStatus(message);
			break;
		default:
			IJ.showMessage("Unhandled UpToDate case: " + result);
		}
	}

	static void run() {
		new ImageJUpdater().run();
	}

}
