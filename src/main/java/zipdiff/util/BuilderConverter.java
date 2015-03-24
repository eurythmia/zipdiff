/* zipdiff-ng is available under the terms of the
 * Apache License, version 2.0
 *
 * Link: http://www.apache.org/licenses/
 */

package zipdiff.util;

import com.beust.jcommander.IStringConverter;
import zipdiff.output.*;

public class BuilderConverter implements IStringConverter<Builder> {
    @Override
    public Builder convert(String value){
        //TODO: figure out how to inject a builder without knowing about the builders present.
        switch(value){
            case "html":
                return new HtmlBuilder();
            case "xml":
                return new XmlBuilder();
            case "zip":
                return new ZipBuilder();
            default:
                return new TextBuilder();
        }
    }
}
