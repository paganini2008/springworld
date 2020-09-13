var lineBar = {
	"title":{
		"text":  "Line and Bar",
		"style": "{font-size: 15px; color:#0000ff; font-family: Verdana; text-align: center;}"
	},
	"y_legend":{
		"text": "y",
		"style": "{color: #736AFF; font-size: 12px;}"
	},
	"x_legend":{
		"text": "x",
		"style": "{color: #736AFF; font-size: 12px;}"
	},
	"elements":[
	{
		"type":      "bar",
		"alpha":     0.7,
		"colour":    "#6D9934",
		"text":      "bar",
		//"on-click": "line_2",
		"font-size": 10,
		"values" :   [0]
	},
	{
		"type": "line",
		//"dot-style": { "type": "solid-dot", "dot-size": 5, "halo-size": 2, "on-click": "line_1" },
		"values": [0],
		"width": 2,
		"colour": "#A18B6A",
		"text": "line",
		"font-size": 12
    }
	],
	"bg_colour": "#FFFFFF",
	"x_axis":{
		"labels": {
			"rotate": -20,
			"labels": [""] // x轴值
		}
	},
	"num_decimals":2,
	"is_fixed_num_decimals_forced":true,
	"y_axis":{
		"min":			0,
		"max":         10,
		"steps":		1
	},
	"tooltip": { "mouse": 2, "stroke": 1, "colour": "#000000", "background": "#ffffff" }
};
function validateLineBarJson(data) {
	lineBar["title"]["text"] = data["title"];
	lineBar["x_legend"]["text"] = data["xTitle"];
	lineBar["y_legend"]["text"] = data["yTitle"];
	// 保留的小数位数
	if (data["num_decimals"] != null) lineBar["num_decimals"] = data["num_decimals"];
	// 是否强制格式化小数
	if (data["is_fixed_num_decimals_forced"] != null) lineBar["is_fixed_num_decimals_forced"] = data["is_fixed_num_decimals_forced"];
	if (data["yMax"] != null) {
		if (data["yMax"] == 0) data["yMax"] = 10;
		//var maxValue = (data["yMax"]/10).toFixed(0)*10;
		//if (maxValue > data["yMax"]) data["yMax"] = maxValue;
		lineBar["y_axis"]["steps"] = data["yMax"]/10;
		lineBar["y_axis"]["max"] = (data["yMax"]/10).toFixed(2)*10+0.1;
		//if (maxValue > 0) lineBar["y_axis"]["steps"] = maxValue/10;
	}
	if (data["xLabels"] == null || data["xLabels"] == '') {
		data["value"] = [0];
		data["xLabels"] = [''];
	}
	lineBar["elements"] = data["elements"];
	lineBar["x_axis"] = {
			"stroke":1,
			"tick-length":2,
			"colour":"#696969",
			"grid-visible":false,
			"labels": {
				"rotate": -20,
				"labels": data["xLabels"] // x轴值
			}
	};
	return lineBar;
}