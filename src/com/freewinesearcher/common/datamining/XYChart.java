package com.freewinesearcher.common.datamining;


import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * <p>
 * The XYChart object 
 * </p>
 * @author <a href="mailto:luke.trevorrow@freenetname.co.uk">Luke Trevorrow</a>
 * @version 0.21
 * 
 */
public class XYChart {

	public final static int TYPEBAR = 0;
	public final static int TYPESTEPAREA = 1;
	public final static int TYPEAREA = 2;
	public final static int TYPELINE = 3;
	public final static int TYPESCATTER = 4;
	public final static int TYPEPOLAR = 5;
	public final static int TYPETIME = 6;
	
	//public boolean isTimeSeries = false;
	
	public XYSeriesCollection dataset = new XYSeriesCollection();
	public TimeSeriesCollection timeset = new TimeSeriesCollection();

	// some default values
	public String title = null;
	public String xLabel = "Category";
	public String yLabel = "Value";
	public Double upper = null;
	public Double lower = null;
	public int type = TYPELINE;
	public PlotOrientation orientation = PlotOrientation.VERTICAL;

	
    /**
     * Creates a new chart instance.
     *
     */
    public XYChart() {
    }
    
    /**
     * Create XY chart method.
     *
     * @param type         the type of chart: either Bar, Step, Area, Line, Scatter, or Time.
     * @param title        the title of the chart.
     * @param xLabel       the domain title.
     * @param yLabel       the range title.
     * @param dataset      the data to plot. Format is <name>=<val>&<name>=<val>, eg: Fish=70&Chips=66
     * @param orientation  the orientation of the chart, either H or V.
     * @param lower        the lower value of the range.
     * @param upper        the upper value of the range.
     * 
     * @return JFreeChart
     *
     */
    private static JFreeChart createChart(int type, String title, String xLabel, String yLabel,
    		XYDataset dataset, PlotOrientation orientation, Double lower, Double upper) {
        JFreeChart chart = null;
        
        switch (type) {
        case TYPEBAR: // ? not sure this works?!
        	// create the chart...
            chart = ChartFactory.createXYBarChart(
                title,						// chart title
                xLabel,						// X axis label
                false,						// make X axis date
                yLabel,						// Y axis label
                (IntervalXYDataset) dataset,// data
                orientation,				// orientation
                true,						// include legend
                true,						// tooltips?
                false						// URLs?
            );
            break;
        case TYPESTEPAREA:
        	// create the chart...
            chart = ChartFactory.createXYStepAreaChart(
                title,						// chart title
                xLabel,						// domain axis label
                yLabel,						// range axis label
                dataset,					// data
                orientation,				// orientation
                true,						// include legend
                true,						// tooltips?
                false						// URLs?
            );
            break;
        case TYPEAREA:
        	// create the chart...
            chart = ChartFactory.createXYAreaChart(
                title,						// chart title
                xLabel,						// domain axis label
                yLabel,						// range axis label
                dataset,					// data
                orientation,				// orientation
                true,						// include legend
                true,						// tooltips?
                false						// URLs?
            );
            break;
        case TYPELINE:
        	// create the chart...
            chart = ChartFactory.createXYLineChart(
                title,						// chart title
                xLabel,						// domain axis label
                yLabel,						// range axis label
                dataset,					// data
                orientation,				// orientation
                true,						// include legend
                true,						// tooltips?
                false						// URLs?
            );
            break;
        case TYPESCATTER:
        	// create the chart...
            chart = ChartFactory.createScatterPlot(
                title,						// chart title
                xLabel,						// domain axis label
                yLabel,						// range axis label
                dataset,					// data
                orientation,				// orientation
                true,						// include legend
                true,						// tooltips?
                false						// URLs?
            );
            break;
        //case TYPEPOLAR: //? I don't think this works?
            // <a href="ChartServlet?Series=XY&amp;Type=Polar&amp;Title=Distribution Sphere&amp;Width=800&amp;Height=600&amp;RDC 1=25,0&amp;RDC 1=15,90&amp;RDC 1=-10,180&amp;RDC 1=5,270">Polar Chart</a>, 
        	// create the chart...
            //chart = ChartFactory.createPolarChart(
        	//chart = ChartFactory.createHistogram(
                //title,						// chart title
                //(IntervalXYDataset) dataset,// data
                //true,						// include legend
                //true,						// tooltips?
                //false						// URLs?
            //);
            //break;
        case TYPETIME:
        	// create the chart...
            chart = ChartFactory.createTimeSeriesChart(
                title,						// chart title
                xLabel,						// domain axis label
                yLabel,						// range axis label
                dataset,					// data
                true,						// include legend
                true,						// tooltips?
                false						// URLs?
            );
            break;
        }
        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.white);
        
        // set the axis
        if (lower != null && upper != null) {
        	ValueAxis axis = plot.getRangeAxis();
        	axis.setRange(lower.doubleValue(), upper.doubleValue());
        }
        
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
        }
        
        //DateAxis axis = (DateAxis) plot.getDomainAxis();
        //axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));

        return chart;
        
    }
    
    /**
     * Create XY chart method.
     * 
     * @return JFreeChart
     *
     */
    public JFreeChart getChart() {
    	JFreeChart chart = null;
    	if (type == TYPETIME) {
    		chart = createChart(type, title, xLabel, yLabel, timeset, orientation, lower, upper);
    	} else {
    		chart = createChart(type, title, xLabel, yLabel, dataset, orientation, lower, upper);
    	}
    	return chart;
    }


}
