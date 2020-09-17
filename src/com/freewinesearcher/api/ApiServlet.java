package com.freewinesearcher.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.freewinesearcher.api.Api.Actions;
import com.freewinesearcher.batch.Spider;
import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.Webactionlogger;
import com.freewinesearcher.online.Webroutines;

public class ApiServlet extends HttpServlet{

	private static final long serialVersionUID = 5438602324207175244L;
	protected  Map<String, String> params;
	private Api.Actions action;
	private String clientid;
	private String clientdescription;
	private HttpServletRequest request;
	private HttpServletResponse response;



	public void doGet(HttpServletRequest request,HttpServletResponse response){

		setParams(request.getParameterMap());
		try{action=Api.Actions.valueOf(params.get("action").toUpperCase());}catch (Exception e){}
		if (!isValidCall(request)){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

		} else {
			Webactionlogger logger=new Webactionlogger("API", request.getAttribute("originalURL")+"?client="+clientdescription+"&action="+action, request.getRemoteAddr(), "", "", "", 0, (float) 0.0, (float) 0.0,
					"", false, "", "", "", "", (double) 0.0, 0,null);
			Api api=null;
			if (action!=null){
				switch (action){
				case PRICE:
					api=new PriceInfo(request,response, action, getParams(),logger);
					break;
				case SEARCHSTATS:
					api=new SearchStats(request,response, action, getParams(),logger);
					break;
				}
				if (api!=null){
					api.process();
					api.doOutput(api.getResult(),response);
					logger.logaction();
				}
			}
		}

	}


	public String getSig(){
		StringBuilder buff = new StringBuilder(); 
		clientid=Webroutines.filterUserInput(params.get("clientid"));
		if (clientid==null) return "";
		String secret=Dbutil.readValueFromDB("select * from api where id='"+Spider.SQLEscape(clientid)+"' and disabledon is null", "secret");
		clientdescription=Dbutil.readValueFromDB("select * from api where id='"+Spider.SQLEscape(clientid)+"' and disabledon is null", "description");
		buff.append(params.get("clientid")).append("\n");
		buff.append("GET").append("\n");
		buff.append(secret).append("\n");
		buff.append(params.get("timestamp")).append("\n");
		String src = buff.toString().toLowerCase();
		String sig = DigestUtils.md5Hex(src);
		//Dbutil.logger.info("calculated sig \n"+sig);
		//Dbutil.logger.info("Calculated over \n"+src);


		return sig;
	}


	private boolean isValidCall(HttpServletRequest request) {
		try{

			if (params.get("sig")==null||!params.get("sig").equals(getSig())) {
				//Dbutil.logger.info("Received   sig: "+params.get("sig"));
				//Dbutil.logger.info("Claculated sig: "+getSig());

				return false;
			}

			// check time
			if (Math.abs((int)(Long.valueOf(params.get("timestamp"))-System.currentTimeMillis()))>60*1000) return false;
			//((String[])params.get("clientid"))[0]
			if (action==null) return false;
			return true;
		}catch (Exception e){
			Dbutil.logger.error("Error in API",e);
			return false;
		}
	}

	public void setParams(Map<String,String[]> parameters) {
		this.params=new HashMap<String, String>();
		for (String s : parameters.keySet()){
			this.params.put(s, ((String[])parameters.get(s))[0]);
		}

	}

	public Map<String,String> getParams() {
		return params;
	}





}
