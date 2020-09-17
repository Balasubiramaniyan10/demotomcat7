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
import java.awt.geom.Rectangle2D;

import com.sun.j3d.utils.image.TextureLoader;


public class Label extends Shape3D {
	Texture2D texture=null;
	int x=0;
	int y=0;
	int size=0;
	public float labelwidth=0.6f;
	int segments=20; //Number of segments
	public float radius=0.3f;
	public int pixels=512;

	////////////////////////////////////////////
	//
	// create twisted strip subgraph
	//
	public Label(String filename) {
		getLabel("C:\\Users\\Jasper\\Desktop\\IMG_0415.jpg","");
		this.setGeometry(createGeometry());
		this.setAppearance(createAppearance());

	} // end of Twist constructor
	public Label(String winename,String vintage,String region, String price, String url,ArrayList<String> ratings) {
		if (url==null||url.equals("")) {
			createLabel(winename,vintage,region,price, ratings);
		} else {
			getLabel(url,price);
		}
		this.setGeometry(createGeometry());
		this.setAppearance(createAppearance());

	} // end of Twist constructor
	Geometry createGeometry() {

		float scalefactor=1f;
		if ((y*100/x)>110){
			// Make label smaller
			scalefactor=(float)((0.5+(float)x/(float)y) /1.5);
		}
		radius=radius*((float)1+scalefactor)/2;
		labelwidth=labelwidth*scalefactor;
		float height=(float) ((float)y/(float)x*labelwidth*Math.PI*radius)/2;
		float vposition=(float)(0.05-(0.4f*height));
		TriangleStripArray twistStrip;
		// create triangle strip for Twist
		int N = 2*(segments+1);
		int stripCounts[] = { N };
		int coordCounts[] = { N };
		twistStrip = new TriangleStripArray(
				N,
				TriangleStripArray.COORDINATES | TriangleStripArray.TEXTURE_COORDINATE_2|TriangleStripArray.NORMALS,
				stripCounts);

		double a;
		double lasta=0;
		int v;
		Vector3f norm = new Vector3f();
		for (v = 0, a = 0; v < N; v += 2, a = (v) * labelwidth * Math.PI / (N - 2)) {
			//System.out.println(label.radius*Math.cos(a));
			//twistStrip.setTextureCoordinate(0,v,new TexCoord2f(((float)10*v/(N-2)),-1.0f));
			//twistStrip.setTextureCoordinate(0,v+1,new TexCoord2f(((float)10*v/(N-2)),1.0f));
			//System.out.println(((float)v/(N-2)));
			//texCoords[v]=new TexCoord2f(((float)v/(N-2)),0.0f);
			//texCoords[v+1]=new TexCoord2f(((float)v/(N-2)),1.0f);
			if (scalefactor<1)	a=a+(1-scalefactor)* labelwidth * Math.PI;
			twistStrip.setCoordinate(v, new Point3d(radius*Math.cos(a) 
					, -1*height+vposition,radius*Math.sin(a)));
			twistStrip.setCoordinate(v + 1, new Point3d(radius*Math.cos(a)
					, height+vposition, radius*Math.sin(a)));
			twistStrip.setTextureCoordinate(0,v, new TexCoord2f(1-(float)v/(N-2),0f));
			twistStrip.setTextureCoordinate(0,v+1, new TexCoord2f(1-(float)v/(N-2),1f));

			norm.set((float) (Math.cos(a)), 0, (float) (Math.sin(a)));
			norm.normalize();
			twistStrip.setNormal(v, norm);
			twistStrip.setNormal(v + 1, norm);

		}
		return twistStrip;

	}

	Appearance createAppearance() {

		Appearance appearance = new Appearance();
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setBackFaceNormalFlip(true);
		appearance.setPolygonAttributes(polyAttrib);
		appearance.setColoringAttributes(new ColoringAttributes(1f,1f,1f,ColoringAttributes.SHADE_GOURAUD));

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
		paper.setAmbientColor(0.7f,0.7f,0.7f);
		paper.setDiffuseColor(0.3f,0.3f,0.3f);
		paper.setEmissiveColor(0.2f,0.2f,0.2f);
		paper.setSpecularColor(0.0f,0.0f,0.0f);
		paper.setLightingEnable(true);
		paper.setShininess(22f);
		return paper;
	}




	public void getLabel(String filename, String price){
		BufferedImage bi=null;
		if (filename.startsWith("http")){
			try{
				URL url=new URL(filename);
				bi = ImageIO.read(url);
			} catch (Exception e){
				Dbutil.logger.info("Could not find file "+filename);
			}
		} else {
			File file=new File(filename);
			// Read from a URL
			try{
				bi = ImageIO.read(file);
			} catch (Exception e){
				Dbutil.logger.info("Could not find file "+filename);
			}
		}
		if (bi!=null){
			BufferedImage sizedbi=new BufferedImage(512,512*bi.getHeight()/bi.getWidth(),bi.getType());
			Graphics2D g2d = sizedbi.createGraphics(); 
			g2d.drawImage(bi, 0,0, sizedbi.getWidth(), sizedbi.getHeight(), null);
			g2d.dispose();
			this.x=sizedbi.getWidth();
			this.y=sizedbi.getHeight();
			Graphics g=sizedbi.getGraphics();
			//drawPriceLabel(g,price);
			


			ImageComponent2D image=scale(sizedbi);
			size=image.getWidth();
			//ImageComponent2D image=new ImageComponent2D(ImageComponent2D.FORMAT_RGB4,256,256);



			//			Set the image to the specified RenderedImage
			//image.set(bi);

			if(image == null) {
				System.out.println("load failed for texture: "+filename);
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

		BufferedImage bi=new BufferedImage(pixels,pixels, BufferedImage.TYPE_INT_ARGB);
		if (bi!=null){
			this.x=pixels;
			this.y=pixels;
			Graphics g=bi.getGraphics();
			Graphics2D g2d = (Graphics2D) g;

			g.setColor(new Color(255,255,(int)(255-((double)Math.random()*10)),255));
			g.fillRect(0, 0, this.x, this.y);
			g.setColor(new Color(0,0,0));
			Font font = new Font("Serif", Font.PLAIN, 36);
			g.setFont(font);
			//g.drawString(text, 1, 100);
			double offsetx=pixels/20;
			double offsety=pixels/20;

			offsety=writeText(g2d, winename.split("\\\\n")[0],new Font("serif", Font.BOLD, 44),Color.black, offsetx,offsety);
			if (winename.split("\\\\n").length>1) offsety=writeText(g2d, winename.split("\\\\n")[1],new Font("georgia", Font.BOLD, 55),Color.black, offsetx,offsety);
			offsety=writeText(g2d, vintage,new Font("georgia", Font.BOLD, 45),Color.black, offsetx,offsety);
			offsety=writeText(g2d, region.toUpperCase(), new Font("Roman", Font.PLAIN, 35),Color.black, offsetx,Math.max(pixels*0.50,offsety));
			//drawRatings(g2d, ratings);
			//drawPriceLabel(g,price);
			
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

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

	private void drawPriceLabel(Graphics g, String price) {
		ArrayList<Color> colors=new ArrayList<Color>();
		colors.add(new Color(255,100,100));
		colors.add(new Color(255,255,000));
		double offsetx = (double)Math.random()*x/1.5+x/12;
		double offsety=y*0.93;
		g.setColor(colors.get((int)Math.floor(Math.random()*colors.size())));
		g.fillRect((int)(offsetx+(x/40)), (int)(offsety-((int)((40*x)/400))), (int)(x*price.length()/17), x/7);
		Graphics2D g2d=(Graphics2D) g;
		g2d.setPaint(Color.black);
		Point2D.Double pen = new Point2D.Double(offsetx,
				offsety);

		//--- Set the width of the TextLayout box
		double width = x*9/10;
		AttributedString paragraphText = new AttributedString("�"+price.split(",")[0]);
		paragraphText.addAttribute(TextAttribute.FONT, new Font("georgia", Font.PLAIN, (int)((48*x)/400)));
		paragraphText.addAttribute(TextAttribute.FONT, new Font("georgia", Font.BOLD, (int)((29*x)/400)),0,1);
		LineBreakMeasurer lineBreaker = new LineBreakMeasurer(paragraphText
				.getIterator(), new FontRenderContext(null, true, true));
		TextLayout layout;
		while ((layout = lineBreaker.nextLayout((float) width)) != null) {
			pen.x=offsetx+x/20;
			layout.draw(g2d, (float) pen.x, (float) pen.y);
			pen.x+=layout.getAdvance();

		}
		paragraphText = new AttributedString(price.split(",")[1]);
		paragraphText.addAttribute(TextAttribute.FONT, new Font("georgia", Font.PLAIN, (int)((29*x)/400)));
		lineBreaker = new LineBreakMeasurer(paragraphText
				.getIterator(), new FontRenderContext(null, true, true));
		while ((layout = lineBreaker.nextLayout((float) width)) != null) {
			pen.y-=(int)((12*x)/400);
			layout.draw(g2d, (float) pen.x, (float) pen.y);

		}
	}


	private void drawRatings(Graphics g, ArrayList<String> ratings) {
		if (ratings!=null){
			double offsetx = x/2-ratings.size()*x/16;
			double offsety=y*0.75;
			Graphics2D g2d=(Graphics2D) g;
			g2d.setPaint(Color.black);
			Point2D.Double pen = new Point2D.Double(offsetx,
					offsety);

			//--- Set the width of the TextLayout box
			double width = x*9/10;
			AttributedString paragraphText;
			TextLayout layout;
			for (int i=0;i<ratings.size();i=i+2){
				paragraphText = new AttributedString(" "+ratings.get(i)+" ");
				paragraphText.addAttribute(TextAttribute.FONT, new Font("arial", Font.PLAIN, 29));
				paragraphText.addAttribute(TextAttribute.BACKGROUND, Color.darkGray);
				paragraphText.addAttribute(TextAttribute.FOREGROUND, Color.white);
				LineBreakMeasurer lineBreaker = new LineBreakMeasurer(paragraphText
						.getIterator(), new FontRenderContext(null, true, true));

				while ((layout = lineBreaker.nextLayout((float) width)) != null) {
					layout.draw(g2d, (float) pen.x, (float) pen.y);
					pen.x+=layout.getAdvance();

				}
				paragraphText = new AttributedString(" "+ratings.get(i+1)+" ");
				paragraphText.addAttribute(TextAttribute.FONT, new Font("arial", Font.PLAIN, 29));
				paragraphText.addAttribute(TextAttribute.BACKGROUND, Color.LIGHT_GRAY);
				paragraphText.addAttribute(TextAttribute.FOREGROUND, Color.black);
				lineBreaker = new LineBreakMeasurer(paragraphText
						.getIterator(), new FontRenderContext(null, true, true));
				while ((layout = lineBreaker.nextLayout((float) width)) != null) {
					layout.draw(g2d, (float) pen.x, (float) pen.y);
					pen.x+=layout.getAdvance()+x/40;
				}
			}
		}


	}

	public double writeText(Graphics2D g2d,String text, Font font, Color color,double offsetx,double offsety){
		if (text.length()>0){
			//--- Translate the origin to 0,0 for the top left corner
			//        g2d.translate(pageFormat.getImageableX(), pageFormat
			//            .getImageableY());

			//--- Set the drawing color to black
			g2d.setPaint(color);


			//--- Create a point object to set the top left corner of the
			// TextLayout object
			Point2D.Double pen = new Point2D.Double(offsetx,
					offsety);

			//--- Set the width of the TextLayout box
			double width = x*9/10;

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
		return offsety;
	}

	private ImageComponent2D scale(BufferedImage bi)
	{
		ImageComponent2D scaledImage=null;
		x=bi.getWidth();
		y=bi.getHeight();
		int size=Math.max(x,y);
		int target=Math.round((float)Math.pow(2, Math.round((float)((Math.log(size)/Math.log(2)+0.5)))));
		//bi.createGraphics()..drawImage(bi, 0, 0, target, target, null);

		target=512;

		TextureLoader texLoader;
		texLoader = new TextureLoader(bi,0);


		scaledImage = texLoader.getScaledImage(target,target);


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