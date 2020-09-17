package com.freewinesearcher.online;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.freewinesearcher.common.Dbutil;

public class Text2Gif  extends HttpServlet {
	private String text;
	private String family="Arial";
	private int size=14;
	private int style=Font.PLAIN;
	private Color background=Color.GRAY;
	private Color color=Color.WHITE;

	public void service(HttpServletRequest request, HttpServletResponse response) {
		Text2Gif t=new Text2Gif();
		if (request.getParameter("region")!=null){
			int regionid=0;
			try{regionid=Integer.parseInt(request.getParameter("region"));}catch(Exception e){}
			if (regionid>0){
				t.text=Dbutil.readValueFromDB("select * from kbregionhierarchy where id="+regionid, "shortregion");
			}
		}
		if (request.getParameter("address")!=null){
			int producerid=0;
			try{producerid=Integer.parseInt(request.getParameter("address"));}catch(Exception e){}
			if (producerid>0){
				t.text=Dbutil.readValueFromDB("select * from kbproducers where id="+producerid, "address");
				if (request.getParameter("mobile")!=null){
					t.color=new Color(208,208,208);
					t.background=new Color(26,26,26);
					t.family="Helvetica";
					t.size=16;
					t.style=Font.BOLD;
				} else{
					t.color=new Color(77*256*256+39);
					t.background=Color.WHITE;
					t.family="Georgia";
					t.size=14;
				}
			}
		}
		if (t.text!=null){
			response.setContentType("image/gif");
			try {
				OutputStream os = response.getOutputStream();
				BufferedImage buffer = t.getImage(request);
				response.setStatus(200);
				ImageIO.write(buffer, "gif", os);
				os.close();
			} catch (Exception e) {
				response.setStatus(500);
				Dbutil.logger.error("Problem: ", e);
			}
		}
	}

	private BufferedImage getImage(HttpServletRequest request){
		String font_file = "dungeon.ttf";
		Font font=new Font(family,style,size);
		BufferedImage buffer =
			new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = buffer.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		FontRenderContext fc = g2.getFontRenderContext();
		Rectangle2D bounds = font.getStringBounds(text,fc);

		// calculate the size of the text
		int width = (int) bounds.getWidth()+2;
		int height = (int) bounds.getHeight()+1;

		// prepare some output
		buffer = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		g2 = buffer.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(font);
		g2.setColor(background);
		g2.fillRect(0,0,width,height);
		g2.setColor(color);
		g2.drawString(text,1,1+(int)-bounds.getY());
		return buffer;
	}
}
