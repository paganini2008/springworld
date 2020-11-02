var bar = {
	"title":{
		"text":  "bar title",
		"style": "{font-size: 15px; color:#0000ff; font-family: Verdana; text-align: center;}"
	},
	"y_legend":{
		"text": "Price",
		"style": "{color: #736AFF; font-size: 12px;}"
	},
	"x_legend":{
		"text": "Campaign",
		"style": "{color: #736AFF; font-size: 12px;}"
	},
	"x_axis":{
		"tick_height":4,
		"stroke":2,
		"colour":"#cccccc",
		"font-colour":"#dddddd",
		"grid-colour":"#cccccc",
		"labels":{
		      "visible": true,
		      "rotate": "vertical"
		    }
	},
	"num_decimals":2,
	"is_fixed_num_decimals_forced":true,
	"y_axis":{
		"stroke":1,
		"tick_length": 3,
		"colour":"#d000d0",
		"grid_colour": "#00ff00",
		"labels": { "steps": 4, "rotate": 270 }
	},
	"bg_colour":  "#FFFFFF"
};
function validateBarJson(data) {
	bar["title"]["text"] = data["title"];
	bar["x_legend"]["text"] = data["xTitle"];
	bar["y_legend"]["text"] = data["yTitle"];
	// 保留的小数位数
	if (data["num_decimals"] != null) bar["num_decimals"] = data["num_decimals"];
	// 是否强制格式化小数
	if (data["is_fixed_num_decimals_forced"] != null) bar["is_fixed_num_decimals_forced"] = data["is_fixed_num_decimals_forced"];
	if (data["yMax"] != null) {
		if (data["yMax"] ==0) data["yMax"] = 0;
		//if ((data["yMax"]/10).toFixed(0)<data["yMax"]) data["yMax"]++;
		bar["y_axis"]["steps"] = data["yMax"]/10;
		bar["y_axis"]["max"] = (data["yMax"]/10).toFixed(2)*10+0.1;
		//if (maxValue > 0) bar["y_axis"]["steps"] = maxValue/10;
	}
	bar["x_axis"]["labels"] = {
		"rotate": -20,
		"labels": data["labels"]
	}; 
	
	bar["elements"] =[
  	{
  	  "type":      "bar",
  	  "alpha":     0.5,
  	  "colour":    "#9933CC",
  	  "values" :   data["value"]
  	}]; 
	return bar;
}