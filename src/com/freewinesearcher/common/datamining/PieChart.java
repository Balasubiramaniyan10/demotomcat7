package com.freewinesearcher.common.datamining;

import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

/**
 * <p>
 * The PieChart object 
 * </p>
 * @author <a href="mailto:luke.trevorrow@freenetname.co.uk">Luke Trevorrow</a>
 * @version 0.11
 * 
 */
public class PieChart {
	public final static int TYPE2D = 0;
	public final static int TYPE3D = 1;
	public final static int TYPERING = 2;
	
	public String title = null;
	public int type = TYPE2D;
	public DefaultPieDataset dataset = new DefaultPieDataset();
	
    /**
     * Default constructor.
     *
     */
    public PieChart() {
    }
    
    /**
     * Create pie chart method.
     *
     * @param type     the type of chart: either 2D, 3D, or Ring.
     * @param title    the title of the chart.
     * @param dataset  the data to plot. Format is <name>=<val>&<name>=<val>, eg: Fish=70&Chips=66
     *
     * @return JFreeChart
     */
    private static JFreeChart createChart(int type, String title, PieDataset dataset) {
    	JFreeChart chart = null;
    	
        switch (type) {
        case 0:
        	chart = ChartFactory.createPieChart(
                    title,  			// chart title
                    dataset,            // data
                    true,               // include legend
                    true,
                    false);
        	break;
        case 1:
        	chart = ChartFactory.createPieChart3D(
                    title,  			// chart title
                    dataset,            // data
                    true,               // include legend
                    true,
                    false);
        	break;
        case 2:
        	chart = ChartFactory.createRingChart(
                    title,  			// chart title
                    dataset,            // data
                    true,               // include legend
                    true,
                    false);
        	break;
        }
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionOutlinesVisible(true);
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 11));
        plot.setNoDataMessage("No data available");
        plot.setCircular(false);
        plot.setLabelGap(0.02);
        
        return chart;
        
    }
    
    /**
     * Creates the pie chart
     * 
     * @return JFreeChart
     */
    public JFreeChart getChart() {
    	JFreeChart chart = createChart(type, title, dataset);
    	return chart;
    }

}