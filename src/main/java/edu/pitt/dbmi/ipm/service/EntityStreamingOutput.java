package edu.pitt.dbmi.ipm.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.ws.rs.WebApplicationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
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
 * Streams entity for downloading on client machine.
 * 
 * @author opm1
 * @version 1
 * @since Dec 8, 2015
 * 
 */
public class EntityStreamingOutput implements javax.ws.rs.core.StreamingOutput {
	private HttpEntity entity = null;
	private HttpClient httpclient = null;

	/**
	 * 
	 * @param httpclient - HttpClient
	 * @param entity - HttpEntity
	 */
	public EntityStreamingOutput(HttpClient httpclient, HttpEntity entity) {
		this.httpclient = httpclient;
		this.entity = entity;
	}

	/**
	 * 
	 * @return HttpEntity entity
	 */
	public HttpEntity getEntity() {
		return entity;
	}

	/**
	 * Writes to stream.
	 */
	@Override
	public void write(OutputStream output) throws IOException,
			WebApplicationException {

		byte[] buffer = new byte[1024];
		int read = -1;
		InputStream instream = null;

		try {

			instream = entity.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					instream));
			Writer writer = new BufferedWriter(new OutputStreamWriter(output));

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

			if (output != null)
				output.close();

			httpclient.getConnectionManager().shutdown();
		}

	}

}