/* zipdiff is available under the terms of the
 * Apache License, version 2.0
 *
 * Link: http://www.apache.org/licenses/
 */
package zipdiff;

import com.beust.jcommander.JCommander;
import zipdiff.output.Builder;
import zipdiff.output.BuilderFactory;

import java.io.File;
import java.util.List;

/**
 * Provides a command line interface to zipdiff-ng
 *
 * @author Sean C. Sullivan, J.Stewart, Hendrik Brummermann, Aaron Cripps
 */
public class Main {

	private static void writeOutputFile(String filename, int numberOfOutputPrefixesToSkip, Differences d) throws java.io.IOException {
		Builder builder = BuilderFactory.create(filename);
		builder.build(filename, numberOfOutputPrefixesToSkip, d);
	}

	public static void main(String[] args) throws Exception{
        CommandLineArgs cliArgs = new CommandLineArgs();
        JCommander jCommander = new JCommander(cliArgs, args);

        if(cliArgs.isHelp()) {
            jCommander.setProgramName("zipdiff-ng");
            jCommander.usage();
            return;
        }

        //TODO: use something better than a list.
        List<String> inputFiles = cliArgs.getInputFiles();
        DifferenceCalculator calc = new DifferenceCalculator(new File(inputFiles.get(0)), new File(inputFiles.get(1)));

        calc.setNumberOfPrefixesToSkip1(cliArgs.getInputPrefixesToSkip());
        calc.setNumberOfPrefixesToSkip2(cliArgs.getComparePrefixesToSkip());
        calc.setCompareCRCValues(cliArgs.useCRCvalues());
        calc.useTimestamps(cliArgs.useTStamp());
        // TODO: calc.setFilenamesToIgnore();

        Differences d = calc.getDifferences();
        // TODO: use File instead of passing down strings ... Stringly typed vars are bad
        writeOutputFile(cliArgs.getOutputFile(), cliArgs.getOutputPrefixesToSkip(), d);
	}

}
