/* zipdiff-ng is available under the terms of the
 * Apache License, version 2.0
 *
 * Link: http://www.apache.org/licenses/
 */

package zipdiff;


import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CliTest{

    private String[] constructArgs(String... args) {
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        argList.add("-n");
        return argList.toArray(new String[argList.size()]);
    }

    @Test(expected = IndexOutOfBoundsException.class) /* fails validation */
    public void testOneInputFile() throws Exception
    {
        Main.main(constructArgs("file1.zip"));
    }

    @Test(expected = FileNotFoundException.class) /* passes validation, but these files don't actually exist */
    public void testTwoInputFiles() throws Exception{
        Main.main(constructArgs("file1.zip", "file2.zip"));
    }
}
