/* zipdiff is available under the terms of the
 * Apache License, version 2.0
 *
 * Link: http://www.apache.org/licenses/
 */
package zipdiff.output;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

import zipdiff.Differences;

/**
 *
 * Generates xml output for a Differences instance
 *
 * @author Sean C. Sullivan
 *
 */
public class XmlBuilder extends AbstractBuilder {

	/**
	 * builds the output
	 *
	 * @param out OutputStream to write to
	 * @param d differences
	 */
	@Override
	public void build(OutputStream out, Differences d) {
		PrintWriter pw = new PrintWriter(out);

		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw.print("<zipdiff filename1=\"");

		String filename1 = d.getFilename1();

		if (filename1 == null) {
			filename1 = "filename1.zip";
		}
		pw.print(filename1);
		pw.print("\" filename2=\"");

		String filename2 = d.getFilename2();

		if (filename2 == null) {
			filename2 = "filename2.zip";
		}
		pw.print(filename2);
		pw.println("\">");

		pw.println("<differences>");
        writeStatusTags(pw, "added", d.getAdded().keySet());
        writeStatusTags(pw, "removed", d.getRemoved().keySet());
        writeStatusTags(pw, "changed", d.getChanged().keySet());
		pw.println("</differences>");
		pw.println("</zipdiff>");

		pw.flush();
	}

    protected void writeStatusTags(PrintWriter pw, String statusTag, Set<String> modified) {
        for(String key : modified) {
            pw.print(String.format("<%s>%s</%s>",statusTag,key,statusTag));
        }
    }
}
