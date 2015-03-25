/* zipdiff-ng is available under the terms of the
 * Apache License, version 2.0
 *
 * Link: http://www.apache.org/licenses/
 */

package zipdiff.util.commandline.validators;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.util.regex.Pattern;

public class OutputFormat implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        String validOptionRegex = "xml|text|zip|html";
        if (!Pattern.compile("xml|text|zip|html").matcher(value).matches()) {
            throw new ParameterException(
                    String.format("%s must be one of the following: %s", name, validOptionRegex.replace('|', ','))
            );
        }
    }
}
