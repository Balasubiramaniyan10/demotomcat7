package com.freewinesearcher.online.winebottle;

import javax.imageio.ImageIO;
import javax.media.j3d.Geometry;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Material;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.*;
import javax.media.j3d.*;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Wijnzoeker;
import com.sun.j3d.utils.image.TextureLoader;

import java.applet.Applet;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.AttributedString;
import java.util.ArrayList;
import java.io.*;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import com.sun.j3d.utils.image.TextureLoader;


public class Back extends Shape3D {
	Texture2D texture=null;
	int x=0;
	int y=0;
	int size=0;
	public float labelwidth=0.6f;
	int segments=20; //Number of segments
	public static float radius=0.3f;
	public int pixels=400;
	
	////////////////////////////////////////////
	//
	// create twisted strip subgraph
	//
	public Back(String filename) {
		if (Wijnzoeker.serverrole.equals("DEV")) {
			getBack("https://localhost:6001/images/wood"+(int)(Math.floor(Math.random()*4)+1)+".jpg");
		} else {
			getBack("https://localhost/images/wood"+(int)(Math.floor(Math.random()*4)+1)+".jpg");
		}
		this.setGeometry(createGeometry());
		this.setAppearance(createAppearance());
		
	} // end of Twist constructor
	Geometry createGeometry() {

		float height=1.5f*getLabelHeight();
		float width=1.5f*getLabelWidth();
		
		float vposition=(float)(0.2-(0.5f*getLabelHeight()));
		TriangleStripArray twistStrip;
		// create triangle strip for Twist
		int N = 2*(1+1);
		int stripCounts[] = { N };
		int coordCounts[] = { N };
		twistStrip = new TriangleStripArray(
				N,
				TriangleStripArray.COORDINATES | TriangleStripArray.TEXTURE_COORDINATE_2|TriangleStripArray.NORMALS,
				stripCounts);

		twistStrip.setCoordinate(0, new Point3d(-1*width, -0.9*height,0));
		twistStrip.setCoordinate(1, new Point3d(width, -0.9*height,0));
		twistStrip.setCoordinate(2, new Point3d(-1*width, 1.1*height,0));
		twistStrip.setCoordinate(3, new Point3d(width, 1.1*height,0));
		twistStrip.setTextureCoordinate(0,0, new TexCoord2f(0.1f,0f));
		twistStrip.setTextureCoordinate(0,1, new TexCoord2f(0.9f,0f));
		twistStrip.setTextureCoordinate(0,2, new TexCoord2f(0.1f,1f));
		twistStrip.setTextureCoordinate(0,3, new TexCoord2f(0.9f,1f));
		Vector3f norm=new Vector3f();	
		norm.set(0f, 0f, 1f);
		norm.normalize();
		twistStrip.setNormal(0, norm);
		twistStrip.setNormal(1, norm);
		twistStrip.setNormal(2, norm);
		twistStrip.setNormal(3, norm);
		return twistStrip;

	}

	Appearance createAppearance() {

		Appearance appearance = new Appearance();
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setBackFaceNormalFlip(true);
		appearance.setPolygonAttributes(polyAttrib);

		Material material = paper();
		appearance.setTexture(texture);
		appearance.setTextureAttributes(attributes());
		appearance.setMaterial(material);
		appearance.setTransparencyAttributes(transparency());
		return appearance;
	}




	public TextureAttributes attributes(){
		TextureAttributes ta=new TextureAttributes();
		
		ta.setTextureMode(TextureAttributes.MODULATE);
		
		return ta;
	}

	public TransparencyAttributes transparency(){	
		TransparencyAttributes transpa = new TransparencyAttributes();
		transpa.setTransparency(0.0f);
		transpa.setTransparencyMode(TransparencyAttributes.NONE);
		return transpa;
	}





	public Material paper(){


		Material paper = new Material();
		paper.setAmbientColor(0.6f,0.6f,0.6f);
		paper.setDiffuseColor(0.6f,0.6f,0.6f);
		paper.setEmissiveColor(0.2f,0.2f,0.2f);
		paper.setSpecularColor(0.2f,0.2f,0.2f);
		paper.setLightingEnable(true);
		//paper.setShininess(122f);
		return paper;
	}
	
	
	
	
	public void getBack(String urlstr){

		BufferedImage bi=null;
		try{
			URL url=new URL(urlstr);
		
		// Read from a URL
		
			bi = ImageIO.read(url);
		} catch (Exception e){
			Dbutil.logger.info("Could not find url "+urlstr);
		}
		if (bi!=null){
			this.x=bi.getWidth();
			this.y=bi.getHeight();
			
			ImageComponent2D image=scale(bi);
			size=image.getWidth();
			//ImageComponent2D image=new ImageComponent2D(ImageComponent2D.FORMAT_RGB4,256,256);



//			Set the image to the specified RenderedImage
			//image.set(bi);

			if(image == null) {
				System.out.println("load failed for texture: "+urlstr);
			}
			texture = new Texture2D(Texture.NICEST, Texture.MULTI_LEVEL_LINEAR,
					image.getWidth(), image.getHeight());
			texture.setImage(0, image);
			texture.setEnable(true);
			texture.setMagFilter(Texture.BASE_LEVEL_POINT);
			texture.setMinFilter(Texture.BASE_LEVEL_POINT);
			texture.setBoundaryModeS(Texture.CLAMP);
			texture.setBoundaryModeT(Texture.CLAMP);
			//texture.setBoundaryColor(0f,0f,0f,0f);
		}
		
	}
	
	public void createLabel(String winename, String vintage, String region, String price,ArrayList<String> ratings){

		BufferedImage bi=new BufferedImage(pixels,pixels, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		if (bi!=null){
			this.x=bi.getWidth();
			this.y=bi.getHeight();
			Graphics g=bi.getGraphics();
			g.setColor(new Color(255,255,255));
			g.fillRect(0, 0, this.x, this.y);
			g.setColor(new Color(0,0,0));
			Font font = new Font("Serif", Font.PLAIN, 36);
	        g.setFont(font);
	        //g.drawString(text, 1, 100);
	        Graphics2D g2d = (Graphics2D) g;
	        double offset=pixels/20;
	        offset=writeText(g2d, winename,new Font("serif", Font.BOLD, 40),Color.black, offset);
	        offset=writeText(g2d, vintage,new Font("serif", Font.BOLD, 40),Color.black, offset);
	        offset=writeText(g2d, region, new Font("georgia", Font.PLAIN, 34),Color.black, offset);
	        offset=writeText(g2d, price, new Font("georgia", Font.PLAIN, 45),Color.red, offset);
	        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	        String fontNames[] = ge.getAvailableFontFamilyNames();
	        
	        // Iterate the font family names
	        for (int i=0; i<fontNames.length; i++) {
	        	System.out.println(fontNames[i]);
	        }
	        
	        
/*	        
	        
	      //--- Create the Graphics2D object
	        

	        //--- Translate the origin to 0,0 for the top left corner
//	        g2d.translate(pageFormat.getImageableX(), pageFormat
//	            .getImageableY());

	        //--- Set the drawing color to black
	        g2d.setPaint(Color.black);

	        
	        //--- Create a point object to set the top left corner of the
	        // TextLayout object
	        Point2D.Double pen = new Point2D.Double(pixels/20,
	            pixels/20);

	        //--- Set the width of the TextLayout box
	        double width = pixels*9/10;

	        //--- Create an attributed string from the text string. We are
	        // creating an
	        //--- attributed string because the LineBreakMeasurer needs an
	        // Iterator as
	        //--- parameter.
	        AttributedString paragraphText = new AttributedString(winename+"\n"+region);

	        //--- Set the font for this text
	        paragraphText.addAttribute(TextAttribute.FONT, new Font("serif",
		            Font.PLAIN, pixels/7),0,winename.length());
	        paragraphText.addAttribute(TextAttribute.FONT, new Font("serif",
		            Font.PLAIN, pixels/9),winename.length()+1,winename.length()+region.length()+1);

	        //--- Create a LineBreakMeasurer to wrap the text for the
	        // TextLayout object
	        //--- Note the second parameter, the FontRendereContext. I have set
	        // the second
	        //--- parameter antiAlised to true and the third parameter
	        // useFractionalMetrics
	        //--- to true to get the best possible output
	        LineBreakMeasurer lineBreaker = new LineBreakMeasurer(paragraphText
	            .getIterator(), new FontRenderContext(null, true, true));

	        //--- Create the TextLayout object
	        TextLayout layout;

	        //--- LineBreakMeasurer will wrap each line to correct length and
	        //--- return it as a TextLayout object
	        while ((layout = lineBreaker.nextLayout((float) width)) != null) {

	          //--- Align the Y pen to the ascend of the font, remember that
	          //--- the ascend is origin (0, 0) of a font. Refer to figure 1
	          pen.y += layout.getAscent();
	          pen.x=this.x/2-layout.getAdvance()/2;

	          //--- Draw the line of text
	          layout.draw(g2d, (float) pen.x, (float) pen.y);

	          //--- Move the pen to the next position adding the descent and
	          //--- the leading of the font
	          pen.y += layout.getDescent() + layout.getLeading();
	        }

	        
	        
	*/        
	        
	        
	        
	        
	        
	        
	        
			ImageComponent2D image=scale(bi);
			size=image.getWidth();
			//ImageComponent2D image=new ImageComponent2D(ImageComponent2D.FORMAT_RGB4,256,256);

			texture = new Texture2D(Texture.NICEST, Texture.MULTI_LEVEL_LINEAR,
					image.getWidth(), image.getHeight());
			texture.setImage(0, image);
			texture.setEnable(true);
			texture.setMagFilter(Texture.BASE_LEVEL_POINT);
			texture.setMinFilter(Texture.BASE_LEVEL_POINT);
			texture.setBoundaryModeS(Texture.CLAMP);
			texture.setBoundaryModeT(Texture.CLAMP);
			//texture.setBoundaryColor(0f,0f,0f,0f);
		}
		
	}
	
	public double writeText(Graphics2D g2d,String text, Font font, Color color,double offset){
		//--- Translate the origin to 0,0 for the top left corner
//        g2d.translate(pageFormat.getImageableX(), pageFormat
//            .getImageableY());

        //--- Set the drawing color to black
        g2d.setPaint(Color.black);

        
        //--- Create a point object to set the top left corner of the
        // TextLayout object
        Point2D.Double pen = new Point2D.Double(pixels/20,
            offset);

        //--- Set the width of the TextLayout box
        double width = pixels*9/10;

        //--- Create an attributed string from the text string. We are
        // creating an
        //--- attributed string because the LineBreakMeasurer needs an
        // Iterator as
        //--- parameter.
        AttributedString paragraphText = new AttributedString(text);

        //--- Set the font for this text
        paragraphText.addAttribute(TextAttribute.FONT, font);

        //--- Create a LineBreakMeasurer to wrap the text for the
        // TextLayout object
        //--- Note the second parameter, the FontRendereContext. I have set
        // the second
        //--- parameter antiAlised to true and the third parameter
        // useFractionalMetrics
        //--- to true to get the best possible output
        LineBreakMeasurer lineBreaker = new LineBreakMeasurer(paragraphText
            .getIterator(), new FontRenderContext(null, true, true));

        //--- Create the TextLayout object
        TextLayout layout;

        //--- LineBreakMeasurer will wrap each line to correct length and
        //--- return it as a TextLayout object
        while ((layout = lineBreaker.nextLayout((float) width)) != null) {

          //--- Align the Y pen to the ascend of the font, remember that
          //--- the ascend is origin (0, 0) of a font. Refer to figure 1
          pen.y += layout.getAscent();
          pen.x=this.x/2-layout.getAdvance()/2;

          //--- Draw the line of text
          layout.draw(g2d, (float) pen.x, (float) pen.y);

          //--- Move the pen to the next position adding the descent and
          //--- the leading of the font
          pen.y += layout.getDescent() + layout.getLeading();
        }
        return pen.y;

	}
	
	private ImageComponent2D scale(BufferedImage bi)
    {
		ImageComponent2D scaledImage=null;
		int x=bi.getWidth();
		int y=bi.getHeight();
        int size=Math.max(x,y);
        int target=Math.round((float)Math.pow(2, Math.round((float)((Math.log(size)/Math.log(2)+0.5)))));
        //bi.createGraphics()..drawImage(bi, 0, 0, target, target, null);
        

        TextureLoader texLoader;
        texLoader = new TextureLoader(bi,0);

        
        if ((x == target) && (y == target)) {
            scaledImage.set(bi);
        } else {
            scaledImage = texLoader.getScaledImage(target,target);
        }
        
        /*BufferedImage newImage = new BufferedImage(target, 
          target, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = newImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(bi, 0, 0, x,y, null);
        BufferedImage result=(BufferedImage)graphics2D.d;
        
        AffineTransform tx = new AffineTransform();
        //tx.scale(, 0.80);
        AffineTransformOp op = new AffineTransformOp(tx,
                                         AffineTransformOp.TYPE_BICUBIC);
        return op.filter(bi, null);
        */
        return scaledImage;
    }
	
	public float getLabelWidth(){
		return (float)(radius*Math.sin(labelwidth * Math.PI /2/segments))*2f*segments;
		
	}
		
	
	public float getLabelHeight(){
		return getLabelWidth()*y/x;
		//return (float)(radius * Math.PI* labelwidth*(float)y /(float)x);
	}



}
