package com.freewinesearcher.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Couple of String gzipping utilities.
 * @author Scott McMaster
 *
 */
public class Zipper {

	/**
	 * Gzip the input string into a byte[].
	 * @param input
	 * @return
	 * @throws IOException 
	 */
	public static byte[] zipObjectToBytes( Object inputobject  ) 
	{
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gzos = new GZIPOutputStream(bos);
			ObjectOutputStream oos = new ObjectOutputStream(gzos);
			oos.writeObject(inputobject);
			gzos.close();
			byte[] retval = bos.toByteArray();
			bos.close();
			return retval;
		} catch (Exception e) {
			Dbutil.logger.error("Problem: ", e);
			return null;
		}
	}

	/**
	 * Unzip a string out of the given gzipped byte array.
	 * @param bytes
	 * @return
	 * @throws IOException 
	 */
	public static Object unzipObjectFromBytes( byte[] bytes )  {
		Object obj=null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			BufferedInputStream bufis = new BufferedInputStream(new GZIPInputStream(bis));
			ObjectInputStream inobj=new ObjectInputStream(bufis);
			/*
    byte[] buf = new byte[1024];
    int len;
    while( (len = bufis.read(buf)) > 0 )
    {
      inobj.read(buf, 0, len);
    }
			 */

			obj = inobj.readObject();
			bis.close();
			bufis.close();
		} catch (Exception e) {
			Dbutil.logger.info("Could not read zipped object.",e);
		}

		return obj;
	}

	/**
	 * Static class.
	 *
	 */
	private Zipper()
	{
	}
}