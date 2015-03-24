/* zipdiff-ng is available under the terms of the
 * Apache License, version 2.0
 *
 * Link: http://www.apache.org/licenses/
 */

package zipdiff.util;

import com.beust.jcommander.IStringConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class StreamConverter implements IStringConverter<PrintStream> {
    @Override
    public PrintStream convert(String value){
        if ("-".equals(value)) {
            return System.out;
        }
        try {
            return new PrintStream(new File(value));
        } catch(FileNotFoundException e) {
            throw new OutputFileNotFoundException(value, e);
        }

    }

    private class OutputFileNotFoundException extends RuntimeException {
        public OutputFileNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
