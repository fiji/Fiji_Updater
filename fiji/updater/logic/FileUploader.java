package fiji.updater.logic;

import fiji.updater.util.Progress;
import fiji.updater.util.Progressable;

import ij.IJ;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.List;

/*
 * This FileUploader is highly specialized to upload plugins and XML
 * information over to Pacific. There is a series of steps to follow. Any
 * exception means entire upload process is considered invalid.
 *
 * 1.) Set db.xml.gz to read-only
 * 2.) Verify db.xml.gz has not been modified, if not, upload process cancelled
 * 3.) Upload db.xml.gz.lock (Lock file, prevent others from writing it ATM)
 * 4.) Upload plugin files and current.txt
 * 5.) If all goes well, force rename db.xml.gz.lock to db.xml.gz
 */
public class FileUploader extends Progressable {
	protected final String uploadDir;
	int total;

	public FileUploader() {
		this("/var/www/update/");
	}

	public FileUploader(String uploadDir) {
		this.uploadDir = uploadDir;
	}

	protected void calculateTotalSize(List<SourceFile> sources) {
		total = 0;
		for (SourceFile source : sources)
			total += (int)source.getFilesize();
	}

	//Steps to accomplish entire upload task
	public synchronized void upload(long xmlLastModified,
			List<SourceFile> sources) throws IOException {
		setTitle("Uploading");

		calculateTotalSize(sources);
		int count = 0;

		File lock = null;
		File db = new File(uploadDir + PluginManager.XML_COMPRESSED);
		byte[] buffer = new byte[65536];
		for (SourceFile source : sources) {
			addItem(source);

			File file = new File(uploadDir + source.getFilename());
			File dir = file.getParentFile();
			if (!dir.exists())
				dir.mkdirs();
			OutputStream out = new FileOutputStream(file);

			// The first file is special; it is the lock file
			if (lock == null) {
				lock = file;
				if (!lock.setReadOnly())
					throw new IOException("Could not mark "
						+ source.getFilename()
						+ " read-only!");
				if (xmlLastModified != db.lastModified()) {
					// TODO: SSHFileUploader must delete the lock here, too
					lock.delete();
					throw new IOException("Conflict: "
						+ db.getName()
						+ " has been modified");
				}
			}

			InputStream in = source.getInputStream();
			int currentCount = 0;
			int currentTotal = (int)source.getFilesize();
			for (;;) {
				int read = in.read(buffer);
				if (read < 0)
					break;
				out.write(buffer, 0, read);
				currentCount += read;
				setItemCount(currentCount, currentTotal);
				setCount(count + currentCount, total);
			}
			in.close();
			out.close();
			count += currentCount;
		}

		File backup = new File(db.getAbsolutePath() + ".old");
		if (backup.exists())
			backup.delete();
		db.renameTo(backup);
		lock.renameTo(db);
	}

	public interface SourceFile {
		public String getFilename();
		public String getPermissions();
		public long getFilesize();
		public InputStream getInputStream() throws IOException;
	}
}
