function getHbarJson() {
	return {
		"bg_colour" : "#ffffff",
		"elements" : [ {
			"type" : "hbar",
			"values" : [ {
				"colour" : "#FF0000",
				"right" : 53
			}, {
				"colour" : "#8600FF",
				"right" : 43
			}, {
				"colour" : "#AFAF61",
				"right" : 31
			}, {
				"colour" : "#D9B300",
				"right" : 19
			} ]
		} ],
		"num_decimals" : 2,
		"is_fixed_num_decimals_forced" : true,
		"title" : {
			"style" : "{font-size: 14px; font-weight:normal; color:#0000ff; font-family: 微软雅黑; text-align: center;}",
			"text" : "注册用户汇总"
		},
		"tooltip" : {
			"background" : "#ffffff",
			"colour" : "#000000",
			"mouse" : "2",
			"stroke" : 1
		},
		"x_axis" : {
			"colour" : "#784016",
			"grid-colour" : "#f5e1aa",
			"labels" : {
				"rotate" : "0"
			},
			"max" : 100,
			"min" : 0,
			"offset" : 0,
			"steps" : 10,
			"stroke" : 2,
			"tick-height" : 20
		},
		"x_legend" : {
			"style" : "{color: #736AFF; font-size: 14px;font-weight: bold;font-family: 微软雅黑;}",
			"text" : "注册用户数"
		},
		"y_axis" : {
			"colour" : "#784016",
			"grid-colour" : "#f5e1aa",
			"labels" : [ "学生", "学校", "企业", "专家" ],
			"offset" : 1,
			"stroke" : 2,
			"tick-length" : 20
		},
		"y_legend" : {
			"style" : "{color: #736AFF; font-size: 14px;font-weight: bold;font-family: 微软雅黑;}",
			"text" : "学生/学校/企业/专家"
		}
	}
}

function getHbarJson2() {
	return {
		"title" : {
			"text" : "Top Car Speed",
			"style" : "{font-size: 15px; color:#0000ff; font-family: Verdana; text-align: center;}"
		},
		"tooltip" : {
			"mouse" : 2,
			"stroke" : 1,
			"colour" : "#000000",
			"background" : "#ffffff"
		},
		"x_axis" : {
			"max" : 1000,
			"min" : 0,
			"offset" : 0,
			"steps" : 100,
			"labels" : {
				"rotate" : -20
			}
		},
		"y_axis" : {
			"offset" : 1,
			"labels" : [ "测试-1", "测试-2", "测试-3", "测试-4" ]
		},
		"bg_colour" : "#ffffff",
		"x_legend" : {
			"text" : "Price",
			"style" : "{color: #736AFF; font-size: 12px;}"
		},
		"y_legend" : {
			"text" : "Campaign",
			"style" : "{color: #736AFF; font-size: 12px;}"
		},
		"num_decimals" : 2,
		"is_fixed_num_decimals_forced" : true,
		"elements" : [ {
			"alpha" : 0.6,
			"type" : "hbar",
			"values" : [ {
				"colour" : "#FF0000",
				"right" : 537
			}, {
				"colour" : "#8600FF",
				"right" : 430
			}, {
				"colour" : "#AFAF61",
				"right" : 310
			}, {
				"colour" : "#D9B300",
				"right" : 190
			} ]
		} ]
	}
}