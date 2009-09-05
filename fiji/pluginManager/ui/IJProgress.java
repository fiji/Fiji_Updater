package fiji.pluginManager.ui;

import fiji.pluginManager.util.Progress;

import ij.IJ;

// TODO: extend to ProgressPanel
public class IJProgress implements Progress {
	String title;

	public void setTitle(String title) {
		IJ.showStatus(title);
		IJ.showProgress(0, 1);
		this.title = title;
	}

	public void setCount(int count, int total) {
		IJ.showProgress(count, total);
	}

	public void addItem(Object title) {
		IJ.showStatus(this.title + " " + title + "...");
	}

	public void setItemCount(int count, int total) {}
}