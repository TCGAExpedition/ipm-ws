package edu.pitt.dbmi.ipm.service;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.ws.rs.WebApplicationException;

/**
 * Copyright (C) 2015  University of Pittsburgh
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * 
 * Streams pre-loaded temporal file on server to be downloaded on a client. 
 * Used for temporary stored in CATALINA_HOME/temp dir requested tsv files from PostgresSQL queries, since PostgreSQL can't stream data itself. 
 * 
 * @author opm1
 * @version 1
 * @since Dec 8, 2015
 * 
 */
public class FileStreamingOutput implements javax.ws.rs.core.StreamingOutput {
	private File temp_file = null;

	/**
	 * Constructor
	 * 
	 * @param temp_file - temporal file on server to save output before streaming.
	 */
	public FileStreamingOutput(File temp_file) {
		this.temp_file = temp_file;
	}

	/**
	 * Writes to output stream
	 * 
	 * @param os - OutputStream
	 */
	@Override
	public void write(OutputStream os) throws IOException,
			WebApplicationException {

		InputStream instream = null;
		Writer writer = null;

		try {

			BufferedReader br = new BufferedReader(new FileReader(temp_file));
			writer = new BufferedWriter(new OutputStreamWriter(os));

			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.replaceAll("\"", "");
				writer.write(line + "\n");
				writer.flush();
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {

			if (instream != null)
				instream.close();

			if (os != null)
				os.close();
			if (writer != null)
				writer.close();

			// remove file
			if (temp_file != null) {
				temp_file.delete();
			}

		}
	}

}
