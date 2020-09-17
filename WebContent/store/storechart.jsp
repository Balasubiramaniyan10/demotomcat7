<%@page import="com.freewinesearcher.common.Dbutil"%>
<%@page import="com.freewinesearcher.online.Shop"%>
<%@page import="com.freewinesearcher.online.StoreInfo"%>
<%@page import="com.freewinesearcher.common.datamining.Chart"%>
<%@page import="org.json.JSONObject"%>
<jsp:useBean id="cartmanager" class="com.freewinesearcher.online.shoppingcart.CartManager" scope="session"/>
<% 

int chart=0;
try{chart=Integer.parseInt(request.getParameter("chart"));}catch(Exception e){}
int shopid=cartmanager.getShopid();
Shop shop=StoreInfo.getStore(shopid);

JSONObject j=new JSONObject();

if (chart<3){%>
{
  "title":{
    "text":  "<%
	if (chart==1) out.write("Countries"); 
	if (chart==2) out.write("Wine Regions in "+shop.regions.keySet().iterator().next()); 
    %>",
    "style": "{font-size: 13px; color:#000000; font-family: Arial; text-align: center;}"
  },
 
  
 
  "elements":[
    {
      "type":      "pie",
      "start-angle": 0,
      "radius": 70,
      "colours":   ["#73880A","#D15600","#356aa0","#d01f3c","#C79810","#AAAAAA"],
      "alpha":     0.6,
      "stroke":    2,
      "onclick":	"pieclick",
      "animate":   1,
      "tip": "#label#: #percent#",
      "values" :   [<%
      if (chart==1){
                        int n=0;
                        for (String r:shop.regions.keySet()) {
                      	  n++;
                      	  if (n>1) out.write (",");
                      	  out.write ("{\"value\":"+shop.regions.get(r)+",\"label\":\""+r+"\""+(!r.equals("Other")?",\"on-click\":\"setRegion('"+r.replaceAll("'","&apos;")+"')\"":"")+"}");              
                        }   
                        }
      if (chart==2){
      int n=0;
      for (String r:shop.subregions.keySet()) {
    	  n++;
    	  if (n>1) out.write (",");
    	  out.write ("{\"value\":"+shop.subregions.get(r)+",\"label\":\""+r+"\""+(!r.equals("Other")?",\"on-click\":\"setRegion('"+r.replaceAll("'","&apos;")+"')\"":"")+"}");          
      }   
      }
      %>]
     }
   ]
   }
  <%}
  if (chart==3){
	  %>
{
    "elements": [
        {
            "type": "bar_cylinder",
            "values": [
              <%
              int n=0;
              for (int r:shop.prices.keySet()) {
            	  n++;
            	  if (n>1) out.write (",");
            	  out.print (shop.prices.get(r));          
              }
              %>  
            ]
        }
    ],
    "title": {
        "text": "Price distribution"
    },
    "x_axis": {
        "3d": 5,
        "colour": "#d0d0d0",
        "labels": {
  		
        "labels": ["10","20","30","50","100","100+"]
        }
    },
    "y_axis": {
    	"max": <%=shop.pricescale%>
    }
}	  
	  
	  <%
	  
  }
  
  %>