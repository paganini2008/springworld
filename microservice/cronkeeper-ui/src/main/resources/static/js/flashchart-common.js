var barColorSet = ["#FF0000","#FF8000","#00DB00","#0072E3","#B15BFF","#B87070","#AFAF61"];

var firstData,secondData,thirdData;

function createFirstData() {
	return JSON.stringify(firstData);
}
function createSecondData() {
	return JSON.stringify(secondData);
}
function createThirdData() {
	return JSON.stringify(thirdData);
}

var ChartUtils = {
		
	  loadPieChart: function(o){
		  $('#chartContent3').empty();
		  var items = o.pieChartItems;
		  var a = [];
		  $.each(items,function(j,i){
			  a.push({
				  "label" : i.label,
				  "value" : i.value,
				  "font-size" : 11,
				  "text": i.text,
				  "tip": i.tip
			  });
		  });
		 
		  var b = o.title;
		  thirdData = ChartUtils.getPieChartData(a, b);
			swfobject.embedSWF($contextPath
					+ "/resources/js/chart/swf/open-flash-chart-legend.swf", 'chartContent3',
					"100%", "300", "9.0.0", "#FFFFFF", {
						"get-data" : "createThirdData"
					},{wmode:"opaque"});
	  },
		
	  loadBarChart: function(a,b,o){
		   $('#chartContent').empty();
		    firstData = ChartUtils.getBarChartData(a, b, o);
			swfobject.embedSWF($contextPath
					+ "/resources/js/chart/swf/open-flash-chart-SimplifiedChinese.swf", 'chartContent',
					"100%", "300", "9.0.0", "#FFFFFF", {
						"get-data" : "createFirstData"
					},{wmode:"opaque"});
	  },
	  
	  loadMultiLineChart: function(o){
		    $('#chartContent1').empty();
		    firstData = ChartUtils.getMultiLineChartData(o.chart1);
			swfobject.embedSWF($contextPath
					+ "/resources/js/chart/swf/open-flash-chart-SimplifiedChinese.swf", 'chartContent1',
					"100%", "300", "9.0.0", "#FFFFFF", {
						"get-data" : "createFirstData"
					},{wmode:"opaque"});
			
			$('#chartContent2').empty();
			secondData = ChartUtils.getMultiLineChartData(o.chart2);
			swfobject.embedSWF($contextPath
					+ "/resources/js/chart/swf/open-flash-chart-SimplifiedChinese.swf", 'chartContent2',
					"100%", "300", "9.0.0", "#FFFFFF", {
						"get-data" : "createSecondData"
					},{wmode:"opaque"});
	  },
	  
	  getBarChartData: function(a, b, o) {
		    var e = o.max;
		    var c = o.values;
		    var d = o.labels;
		    var labels = [];
			for(var j = 0;j < d.length;j++){
				var q = {
					"text": d[j],
					"rotate":340
				};
				labels.push(q);
			};
		  	var values = [];
			for(var j = 0; j < c.length;j++){
				var q = {
					"top": c[j],
					"colour": barColorSet[j % barColorSet.length]
				};
				values.push(q);
			}
		  	
		  
			return obj = {
				"title" : {
					"text" : a,
					"style" : "{font-size: 14px; font-weight:normal; color:#0000ff; font-family: Verdana; text-align: center;}"
				},

				"y_legend" : {
					"text" : b,
					"style" : "{color: #736AFF; font-size: 14px;font-weight: bold;}"
				},

				"elements" : [
					{
						"type" : "bar",
						"alpha" : 0.6,
						"colour" : '#FF8800',
						//"text" : b,
						"font-size" : 11,
						"values" : values
					}
				 ],

				"x_axis" : {
					"stroke" : 2,
					"tick_height" : 20,
					"colour" : "#784016",
					"grid-colour" : "#f5e1aa",
					"3d": true,
					"labels" : {
						"labels" : labels
					}
				},

				"y_axis" : {
					"stroke" : 2,
					"tick_height" : 20,
					"colour" : "#784016",
					"grid-colour" : "#f5e1aa",
					"offset" : 0,
					"max" : e,
					"steps":e/5
				},
				"bg_colour" : "#ffffff"
			};

		},
	  
	  getMultiBarChartData: function(a, b, c, d, e) {
		  var labels = [];
			for(var j = 0;j < d.length;j++){
				var q = {
					"text": d[j],
					"rotate":340
				};
				labels.push(q);
			};
		  var values = [];
		  for(var j = 0;j < c.length;j++){
			  var o = [];
			  for(var k = 0; k < c[j].length;k++){
					var q = {
						"top": c[j][k],
						"colour": barColorSet[j % barColorSet.length]
					};
					o.push(q);
			  }
			  
			  var p = {
					"type" : "bar",
					"alpha" : 0.6,
					"colour" : '#FF8800',
					"text" : d[j],
					"font-size" : 11,
					"values" : o
				};
			  
			  values.push(p);
		  }
			return obj = {
				"title" : {
					"text" : a,
					"style" : "{font-size: 14px; font-weight:normal; color:#0000ff; font-family: Verdana; text-align: center;}"
				},

				"y_legend" : {
					"text" : b,
					"style" : "{color: #736AFF; font-size: 14px;font-weight: bold;}"
				},

				"elements" : values,

				"x_axis" : {
					"stroke" : 2,
					"tick_height" : 20,
					"colour" : "#784016",
					"grid-colour" : "#f5e1aa",
					"3d": true,
					"labels" : {
						"labels" : labels
					}
				},

				"y_axis" : {
					"stroke" : 2,
					"tick_height" : 20,
					"colour" : "#784016",
					"grid-colour" : "#f5e1aa",
					"offset" : 0,
					"max" : e,
					"steps":e/5
				},
				"bg_colour" : "#ffffff"
			};

		},
		
		getMultiLineChartData: function(o){
			var a = o.title;
			var k = o.labels;
			var m = o.max;
			var s = o.dataList;
			var e = [];
			$.each(s, function(i,n){
				e.push({ 
					"type": "line", 
					"text":      n.text,
					"values": n.values,
					"dot-style": { "type": "hollow-dot" , "dot-size": 5,  "colour": "#8dbce8" ,"tip": "#x_label# \n #val#" },
					"width": 2, 
					"line-style": { "style": "solid", "on": 5, "off": 5 },
					"colour": barColorSet[i % barColorSet.length]
				});
			});
			return obj = {
					    "elements": e,
						"y_axis": { "min": 0, "max": m, "steps": m / 5 , "stroke":1,"colour" : "#784016","grid-colour" : "#f5e1aa"} ,
						"x_axis":{
							"stroke":1,
							"colour" : "#784016",
						    "grid-colour" : "#f5e1aa",
						    "3d": true,
							"labels": 
							{
								"labels": k,
								"rotate" : -45
							 } 
					    },
						"title": {  "text": a ,  "style": "{font-size: 16px; color:#0000ff; font-family: 微软雅黑; text-align: center;margin-bottom: 10px;}"  }, 
					    "bg_colour":"#ffffff"
					 };
			},
			
			getPieChartData : function(a, b) {
				return obj = {
					"legend":{
						"visible":true, 
						"bg_colour":"#fefefe", 
						"position":"right", 
						"border":true, 
						"shadow":true
					},
					"elements" : [ {
						"type" : "pie",
						"radius" : 120,
						"start-angle" : 35,
						"animate" : animate = [ {
										"type" : "fade"
									}, {
										"type" : "bounce",
										"distance" : 4
									} ],
						"gradient-fill" : true,
						"no-labels":false,
						"tip" : "#name#<br>#percent#",
						"colours" : barColorSet,
						"values" : a
					} ],
					"title" : {
						"text" : b,
						"style" : "{font-size: 14px; font-weight:normal; color:#0000ff; font-family: Verdana; text-align: center;}"
					},
					"bg_colour" : "#ffffff"
				};

	}
};