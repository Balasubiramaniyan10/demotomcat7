package com.freewinesearcher.online.winebottle;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.imageio.ImageIO;
import javax.media.j3d.*;
import javax.vecmath.*;

import com.freewinesearcher.common.Dbutil;

/** Class CapturingCanvas3D, using the instructions from the Java3D 
    FAQ pages on how to capture a still image in jpeg format.

    A capture button would call a method that looks like


    public static void captureImage(CapturingCanvas3D MyCanvas3D) {
	MyCanvas3D.writeJPEG_ = true;
	MyCanvas3D.repaint();
    }


    Peter Z. Kunszt
    Johns Hopkins University
    Dept of Physics and Astronomy
    Baltimore MD
*/

public class CapturingCanvas3D extends Canvas3D  {

    public boolean writeJPEG=false;
    
    
    public CapturingCanvas3D(GraphicsConfiguration gc) {
    	super(gc);
    	
    	
	}

    public void postSwap() {
	if(writeJPEG) {
		Raster ras;
		GraphicsContext3D  ctx;
		BufferedImage bimg=null;
		boolean ok=false;
		int n=0;
		while(!ok&&n<10){
			n++;
			ctx= getGraphicsContext3D();

			// The raster components need all be set!
			ras = new Raster(
					new Point3f(-1.0f,-1.0f,-1.0f),
					Raster.RASTER_COLOR,
					0,0,
					WineBottle.imagewidth,WineBottle.imageheight,
					new ImageComponent2D(
							ImageComponent.FORMAT_RGB,
							new BufferedImage(WineBottle.imagewidth,WineBottle.imageheight,
									BufferedImage.TYPE_INT_RGB)),
									null);

			
			ctx.readRaster(ras);

			// Now strip out the image info
			bimg =  ras.getImage().getImage();
			
			int color=bimg.getRGB(10, 10);
			if (bimg.getRGB(1,1)!=-16777216) {
				ok=true;
				// write that to the database
				Connection con=Dbutil.openNewConnection();
				try {
					BufferedImage img=new BufferedImage(160,180,BufferedImage.TYPE_INT_RGB);
					Graphics2D g = img.createGraphics();
					   AffineTransform at =
					      AffineTransform.getScaleInstance(0.3333, 0.3333);
					   g.drawRenderedImage(bimg,at);

					ByteArrayOutputStream baos=new ByteArrayOutputStream();
					ImageIO.write((RenderedImage) img, "png", baos);
					baos.close();
					ByteArrayInputStream is=new ByteArrayInputStream(baos.toByteArray());
					String sqlStatement = "update wineads set image=? where wineid=1;";
					PreparedStatement pstmt = con.prepareStatement(sqlStatement);
					//	   set up input stream
					pstmt.setBinaryStream(1,is,is.available());
					//	   execute statement
					pstmt.executeUpdate();
					//System.exit(0);

				} catch ( Exception e ) {
					Dbutil.logger.error("Cannot generate bottle graphics",e);
				} finally {
					Dbutil.closeConnection(con);
				}
			    
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					
				}
			}

		}
		if (!ok){
			//System.exit(0);

		}
		//frame.dispose();	
	}
    }
}
