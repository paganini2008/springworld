var hbar = {
	"title":{
		"text":"Top Car Speed", 
		"style": "{font-size: 15px; color:#0000ff; font-family: Verdana; text-align: center;}"
	}, 
	"tooltip": { "mouse": 2, "stroke": 1, "colour": "#000000", "background": "#ffffff" } , 
	"x_axis":{"steps":1, "min":0, "max":10, "offset": false, "labels":{"rotate": -20}}, 
	"y_axis":{"offset":1, "labels":[""]}, 
	"bg_colour":"#ffffff",
	"x_legend":{
		"text": "Price",
		"style": "{color: #736AFF; font-size: 12px;}"
	},
	"y_legend":{
		"text": "Campaign",
		"style": "{color: #736AFF; font-size: 12px;}"
	},
	"num_decimals":2,
	"is_fixed_num_decimals_forced":true,
	"elements":[
	    {
	    	 "type":"hbar", 
	    	 "tip":"#val#", 
	    	 "values":[
	    	      {"right":0, "colour":"#ff0000"}
	    	 ]
	     }
	]
};
function validateHBarJson(data) {
	hbar["title"]["text"] = data["title"];
	hbar["y_legend"]["text"] = data["yTitle"];
	hbar["x_legend"]["text"] = data["xTitle"];
	// 保留的小数位数
	if (data["num_decimals"] != null) hbar["num_decimals"] = data["num_decimals"];
	// 是否强制格式化小数
	if (data["is_fixed_num_decimals_forced"] != null) hbar["is_fixed_num_decimals_forced"] = data["is_fixed_num_decimals_forced"];
	if (data["labels"].length == 0 || data["value"].length == 0) {
		if (data["xMax"] == 0) data["xMax"] = 10;
		hbar["x_axis"]["steps"] = data["xMax"]/10;
		hbar["x_axis"]["max"] = (data["xMax"]/10).toFixed(2)*10+0.1;
		hbar["y_axis"]["labels"] = null;
		hbar["elements"] =[{"type":"hbar", "values" :   null}]; 
		return hbar;
	}
	if (data["xMax"] != null) {
		if (data["xMax"] == 0) data["xMax"] = 10;
		//var maxValue = (data["xMax"]/10).toFixed(0)*10;
		//if (maxValue > data["xMax"]) data["xMax"] = maxValue;
		//if (maxValue > 0) data["xMax"] = maxValue;
		hbar["x_axis"]["steps"] = data["xMax"]/10;
		hbar["x_axis"]["max"] = (data["xMax"]/10).toFixed(2)*10+0.1;
		//if (maxValue > 0) hbar["x_axis"]["steps"] = maxValue/10;
	}
	if (data["labels"].length != 0) {
		hbar["y_axis"]["labels"] = data["labels"];
	}
	
	if (data["value"].length != 0) {
	    hbar["elements"] =[
        {
    	   "type":"hbar", 
    	   //"tip":"#val#", 
    	   "values" :   data["value"]
        }]; 
	}
	return hbar;
}
