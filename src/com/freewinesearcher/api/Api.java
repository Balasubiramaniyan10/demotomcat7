package com.freewinesearcher.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.common.Knownwine;
import com.freewinesearcher.common.Wineset;
import com.freewinesearcher.online.Searchdata;
import com.freewinesearcher.online.Webactionlogger;
import com.freewinesearcher.online.Webroutines;

public abstract class Api extends HttpServlet{

	private static final long serialVersionUID = 5438602324207175244L;
	protected  Map<String, String> params;
	public static  enum Actions {PRICE,SEARCHSTATS}
	private Actions action;
	protected Result result;
	private HttpServletRequest request;
	private HttpServletResponse response;
	public Webactionlogger logger;

	public   Api(HttpServletRequest request,HttpServletResponse response,  Actions action, Map<String, String> params, Webactionlogger logger){
		this.request=request;
		this.response=response;
		this.params=params;
		this.logger=logger;
		this.action=action;
	}

	
	abstract void process();
	abstract Result getResult();

	
	public void doOutput(Result result,HttpServletResponse response){

		try {
			if (result!=null){
				response.setCharacterEncoding("UTF-8");
				PrintWriter out = response.getWriter();
				JSONObject output=new JSONObject();
				output.append("object", result.object);
				//result.content.append("object", result.object);
				if (result.content!=null) output.append("results", result.content);
				out.println(output);
			}
		}catch (Exception e){
			Dbutil.logger.error("Error in API",e);
		}

	}
	
	
	class Result{
		JSONObject object;
		JSONObject content;

		public Result(JSONObject object, JSONObject content) {
			super();
			this.object = object;
			if (content==null) content=new JSONObject();
			this.content = content;
		}
	}
	
	public static Knownwine matchWine(String name){
	Wineset w = new Wineset();
	w.s = new Searchdata();
	w.s.setName(name);
	Knownwine wine = new Knownwine(w.guessKnownWineId());
	if (wine.id!=0)	wine.getProperties();
	return wine;
	}
}
