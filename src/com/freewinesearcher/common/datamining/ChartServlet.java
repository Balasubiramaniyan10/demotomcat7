package com.freewinesearcher.common.datamining;
/*
 * Deze servlet dient om een grafiek te tonen
 */

import javax.servlet.http.HttpServlet; 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; 
import javax.servlet.ServletException;

import java.io.*;
import org.jfree.chart.*;
import org.jfree.chart.renderer.*;
import org.jfree.data.xy.*;
import org.jfree.chart.plot.*;
import org.jfree.data.time.*;

import com.freewinesearcher.common.datamining.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.*;

/**
 * @author Jasper
 *
 */
public class ChartServlet extends HttpServlet {
	static final long serialVersionUID=5719896;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void service(HttpServletRequest _request, HttpServletResponse _response) 
	throws ServletException, IOException   
	{
		String attributename="";
		Matcher matcher;
		Pattern pattern;
		pattern=Pattern.compile("/chart/([^/]+)/");
		matcher=pattern.matcher((String)_request.getRequestURI());
		if (matcher.find()){
			attributename="chart"+(matcher.group(1));
		}
		Chart chart=(Chart)_request.getSession().getAttribute(attributename);
		if (chart!=null){


//			send the picture
			OutputStream out = _response.getOutputStream();
			_response.setContentType("image/png");
			ChartUtilities.writeChartAsPNG(out, chart.chart, chart.xscale, chart.yscale);
		}
		_request.getSession().removeAttribute(attributename);
		chart=null;


	} /* (sessie != null) */ 



}
