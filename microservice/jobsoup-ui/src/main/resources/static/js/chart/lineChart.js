var line = {
	"title":{
		"text":  "line title",
		"style": "{font-size: 15px; color:#0000ff; font-family: Verdana; text-align: center;}"
	},
	"y_legend":{
		"text": "y title",
		"style": "{color: #736AFF; font-size: 12px;}"
	},
	"num_decimals":2,
	"is_fixed_num_decimals_forced":true,
	"x_legend":{
		"text": "x title",
		"style": "{color: #736AFF; font-size: 12px;}"
	},
	"y_axis":{
		"max":   3
	},
	"bg_colour":  "#FFFFFF"
};
function validateLineJson(data) {
	line["title"]["text"] = data["title"];
	line["x_legend"]["text"] = data["xTitle"];
	line["y_legend"]["text"] = data["yTitle"];
	// 保留的小数位数
	if (data["num_decimals"] != null) line["num_decimals"] = data["num_decimals"];
	// 是否强制格式化小数
	if (data["is_fixed_num_decimals_forced"] != null) line["is_fixed_num_decimals_forced"] = data["is_fixed_num_decimals_forced"];
	if (data["yMax"] != null) {
		if (data["yMax"] == 0) data["yMax"] = 10;
		//var maxValue = (data["yMax"]/10).toFixed(0)*10;
		//if (maxValue > data["yMax"]) data["yMax"] = maxValue;
		line["y_axis"]["steps"] = data["yMax"]/10;
		line["y_axis"]["max"] = (data["yMax"]/10).toFixed(2)*10+0.1;
		//if (maxValue > 0) line["y_axis"]["steps"] = maxValue/10;
	}
	if (data["xLabels"] == null || data["xLabels"] == '') {
		data["value"] = [0];
		data["xLabels"] = [''];
	}
	line["elements"] =[
	{
		"type":      "line",
		"colour":    "#736AFF",
		"text":      data["lineTitle"],
		"font-size": 10,
		"width":     2,
		"tip":   data["tip"],
		"values" :   data["value"]
	}];
	line["x_axis"] = {
		"labels": {
		"rotate": -20,
		"labels": data["xLabels"] // x轴值
	}
	};
	return line;
}