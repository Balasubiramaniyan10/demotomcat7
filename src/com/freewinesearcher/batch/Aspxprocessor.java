package com.freewinesearcher.batch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URL.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Webpage;
import com.freewinesearcher.online.Webroutines;

public class Aspxprocessor {
	ArrayList<ArrayList<String>> postvalues=new ArrayList<ArrayList<String>>();
	// 0=type, 1=name, 2=value
	ArrayList<ArrayList<String>> postvaluesnoimage=new ArrayList<ArrayList<String>>();
	// 0=type, 1=name, 2=value. But all type=image filtered out.
	ArrayList<ArrayList<String>> actions=new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<String>> urlsfound=new ArrayList<ArrayList<String>>();
	String site="";
	String parenturl="";
	String Filter="";
	String action="";
	//private String postdata="";


	public void processPage(){
		determineSite();
		processData();
		processActions();
	}

	private void determineSite(){
		if (parenturl.contains("erobertparker")) site="RP";
		if (parenturl.contains("localhost")) site="RP";
	}

	private void processActions(){
		ArrayList<ArrayList<String>> postcopy;
		if (site.equals("RP")){
			for (int i=0;i<postvalues.size();i++){
				if (postvalues.get(i).get(0).equalsIgnoreCase("image")&&postvalues.get(i).get(1).equalsIgnoreCase("ctl00$ContentMaster$ImageButton3")){
					postcopy=newCopy(postvaluesnoimage);
					String postdata="";
					for (int j=0;j<postcopy.size();j++){
						if (postcopy.get(j).get(1).startsWith("__")) postdata+="&"+Webroutines.URLEncode(postcopy.get(j).get(1))+"="+Webroutines.URLEncode(postcopy.get(j).get(2));
					}
					if (postdata.length()>1) postdata=postdata.substring(1);
					postdata+="&MainFormSearchFlag=0";
					postdata+="&ctl00%24siteSearch=WineTastings";
					postdata+="&ctl00%24keyWordSearch=Enter+Full%2FPartial+Keyword%28s%29";
					postdata+="&ctl00%24ContentMaster%24ImageButton3.x="+(1+Math.round(Math.random()*15));
					postdata+="&ctl00%24ContentMaster%24ImageButton3.y="+(1+Math.round(Math.random()*10));
					postdata+="&JScheck=true";
					ArrayList<String> newaction=new ArrayList<String>();
					newaction.add(action);
					newaction.add(postdata);
					urlsfound.add(newaction);
				}
			}
			for (int i=0;i<actions.size();i++){
				if (actions.get(i).get(0).equalsIgnoreCase("doPostback")){
					postcopy=newCopy(postvaluesnoimage);
					for (int j=0;j<postcopy.size();j++){
						if (postcopy.get(j).get(1).equals("__EVENTTARGET")){
							postcopy.get(j).remove(2);
							postcopy.get(j).add(actions.get(i).get(1));
						}
						if (postcopy.get(j).get(1).equals("__EVENTARGUMENT")){
							postcopy.get(j).remove(2);
							postcopy.get(j).add(actions.get(i).get(2));
						}
					}
					String postdata="";
					for (int j=0;j<postcopy.size();j++){
						if (postcopy.get(j).get(1).startsWith("__")) postdata+="&"+Webroutines.URLEncode(postcopy.get(j).get(1))+"="+Webroutines.URLEncode(postcopy.get(j).get(2));
					}
					if (postdata.length()>1) postdata=postdata.substring(1);
					postdata+="&MainFormSearchFlag=0";
					postdata+="&ctl00%24siteSearch=WineTastings";
					postdata+="&ctl00%24keyWordSearch=Enter+Full%2FPartial+Keyword%28s%29";
					postdata+="&x="+(1+Math.round(Math.random()*40));
					postdata+="&y="+(1+Math.round(Math.random()*10));
					postdata+="&JScheck=true";
					ArrayList<String> newaction=new ArrayList<String>();
					newaction.add(action);
					newaction.add(postdata);
					urlsfound.add(newaction);
					break;
				}
			}

		} else {
			for (int i=0;i<actions.size();i++){
				if (actions.get(i).get(0).equalsIgnoreCase("doPostback")){
					postcopy=newCopy(postvaluesnoimage);
					for (int j=0;j<postcopy.size();j++){
						if (postcopy.get(j).get(1).equals("__EVENTTARGET")){
							postcopy.get(j).remove(2);
							postcopy.get(j).add(actions.get(i).get(1));
						}
						if (postcopy.get(j).get(1).equals("__EVENTARGUMENT")){
							postcopy.get(j).remove(2);
							postcopy.get(j).add(actions.get(i).get(2));
						}
					}
					String postdata="";
					for (int j=0;j<postcopy.size();j++){
						postdata+="&"+Webroutines.URLEncode(postcopy.get(j).get(1))+"="+Webroutines.URLEncode(postcopy.get(j).get(2));
					}
					if (postdata.length()>1) postdata=postdata.substring(1);
					ArrayList<String> newaction=new ArrayList<String>();
					newaction.add(action);
					newaction.add(postdata);
					urlsfound.add(newaction);
				}
			}

		}
	}

	private static ArrayList<ArrayList<String>> newCopy(ArrayList<ArrayList<String>> oldal){
		ArrayList<ArrayList<String>> newal=new ArrayList<ArrayList<String>>();
		for (int i=0;i<oldal.size();i++){
			newal.add(new ArrayList<String>());
			for (int j=0;j<oldal.get(i).size();j++){
				newal.get(i).add(oldal.get(i).get(j));
			}
		}
		return newal;

	}


	private void processData(){
		int i;
		for (i=0;i<Filter.split(":").length;i=i+2){
			String Search = Filter.split(":")[i];
			String Replace = null;
			if (i==Filter.split(":").length-1){
				Replace="";
			} else {
				Replace=Filter.split(":")[i+1];
			}
			if (Replace.equals("colon")) Replace=":";
			if (Replace.equals("mandatory")) {
				for (int j=0;j<actions.size();){
					if (Webroutines.getRegexPatternValue("("+Search+")", actions.get(j).get(1)+actions.get(j).get(2)).equals("")) {
						actions.remove(j);
					} else {
						j++;
					}
				}
			} else {
				for (int j=0;j<postvalues.size();j++){
					if (!postvalues.get(j).get(2).equals(postvalues.get(j).get(2).replaceAll(Search,Replace))){
						String newvalue=postvalues.get(j).get(2).replaceAll(Search,Replace);
						postvalues.get(j).remove(2);
						postvalues.get(j).add(newvalue);
					}
				}
				for (int j=0;j<actions.size();j++){
					if (!actions.get(j).get(2).equals(actions.get(j).get(2).replaceAll(Search,Replace))){
						String newvalue=actions.get(j).get(2).replaceAll(Search,Replace);
						actions.get(j).remove(2);
						actions.get(j).add(newvalue);
					}
					if (!actions.get(j).get(1).equals(actions.get(j).get(1).replaceAll(Search,Replace))){
						String key=actions.get(j).get(1).replaceAll(Search,Replace);
						String value=actions.get(j).get(2);
						actions.get(j).remove(2);
						actions.get(j).remove(1);
						actions.get(j).add(key);
						actions.get(j).add(value);
					}
				}
			}
		}
		for (i=0;i<postvalues.size();i++){
			if (!postvalues.get(i).get(0).equalsIgnoreCase("image")){
				postvaluesnoimage.add(postvalues.get(i));
			}
		}
	}


	public ArrayList<ArrayList<String>> getUrls(){
		processPage();
		return urlsfound;
	}



}
