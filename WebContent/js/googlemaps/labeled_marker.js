function LabeledMarker(latlng,options){this.latlng=latlng;this.labelText=options.labelText||"";this.labelClass=options.labelClass||"markerLabel";this.labelOffset = options.labelOffset || new GSize(-0.5*this.labelText.length, -10);this.clickable=options.clickable||true;if(options.draggable){options.bouncy=false;}
GMarker.apply(this,arguments);}
LabeledMarker.prototype=new GMarker(new GLatLng(0,0));LabeledMarker.prototype.initialize=function(map){GMarker.prototype.initialize.apply(this,arguments);var div=document.createElement("div");div.className=this.labelClass;div.innerHTML=this.labelText;div.style.position="absolute";map.getPane(G_MAP_MARKER_PANE).appendChild(div);if(this.clickable){var eventPassthrus=['click','dblclick','mousedown','mouseup','mouseover','mouseout'];for(var i=0;i<eventPassthrus.length;i++){var name=eventPassthrus[i];GEvent.addDomListener(div,name,newEventPassthru(this,name));}
div.style.cursor="pointer";}
this.map=map;this.div=div;}
function newEventPassthru(obj,event){return function(){GEvent.trigger(obj,event);};}
LabeledMarker.prototype.redraw=function(force){GMarker.prototype.redraw.apply(this,arguments);if(!force)return;var p=this.map.fromLatLngToDivPixel(this.getPoint());var z=GOverlay.getZIndex(this.latlng.lat());this.div.style.left=(p.x+this.labelOffset.width)+"px";this.div.style.top=(p.y+this.labelOffset.height)+"px";}
LabeledMarker.prototype.remove=function(){GEvent.clearInstanceListeners(this.div);this.div.parentNode.removeChild(this.div);this.div=null;GMarker.prototype.remove.apply(this,arguments);}
