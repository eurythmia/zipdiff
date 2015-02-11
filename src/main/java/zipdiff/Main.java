/* zipdiff is available under the terms of the
 * Apache License, version 2.0
 *
 * Link: http://www.apache.org/licenses/
 */
package zipdiff;

import org.apache.commons.cli.*;
import zipdiff.output.Builder;
import zipdiff.output.BuilderFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides a command line interface to zipdiff-ng
 *
 * @author Sean C. Sullivan, J.Stewart, Hendrik Brummermann, Aaron Cripps
 */
public class Main {



	private static final String OPTION_COMPARE_CRC_VALUES = "comparecrcvalues";

	private static final String OPTION_COMPARE_TIMESTAMPS = "comparetimestamps";

	private static final String OPTION_OUTPUT_FILE = "outputfile";

	private static final String OPTION_FILE1 = "file1";

	private static final String OPTION_FILE2 = "file2";

	private static final String OPTION_SKIP_OUTPUT_PREFIXES = "skipoutputprefixes";

	private static final String OPTION_SKIP_PREFIX1 = "skipprefixes1";

	private static final String OPTION_SKIP_PREFIX2 = "skipprefixes2";

	private static final String OPTION_REGEX = "regex";

	private static final Options options;

	// static initializer
	static {
		options = new Options();

		Option compareTS = new Option(OPTION_COMPARE_TIMESTAMPS, OPTION_COMPARE_TIMESTAMPS, false, "Compare timestamps");
		compareTS.setRequired(false);

		Option compareCRC = new Option(OPTION_COMPARE_CRC_VALUES, OPTION_COMPARE_CRC_VALUES, false, "Compare CRC values");
		compareCRC.setRequired(false);

		Option file1 = new Option(OPTION_FILE1, OPTION_FILE1, true, "<filename> first file to compare");
		file1.setRequired(true);

		Option file2 = new Option(OPTION_FILE2, OPTION_FILE2, true, "<filename> second file to compare");
		file2.setRequired(true);

		Option numberOfOutputPrefixesToSkip = new Option(OPTION_SKIP_OUTPUT_PREFIXES, OPTION_SKIP_OUTPUT_PREFIXES, true, "<n> number of directory prefix to skip in the output file (if supported by outputter");
		numberOfOutputPrefixesToSkip.setRequired(false);


		Option numberOfPrefixesToSkip1 = new Option(OPTION_SKIP_PREFIX1, OPTION_SKIP_PREFIX1, true, "<n> number of directory prefix to skip for the first file");
		numberOfPrefixesToSkip1.setRequired(false);

		Option numberOfPrefixesToSkip2 = new Option(OPTION_SKIP_PREFIX2, OPTION_SKIP_PREFIX2, true, "<n> number of directory prefix to skip for the second file");
		numberOfPrefixesToSkip2.setRequired(false);

		Option outputFileOption = new Option(OPTION_OUTPUT_FILE, OPTION_OUTPUT_FILE, true, "output filename");
		outputFileOption.setRequired(false);

		Option regex = new Option(OPTION_REGEX, OPTION_REGEX, true, "regular expression to match files to exclude e.g. (?i)meta-inf.*");
		regex.setRequired(false);

		options.addOption(compareTS);
		options.addOption(compareCRC);
		options.addOption(file1);
		options.addOption(file2);
		options.addOption(numberOfOutputPrefixesToSkip);
		options.addOption(numberOfPrefixesToSkip1);
		options.addOption(numberOfPrefixesToSkip2);
		options.addOption(regex);
		options.addOption(outputFileOption);
	}

	private static void writeOutputFile(String filename, int numberOfOutputPrefixesToSkip, Differences d) throws java.io.IOException {
		Builder builder = BuilderFactory.create(filename);
		builder.build(filename, numberOfOutputPrefixesToSkip, d);
	}

	/**
	 *
	 * The command line interface to zipdiff utility
	 *
	 * @param args The command line parameters
	 *
	 */
	public static void main(String[] args) throws Exception{
		CommandLineParser parser = new GnuParser();

		try {
			CommandLine line = parser.parse(options, args);

            File f1 = new File(line.getOptionValue(OPTION_FILE1));
			File f2 = new File(line.getOptionValue(OPTION_FILE2));

			DifferenceCalculator calc = new DifferenceCalculator(f1, f2);

			int numberOfPrefixesToSkip1 = 0;
			if (line.getOptionValue(OPTION_SKIP_PREFIX1) != null) {
				numberOfPrefixesToSkip1 = Integer.parseInt(line.getOptionValue(OPTION_SKIP_PREFIX1));
			}
			int numberOfPrefixesToSkip2 = 0;
			if (line.getOptionValue(OPTION_SKIP_PREFIX2) != null) {
				numberOfPrefixesToSkip2 = Integer.parseInt(line.getOptionValue(OPTION_SKIP_PREFIX2));
			}
			int numberOfOutputPrefixesToSkip = 0;
			if (line.getOptionValue(OPTION_SKIP_OUTPUT_PREFIXES) != null) {
				numberOfOutputPrefixesToSkip = Integer.parseInt(line.getOptionValue(OPTION_SKIP_OUTPUT_PREFIXES));
			}

			calc.setNumberOfPrefixesToSkip1(numberOfPrefixesToSkip1);
			calc.setNumberOfPrefixesToSkip2(numberOfPrefixesToSkip2);

			// todo - calc.setFilenamesToIgnore();

			if (line.hasOption(OPTION_COMPARE_CRC_VALUES)) {
				calc.setCompareCRCValues(true);
			} else {
				calc.setCompareCRCValues(false);
			}

			if (line.hasOption(OPTION_COMPARE_TIMESTAMPS)) {
				calc.setIgnoreTimestamps(false);
			} else {
				calc.setIgnoreTimestamps(true);
			}

			if (line.hasOption(OPTION_REGEX)) {
				String regularExpression = line.getOptionValue(OPTION_REGEX);
				Set<String> regexSet = new HashSet<>();
				regexSet.add(regularExpression);

				calc.setFilenameRegexToIgnore(regexSet);
			}

			Differences d = calc.getDifferences();
            writeOutputFile(line.getOptionValue(OPTION_OUTPUT_FILE), numberOfOutputPrefixesToSkip, d);
		} catch (ParseException pex) {
			System.err.println(pex.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("zipdiff.Main [options] ", options);
		}
	}

}
