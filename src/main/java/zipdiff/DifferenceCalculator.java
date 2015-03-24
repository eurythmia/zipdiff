/* zipdiff-ng is available under the terms of the
 * Apache License, version 2.0
 *
 * Link: http://www.apache.org/licenses/
 */
package zipdiff;

import zipdiff.util.StringUtil;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Checks and compiles differences between two zip files.
 * It also has the ability to exclude entries from the comparison
 * based on a regular expression.
 *
 * @author Sean C. Sullivan, Hendrik Brummermann
 */
public class DifferenceCalculator {

	private final Logger logger = Logger.getLogger(getClass().getName());

	private final ZipFile file1;

	private final ZipFile file2;

	private int numberOfPrefixesToSkip1 = 0;

	private int numberOfPrefixesToSkip2 = 0;

	private boolean useTimeStamps = false;

	private boolean compareCRCValues = true;

    private Pattern fileFilterPattern;

	/**
	 * Constructor taking 2 filenames to compare
	 * @throws java.io.IOException
	 */
	public DifferenceCalculator(String filename1, String filename2) throws java.io.IOException {
		this(new File(filename1), new File(filename2));
	}

	/**
	 * Constructor taking 2 Files to compare
	 * @throws java.io.IOException
	 */
	public DifferenceCalculator(File f1, File f2) throws java.io.IOException {
		this(new ZipFile(f1), new ZipFile(f2));
	}

	/**
	 * Constructor taking 2 ZipFiles to compare
	 */
	public DifferenceCalculator(ZipFile zf1, ZipFile zf2) {
		file1 = zf1;
		file2 = zf2;
	}

    public void setFilenameFilter(String regex) {
        fileFilterPattern = Pattern.compile(regex);
        logger.log(Level.FINE, "Regular expression is : " + regex);
    }

	/**
	 * returns true if fileToIgnorePattern matches the filename given.
	 * @param entryName The name of the file to check to see if it should be ignored.
	 * @return true if the file should be ignored.
	 */
	protected boolean ignoreThisFile(String entryName) {
		if (entryName == null || fileFilterPattern == null) {
			return false;
		} else {
            boolean ignore = !fileFilterPattern.matcher(entryName).matches();
			if (ignore) {
				logger.log(Level.FINEST, String.format("%s does not match filter, excluding", entryName));
			}
			return ignore;
		}
	}

	/**
	 * Ensure that the comparison checks against the CRCs of the entries.
	 * @param b true ensures that CRCs will be checked
	 */
	public void setCompareCRCValues(boolean b) {
		compareCRCValues = b;
	}

	/**
	 * @return true if this instance will check the CRCs of each ZipEntry
	 */
	public boolean getCompareCRCValues() {
		return compareCRCValues;
	}

	/**
	 * sets the number of directory prefixes to skip in the first file
	 *
	 * @param numberOfPrefixesToSkip1 number of directory prefixes to skip
	 */
	public void setNumberOfPrefixesToSkip1(int numberOfPrefixesToSkip1) {
		this.numberOfPrefixesToSkip1 = numberOfPrefixesToSkip1;
	}

	/**
	 * sets the number of directory prefixes to skip in the first file
	 *
	 * @param numberOfPrefixesToSkip2 number of directory prefixes to skip
	 */
	public void setNumberOfPrefixesToSkip2(int numberOfPrefixesToSkip2) {
		this.numberOfPrefixesToSkip2 = numberOfPrefixesToSkip2;
	}

	/**
	 * Opens the ZipFile and builds up a map of all the entries. The key is the name of
	 * the entry and the value is the ZipEntry itself.
	 * @param zf The ZipFile for which to build up the map of ZipEntries
	 * @param number of directory prefixes to skip
	 * @return The map containing all the ZipEntries. The key being the name of the ZipEntry.
	 * @throws java.io.IOException
	 */
	protected Map<String,ZipEntry> buildZipEntryMap(ZipFile zf, int number) throws java.io.IOException {
		Map<String,ZipEntry> zipEntryMap = new HashMap<>();
		try {
			Enumeration entries = zf.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				InputStream is = null;
				try {
					is = zf.getInputStream(entry);
					processZipEntry("", entry, is, zipEntryMap, number);
				} finally {
					if (is != null) {
						is.close();
					}
				}
			}
		} finally {
			zf.close();
		}

		return zipEntryMap;
	}

	/**
	 * Will place ZipEntries for a given ZipEntry into the given Map. More ZipEntries will result
	 * if zipEntry is itself a ZipFile. All embedded ZipFiles will be processed with their names
	 * prefixed onto the names of their ZipEntries.
	 * @param prefix The prefix of the ZipEntry that should be added to the key. Typically used
	 * when processing embedded ZipFiles. The name of the embedded ZipFile would be the prefix of
	 * all the embedded ZipEntries.
	 * @param zipEntry The ZipEntry to place into the Map. If it is a ZipFile then all its ZipEntries
	 * will also be placed in the Map.
	 * @param is The InputStream of the corresponding ZipEntry.
	 * @param zipEntryMap The Map in which to place all the ZipEntries into. The key will
	 * be the name of the ZipEntry.
	 * @param prefixDirsToSkip number of directory prefixes to skip
	 * @throws IOException
	 */
	protected void processZipEntry(String prefix, ZipEntry zipEntry, InputStream is, Map<String,ZipEntry> zipEntryMap, int prefixDirsToSkip) throws IOException {
		if (ignoreThisFile(zipEntry.getName())) {
			logger.log(Level.FINE, "ignoring file: " + zipEntry.getName());
		} else {
			String name = StringUtil.removeDirectoryPrefix(prefix + zipEntry.getName(), prefixDirsToSkip);
			if ((name == null) || name.equals("")) {
				return;
			}

			logger.log(Level.FINEST, "processing ZipEntry: " + name);
			zipEntryMap.put(name, zipEntry);

			if (!zipEntry.isDirectory() && isZipFile(name)) {
				processEmbeddedZipFile(name + "!", is, zipEntryMap);
			}
		}
	}



	protected void processEmbeddedZipFile(String prefix, InputStream is, Map<String, ZipEntry> m) throws java.io.IOException {
		ZipInputStream zis = new ZipInputStream(is);

		ZipEntry entry = zis.getNextEntry();

		while (entry != null) {
			processZipEntry(prefix, entry, zis, m, 0);
			zis.closeEntry();
			entry = zis.getNextEntry();
		}

	}

	/**
	 * Returns true if the file iz a zip format
	 * @param filename The name of the file to check.
	 * @return true if file is a valid zip
	 */
	public static boolean isZipFile(String filename){
        try {
            return new ZipInputStream(new FileInputStream(filename)).getNextEntry() != null;
        } catch(IOException e) {
            return false;
        }
	}

	/**
	 * Calculates all the differences between two zip files.
	 * It builds up the 2 maps of ZipEntries for the two files
	 * and then compares them.
	 * @param zf1 The first ZipFile to compare
	 * @param zf2 The second ZipFile to compare
	 * @param p1 number of directory prefixes to skip in the 1st file
	 * @param p2 number of directory prefixes to skip in the 2nd file
	 * @return All the differences between the two files.
	 * @throws java.io.IOException
	 */
	protected Differences calculateDifferences(ZipFile zf1, ZipFile zf2, int p1, int p2) throws java.io.IOException {
		Map<String,ZipEntry> map1 = buildZipEntryMap(zf1, p1);
		Map<String,ZipEntry> map2 = buildZipEntryMap(zf2, p2);

		return calculateDifferences(map1, map2);
	}

	/**
	 * Given two Maps of ZipEntries it will generate a Differences of all the
	 * differences found between the two maps.
	 * @return All the differences found between the two maps
	 */
	protected Differences calculateDifferences(Map<String, ZipEntry> m1, Map<String, ZipEntry> m2) {
		Differences d = new Differences();

		Set<String> names1 = m1.keySet();
		Set<String> names2 = m2.keySet();

		Set<String> allNames = new HashSet<>();
		allNames.addAll(names1);
		allNames.addAll(names2);

        for(String name : allNames) {
            if (names1.contains(name) && (!names2.contains(name))) {
                d.fileRemoved(name, m1.get(name));
            } else if (names2.contains(name) && (!names1.contains(name))) {
                d.fileAdded(name, m2.get(name));
            } else if (names1.contains(name) && (names2.contains(name))) {
                ZipEntry entry1 = m1.get(name);
                ZipEntry entry2 = m2.get(name);
                if (!entriesMatch(entry1, entry2)) {
                    d.fileChanged(name, entry1, entry2);
                }
            } else {
                throw new IllegalStateException("unexpected state");
            }
        }

		return d;
	}

	/**
	 * returns true if the two entries are equivalent in type, name, size, compressed size
	 * and time or CRC.
	 * @param entry1 The first ZipEntry to compare
	 * @param entry2 The second ZipEntry to compare
	 * @return true if the entries are equivalent.
	 */
	protected boolean entriesMatch(ZipEntry entry1, ZipEntry entry2) {
		boolean result;

		result = (entry1.isDirectory() == entry2.isDirectory()) && (entry1.getSize() == entry2.getSize()) && (entry1.getCompressedSize() == entry2.getCompressedSize());

		if (isUsingTimestamps()) {
			result = result && (entry1.getTime() == entry2.getTime());
		}

		if (getCompareCRCValues()) {
			result = result && (entry1.getCrc() == entry2.getCrc());
		}
		return result;
	}

	public void useTimestamps(boolean b) {
		useTimeStamps = b;
	}

	public boolean isUsingTimestamps() {
		return useTimeStamps;
	}

	/**
	 *
	 * @return all the differences found between the two zip files.
	 * @throws java.io.IOException
	 */
	public Differences getDifferences() throws java.io.IOException {
		Differences d = calculateDifferences(file1, file2, numberOfPrefixesToSkip1, numberOfPrefixesToSkip2);
		d.setFilename1(file1.getName());
		d.setFilename2(file2.getName());

		return d;
	}
}
