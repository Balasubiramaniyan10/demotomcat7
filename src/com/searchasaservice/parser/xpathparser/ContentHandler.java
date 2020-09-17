package com.searchasaservice.parser.xpathparser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;


/**
 * @author Jasper Hammink
 * The ContentHandler interface handles text content that has been retrieved from a
 * document. 
 */
public interface ContentHandler extends Serializable{
	
	/**
	 * setPositiveConfig sets a parameter that can be used for configuring the getContent method, 
	 * so that it known which content to grab (for instance, a regular expression).
	 * @param regex: the input String to set
	 * @return Empty String ("") if setting the parameter went fine, or a String containing 
	 * a message that can be shown to users if something went wrong. The message should contain
	 * text that allows a user to understand what went wrong
	 */
	public String setPositiveConfig(String input);
	
	/**
	 * setNegativeConfig sets a parameter that can be used for configuring the getContent method, 
	 * so that it known which content to reject (for instance, a regular expression).
	 * @param regex: the input String to set
	 * @return Empty String ("") if setting the parameter went fine, or a String containing 
	 * a message that can be shown to users if something went wrong. The message should contain
	 * text that allows a user to understand what went wrong
	 */

	public String setNegativeConfig(String input);
	/**
	 * getContent must implement the logic to interpret the content of a node and turn it into 
	 * an Object that represents the value for a Field. it is used during the parsing 
	 * of a document to retrieve values. 
	 * @param input: The text to analyze
	 * @return an Object that represents the value that has been retrieved from the 
	 * input. A value of null means no value could be retrieved 
	 */
	public Object getContent(Node n);
	
	
	/**
	 * recognizeContent must implement a filter function that is used during the analysis
	 * phase to determine whether a text contains the right information. It reads values 
	 * from the HashMap and tries to recognize them as valid values. The HashMap that is returned
	 * contains only those entries of the original map whose values have been recognized. 
	 * @param Set inputset: A set of String values to test. 
	 * @return A Set<String> of all values from the inputset whose values have been recognized
	 */
	public Set<Node> recognizeContent(Set<Node> inputset);
	
	
	/**
	 * Initializes the class and checks if all settings are correct
	 * @return A message describing the problem or an empty String ("") if no problems were found
	 */
	public String init();
}
