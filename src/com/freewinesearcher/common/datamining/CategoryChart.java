package com.freewinesearcher.common.datamining;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * <p>
 * The CategoryChart object 
 * </p>
 * @author <a href="mailto:luke.trevorrow@gmail.com">Luke Trevorrow</a>
 * @version 0.21
 * 
 */
public class CategoryChart {
	public final static int TYPEBAR = 0;
	public final static int TYPEBAR3D = 1;
	public final static int TYPEAREA = 2;
	public final static int TYPELINE = 3;
	public final static int TYPELINE3D = 4;
	
	public String title = null;
	public String domain = "Category";
	public String range = "Value";
	public Double upper = null;
	public Double lower = null;
	public int type = TYPEBAR;
	public PlotOrientation orientation = PlotOrientation.VERTICAL;
	public DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	
    /**
     * Creates a new chart instance.
     *
     */
    public CategoryChart() {
    }
    
    /**
     * Create category chart method.
     *
     * @param type         the type of chart: either Bar, Bar3D, Area, Line, or Line3D.
     * @param title        the title of the chart.
     * @param domain       the domain title.
     * @param range        the range title.
     * @param dataset      the data to plot. Format is <name>=<val>&<name>=<val>, eg: Fish=70&Chips=66
     * @param orientation  the orientation of the chart, either H or V.
     * @param lower        the lower value of the range.
     * @param upper        the upper value of the range.
     * 
     * @return The chart.
     */
    private static JFreeChart createChart(int type, String title, String domain, String range,
    		CategoryDataset dataset, PlotOrientation orientation, Double lower, Double upper) {
        JFreeChart chart = null;
        
        switch (type) {
        case 0:
        	// create the chart...
            chart = ChartFactory.createBarChart(
                title,						// chart title
                domain,						// domain axis label
                range,						// range axis label
                dataset,					// data
                orientation,				// orientation
                true,						// include legend
                true,						// tooltips?
                false						// URLs?
            );
            break;
        case 1:
        	// create the chart...
            chart = ChartFactory.createBarChart3D(
                title,						// chart title
                domain,						// domain axis label
                range,						// range axis label
                dataset,					// data
                orientation,				// orientation
                true,						// include legend
                true,						// tooltips?
                false						// URLs?
            );
            break;
        case 2:
        	// create the chart...
            chart = ChartFactory.createAreaChart(
                title,						// chart title
                domain,						// domain axis label
                range,						// range axis label
                dataset,					// data
                orientation,				// orientation
                true,						// include legend
                true,						// tooltips?
                false						// URLs?
            );
            break;
        case 3:
        	// create the chart...
            chart = ChartFactory.createLineChart(
                title,						// chart title
                domain,						// domain axis label
                range,						// range axis label
                dataset,					// data
                orientation,				// orientation
                true,						// include legend
                true,						// tooltips?
                false						// URLs?
            );
            break;
        case 4:
        	// create the chart...
            chart = ChartFactory.createLineChart3D(
                title,						// chart title
                domain,						// domain axis label
                range,						// range axis label
                dataset,					// data
                orientation,				// orientation
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
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.white);

        // set the axis
        if (lower != null && upper != null) {
        	ValueAxis axis = plot.getRangeAxis();
        	axis.setRange(lower.doubleValue(), upper.doubleValue());
        }
        
        /* set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // disable bar outlines...
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        */
        
        return chart;
        
    }
    
    /**
     * Create category chart method.
     * 
     * @return JFreeChart
     *
     */
    public JFreeChart getChart() {
    	JFreeChart chart = createChart(type, title, domain, range, dataset, orientation, lower, upper);
    	return chart;
    }

}
