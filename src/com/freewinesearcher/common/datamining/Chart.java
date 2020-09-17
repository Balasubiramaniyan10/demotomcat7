package com.freewinesearcher.common.datamining;
/*
 * Deze servlet dient om een grafiek te tonen
 */

import javax.servlet.http.HttpServlet; 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; 
import javax.servlet.ServletException;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.io.*;
import java.net.URL;

import org.jfree.chart.*;
import org.jfree.chart.renderer.*;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.xml.DatasetReader;
import org.jfree.data.xy.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.data.time.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.*;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.datamining.Pricehistory;

import java.util.Date;

import java.util.HashMap;
import java.util.Map.*;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.*;


/**
 * @author Jasper
 *
 */
public class Chart implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int masterid=0;
	public int id=0;
	public int xscale=300;
	public int yscale=200;
	public String title="";
	public JFreeChart chart;
	public boolean hasdata=false;
	public float average=0;

	public Chart(){
		masterid++;
		id=masterid;
	}

	public void createPerformanceChart(int hours){
		TimePeriodValuesCollection xyds=null;
		ResultSet rs=null;		
		Connection con=Dbutil.openNewConnection();
		TimePeriodValues clicks = new TimePeriodValues("Response time");
		TimePeriodValues clicks2 = new TimePeriodValues("");
		
		try{
			String query="select min(date) as min,max(date) as max,avg(loadtime) as loadtime from logging where date>now()- interval "+hours+" hour group by date(date),hour(date) order by date;";
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				clicks.add(new SimpleTimePeriod(rs.getTimestamp("min"),rs.getTimestamp("max")), rs.getDouble("loadtime"));
			}
			Dbutil.closeRs(rs);
			query="select min(date) as min,max(date) as max,avg(loadtime) as loadtime from logging where date>now()- interval "+hours+" hour and loadtime<10000 group by date(date),hour(date) order by date;";
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				clicks2.add(new SimpleTimePeriod(rs.getTimestamp("min"),rs.getTimestamp("max")), rs.getDouble("loadtime"));
			}
		} catch (Exception E){
			Dbutil.logger.error("Problem while looking up visitor overview",E);
		}finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		xyds = new TimePeriodValuesCollection();
		
		xyds.addSeries(clicks2);
		xyds.addSeries(clicks);
		
		chart = ChartFactory.createTimeSeriesChart(title, "Date", "Loadtime [ms]", xyds, true, true, false);
		chart.removeLegend();
		
	}
	
	public void createChart(String startdate, String enddate, int knownwineid, int vintage, Pricehistory.types type){
		// Constructor for showing the price history of a wine
		int n=0; // # datapoints >0
		float total=0; // total of values for creating average;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Pricehistory ph = new Pricehistory((sdf.parse(startdate)), sdf.parse(enddate), knownwineid,vintage,type);
			TimeSeriesCollection xyds = new TimeSeriesCollection();
			if (ph.hasdata){
				for (int i=0;i<ph.pricecollection.size();i++){

					TimeSeries timeseries = new TimeSeries("Price in � for "+com.freewinesearcher.common.Knownwines.getKnownWineName(knownwineid)+" "+vintage+" ("+knownwineid+")",
							org.jfree.data.time.Day.class);
					Iterator it = ph.pricecollection.get(i).keySet().iterator();
					while (it.hasNext()) {
						Object Key = it.next();
						String keystring = Key.toString();
						try {
							Date d = sdf.parse(keystring);
							timeseries.add(new Day(d), ph.pricecollection.get(i).get(Key));
							if (ph.pricecollection.get(i).get(Key)>1) {
								n++;
								total+=ph.pricecollection.get(i).get(Key);
							}
						} catch (Exception e) {
							com.freewinesearcher.common.Dbutil.logger.info("e", e);
						}

					}
					xyds.addSeries(timeseries);
				}
				chart = ChartFactory.createTimeSeriesChart(title, "Date", "Count", xyds, true, true, false);
				if (type.equals(Pricehistory.types.individual)){
					chart.removeLegend();
				}
				if (n>0){
					average=total/n;
					hasdata=true;
				}
			}
		} catch (Exception e) {
			com.freewinesearcher.common.Dbutil.logger.error("Problem: ", e);
		}		



	} /* (sessie != null) */ 

	public void createChart(int days){
		TimeSeriesCollection xyds=null;
		ResultSet rs=null;		
		Connection con=Dbutil.openNewConnection();
		TimeSeries visitors = new TimeSeries("Unique Visitors",	org.jfree.data.time.Day.class);
		TimeSeries hits = new TimeSeries("Hits",	org.jfree.data.time.Day.class);
		TimeSeries searches = new TimeSeries("Searches",	org.jfree.data.time.Day.class);
		TimeSeries clicks = new TimeSeries("Clicks",	org.jfree.data.time.Day.class);

		try{
			String query="select searchdate, sum(search) as searches, sum(uniquevisitors) as uniquevisitors,sum(clicked) as clicks, sum(sponsoredclicks) as sponsoredclicks, sum(earned) as earned, sum(rss) as rss, sum(rsshits) as rsshits, sum(hits) as hits, sum(bothits) as bothits,sum(retailers) as retailers, sum(abuse) as abuse from ("+
			//Search
			"SELECT date(date) as searchdate, count(*) as search, 0 as uniquevisitors, 0 as clicked,0 as sponsoredclicks,0 as earned,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse from logging where date>=DATE_SUB(curdate(),INTERVAL "+days+" DAY) and bot=0 and type='Search' and name !='' group by to_days(date) union " +
			//Visitors
			"SELECT date(date) as searchdate, 0 as search, count(distinct(ip)) as uniquevisitors, 0 as clicked,0 as sponsoredclicks,0 as earned,0 as rss, 0 as rsshits, count(*) as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "+days+" DAY) and bot=0 and type!='RSS' and type not like 'Abuse%' group by to_days(date) union " +
			//Clicks
			"SELECT date(date) as searchdate, 0 as search, 0 as uniquevisitors,count(*) as clicked,0 as sponsoredclicks,0 as earned,0 as rss, 0 as rsshits, 0 as hits,0 as bothits,0 as retailers, 0 as abuse  from logging where date>=DATE_SUB(curdate(),INTERVAL "+days+" DAY) and bot=0 and type='Link clicked' and cpc=0 group by to_days(date)) as thetable group by searchdate order by searchdate desc;";
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				visitors.add(new Day(rs.getDate("searchdate")), rs.getInt("uniquevisitors"));
				searches.add(new Day(rs.getDate("searchdate")), rs.getInt("searches"));
				hits.add(new Day(rs.getDate("searchdate")), rs.getInt("hits"));
				clicks.add(new Day(rs.getDate("searchdate")), rs.getInt("clicks"));
			}
		} catch (Exception E){
			Dbutil.logger.error("Problem while looking up visitor overview",E);
		}finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		xyds = new TimeSeriesCollection();
		xyds.addSeries(visitors);
		xyds.addSeries(hits);
		xyds.addSeries(searches);
		xyds.addSeries(clicks);
		chart = ChartFactory.createTimeSeriesChart(title, "Date", "Count", xyds, true, true, false);


	} /* (sessie != null) */ 

	public void createSiteChart(int days){
		TimeSeriesCollection xyds=null;
		ResultSet rs=null;		
		Connection con=Dbutil.openNewConnection();
		TimeSeries fws = new TimeSeries("Free Wine Searcher",	org.jfree.data.time.Day.class);
		TimeSeries vinopedia = new TimeSeries("Vinopedia",	org.jfree.data.time.Day.class);

		try{
			String query="select searchdate, sum(fws) as fws, sum(vinopedia) as vinopedia from ("+
			//fws
			"SELECT date(date) as searchdate, count(*) as fws, 0 as vinopedia from logging where date>=DATE_SUB(curdate(),INTERVAL "+days+" DAY) and bot=0 and referrer!='' and referrer not like '%free%wine%searcher%' and referrer not like '%vinopedia%' and type!='abuse' and page like '%freewinesearcher%' and bot=0 group by to_days(date) union " +
			//vinopedia
			"SELECT date(date) as searchdate, 0 as fws, count(*) as vinopedia from logging where date>=DATE_SUB(curdate(),INTERVAL "+days+" DAY) and bot=0 and referrer!='' and referrer not like '%free%wine%searcher%' and referrer not like '%vinopedia%' and type!='abuse' and page like '%vinopedia%'  and bot=0 group by to_days(date)) as thetable group by searchdate order by searchdate desc;";
			rs=Dbutil.selectQuery(query,con);
			while (rs.next()){
				fws.add(new Day(rs.getDate("searchdate")), rs.getInt("fws"));
				vinopedia.add(new Day(rs.getDate("searchdate")), rs.getInt("vinopedia"));
			}
		} catch (Exception E){
			Dbutil.logger.error("Problem while looking up visitor overview",E);
		}finally {
			Dbutil.closeRs(rs);
			Dbutil.closeConnection(con);
		}

		xyds = new TimeSeriesCollection();
		xyds.addSeries(vinopedia);
		xyds.addSeries(fws);
		chart = ChartFactory.createTimeSeriesChart(title, "Date", "Count", xyds, true, true, false);


	} /* (sessie != null) */ 


	/**
	 * Create pie chart method.
	 * 
	 * @param data which can consist of elements such as:
	 * <ul>
	 * <li> Type - the type of chart: either 2D, 3D, or Ring.</li>
	 * <li> Title - the title of the chart.</li>
	 * <li> XMLFile - the XML file name if applicable.</li>
	 * <li> data - the data to plot. Format is name=value&name=value, eg: Fish=70&Chips=66.</li>
	 * </ul>
	 * @param req which is the Http Servlet Request	
	 *
	 * @return JFreeChart
	 * @throws ServletException
	 * 
	 */
	public void createPie(String title,Map<String, Double> data) 
	throws ServletException {
		PieChart pie = new PieChart();

		// set the title for the Pie Chart
		pie.title = title;
		pie.type=PieChart.TYPE3D;

		// get the data from the map
		for (String k:data.keySet()) {
			pie.dataset.setValue(k,data.get(k));
		}
		
		chart=pie.getChart();
		PiePlot plot = (PiePlot)chart.getPlot();
	      plot.setNoDataMessage("No data available");
	      plot.setIgnoreZeroValues(true);
	      plot.setCircular(false);
	      plot.setOutlinePaint(null);
	      plot.setLabelGap(0.02);
	      plot.setInteriorGap(0.02);
	      plot.setMaximumLabelWidth(0.20);
	      plot.setLabelOutlinePaint(null);
	      plot.setLabelShadowPaint(null);
	      Color[] colors = {ChartColor.VERY_LIGHT_GREEN,ChartColor.VERY_LIGHT_BLUE,ChartColor.VERY_LIGHT_CYAN,ChartColor.VERY_LIGHT_YELLOW,ChartColor.VERY_LIGHT_RED,ChartColor.LIGHT_GRAY,ChartColor.VERY_LIGHT_MAGENTA,ChartColor.LIGHT_YELLOW};
	        PieRenderer renderer = new PieRenderer(colors);
	        renderer.setColor(plot, pie.dataset);

		
		 
		
	}
	
	public void create3dBar(String title,Map<String, Integer> data){
		DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 
		for (String key:data.keySet()){
			dataset.setValue(data.get(key), "",key); 
			
		}
		 CategoryItemRenderer renderer = new CustomRenderer(
	            new Paint[] {Color.blue, Color.green,
	                Color.yellow, Color.orange, Color.cyan,
	                Color.magenta, Color.blue}
	        );
		chart = ChartFactory.createBarChart3D( 
				   title,  // Chart name 
				   "Source",                     // X axis label 
				   "Clicks",                    // Y axis value 
				   dataset,                        // data set 
				   PlotOrientation.VERTICAL, 
				   false, true, false);
		
		chart.getCategoryPlot().setRenderer(renderer);
		chart.setAntiAlias(true);
		chart.setTextAntiAlias(false);
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB); 
		rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); 
		chart.setRenderingHints(rh); 
		Font titlefont=new java.awt.Font("Georgia", Font.BOLD, 15);
		chart.getTitle().setFont(titlefont);
		chart.setBackgroundPaint( (Color.white));
		
		CategoryPlot plot = (CategoryPlot)chart.getPlot();
		plot.setForegroundAlpha(0.50f);
		chart.setRenderingHints(rh); 
		
		
	}
	class CustomRenderer extends BarRenderer3D {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/** The colors. */
        private Paint[] colors;

        /**
         * Creates a new renderer.
         *
         * @param colors  the colors.
         */
        public CustomRenderer(final Paint[] colors) {
            this.colors = colors;
        }

        /**
         * Returns the paint for an item.  Overrides the default behaviour inherited from
         * AbstractSeriesRenderer.
         *
         * @param row  the series.
         * @param column  the category.
         *
         * @return The item color.
         */
        public Paint getItemPaint(final int row, final int column) {
            return this.colors[column % this.colors.length];
        }
    }
	
	public void createPieSponsors(String title,Map<String, Integer> data) 
			throws ServletException {
				PieChart pie = new PieChart();
				Font titlefont=new java.awt.Font("Georgia", Font.BOLD, 15);
				Font descfont=new java.awt.Font("Georgia", Font.PLAIN, 13);
				// set the title for the Pie Chart
				pie.title = title;
				pie.type=PieChart.TYPE3D;

				// get the data from the map
				for (String k:data.keySet()) {
					pie.dataset.setValue(k,data.get(k));
				}
				
				chart=pie.getChart();
				chart.getTitle().setFont(titlefont);
				chart.setTextAntiAlias(true);
				chart.removeLegend();
				PiePlot plot = (PiePlot)chart.getPlot();
				plot.setForegroundAlpha(0.50f);
				plot.setLabelFont(descfont);
				plot.setStartAngle(360);
		        
			      plot.setNoDataMessage("No data available");
			      plot.setIgnoreZeroValues(true);
			      plot.setCircular(false);
			      plot.setOutlinePaint(null);
			      plot.setLabelGap(0.02);
			      plot.setInteriorGap(0.02);
			      plot.setMaximumLabelWidth(0.20);
			      plot.setLabelOutlinePaint(null);
			      plot.setLabelShadowPaint(null);
			      Color[] colors = {ChartColor.LIGHT_GREEN,ChartColor.YELLOW,ChartColor.VERY_LIGHT_CYAN,ChartColor.VERY_LIGHT_YELLOW,ChartColor.VERY_LIGHT_RED,ChartColor.LIGHT_GRAY,ChartColor.VERY_LIGHT_MAGENTA,ChartColor.LIGHT_YELLOW};
			        PieRenderer renderer = new PieRenderer(colors);
			        renderer.setColor(plot, pie.dataset);

				
				 
				
			}
			
			
	
	public static class PieRenderer
    {
        private Color[] color;
       
        public PieRenderer(Color[] color)
        {
            this.color = color;
        }       
       
        public void setColor(PiePlot plot, DefaultPieDataset dataset)
        {
            List <Comparable> keys = dataset.getKeys();
            int aInt;
           
            for (int i = 0; i < keys.size(); i++)
            {
                aInt = i % this.color.length;
                plot.setSectionPaint(keys.get(i), this.color[aInt]);
            }
        }
    }

	
	/**
	 * Create category chart method.
	 * 
	 * @param map which can consist of elements such as:
	 * <ul>
	 * <li> Type - the type of chart: either Bar, Bar3D, Area, Line, or Line3D.</li>
	 * <li> Title - the title of the chart.</li>
	 * <li> XLabel - the domain title.</li>
	 * <li> YLabel - the range title.<li>
	 * <li> Orientation - the orientation of the chart, either H or V.<li>
	 * <li> Range - the upper and lower value of the range. Format is Range=lower,upper, e.g. Range=68,72.</li>
	 * <li> XMLFile - the XML file name if applicable.</li>
	 * <li> data - the data to plot. Format is name=value&name=value, eg: Fish=70&Chips=66.</li>
	 * </ul>
	 * @param req which is the Http Servlet Request	
	 *
	 * @return JFreeChart
	 * @throws ServletException
	 * 
	 */
	private JFreeChart createCategory(Map<String, String[]> map, HttpServletRequest req) 
	throws ServletException {
		CategoryChart bar = new CategoryChart();

		// set the title for the Category Chart
		if (map.containsKey("Title")) {
			bar.title = req.getParameter("Title");
			map.remove("Title");
		}

		// what type? (default is Bar)
		if (map.containsKey("Type")) {
			String type = req.getParameter("Type");
			if (type.equalsIgnoreCase("Bar")) {
				bar.type = CategoryChart.TYPEBAR;
			} else if (type.equalsIgnoreCase("Bar3D")) {
				bar.type = CategoryChart.TYPEBAR3D;
			} else if (type.equalsIgnoreCase("Area")) {
				bar.type = CategoryChart.TYPEAREA;
			} else if (type.equalsIgnoreCase("Line")) {
				bar.type = CategoryChart.TYPELINE;
			} else if (type.equalsIgnoreCase("Line3D")) {
				bar.type = CategoryChart.TYPELINE3D;
			}
			map.remove("Type");
		}

		// set the range
		if (map.containsKey("XLabel")) {
			bar.range = req.getParameter("XLabel");
			map.remove("XLabel");
		}
		// set the domain
		if (map.containsKey("YLabel")) {
			bar.domain = req.getParameter("YLabel");
			map.remove("YLabel");
		}

		// set the orientation
		if (map.containsKey("Orientation")) {
			String orientation = req.getParameter("Orientation");
			if (orientation.equalsIgnoreCase("V")) {
				bar.orientation = PlotOrientation.VERTICAL;
			} else if (orientation.equalsIgnoreCase("H")) {
				bar.orientation = PlotOrientation.HORIZONTAL;
			}
			map.remove("Orientation");
			//out.println("<br>Orientation : " + orientation);
		}

		// set the lower and upper range
		if (map.containsKey("Range")) {
			String rangeArray[] = (String[]) req.getParameter("Range").split(",");
			bar.lower = new Double(rangeArray[0]);
			bar.upper = new Double(rangeArray[1]);
			map.remove("Range");
			//out.println("<br>Range : " + range);
		}

		// get the chart data from an XML file or...
		if (map.containsKey("XMLFile")) {
			URL url = getClass().getResource(req.getParameter("XMLFile"));
			try
			{
				java.io.InputStream inputstream = url.openStream();
				bar.dataset = (DefaultCategoryDataset) DatasetReader.readCategoryDatasetFromXML(inputstream);
			}
			catch(IOException ioexception)
			{
				System.out.println(ioexception.getMessage());
			}
			map.remove("XMLFile");
		} else {
			// get the data
			for (Iterator<Entry<String, String[]>> it=map.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry entry = it.next();
				String key = (String) entry.getKey();
				//out.println("<br>Key : " + key);
				String value[] = (String[]) entry.getValue();
				for (int i=0;i != value.length;i++) {
					String data[] = value[i].split(",");
					bar.dataset.setValue(new Double(data[1]),key,data[0]);
					//out.println("  Value : " + value[i] + "Data : " + data[0]);
				}
			}
		}

		JFreeChart chart = bar.getChart();
		return chart;
	}

	/**
	 * Create XY chart method.
	 * 
	 * @param map which can consist of elements such as:
	 * <ul>
	 * <li> Type - the type of chart: either Bar, Step, Area, Line, Scatter, or Time.</li>
	 * <li> Title - the title of the chart.</li>
	 * <li> XLabel - the domain title.</li>
	 * <li> YLabel - the range title.<li>
	 * <li> Orientation - the orientation of the chart, either H or V.<li>
	 * <li> Range - the upper and lower value of the range. Format is Range=lower,upper, e.g. Range=68,72.</li>
	 * <li> data - the data to plot. Format is name=value&name=value, eg: Fish=70&Chips=66.</li>
	 * </ul>
	 * @param req which is the Http Servlet Request	
	 * 
	 * @return JFreeChart
	 * @throws ServletException
	 *
	 */
	private JFreeChart createXY(Map<String, String[]> map, HttpServletRequest req)
	throws ServletException {
		XYChart xy = new XYChart();
		// set the title for the Category Chart
		if (map.containsKey("Title")) {
			xy.title = req.getParameter("Title");
			map.remove("Title");
		}

		// what type? (default is Bar)
		if (map.containsKey("Type")) {
			String type = req.getParameter("Type");
			if (type.equalsIgnoreCase("Bar")) {
				xy.type = XYChart.TYPEBAR; 
			} else if (type.equalsIgnoreCase("Step")) {
				xy.type = XYChart.TYPESTEPAREA;
			} else if (type.equalsIgnoreCase("Area")) {
				xy.type = XYChart.TYPEAREA;
			} else if (type.equalsIgnoreCase("Line")) {
				xy.type = XYChart.TYPELINE;
			} else if (type.equalsIgnoreCase("Scatter")) {
				xy.type = XYChart.TYPESCATTER;
				//} else if (xyType.equalsIgnoreCase("Polar")) {
				//xy.type = XYChart.TYPEPOLAR;
			} else if (type.equalsIgnoreCase("Time")) {
				xy.type = XYChart.TYPETIME;
			}
			map.remove("Type");
		}

		// set the range
		if (map.containsKey("XLabel")) {
			xy.xLabel = req.getParameter("XLabel");
			map.remove("XLabel");
		}
		// set the domain
		if (map.containsKey("YLabel")) {
			xy.yLabel = req.getParameter("YLabel");
			map.remove("YLabel");
		}

		// set the orientation
		if (map.containsKey("Orientation")) {
			String orientation = req.getParameter("Orientation");
			if (orientation.equalsIgnoreCase("V")) {
				xy.orientation = PlotOrientation.VERTICAL;
			} else if (orientation.equalsIgnoreCase("H")) {
				xy.orientation = PlotOrientation.HORIZONTAL;
			}
			map.remove("Orientation");
			//out.println("<br>Orientation : " + orientation);
		}

		// set the lower and upper range
		if (map.containsKey("Range")) {
			String rangeArray[] = (String[]) req.getParameter("Range").split(",");
			xy.lower = new Double(rangeArray[0]);
			xy.upper = new Double(rangeArray[1]);
			map.remove("Range");
			//out.println("<br>Range : " + range);
		}

		// get the data
		for (Iterator<Entry<String, String[]>> it=map.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = it.next();
			String key = (String) entry.getKey();
			//out.println("<br>Key : " + key);

			String value[] = (String[]) entry.getValue();
			if (xy.type == XYChart.TYPETIME) {
				TimeSeries timeSeries = new TimeSeries(key,Second.class);
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				Date date = null;
				for (int i=0;i != value.length;i++) {
					String data[] = value[i].split(",");
					try {
						date = sdf.parse(data[0]);
					} catch (ParseException pe) {
						throw new ServletException(pe.getMessage()); 
					}
					timeSeries.add(new Second(date),new Double(data[1]));
					//out.println("  Value : " + value[i]);
				}
				xy.timeset.addSeries(timeSeries);
			} else {
				XYSeries xySeries = new XYSeries(key);
				for (int i=0;i != value.length;i++) {
					String data[] = value[i].split(",");
					xySeries.add(new Double(data[0]),new Double(data[1]));
					//out.println("  Value : " + value[i]);
				}
				xy.dataset.addSeries(xySeries);
			}
		}

		JFreeChart chart = xy.getChart();
		return chart;
	}


}