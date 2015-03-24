/* zipdiff-ng is available under the terms of the
 * Apache License, version 2.0
 *
 * Link: http://www.apache.org/licenses/
 */
package zipdiff;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import junit.framework.TestCase;
import zipdiff.output.AbstractBuilder;
import zipdiff.output.HtmlBuilder;
import zipdiff.output.TextBuilder;
import zipdiff.output.XmlBuilder;

/**
 * tests for DifferenceCalculator
 *
 * @author jastewart
 */
public class DifferenceCalculatorTest extends TestCase {
	private static String ENTRY_A = "A";

    public static final String SYSTEM_TMP_DIR_PROPERTY = "java.io.tmpdir";

	public static final String TEST_DIR_POSTFIX = File.separator + "UnitTestsDifferenceCalculatorTest";

	private static String testDirPathName;

	// naming convention The Capital letter denotes the entry so A will be the same as A
	// OneEntry denotes that the jar has one entry
	private static String testJarOneEntryA1Filename;

	private static String testJarOneEntryA2Filename;

	private static String testJarOneEntryB1Filename;

	private static String testJarOneEntryAContentsChangedFilename;

	static {
		testDirPathName = System.getProperty(SYSTEM_TMP_DIR_PROPERTY);
		if (testDirPathName == null) {
			testDirPathName = File.separator + "temp" + TEST_DIR_POSTFIX;
		}
		testJarOneEntryA1Filename = testDirPathName + File.separator + "testJarOneEntryA1Filename.jar";
		testJarOneEntryA2Filename = testDirPathName + File.separator + "testJarOneEntryA2Filename.jar";
		testJarOneEntryB1Filename = testDirPathName + File.separator + "testJarOneEntryB1Filename.jar";
		testJarOneEntryAContentsChangedFilename = testDirPathName + File.separator + "testJarOneEntryAContentsChangedFilename.jar";
	}

	/**
	 * Create a jar with only one entry in it. That entry being A
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void createJarOneEntryA1() throws IOException {
		//  create a jar file with no duplicates
		File testDir = new File(testDirPathName);
		assertNotNull(testDir.mkdirs());
		JarOutputStream testJarOS = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(testJarOneEntryA1Filename)));

		// ad an entry
        testJarOS.putNextEntry(new JarEntry(ENTRY_A));
        testJarOS.write(getPopulatedByteArray(2048, (byte)'a'));
		testJarOS.flush();
		testJarOS.close();
	}

    private byte[] getPopulatedByteArray(int size, byte value) {
        byte data1[] = new byte[size];
        Arrays.fill(data1, value);
        return data1;
    }

	/**
	 * Create a jar with only one entry in it. That entry being A
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void createJarOneEntryA2() throws IOException {
		//  create a jar file with no duplicates
		File testDir = new File(testDirPathName);
		assertNotNull(testDir.mkdirs());
		JarOutputStream testJarOS = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(testJarOneEntryA2Filename)));

		// add an entry
        testJarOS.putNextEntry(new JarEntry(ENTRY_A));
        testJarOS.write(getPopulatedByteArray(2048, (byte)'a'));
		testJarOS.flush();
		testJarOS.close();
	}

    /**
	 * Create a jar with only one entry in it. That entry being A
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void createJarOneEntryAContentsChanged() throws IOException {
		//  create a jar file with no duplicates
		File testDir = new File(testDirPathName);
		assertNotNull(testDir.mkdirs());
		JarOutputStream testJarOS = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(testJarOneEntryAContentsChangedFilename)));

		// add an entry
		JarEntry entry1 = new JarEntry(ENTRY_A);
		testJarOS.putNextEntry(entry1);
		byte data1[] = getPopulatedByteArray(2048, (byte)'a');
		// set a different content so that it will come up as changed
		data1[data1.length - 1] = 'b';
		testJarOS.write(data1);

		testJarOS.flush();
		testJarOS.close();
	}

	/**
	 * Create a jar with only one entry in it. That entry being A
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void createJarOneEntryB1() throws IOException {
		//  create a jar file with no duplicates
		File testDir = new File(testDirPathName);
		assertNotNull(testDir.mkdirs());
		JarOutputStream testJarOS = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(testJarOneEntryB1Filename)));

		// add an entry
        testJarOS.putNextEntry(new JarEntry("B"));
        testJarOS.write(getPopulatedByteArray(2048, (byte)'b'));
		testJarOS.flush();
		testJarOS.close();
	}


    //TODO: Write a test for no changes with filename filter that matches no files
    //TODO: Write a test for no changes with filename filter that matches one filename

    //TODO: Write a test for one change with filename filter that matches no files
    //TODO: Write a test for one change with filename filter that matches that file

    //TODO: Write a test for multiple changes with filename filter that matches no files
    //TODO: Write a test for multiple changes with filename filter that matches one file
    //TODO: Write a test for multiple changes with a filename filter that matches some files

    //NOTE: no need to write a test for any number of changes with a filter that matches all files, this is the default
    //      behaviour, and it is caught by the existing tests.

	/**
	 * Test for Differences calculateDifferences(ZipFile, ZipFile)
	 * with the same file - no differences should be found
	 */
	public void testCalculateDifferencesSameZip() throws IOException {
		createJarOneEntryA1();
		DifferenceCalculator calc = new DifferenceCalculator(testJarOneEntryA1Filename, testJarOneEntryA1Filename);
		Differences differences = calc.getDifferences();
		assertFalse(differences.hasDifferences());
		Map addedEntries = differences.getAdded();
		assertTrue(addedEntries.size() == 0);
		Map removedEntries = differences.getRemoved();
		assertTrue(removedEntries.size() == 0);
		Map changedEntries = differences.getChanged();
		assertTrue(changedEntries.size() == 0);

		exerciseOutputBuilders(differences);

	}

	/**
	 * Test for Differences calculateDifferences(ZipFile, ZipFile)
	 */
	public void testCalculateDifferencesZipsSameEntries() throws IOException {
		createJarOneEntryA1();
		createJarOneEntryA2();
		DifferenceCalculator calc = new DifferenceCalculator(testJarOneEntryA1Filename, testJarOneEntryA2Filename);
		Differences differences = calc.getDifferences();
		assertFalse(differences.hasDifferences());
		Map addedEntries = differences.getAdded();
		assertTrue(addedEntries.size() == 0);
		Map removedEntries = differences.getRemoved();
		assertTrue(removedEntries.size() == 0);
		Map changedEntries = differences.getChanged();
		assertTrue(changedEntries.size() == 0);

		exerciseOutputBuilders(differences);
	}

	/**
	 * Test for Differences calculateDifferences(ZipFile, ZipFile)
	 * Test that the differences between two zips with A in one and B in the second.
	 * A will have been removed and B will have been added.
	 */
	public void testCalculateDifferencesZipsDifferentEntries() throws IOException {
		createJarOneEntryA1();
		createJarOneEntryB1();
		DifferenceCalculator calc = new DifferenceCalculator(testJarOneEntryA1Filename, testJarOneEntryB1Filename);
		Differences differences = calc.getDifferences();
		assertTrue(differences.hasDifferences());
		Map addedEntries = differences.getAdded();
		assertTrue(addedEntries.containsKey("B"));
		Map removedEntries = differences.getRemoved();
		assertTrue(removedEntries.containsKey("A"));
		Map changedEntries = differences.getChanged();
		assertTrue(changedEntries.size() == 0);

		exerciseOutputBuilders(differences);

	}


	/**
	 * Test for Differences calculateDifferences(ZipFile, ZipFile)
	 * Test that the differences between two zips with A in one and A in the second with different content.
	 * A will have been removed and B will have been added.
	 */
	public void testCalculateDifferencesZipsSameEntriesDifferentContent() throws IOException {
		createJarOneEntryA1();
		createJarOneEntryAContentsChanged();
		DifferenceCalculator calc = new DifferenceCalculator(testJarOneEntryA1Filename, testJarOneEntryAContentsChangedFilename);
		Differences differences = calc.getDifferences();
		assertTrue(differences.hasDifferences());
		Map addedEntries = differences.getAdded();
		assertTrue(addedEntries.size() == 0);
		Map removedEntries = differences.getRemoved();
		assertTrue(removedEntries.size() == 0);
		Map changedEntries = differences.getChanged();
		assertTrue(changedEntries.containsKey("A"));

		exerciseOutputBuilders(differences);

	}

	private void exerciseHtmlBuilder(Differences differences) {
		assertNotNull(differences);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		AbstractBuilder b = new HtmlBuilder();
		b.build(baos, differences);

		assertTrue(baos.size() > 0);
	}

	private void exerciseXmlBuilder(Differences differences) {
		assertNotNull(differences);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		AbstractBuilder b = new XmlBuilder();
		b.build(baos, differences);

		assertTrue(baos.size() > 0);
	}

	private void exerciseTextBuilder(Differences differences) {
		assertNotNull(differences);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		AbstractBuilder b = new TextBuilder();
		b.build(baos, differences);

		assertTrue(baos.size() > 0);
	}

	private void exerciseOutputBuilders(Differences differences) {
		assertNotNull(differences);
		exerciseHtmlBuilder(differences);
		exerciseXmlBuilder(differences);
		exerciseTextBuilder(differences);
	}

}
