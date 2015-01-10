
package demo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import bzip2.*;


public class Compress {

	public static void main (String[] args) throws IOException {

		if (args.length == 0) {
			System.err.println ("BZip2 compressor\n\nUsage:\n  java demo.Compress <filename>\n");
			System.exit (1);
		}

		File inputFile = new File (args[0]);
		if (!inputFile.exists() || !inputFile.canRead()) {
			System.err.println ("Cannot read file " + inputFile.getPath());
			System.exit (1);
		}

		File outputFile = new File (args[0] + ".bz2");
		if (outputFile.exists()) {
			System.err.println ("File " + outputFile.getPath() + " already exists");
			System.exit (1);
		}

		InputStream fileInputStream = new BufferedInputStream (new FileInputStream (inputFile));
		OutputStream fileOutputStream = new BufferedOutputStream (new FileOutputStream (outputFile), 524288);
		BZip2OutputStream outputStream = new BZip2OutputStream (fileOutputStream);

		byte[] buffer = new byte [524288];
		int bytesRead;
		while ((bytesRead = fileInputStream.read (buffer)) != -1) {
			outputStream.write (buffer, 0, bytesRead);
		}
		outputStream.close();

	}

}
