/* zipdiff is available under the terms of the
 * Apache License, version 2.0
 *
 * Link: http://www.apache.org/licenses/
 */
package zipdiff;

import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;

/**
 * Used to keep track of difference between 2 zip files.
 *
 * @author Sean C. Sullivan
 */
public class Differences {
	private final Map<String,ZipEntry> added = new TreeMap<>();

	private final Map<String,ZipEntry> removed = new TreeMap<>();

	private final Map<String,ZipEntry[]> changed = new TreeMap<>();

	private String filename1;

	private String filename2;

	public Differences() {
		// todo
	}

	public void setFilename1(String filename) {
		filename1 = filename;
	}

	public void setFilename2(String filename) {
		filename2 = filename;
	}

	public String getFilename1() {
		return filename1;
	}

	public String getFilename2() {
		return filename2;
	}

	public void fileAdded(String fullFilePath, ZipEntry ze) {
		added.put(fullFilePath, ze);
	}

	public void fileRemoved(String fullFilePath, ZipEntry ze) {
		removed.put(fullFilePath, ze);
	}

	public void fileChanged(String fullFilePath, ZipEntry z1, ZipEntry z2) {
		ZipEntry[] entries = new ZipEntry[2];
		entries[0] = z1;
		entries[1] = z2;
		changed.put(fullFilePath, entries);
	}

	public Map<String,ZipEntry> getAdded() {
		return added;
	}

	public Map<String, ZipEntry> getRemoved() {
		return removed;
	}

	public Map<String,ZipEntry[]> getChanged() {
		return changed;
	}

	public boolean hasDifferences() {
		return ((getChanged().size() > 0) || (getAdded().size() > 0) || (getRemoved().size() > 0));
	}

	@Override
    public String toString() {
		StringBuilder sb = new StringBuilder();

        sb.append(String.format("%d file(s) added to %s\n", added.size(), getFilename2()));
        for(String key : added.keySet()) {
            sb.append(String.format("\t[added] %s\n", key));
        }

        sb.append(String.format("%d file(s) removed from %s\n", removed.size(), getFilename2()));
        for(String key : removed.keySet()) {
            sb.append(String.format("\t[removed] %s\n", key));
        }

        sb.append(String.format("%d file(s) changed\n", changed.size()));
        for(String key : changed.keySet()) {
            ZipEntry[] entries = changed.get(key);
            sb.append(String.format("\t[changed] %s (size: %d : %d)\n", key, entries[0].getSize(), entries[1].getSize()));
        }

		int differenceCount = added.size() + changed.size() + removed.size();
		sb.append(String.format("Total differences: %d", differenceCount));
		return sb.toString();
	}

}
