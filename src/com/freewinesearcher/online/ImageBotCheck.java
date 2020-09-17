package com.freewinesearcher.online;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.freewinesearcher.common.Configuration;
import com.freewinesearcher.common.Dbutil;

public class ImageBotCheck extends HttpServlet {
	private static final long serialVersionUID = 3641611434289286048L;

	private String text;
	private String family = "Arial";
	private int size = 14;
	private int style = Font.PLAIN;
	private Color background = Color.GRAY;
	private Color color = Color.WHITE;

	public void service(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		Dbutil.logger.info("*********** IMAGE BOT CHECK A -> session: " + session);
		if (session != null) {
			String respcode = request.getParameter("response");
			Dbutil.logger.info("*********** IMAGE BOT CHECK B-> respcode: " + respcode);
			PageHandler p = PageHandler.getInstance(request, response);
			String challenge = PageHandler.getInstance(request, response).challenge;
			Dbutil.logger.info("*********** IMAGE BOT CHECK C-> challenge: " + challenge);
			if (challenge == null) {
				challenge = "";
			}
			Dbutil.logger.info("*********** IMAGE BOT CHECK D-> " + challenge + ".equals(" + respcode + "): "
					+ (challenge.equals(respcode)));
			if (challenge.equals(respcode)) {
				String hostname = "";
				try {
					hostname = InetAddress.getByName(p.ipaddress).getHostName();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				Dbutil.logger.info("*********** IMAGE BOT CHECK E-> hostname: " + hostname);
				Dbutil.logger.info("*********** IMAGE BOT CHECK F->Check OK, normal ip='" + p.normalip
						+ "', forwarded ip='" + p.forwardedforip + "', useragent='" + p.useragent + "', hostname='"
						+ hostname + "', referrer='" + p.referrer + "', target='" + p.URLbeforebotcheck + "'");

				session.setAttribute("imageverified", true);
				PageHandler.getInstance(request, response).botstatus = 0;
				Dbutil.logger.info("*********** IMAGE BOT CHECK G-> setting botstatus=0");
			} else {
				Dbutil.logger.info("*********** IMAGE BOT CHECK H ");
				if (PageHandler.getInstance(request, response).botstatus == 3) {
					Dbutil.logger.info("*********** IMAGE BOT CHECK I setting botstatus=2");
					PageHandler.getInstance(request, response).botstatus = 2;
				}
				if (Configuration.logSuspectedBot) {
					Dbutil.logger.info("*********** IMAGE BOT CHECK J Bot detected, normal ip='" + p.normalip
							+ "', forwarded ip='" + p.forwardedforip + "', useragent='" + p.useragent + "', referrer='"
							+ p.referrer + "', target='" + p.URLbeforebotcheck + "'");
				}
			}
			ImageBotCheck t = new ImageBotCheck();
			t.text = "";
			if (t.text != null) {
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
	}

	private BufferedImage getImage(HttpServletRequest request) {
		// String font_file = "dungeon.ttf";
		Font font = new Font(family, style, size);
		BufferedImage buffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = buffer.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		FontRenderContext fc = g2.getFontRenderContext();
		Rectangle2D bounds = font.getStringBounds(text, fc);

		// calculate the size of the text
		int width = (int) bounds.getWidth() + 2;
		int height = (int) bounds.getHeight() + 1;

		// prepare some output
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g2 = buffer.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(font);
		g2.setColor(background);
		g2.fillRect(0, 0, width, height);
		g2.setColor(color);
		g2.drawString(text, 1, 1 + (int) -bounds.getY());
		return buffer;
	}
}