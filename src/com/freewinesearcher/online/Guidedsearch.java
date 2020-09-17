package com.freewinesearcher.online;

import java.io.Serializable;

import com.freewinesearcher.common.Dbutil;
import com.freewinesearcher.online.WineAdvice.Winetypes;

public class Guidedsearch implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public transient WineAdvice advice;
	boolean byprice=true;
	boolean byregion=true;
	boolean bytype=true;
	boolean byvintage=true;
	public boolean bycountryofseller=true;
	boolean bygrape=true;
	boolean byrating=true;
	boolean bysubregion=true;
	
	
	
	
	public String getSearchHtml(){
		StringBuffer type=new StringBuffer();
		String vintage="";
		String region="";
		String subregion="";
		String countryofseller="";
		String grape=""; 
		String rating="";
		/* Not clear why this was inserted. It is wrong, as a new advice does not get initialized
		if (advice.getFacets().vintage.keySet().size()==0){
			if (advice.getClass().getCanonicalName().contains("Shop")){
				advice=new ShopAdvice();
			} else {
				advice=new WineAdvice();
			}
			advice.getFacets();
		}
		*/
		advice.getFacets();
		
		String other="<input type='hidden' id='symbol' value='"+Dbutil.readValueFromDB("Select * from currency where currency='"+advice.getCurrency()+"';", "symbol").replaceAll("\"", "&quot;")+"'/>";
		if (advice.rows>0) {
			other+="<input type='hidden' id='numpages' value='"+(1+(int)Math.floor((double)((advice.rows-1)/advice.resultsperpage)))+"'/>";
		}
		if (byrating){
			if (advice.getFacets().ratingmin<advice.getFacets().ratingmax){
				rating+="<div class='criterionh'>By rating:</div>";
				rating+="<img class='spinner' id='ratingspinner' alt='Loading...' src='/images/spinner.gif'/><img id='ratingclose' style='display:none;' alt='Clear rating' src='/images/transparent.gif' class='close sprite sprite-close'  onclick='javascript:clearRating();'/><div class='slider' id='rating'></div>"; 
				rating+="<div class='sliderlegend'><div class='slider-min'>"+advice.getFacets().ratingmin+"</div><div class='slider-max'>"+advice.getFacets().ratingmax+"</div></div>";
				rating+="<input type='hidden' id='ratingminscale' name='ratingminscale' value='"+advice.getFacets().ratingmin+"'/>";
				rating+="<input type='hidden' id='ratingmaxscale' name='ratingmaxscale' value='"+advice.getFacets().ratingmax+"'/>";
			}
		}
		if (bytype){
			type.append("<div class='criterionh'>By wine type:"+"</div>");
			int n;
			n=advice.getFacets().winetypecodes[1]+advice.getFacets().winetypecodes[2]+advice.getFacets().winetypecodes[3]+advice.getFacets().winetypecodes[4]+advice.getFacets().winetypecodes[5]+advice.getFacets().winetypecodes[6]+advice.getFacets().winetypecodes[7]+advice.getFacets().winetypecodes[8]+advice.getFacets().winetypecodes[9]+advice.getFacets().winetypecodes[10]+advice.getFacets().winetypecodes[11]+advice.getFacets().winetypecodes[12];
			//if(n>0) type.append(getTypeHTML(advice,n,"All types",Winetypes.ALLTYPES,false));
			n=advice.getFacets().winetypecodes[1];
			if(n>0) type.append(getTypeHTML(advice,n,"Red",Winetypes.RED,true));
			n=advice.getFacets().winetypecodes[3]+advice.getFacets().winetypecodes[7];
			if(n>0) type.append(getTypeHTML(advice,n,"White",Winetypes.WHITE,true));
			n=advice.getFacets().winetypecodes[4];
			if(n>0) type.append(getTypeHTML(advice,n,"Ros&eacute;",Winetypes.ROSE,true));
			n=advice.getFacets().winetypecodes[2]+advice.getFacets().winetypecodes[8]+advice.getFacets().winetypecodes[12];
			if(n>0) type.append(getTypeHTML(advice,n,"Sparkling",Winetypes.SPARKLING,true));
			n=advice.getFacets().winetypecodes[5]+advice.getFacets().winetypecodes[10]+advice.getFacets().winetypecodes[11];
			if(n>0) type.append(getTypeHTML(advice,n,"White sweet",Winetypes.DESSERT,true));
			n=advice.getFacets().winetypecodes[6]+advice.getFacets().winetypecodes[9];
			if(n>0) type.append(getTypeHTML(advice,n,"Red sweet/port",Winetypes.PORT,true));
			
		}
		if (byvintage&&advice.getFacets().vintage.keySet().size()>1){
			vintage+="<div class='criterionh'>By vintage:</div><div class='slider' id='vintageslider'>";
			vintage+="<select name='vintageminsl' id='vintageminsl'>";
			for (int v:advice.getFacets().vintage.keySet()) {
				vintage+="<option "+(advice.getFacets().vintagemin==v?"selected='selected'":"")+" value='"+v+"'>"+(v==0?"N.V.":v)+"</option>";
			}
			vintage+="</select><select name='vintagemaxsl' id='vintagemaxsl'>";
			int n=0;
			for (int v:advice.getFacets().vintage.keySet()) {
				n++;
				vintage+="<option "+((advice.getFacets().vintagemax>0&&advice.getFacets().vintagemax==v)||(advice.getFacets().vintagemax==0&&n==advice.getFacets().vintage.keySet().size())?"selected='selected'":"")+" value='"+v+"'>"+v+"</option>";
				if (n==advice.getFacets().vintage.keySet().size()) n=v;
				
			}
			vintage+="</select></div><img class='spinner' id='vintagespinner' alt='Loading...' src='/images/spinner.gif'/>"+(advice.getFacets().vintage.keySet().iterator().hasNext()&&advice.getFacets().vintagemin>advice.getFacets().vintage.keySet().iterator().next()||(advice.getFacets().vintagemax>0&&advice.getFacets().vintagemax<n)?"<img id='vintageclose' alt='Clear vintage' src='/images/transparent.gif' class='close sprite sprite-close' onclick='javascript:clearVintage();'/>":"");
			if (advice.getFacets().vintage.keySet().iterator().hasNext()) vintage+="<input type='hidden' id='vintageminscale' name='vintageminscale' value='"+advice.getFacets().vintage.keySet().iterator().next()+"'/>";
			vintage+="<input type='hidden' id='vintagemaxscale' name='vintagemaxscale' value='"+n+"'/>";
			vintage+="<input type='hidden' id='vintagemin' name='vintagemin' value='"+advice.getFacets().vintagemin+"'/>";
			vintage+="<input type='hidden' id='vintagemax' name='vintagemax' value='"+advice.getFacets().vintagemax+"'/>";
			if (advice.getFacets().vintage.keySet().iterator().hasNext()) vintage+="<div class='sliderlegend'><div class='slider-min' >"+(advice.getFacets().vintage.keySet().iterator().next()==0?"N.V.":advice.getFacets().vintage.keySet().iterator().next())+"</div><div class='slider-max' >"+n+"</div></div>";
			/*
			vintage+="<div class='criterionh'>By vintage:</div>";
			vintage+="<select name='vintage' id='vintage' onchange='javascript:spin(&quot;vintage&quot;);newSearch();' "+(advice.getVintage().equals("")?"":"class='criterions'")+">";
			//int sum=0;
			vintage+="<option "+(advice.getVintage().length()==0?"selected='selected' ":"")+"value=''>All vintages </option>";
			for (int v:advice.getFacets().vintage.keySet()) {
				//sum+=advice.getFacets().vintage.get(v);
				vintage+="<option "+(advice.getVintage().equals(v+"")?"selected='selected'":"")+" value='"+v+"'>"+v+" ("+advice.getFacets().vintage.get(v)+")</option>";
			}
			
			vintage+="</select><img class='spinner' id='vintagespinner' alt='Loading...' src='/images/spinner.gif'/>"+(advice.vintage.length()>0?"<img class='close' id='vintageclose' alt='Clear vintage' src='/images/close.png' onclick='javascript:clearVintage();'/>":"");
			*/
			
		}
		if (byregion){
			region+="<div class='criterionh'>By wine region:</div>";
			region+="<input type='text' "+(advice.getRegion()!=null&&!advice.getRegion().equals("")&&!advice.getRegion().equals("All")?" class='criterions'":"")+" id='region' name='region' value='"+advice.region.replaceAll("'", "&#39;")+"' onfocus='javascript:initRegion();' /><img class='spinner' id='regionspinner' alt='Loading...' src='/images/spinner.gif'/>"+(advice.region.length()>0&&!advice.region.equals("All")?"<img id='regionclose' alt='Clear region' src='/images/transparent.gif' class='close sprite sprite-close' onclick='javascript:clearRegion();'/>":"");
		}
		if (bycountryofseller){
			countryofseller+="<div class='criterionh'>By country of seller:</div>";
			countryofseller+="<select name='countryofseller' id='countryofseller' onchange='javascript:spin(&quot;country&quot;);newSearch();' "+(advice.countryofseller.equals("All")?"":"class='criterions'")+">";
			countryofseller+="<option "+(advice.countryofseller.equals("All")?"selected='selected' ":"")+"value='All'>All countries</option>";
			for (String c:advice.getFacets().countryofseller.keySet()) {
				countryofseller+="<option "+(advice.countryofseller.equals(c)?"selected='selected' ":"")+"value='"+c+"'>"+advice.getFacets().countryofseller.get(c)+"</option>";
			}
			countryofseller+="</select><img class='spinner' id='countryspinner' alt='Loading...' src='/images/spinner.gif'/>"+(!advice.countryofseller.equals("All")?"<img id='countryclose' src='/images/transparent.gif' class='close sprite sprite-close'  alt='clear Country' onclick='javascript:clearcountryofseller();'/>":"")+"";
		}
		if (bygrape){
			String selected=Webroutines.removeAccents(advice.grape);
			grape+="<div class='criterionh'>By grape variety:</div>";
			grape+="<select name='grape' id='grape' onchange='javascript:spin(&quot;grape&quot;);newSearch();' "+(advice.grape.equals("")?"":"class='criterions'")+">";
			grape+="<option "+(advice.grape.equals("")?"selected='selected' ":"")+"value=''>All varieties</option>";
			for (String c:advice.getFacets().getGrape().keySet()) {
				if (c!=null) grape+="<option "+(selected.equalsIgnoreCase(Webroutines.removeAccents(c))?"selected='selected' ":"")+" value='"+c.replaceAll("'", "&apos;")+"'>"+c.replaceAll("'", "&apos;")+"</option>";
			}
			grape+="</select><img class='spinner' id='grapespinner' alt='Loading...' src='/images/spinner.gif'/>"+(!advice.grape.equals("")?"<img id='grapeclose' src='/images/transparent.gif' class='close sprite sprite-close' alt='clear Grape' onclick='javascript:clearGrape();'/>":"")+"";
		}
		if (bysubregion&&advice.getFacets().subregion.size()<=15){
			for (String s:advice.getFacets().subregion.keySet()){
				subregion+="<div class='subregion criterion' onclick='javascript:setSubregion(&quot;"+s.replaceAll("'", "&apos;")+"&quot;,this)'>&nbsp;&raquo;&nbsp;"+s.replaceAll("'", "&apos;")+"<img class='spinner' id='grapespinner' alt='Loading...' src='/images/spinner.gif'/></div>";
			}
		}
		advice.doPerformanceLog("Guided Search");
		advice.loggerinfo=(advice.shopid>0?"Store":"Wine guide")+" "+advice.searchtype+" "+(advice.json?"new search":"initial load");
		
		return (other+rating+vintage+type+region+subregion+grape+countryofseller);
	}
	
	private static String getTypeHTML(WineAdvice advice, int n, String text, Winetypes w,boolean count){
		return "<div class='criterion"+(count&&advice.type==w?"s'":"'")+(advice.type==w?"":" onclick='javascript:setType(&quot;"+w+"&quot;,this)'")+"><img class='spinner'  alt='Loading...' src='/images/spinner.gif'/>"+(count&&advice.type==w?"<img class='close' src='/images/close.png' alt='clear wine type' onclick='javascript:setType(&quot;ALLTYPES&quot)'/>":"")+text+(count?" ("+n+")":"")+"</div>";
	}
}
