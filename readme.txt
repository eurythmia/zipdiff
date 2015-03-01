ZipDiff-ng is a fork of ZipDiff with the intention of modernizing
and simplifying the tool.

-----------------------------------------------------------------------------

ZipDiff-ng compares two zip files and creates a list of differences. Plain text, .xml, .html and even a .zip file are supported as output formats.


Command line arguments
----------------------

java -jar zipdiff-ng.jar [options] foo.zip bar.zip

Valid options are:

-crc                   compares the crc values instead of the file content
-ts                    compares timestamps instead of file content
-output                name of the output file
-skipOutputPrefix n    number of path segment to skip in the output file
-skipPrefix1 n         number of path segment to skip in the first file
-skipPrefix2 n         number of path segment to skip in the second file
-filter <regex>        regex of filenames within the zip to check

This version can be found at https://github.com/eurythmia/zipdiff-ng


The original zipdiff project was developed by Sean C. Sullivan and James Stewart at http://zipdiff.sourceforge.net/

License:  see LICENSE.txt
