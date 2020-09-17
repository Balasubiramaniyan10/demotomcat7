package com.freewinesearcher.common;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookJsonRestClient;
import com.google.code.facebookapi.FacebookXmlRestClient;

public class FacebookPublisher {
	
	public FacebookPublisher() throws FacebookException, JSONException{
		String session="b5d460f549b75532686f2827-100000248970256";
		String secret="c601027a13b8758d25106acb0303e4fa";
		//FacebookXmlRestClient client= new FacebookXmlRestClient(Configuration.Facebookapi,Configuration.Facebooksecret);
		FacebookJsonRestClient client = new FacebookJsonRestClient(Configuration.Facebookapi, Configuration.Facebooksecret, session);
		client.users_setStatus("Just testing here...", false);
	    


	}
	
	public static void main(String[] args){
		try {
			FacebookPublisher fbp=new FacebookPublisher();
		} catch (FacebookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
