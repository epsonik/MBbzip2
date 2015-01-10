
package demo;
import bzip2.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Decompress {
	public static void main (String[] args) throws IOException {

		if (args.length == 0) {
			System.err.println ("BZip2 decompressor\n\nUsage:\n  java demo.Decompress <filename>\n");
			System.exit (1);
		}

		File inputFile = new File (args[0]);
		if (!inputFile.exists() || !inputFile.canRead() || !args[0].endsWith(".bz2")) {
			System.err.println ("Cannot read file " + inputFile.getPath());
			System.exit (1);
		}

		File outputFile = new File (args[0].substring (0, args[0].length() - 4));
		if (outputFile.exists()) {
			System.err.println ("File " + outputFile.getPath() + " already exists");
			System.exit (1);
		}

		InputStream fileInputStream = new BufferedInputStream (new FileInputStream (inputFile));
		BZip2InputStream inputStream = new BZip2InputStream (fileInputStream, false);
		OutputStream fileOutputStream = new BufferedOutputStream (new FileOutputStream (outputFile), 524288);

		byte[] decoded = new byte [524288];
		int bytesRead;
		while ((bytesRead = inputStream.read (decoded)) != -1) {
			fileOutputStream.write (decoded, 0, bytesRead) ;	
		}
		fileOutputStream.close();

	}

}
