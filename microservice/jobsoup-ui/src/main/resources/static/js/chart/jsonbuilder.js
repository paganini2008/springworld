var type = "";
JSONBuilder = {

	setType : function(a) {
		type = a;
	},

	bar : function(a, b, c, d, e) {
		var colour = b == labelName[0] ? "#f22828" : "#32a1ff";
		return obj = {
			"title" : {
				"text" : a,
				"style" : "{font-size: 14px; font-weight:normal; color:#0000ff; font-family: Verdana; text-align: center;}"
			},

			"y_legend" : {
				"text" : b,
				"style" : "{color: #736AFF; font-size: 12px;}"
			},

			"elements" : [ {
				"type" : "bar",
				"alpha" : 0.6,
				"colour" : colour,
				//"text" : b,
				"font-size" : 11,
				"values" : c
			} ],

			"x_axis" : {
				"stroke" : 2,
				"tick_height" : 20,
				"colour" : "#784016",
				"grid-colour" : "#f5e1aa",
				"3d": true,
				"labels" : {
					"labels" : d
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
		}

	},
	doubleBar : function(a, b, c, d, e) {
		return obj = {
			"title" : {
				"text" : a,
				"style" : "{font-size: 15px; font-weight:normal; color:#0000ff; font-family: Verdana; text-align: center;}"
			},

			"y_legend" : {
				"text" : a,
				"style" : "{color: #736AFF; font-size: 12px;}"
			},

			"elements" : [ {
				"type" : "bar",
				"alpha" : 0.6,
				"colour" : "#f22828",
				"outline-colour" : "#ff00ff",
				"text" : labelName[0],
				"font-size" : 11,
				"values" : b
			}, {
				"type" : "bar",
				"alpha" : 0.6,
				"colour" : "#32a1ff",
				"text" : labelName[1],
				"font-size" : 11,
				"values" : c
			} ],

			"x_axis" : {
				"stroke" : 2,
				"tick_height" : 20,
				"colour" : "#784016",
				"grid-colour" : "#f7e8bf",
				"3d": true,
				"labels" : {
					"labels" : d
				}
			},

			"y_axis" : {
				"stroke" : 2,
				"tick_height" : 20,
				"colour" : "#784016",
				"grid-colour" : "#f7e8bf",
				"offset" : 0,
				"max" : e,
				"steps":e/5
			},
			"bg_colour" : "#ffffff"
		}
	},
	pie : function(a, b) {
		var animate = false;
		if (type == "notToPDF") {
			animate = [ {
				"type" : "fade"
			}, {
				"type" : "bounce",
				"distance" : 4
			} ]
		} else {
			animate = false;
		}
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
				"animate" : animate,
				"gradient-fill" : true,
				"no-labels":false,
				"tip" : "#name#<br>#percent#",
				"colours" : [ "#ff0000", "#ffc000", "#dce400", "#3cd600","#006ad6", "#a600d6", "#e70098", "#7c7c7c" ],
				"values" : a
			} ],
			"title" : {
				"text" : b,
				"style" : "{font-size: 14px; font-weight:normal; color:#0000ff; font-family: Verdana; text-align: center;}"
			},
			"bg_colour" : "#ffffff"
		}

	},
	hbar : function(a, b, c, d) {
		return obj = {
			"title" : {
				"text" : a,
				"style" : "{font-size: 15px; color:#0000ff; font-family: Verdana; text-align: center;}"
			},
			"tooltip" : {
				"mouse" : 2,
				"stroke" : 1,
				"colour" : "#000000",
				"background" : "#ffffff"
			},
			"x_axis" : {
				"steps" : 0.508,
				"min" : 0,
				"max" : b,
				"offset" : false,
				"labels" : {
					"rotate" : -20
				}
			},
			"y_axis" : {
				"offset" : 1,
				"labels" : c
			},
			"bg_colour" : "#ffffff",
			"x_legend" : {
				"text" : "Price",
				"style" : "{color: #736AFF; font-size: 12px;}"
			},
			"y_legend" : {
				"text" : "Media",
				"style" : "{color: #736AFF; font-size: 12px;}"
			},
			"num_decimals" : 2,
			"is_fixed_num_decimals_forced" : true,
			"elements" : d
		};
	},
	
	line: function(a,b,c,d){
		return obj = { 
				"elements":
					 [
						{ 
							"type": "line", 
							"text":      a,
							"values": b,
							"dot-style": { "type": "hollow-dot" , "dot-size": 5,  "colour": "#8dbce8" ,"tip": "#x_label# \n #val#" },
							"width": 2, 
							"line-style": { "style": "solid", "on": 5, "off": 5 },
							"colour": "#0fabec" 
						}, 
				     ],
					"y_axis": { "min": 0, "max": c, "steps": 5 , "stroke":1,"colour" : "#784016","grid-colour" : "#f5e1aa"} ,
					"x_axis":{
						"stroke":1,
						"colour" : "#784016",
					    "grid-colour" : "#f5e1aa",
						"labels": 
						{
							"labels": d
						 } 
				    },
					"title": {  "text":  "",  "style": "{font-size: 20px; color:#0000ff; font-family: Verdana; text-align: center;}"  }, 
				    "bg_colour":"#ffffff"
				 }
	}
	
}