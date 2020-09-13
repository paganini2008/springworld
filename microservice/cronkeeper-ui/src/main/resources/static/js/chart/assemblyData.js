var spendingPie		= {"tip": "#val# of #total# <br>#percent#","num_decimals":0};
var spendingBar		= {"yTitle":"Spend","num_decimals":0};
var spendingLine 	= {"tip":"#x_label#<br>#val#","num_decimals":0};

var cpmPricePie 	= {"tip": "#val# of #total# <br>#percent#"};
var cpmPriceBar 	= {"yTitle":"Price"};
var cpmPriceLine 	= {"tip":"#x_label#<br>#val#"};

var cpcPricePie 	= {"tip": "#val# of #total# <br>#percent#"};
var cpcPriceBar 	= {"yTitle":"Price"};
var cpcPriceLine 	= {"tip":"#x_label#<br>#val#"};

var ctrPie 	= {"tip": "#val#%"};
var ctrBar 	= {"yTitle":"Percent"};
var ctrLine 	= {"tip":"#x_label#<br>#val#"};

// 生成报表所需要的数据及表格的html代码
function createHtml(type) {
	var data = $('#campaignJosn').val();
	var array = eval("("+data+")");
	// 列表数据
	var listHtml = "";
	
	// spenging: 0: 总金额汇总，1: 饼状图数据， 2： y轴最大值， 3: 柱状图数据， 4: 线状图x轴坐标， 5: 线状图数据
	var spendingValue = [0,[],0,[],[],[]];
	// cpmPrice: 0: 总金额汇总，1: y轴最大值，2: 柱状图数据， 3: 饼状图数据， 4: 线状图x轴坐标， 5: 线状图数据
	var cpmPriceValue = [0,0,[],[],[],[]];
	// cpcPrice: 0: 总金额汇总，1: y轴最大值，2: 柱状图数据， 3: 饼状图数据， 4: 线状图x轴坐标， 5: 线状图数据
	var cpcPriceValue = [0,0,[],[],[],[]];
	// ctr: 0: 柱状图数据，1: y轴最大值，2： 饼状图数据， 3: 线状图x轴坐标， 4：线状图数据
	var ctrValue = [[],0,[],[],[]];
	// cpmQuan： cpm数量汇总
	var cpmQuanTotal = 0;
	// cpcQuan: cpc数量汇总
	var cpcQuanTotal = 0;
	// x轴的值
	var xText = [];

	var startDate;
	var endDate;
	
	// 0:spending总和, 1:cmpPrice总和, 2:cpcPrice总和
	var totalPercent = [0,0,0];
	// 求总和，显示饼状图百分比
	for (var i=0;i<array.length;i++) {
		var campaignJson = array[i];
		totalPercent[0] += campaignJson.cpConPrice;
		totalPercent[1] += campaignJson.cpCpmPrice;
		totalPercent[2] += campaignJson.cpCpcPrice;
	}
	for (var i=0;i<array.length;i++) {
		var campaignJson = array[i];
		campaignJson.cpName = campaignJson.cpName.replaceAll("<", "&lt;");
		campaignJson.cpName = campaignJson.cpName.replaceAll(">", "&gt;");
		listHtml += "<tr><td align='center'>";
		if ("Campaign" == type) {
			var url = mediaReport(campaignJson.id);
			listHtml += "<a href=\""+url+"\" id='alink"+i+"' class='listViewTdLinkS1'>"+campaignJson.cpName+"</a></td>";
		}
		if ("Media" == type) {
			var url = placementReport(campaignJson.id);
			listHtml += "<a href=\""+url+"\" id='alink"+i+"' class='listViewTdLinkS1'>"+campaignJson.cpName+"</a></td>";
		}
		if ("Placement" == type) {
			listHtml += campaignJson.cpName+"</td>";
		}
		if ("Campaign" == type) {
			if(campaignJson.cpStartDate != null) campaignJson.cpStartDate = new Date(parseInt(campaignJson.cpStartDate.year)+1900, campaignJson.cpStartDate.month, campaignJson.cpStartDate.date).toFormatString("yyyy-MM-dd");
			if(campaignJson.cpEndDate != null) campaignJson.cpEndDate = new Date(parseInt(campaignJson.cpEndDate.year)+1900, campaignJson.cpEndDate.month, campaignJson.cpEndDate.date).toFormatString("yyyy-MM-dd");
			listHtml += "<td align='center'>"+campaignJson.cpStartDate+" to "+campaignJson.cpEndDate+"</td>";
		}
		listHtml += "<td align='right'>"+HRMCommon.formatNumber(campaignJson.cpConPrice)+"</td>"+
		    "<td align='right'>"+parseFloat(campaignJson.cpCpmPrice).toFixed(2)+"</td><td align='right'>"+parseFloat(campaignJson.cpCpcPrice).toFixed(2)+"</td>"+
		    "<td align='right'>"+parseFloat(campaignJson.cpCtr).toFixed(2)+"</td>"+
		    "<td align='right'>"+HRMCommon.formatNumber(campaignJson.cpCpmQuan)+"</td>"+
		    "<td align='right'>"+HRMCommon.formatNumber(campaignJson.cpCpcQuan)+"</td></tr>";
		spendingValue[0] += campaignJson.cpConPrice;
		cpmQuanTotal += campaignJson.cpCpmQuan;
		cpcQuanTotal += campaignJson.cpCpcQuan;
	    // 组装数据
	    spendingValue[1][i] = {"value":campaignJson.cpConPrice,"tip":campaignJson.cpName+"<br>"+(campaignJson.cpConPrice*100/totalPercent[0]).toFixed(2)+"%","label":(campaignJson.cpConPrice*100/totalPercent[0]).toFixed(2)+"%","text":campaignJson.cpName};
	    if (campaignJson.cpConPrice > spendingValue[2]) spendingValue[2] = campaignJson.cpConPrice;
	    spendingValue[3][i] = {"top":campaignJson.cpConPrice,"colour":pieColorArray[i]};
	    spendingValue[4][i] = campaignJson.cpName;
	    spendingValue[5][i] = campaignJson.cpConPrice;
	    
	    if (campaignJson.cpCpmPrice > cpmPriceValue[1]) cpmPriceValue[1] = campaignJson.cpCpmPrice;
	    cpmPriceValue[2][i] = {"top":campaignJson.cpCpmPrice,"colour":pieColorArray[i]};
	    cpmPriceValue[3][i] = {"value":campaignJson.cpCpmPrice,"tip":campaignJson.cpName+"<br>"+(campaignJson.cpCpmPrice*100/totalPercent[1]).toFixed(2)+"%","label":(campaignJson.cpCpmPrice*100/totalPercent[1]).toFixed(2)+"%","text":campaignJson.cpName};
	    cpmPriceValue[4][i] = campaignJson.cpName;
	    cpmPriceValue[5][i] = campaignJson.cpCpmPrice;

	    if (campaignJson.cpCpcPrice > cpcPriceValue[1]) cpcPriceValue[1] = campaignJson.cpCpcPrice;
	    cpcPriceValue[2][i] = {"top":campaignJson.cpCpcPrice,"colour":pieColorArray[i]};
	    cpcPriceValue[3][i] = {"value":campaignJson.cpCpcPrice,"tip":campaignJson.cpName+"<br>"+(campaignJson.cpCpcPrice*100/totalPercent[2]).toFixed(2)+"%","label":(campaignJson.cpCpcPrice*100/totalPercent[2]).toFixed(2)+"%","text":campaignJson.cpName};
	    cpcPriceValue[4][i] = campaignJson.cpName;
	    cpcPriceValue[5][i] = campaignJson.cpCpcPrice;
	    
	    ctrValue[0][i] = {"top":campaignJson.cpCtr,"colour":pieColorArray[i]};
	    if (campaignJson.cpCtr > ctrValue[1]) ctrValue[1] = campaignJson.cpCtr;
	    ctrValue[2][i] = {"value":campaignJson.cpCtr,"tip":campaignJson.cpName+"<br>"+campaignJson.cpCtr+"%","label":campaignJson.cpCtr+"%","text":campaignJson.cpName};
	    ctrValue[3][i] = campaignJson.cpName;
	    ctrValue[4][i] = campaignJson.cpCtr;

	    xText[i] = campaignJson.cpName;
	}
	
	var totalHtml = "<td align='center'><strong>Total</strong></td>";
	if ("Campaign" == type) {
		totalHtml += "<td align='center'></td>";
	}
	if (cpmQuanTotal != 0) cpmPriceValue[0] = spendingValue[0]/cpmQuanTotal;
	if (cpcQuanTotal != 0) cpcPriceValue[0] = spendingValue[0]/cpcQuanTotal;
	var avgCtr = 0;
	if (cpmQuanTotal != 0) avgCtr = cpcQuanTotal*100/(cpmQuanTotal*1000);
	totalHtml += "<td align='right'>"+HRMCommon.formatNumber(spendingValue[0])+"</td>"+
				"<td align='right'>"+parseFloat(cpmPriceValue[0]).toFixed(2)+"</td><td align='right'>"+parseFloat(cpcPriceValue[0]).toFixed(2)+"</td>"+
				"<td align='right'>"+parseFloat(avgCtr).toFixed(2)+"</td>"+
	    		"<td align='right'>"+HRMCommon.formatNumber(cpmQuanTotal)+"</td><td align='right'>"+HRMCommon.formatNumber(cpcQuanTotal)+"</td>";
	// 拼spending饼状图的数据
	spendingPie.title = type+" Spend";
	spendingPie.value = spendingValue[1];
	// 柱状图
	spendingBar.title = type+" Spend";
	spendingBar.xTitle = type;
	spendingBar.yMax = spendingValue[2];
	spendingBar.labels = xText;
	spendingBar.value = spendingValue[3];
	// 线状图
	spendingLine.title = type+" Spend";
	spendingLine.xTitle = type;
	spendingLine.yTitle = "Spend";
	spendingLine.yMax = spendingValue[2];
	spendingLine.lineTitle = "Spend";
	spendingLine.xLabels = spendingValue[4];
	spendingLine.value = spendingValue[5];

	// 拼cpmPrice柱状图的数据
	// 计算y轴最大值
	cpmPriceBar.title = type+" CPM Price";
	cpmPriceBar.xTitle = type;
	cpmPriceBar.yMax = cpmPriceValue[1];
	cpmPriceBar.labels = xText;
	cpmPriceBar.value = cpmPriceValue[2];
	//饼状图
	cpmPricePie.title = type+" CPM Price";
	cpmPricePie.value= cpmPriceValue[3];
	// 线状图
	cpmPriceLine.title = type+" CPM Price";
	cpmPriceLine.xTitle = type;
	cpmPriceLine.yTitle = "Price";
	cpmPriceLine.yMax = cpmPriceValue[1];
	cpmPriceLine.lineTitle = "Price";
	cpmPriceLine.xLabels = cpmPriceValue[4];
	cpmPriceLine.value = cpmPriceValue[5];

	// 拼cpcPrice柱状图的数据
	// 计算y轴最大值
	cpcPriceBar.title = type+" CPC Price";
	cpcPriceBar.xTitle = type;
	cpcPriceBar.yMax = cpcPriceValue[1];
	cpcPriceBar.labels = xText;
	cpcPriceBar.value = cpcPriceValue[2];
	//饼状图
	cpcPricePie.title = type+" CPC Price";
	cpcPricePie.value= cpcPriceValue[3];
	// 线状图
	cpcPriceLine.title = type+" CPC Price";
	cpcPriceLine.xTitle = type;
	cpcPriceLine.yTitle = "Price";
	cpcPriceLine.yMax = cpcPriceValue[1];
	cpcPriceLine.lineTitle = "Price";
	cpcPriceLine.xLabels = cpcPriceValue[4];
	cpcPriceLine.value = cpcPriceValue[5];
	
	// 拼cpCtr柱状图的数据
	// 计算y轴最大值
	ctrBar.title = type+" CTR";
	ctrBar.xTitle = type;
	ctrBar.yMax = ctrValue[1];
	ctrBar.labels = xText;
	ctrBar.value = ctrValue[0];
	//饼状图
	ctrPie.title = type+" CTR";
	ctrPie.value= ctrValue[2];
	// 线状图
	ctrLine.title = type+" CTR";
	ctrLine.xTitle = type;
	ctrLine.yTitle = "Percent";
	ctrLine.yMax = ctrValue[1];
	ctrLine.lineTitle = "Percent";
	ctrLine.xLabels = ctrValue[3];
	ctrLine.value = ctrValue[4];
	
	$('#campaignBody').replaceWith("<tbody id='campaignBody'></tbody>");
	$('#campaignBody').html(listHtml);
	$('#campaignTotal').html(totalHtml);
	//$("#campaignTable").tablesorter(); 
}

function readDataSpending() {
	var chart = validatePieJson(spendingPie);
	return JSON.stringify(chart);
}

function readDataCpmPrice() {
	var chart = validateBarJson(cpmPriceBar);
	return JSON.stringify(chart);
}

function readDataCpcPrice() {
	var chart = validateBarJson(cpcPriceBar);
	return JSON.stringify(chart);
}

function readDataCtr() {
	var chart = validateBarJson(ctrBar);
	return JSON.stringify(chart);
}

function findSWF(movieName) {
	if (window[movieName] != null) return window[movieName] ;
	else return document[movieName];
}

function changeSpending(type) {
	var data;
	if (type == "bar") data = spendingBar;
	if (type == "pie") data = spendingPie;
	if (type == "line") data = spendingLine;
	changeData("spendingDiv", data, type);
}

function changeCpm(type) {
	var data;
	if (type == "bar") data = cpmPriceBar;
	if (type == "pie") data = cpmPricePie;
	if (type == "line") data = cpmPriceLine;
	changeData("cpmPriceDiv", data, type);
}

function changeCpc(type) {
	var data;
	if (type == "bar") data = cpcPriceBar;
	if (type == "pie") data = cpcPricePie;
	if (type == "line") data = cpcPriceLine;
	changeData("cpcPriceDiv", data, type);
}
function changeCtr(type) {
	var data;
	if (type == "bar") data = ctrBar;
	if (type == "pie") data = ctrPie;
	if (type == "line") data = ctrLine;
	changeData("ctrDiv", data, type);
}

function changeData(divId, data, type) {
	var chart;
	var tmp = findSWF(divId);
	if (type == "bar") chart = validateBarJson(data);
	if (type == "pie") chart = validatePieJson(data);
	if (type == "line") chart = validateLineJson(data);
	tmp.load(JSON.stringify(chart));
}

function recount(action, searchBean, type) {
	overDiv.showBox('divWait');
	$.post(action, searchBean, recountCampaignCallback, 'text');
	function recountCampaignCallback(data) {
		/**if(data!=null && (data.indexOf("SUCC:")>-1 || data.indexOf("FAIL:")>-1)){
			HRMCommon.actionMsgHandler(data);
			HRMCommon.closeDialog('divWaitJquery');
			return;
		}*/
		overDiv.hideBox('divWait');
		if (data != '') {
			$('#campaignJosn').val(data);
			createHtml(type);
			changeSWFData();
		}
	}
}
function changeSWFData() {
	var spendingType = $('input:radio[name=changeSpending][checked=true]').val();
	changeSpending(spendingType);
	var cpmType = $('input:radio[name=changeCpm][checked=true]').val();
	changeCpm(cpmType);
	var cpcType = $('input:radio[name=changeCpc][checked=true]').val();
	changeCpc(cpcType);
	var ctrType = $('input:radio[name=changeCtr][checked=true]').val();
	changeCtr(cpcType);
}

// Beachmark Report生成table content；
function createBenchmarkHtml(reportType){
	// 后台数据；
	var data = $('#campaignJosn').val();
	var array = eval("("+data+")");
	
	// spenging: 0: 饼状图数据， 1： y轴最大值，2: 柱状图数据， 3: 线状图x轴坐标， 4: 线状图数据
	var spendingValue = [[],0,[],[],[]];
	// cpmPrice: 0: y轴最大值，1: 柱状图数据， 2: 饼状图数据， 3: 线状图x轴坐标， 4: 线状图数据
	var cpmPriceValue = [0,[],[],[],[]];
	// cpcPrice: 0: y轴最大值，1: 柱状图数据， 2: 饼状图数据， 3: 线状图x轴坐标， 4: 线状图数据
	var cpcPriceValue = [0,[],[],[],[]];
	// ctr: 0: 柱状图数据，1: y轴最大值，2： 饼状图数据， 3: 线状图x轴坐标， 4：线状图数据
	var ctrValue = [[],0,[],[],[]];
	// x轴的值
	var xText = [];
	
	var tableHead = "";
	var tableBody = "";
	var tableTotal = "";
	if(reportType == "platform"){
		tableHead = "<tr>"
		    +"<th align='center'>Platform</th>"
		    +"<th align='center'>Spend</th>"
		    +"<th align='center'>CPM</th>"
		    +"<th align='center'>CPC</th>"
		    +"<th align='center'>CTR(%)</th>"
		    +"<th align='center'>Impression(K)</th>"
		    +"<th align='center'>Click</th>"
		    +"</tr>";
		var total = new Array(0, 0, 0, 0, 0, 0);
		
		// 0:spending总和, 1:cmpPrice总和, 2:cpcPrice总和
		var totalPlatformPercent = [0,0,0];
		// 求总和，显示饼状图百分比
		for (var i=0;i<array.length;i++) {
			var rowData = array[i];
			totalPlatformPercent[0] += rowData.cpExecPrice;
			totalPlatformPercent[1] += rowData.cpExecPvPrice;
			totalPlatformPercent[2] += rowData.cpExecCkPrice;
		}
		for(var i=0;i<array.length;i++) {
			var rowData = array[i];
			tableBody += "<tr><td align='center'><a href='#' onclick='goMediaReport("+rowData.mediacatId+");' class='listViewTdLinkS1'>"+rowData.mediacatName+"</a></td>"+
		    "<td align='center'>"+HRMCommon.formatNumber(parseFloat(rowData.cpExecPrice).toFixed(0))+"</td><td align='center'>"+parseFloat(rowData.cpExecPvPrice).toFixed(2)+"</td><td align='center'>"+parseFloat(rowData.cpExecCkPrice).toFixed(2)+"</td>"+
		    "<td align='center'>"+parseFloat(rowData.cpCtr).toFixed(2)+"</td><td align='center'>"+HRMCommon.formatNumber(parseFloat(rowData.cpExecPv).toFixed(0))+"</td><td align='center'>"+HRMCommon.formatNumber(parseFloat(rowData.cpExecCk).toFixed(0))+"</td></tr>";
		    total[0] += rowData.cpExecPrice;
		    total[1] += rowData.cpExecPvPrice;
		    total[2] += rowData.cpExecCkPrice;
		    total[3] += rowData.cpCtr;
		    total[4] += rowData.cpExecPv;
		    total[5] += rowData.cpExecCk;
		    
		    xText[i] = rowData.mediacatName;
		    // 组装数据
		    spendingValue[0][i] = {"value":rowData.cpExecPrice,"tip":rowData.mediacatName+"<br>"+(rowData.cpExecPrice*100/totalPlatformPercent[0]).toFixed(2)+"%","label":(rowData.cpExecPrice*100/totalPlatformPercent[0]).toFixed(2)+"%","text":rowData.mediacatName};
		    if (rowData.cpExecPrice > spendingValue[1]) spendingValue[1] = rowData.cpExecPrice;
		    spendingValue[2][i] = {"top":rowData.cpExecPrice,"colour":pieColorArray[i]};
		    spendingValue[3][i] = rowData.mediacatName;
		    spendingValue[4][i] = rowData.cpExecPrice;
		    
		    if (rowData.cpExecPvPrice > cpmPriceValue[0]) cpmPriceValue[0] = rowData.cpExecPvPrice;
		    cpmPriceValue[1][i] = {"top":rowData.cpExecPvPrice,"colour":pieColorArray[i]};
		    cpmPriceValue[2][i] = {"value":rowData.cpExecPvPrice,"tip":rowData.mediacatName+"<br>"+(rowData.cpExecPvPrice*100/totalPlatformPercent[1]).toFixed(2)+"%","label":(rowData.cpExecPvPrice*100/totalPlatformPercent[1]).toFixed(2)+"%","text":rowData.mediacatName};
		    cpmPriceValue[3][i] = rowData.mediacatName;
		    cpmPriceValue[4][i] = rowData.cpExecPvPrice;

		    if (rowData.cpExecCkPrice > cpcPriceValue[0]) cpcPriceValue[0] = rowData.cpExecCkPrice;
		    cpcPriceValue[1][i] = {"top":rowData.cpExecCkPrice,"colour":pieColorArray[i]};
		    cpcPriceValue[2][i] = {"value":rowData.cpExecCkPrice,"tip":rowData.mediacatName+"<br>"+(rowData.cpExecCkPrice*100/totalPlatformPercent[2]).toFixed(2)+"%","label":(rowData.cpExecCkPrice*100/totalPlatformPercent[2]).toFixed(2)+"%","text":rowData.mediacatName};
		    cpcPriceValue[3][i] = rowData.mediacatName;
		    cpcPriceValue[4][i] = rowData.cpExecCkPrice;
		    
		    ctrValue[0][i] = {"top":rowData.cpCtr,"colour":pieColorArray[i]};
		    if (rowData.cpCtr > ctrValue[1]) ctrValue[1] = rowData.cpCtr;
		    ctrValue[2][i] = {"value":rowData.cpCtr,"tip":rowData.mediacatName+"<br>"+rowData.cpCtr+"%","label":rowData.cpCtr+"%","text":rowData.mediacatName};
		    ctrValue[3][i] = rowData.mediacatName;
		    ctrValue[4][i] = rowData.cpCtr;
		}
		// 拼spending饼状图的数据
		spendingPie.title = "Platform Spend";
		spendingPie.value = spendingValue[0];
		// 柱状图
		spendingBar.title = "Platform Spend";
		spendingBar.xTitle = "Platform";
		spendingBar.yMax = spendingValue[1];
		spendingBar.labels = xText;
		spendingBar.value = spendingValue[2];
		// 线状图
		spendingLine.title = "Platform Spend";
		spendingLine.xTitle = "Platform";
		spendingLine.yTitle = "Spend";
		spendingLine.yMax = spendingValue[1];
		spendingLine.lineTitle = "Spend";
		spendingLine.xLabels = spendingValue[3];
		spendingLine.value = spendingValue[4];

		// 拼cpmPrice柱状图的数据
		// 计算y轴最大值
		cpmPriceBar.title = "Platform CPM Price";
		cpmPriceBar.xTitle = "Platform";
		cpmPriceBar.yMax = cpmPriceValue[0];
		cpmPriceBar.labels = xText;
		cpmPriceBar.value = cpmPriceValue[1];
		//饼状图
		cpmPricePie.title = "Platform CPM Price";
		cpmPricePie.value= cpmPriceValue[2];
		// 线状图
		cpmPriceLine.title = "Platform CPM Price";
		cpmPriceLine.xTitle = "Platform";
		cpmPriceLine.yTitle = "Price";
		cpmPriceLine.yMax = cpmPriceValue[0];
		cpmPriceLine.lineTitle = "Price";
		cpmPriceLine.xLabels = cpmPriceValue[3];
		cpmPriceLine.value = cpmPriceValue[4];

		// 拼cpcPrice柱状图的数据
		// 计算y轴最大值
		cpcPriceBar.title = "Platform CPC Price";
		cpcPriceBar.xTitle = "Platform";
		cpcPriceBar.yMax = cpcPriceValue[0];
		cpcPriceBar.labels = xText;
		cpcPriceBar.value = cpcPriceValue[1];
		//饼状图
		cpcPricePie.title = "Platform CPC Price";
		cpcPricePie.value= cpcPriceValue[2];
		// 线状图
		cpcPriceLine.title = "Platform CPC Price";
		cpcPriceLine.xTitle = "Platform";
		cpcPriceLine.yTitle = "Price";
		cpcPriceLine.yMax = cpcPriceValue[0];
		cpcPriceLine.lineTitle = "Price";
		cpcPriceLine.xLabels = cpcPriceValue[3];
		cpcPriceLine.value = cpcPriceValue[4];
		
		// 拼cpCtr柱状图的数据
		// 计算y轴最大值
		ctrBar.title = "Platform CTR";
		ctrBar.xTitle = "Platform";
		ctrBar.yMax = ctrValue[1];
		ctrBar.labels = xText;
		ctrBar.value = ctrValue[0];
		//饼状图
		ctrPie.title = "Platform CTR";
		ctrPie.value= ctrValue[2];
		// 线状图
		ctrLine.title = "Platform CTR";
		ctrLine.xTitle = "Platform";
		ctrLine.yTitle = "Percent";
		ctrLine.yMax = ctrValue[1];
		ctrLine.lineTitle = "Percent";
		ctrLine.xLabels = ctrValue[3];
		ctrLine.value = ctrValue[4];
		total[1] = total[4]!=0?(total[0]/(total[4])):0;
		total[2] = total[5]!=0?(total[0]/(total[5])):0;
		total[3] = total[4]!=0?(total[5]/(total[4]*10)):0;
		tableTotal = "<td align='center'><strong>Total</strong></td>"+
		    "<td align='center'>"+HRMCommon.formatNumber(parseFloat(total[0]).toFixed(0))+"</td><td align='center'>"+parseFloat(total[1]).toFixed(2)+"</td><td align='center'>"+parseFloat(total[2]).toFixed(2)+"</td>"+
		    "<td align='center'>"+parseFloat(total[3]).toFixed(2)+"</td><td align='center'>"+HRMCommon.formatNumber(parseFloat(total[4]).toFixed(0))+"</td><td align='center'>"+HRMCommon.formatNumber(parseFloat(total[5]).toFixed(0))+"</td>";
	}
	if(reportType == "brand"){
		tableHead = "<tr>"
		    +"<th align='center'>Brand Name</th>"
		    +"<th align='center'>Spend</th>"
		    +"<th align='center'>CPM</th>"
		    +"<th align='center'>CPC</th>"
		    +"<th align='center'>CTR(%)</th>"
		    +"<th align='center'>Impression(K)</th>"
		    +"<th align='center'>Click</th>"
		    +"</tr>";
		var total = new Array(0, 0, 0, 0, 0);
		
		// 0:spending总和, 1:cmpPrice总和, 2:cpcPrice总和
		var totalBrandPercent = [0,0,0];
		// 求总和，显示饼状图百分比
		for (var i=0;i<array.length;i++) {
			var rowData = array[i];
			totalBrandPercent[0] += rowData.cpExecPrice;
			totalBrandPercent[1] += rowData.cpExecPvPrice;
			totalBrandPercent[2] += rowData.cpExecCkPrice;
		}
		
		for(var i=0;i<array.length;i++) {
			var rowData = array[i];
			tableBody += "<tr><td align='center'><a href='#' onclick='goCampaignReport("+rowData.cpBrandId.id+");' class='listViewTdLinkS1'>"+rowData.cpBrandId.brandName+"</a></td>"+
		    "<td align='center'>"+HRMCommon.formatNumber(parseFloat(rowData.cpExecPrice).toFixed(0))+"</td><td align='center'>"+parseFloat(rowData.cpExecPvPrice).toFixed(2)+"</td><td align='center'>"+parseFloat(rowData.cpExecCkPrice).toFixed(2)+"</td>"+
		    "<td align='center'>"+parseFloat(rowData.cpCtr).toFixed(2)+"</td><td align='center'>"+HRMCommon.formatNumber(parseFloat(rowData.cpExecPv).toFixed(0))+"</td><td align='center'>"+HRMCommon.formatNumber(parseFloat(rowData.cpExecCk).toFixed(0))+"</td></tr>";
			total[0] += rowData.cpExecPrice;
		    total[1] += rowData.cpExecPvPrice;
		    total[2] += rowData.cpExecCkPrice;
		    total[3] += rowData.cpExecPv;
		    total[4] += rowData.cpExecCk;
		    
		    xText[i] = rowData.cpBrandId.brandName;
		    // 组装数据
		    spendingValue[0][i] = {"value":rowData.cpExecPrice,"tip":rowData.cpBrandId.brandName+"<br>"+(rowData.cpExecPrice*100/totalBrandPercent[0]).toFixed(2)+"%","label":(rowData.cpExecPrice*100/totalBrandPercent[0]).toFixed(2)+"%","text":rowData.cpBrandId.brandName};
		    if (rowData.cpExecPrice > spendingValue[1]) spendingValue[1] = rowData.cpExecPrice;
		    spendingValue[2][i] = {"top":rowData.cpExecPrice,"colour":pieColorArray[i]};
		    spendingValue[3][i] = rowData.cpBrandId.brandName;
		    spendingValue[4][i] = rowData.cpExecPrice;
		    
		    if (rowData.cpExecPvPrice > cpmPriceValue[0]) cpmPriceValue[0] = rowData.cpExecPvPrice;
		    cpmPriceValue[1][i] = {"top":rowData.cpExecPvPrice,"colour":pieColorArray[i]};
		    cpmPriceValue[2][i] = {"value":rowData.cpExecPvPrice,"tip":rowData.cpBrandId.brandName+"<br>"+(rowData.cpExecPvPrice*100/totalBrandPercent[1]).toFixed(2)+"%","label":(rowData.cpExecPvPrice*100/totalBrandPercent[1]).toFixed(2)+"%","text":rowData.cpBrandId.brandName};
		    cpmPriceValue[3][i] = rowData.cpBrandId.brandName;
		    cpmPriceValue[4][i] = rowData.cpExecPvPrice;

		    if (rowData.cpExecCkPrice > cpcPriceValue[0]) cpcPriceValue[0] = rowData.cpExecCkPrice;
		    cpcPriceValue[1][i] = {"top":rowData.cpExecCkPrice,"colour":pieColorArray[i]};
		    cpcPriceValue[2][i] = {"value":rowData.cpExecCkPrice,"tip":rowData.cpBrandId.brandName+"<br>"+(rowData.cpExecCkPrice*100/totalBrandPercent[2]).toFixed(2)+"%","label":(rowData.cpExecCkPrice*100/totalBrandPercent[2]).toFixed(2)+"%","text":rowData.cpBrandId.brandName};
		    cpcPriceValue[3][i] = rowData.cpBrandId.brandName;
		    cpcPriceValue[4][i] = rowData.cpExecCkPrice;
		    
		    ctrValue[0][i] = {"top":rowData.cpCtr,"colour":pieColorArray[i]};
		    if (rowData.cpCtr > ctrValue[1]) ctrValue[1] = rowData.cpCtr;
		    ctrValue[2][i] = {"value":rowData.cpCtr,"tip":rowData.cpBrandId.brandName+"<br>"+rowData.cpCtr+"%","label":rowData.cpCtr+"%","text":rowData.cpBrandId.brandName};
		    ctrValue[3][i] = rowData.cpBrandId.brandName;
		    ctrValue[4][i] = rowData.cpCtr;
		}
		// 拼spending饼状图的数据
		spendingPie.title = "Brand Spend";
		spendingPie.value = spendingValue[0];
		// 柱状图
		spendingBar.title = "Brand Spend";
		spendingBar.xTitle = "Brand";
		spendingBar.yMax = spendingValue[1];
		spendingBar.labels = xText;
		spendingBar.value = spendingValue[2];
		// 线状图
		spendingLine.title = "Brand Spend";
		spendingLine.xTitle = "Brand";
		spendingLine.yTitle = "Spend";
		spendingLine.yMax = spendingValue[1];
		spendingLine.lineTitle = "Spend";
		spendingLine.xLabels = spendingValue[3];
		spendingLine.value = spendingValue[4];

		// 拼cpmPrice柱状图的数据
		// 计算y轴最大值
		cpmPriceBar.title = "Brand CPM Price";
		cpmPriceBar.xTitle = "Brand";
		cpmPriceBar.yMax = cpmPriceValue[0];
		cpmPriceBar.labels = xText;
		cpmPriceBar.value = cpmPriceValue[1];
		//饼状图
		cpmPricePie.title = "Brand CPM Price";
		cpmPricePie.value= cpmPriceValue[2];
		// 线状图
		cpmPriceLine.title = "Brand CPM Price";
		cpmPriceLine.xTitle = "Brand";
		cpmPriceLine.yTitle = "Price";
		cpmPriceLine.yMax = cpmPriceValue[0];
		cpmPriceLine.lineTitle = "Price";
		cpmPriceLine.xLabels = cpmPriceValue[3];
		cpmPriceLine.value = cpmPriceValue[4];

		// 拼cpcPrice柱状图的数据
		// 计算y轴最大值
		cpcPriceBar.title = "Brand CPC Price";
		cpcPriceBar.xTitle = "Brand";
		cpcPriceBar.yMax = cpcPriceValue[0];
		cpcPriceBar.labels = xText;
		cpcPriceBar.value = cpcPriceValue[1];
		//饼状图
		cpcPricePie.title = "Brand CPC Price";
		cpcPricePie.value= cpcPriceValue[2];
		// 线状图
		cpcPriceLine.title = "Brand CPC Price";
		cpcPriceLine.xTitle = "Brand";
		cpcPriceLine.yTitle = "Price";
		cpcPriceLine.yMax = cpcPriceValue[0];
		cpcPriceLine.lineTitle = "Price";
		cpcPriceLine.xLabels = cpcPriceValue[3];
		cpcPriceLine.value = cpcPriceValue[4];
		
		// 拼cpCtr柱状图的数据
		// 计算y轴最大值
		ctrBar.title = "Brand CTR";
		ctrBar.xTitle = "Brand";
		ctrBar.yMax = ctrValue[1];
		ctrBar.labels = xText;
		ctrBar.value = ctrValue[0];
		//饼状图
		ctrPie.title = "Brand CTR";
		ctrPie.value= ctrValue[2];
		// 线状图
		ctrLine.title = "Brand CTR";
		ctrLine.xTitle = "Brand";
		ctrLine.yTitle = "Percent";
		ctrLine.yMax = ctrValue[1];
		ctrLine.lineTitle = "Percent";
		ctrLine.xLabels = ctrValue[3];
		ctrLine.value = ctrValue[4];
		total[1] = total[3]!=0?(total[1]/(total[3])):0;
		total[2] = total[4]!=0?(total[2]/(total[4])):0;
		total[5] = total[3]!=0?(total[4]/(total[3]*10)):0;
		tableTotal = "<td align='center'><strong>Total</strong></td>"+
	    "<td align='center'>"+HRMCommon.formatNumber(parseFloat(total[0]).toFixed(0))+"</td><td align='center'>"+parseFloat(total[1]).toFixed(2)+"</td><td align='center'>"+parseFloat(total[2]).toFixed(2)+"</td>"+
	    "<td align='center'>"+parseFloat(total[5]).toFixed(2)+"</td><td align='center'>"+HRMCommon.formatNumber(parseFloat(total[3]).toFixed(0))+"</td><td align='center'>"+HRMCommon.formatNumber(parseFloat(total[4]).toFixed(0))+"</td>";
	}
	if(reportType == "mediaformat"){
		tableHead = "<tr>"
		    +"<th align='center'>Media Format</th>"
		    +"<th align='center'>Spend</th>"
		    +"<th align='center'>CPM</th>"
		    +"<th align='center'>CPC</th>"
		    +"<th align='center'>CTR(%)</th>"
		    +"</tr>";
		var total = new Array(0, 0, 0, 0, 0, 0);
		
		// 0:spending总和, 1:cmpPrice总和, 2:cpcPrice总和
		var totalMediaPercent = [0,0,0];
		// 求总和，显示饼状图百分比
		for (var i=0;i<array.length;i++) {
			var rowData = array[i];
			totalMediaPercent[0] += rowData.cpExecPrice;
			totalMediaPercent[1] += rowData.cpExecPvPrice;
			totalMediaPercent[2] += rowData.cpExecCkPrice;
		}
		
        for(var i=0;i<array.length;i++) {
        	var rowData = array[i];
			tableBody += "<tr><td align='center'>"+rowData.plmFormat+"</td>"+
		    "<td align='center'>"+HRMCommon.formatNumber(parseFloat(rowData.cpExecPrice).toFixed(2))+"</td>"+
		    "<td align='center'>"+parseFloat(rowData.cpExecPvPrice).toFixed(2)+"</td><td align='center'>"+parseFloat(rowData.cpExecCkPrice).toFixed(2)+"</td><td align='center'>"+parseFloat(rowData.cpCtr).toFixed(2)+"</td></tr>";
			total[0] += rowData.cpExecPrice;
			total[1] += rowData.cpCtr;
			total[2] += rowData.cpExecPvPrice;
			total[3] += rowData.cpExecCkPrice;
			total[4] += rowData.cpExecPv;
			total[5] += rowData.cpExecCk;
			
			xText[i] = rowData.plmFormat;
			// 组装数据
		    spendingValue[0][i] = {"value":rowData.cpExecPrice,"tip":rowData.plmFormat+"<br>"+(rowData.cpExecPrice*100/totalMediaPercent[0]).toFixed(0)+"%","label":(rowData.cpExecPrice*100/totalMediaPercent[0]).toFixed(0)+"%","text":rowData.plmFormat};
		    if (rowData.cpExecPrice > spendingValue[1]) spendingValue[1] = rowData.cpExecPrice;
		    spendingValue[2][i] = {"top":rowData.cpExecPrice,"colour":pieColorArray[i]};
		    spendingValue[3][i] = rowData.plmFormat;
		    spendingValue[4][i] = rowData.cpExecPrice;
		    
		    if (rowData.cpExecPvPrice > cpmPriceValue[0]) cpmPriceValue[0] = rowData.cpExecPvPrice;
		    cpmPriceValue[1][i] = {"top":rowData.cpExecPvPrice,"colour":pieColorArray[i]};
		    cpmPriceValue[2][i] = {"value":rowData.cpExecPvPrice,"tip":rowData.plmFormat+"<br>"+(rowData.cpExecPvPrice*100/totalMediaPercent[1]).toFixed(0)+"%","label":(rowData.cpExecPvPrice*100/totalMediaPercent[1]).toFixed(0)+"%","text":rowData.plmFormat};
		    cpmPriceValue[3][i] = rowData.mediacatName;
		    cpmPriceValue[4][i] = rowData.cpExecPvPrice;

		    if (rowData.cpExecCkPrice > cpcPriceValue[0]) cpcPriceValue[0] = rowData.cpExecCkPrice;
		    cpcPriceValue[1][i] = {"top":rowData.cpExecCkPrice,"colour":pieColorArray[i]};
		    cpcPriceValue[2][i] = {"value":rowData.cpExecCkPrice,"tip":rowData.plmFormat+"<br>"+(rowData.cpExecCkPrice*100/totalMediaPercent[2]).toFixed(0)+"%","label":(rowData.cpExecCkPrice*100/totalMediaPercent[2]).toFixed(0)+"%","text":rowData.plmFormat};
		    cpcPriceValue[3][i] = rowData.plmFormat;
		    cpcPriceValue[4][i] = rowData.cpExecCkPrice;
		    
		    ctrValue[0][i] = {"top":rowData.cpCtr,"colour":pieColorArray[i]};
		    if (rowData.cpCtr > ctrValue[1]) ctrValue[1] = rowData.cpCtr;
		    ctrValue[2][i] = {"value":rowData.cpCtr,"tip":rowData.plmFormat+"<br>"+rowData.cpCtr+"%","label":rowData.cpCtr+"%","text":rowData.plmFormat};
		    ctrValue[3][i] = rowData.plmFormat;
		    ctrValue[4][i] = rowData.cpCtr;
		}
		// 拼spending饼状图的数据
		spendingPie.title = "Media Format Spend";
		spendingPie.value = spendingValue[0];
		// 柱状图
		spendingBar.title = "Media Format Spend";
		spendingBar.xTitle = "Media Format";
		spendingBar.yMax = spendingValue[1];
		spendingBar.labels = xText;
		spendingBar.value = spendingValue[2];
		// 线状图
		spendingLine.title = "Media Format Spend";
		spendingLine.xTitle = "Media Format";
		spendingLine.yTitle = "Spend";
		spendingLine.yMax = spendingValue[1];
		spendingLine.lineTitle = "Spend";
		spendingLine.xLabels = spendingValue[3];
		spendingLine.value = spendingValue[4];

		// 拼cpmPrice柱状图的数据
		// 计算y轴最大值
		cpmPriceBar.title = "Media Format CPM Price";
		cpmPriceBar.xTitle = "Media Format";
		cpmPriceBar.yMax = cpmPriceValue[0];
		cpmPriceBar.labels = xText;
		cpmPriceBar.value = cpmPriceValue[1];
		//饼状图
		cpmPricePie.title = "Media Format CPM Price";
		cpmPricePie.value= cpmPriceValue[2];
		// 线状图
		cpmPriceLine.title = "Media Format CPM Price";
		cpmPriceLine.xTitle = "Media Format";
		cpmPriceLine.yTitle = "Price";
		cpmPriceLine.yMax = cpmPriceValue[0];
		cpmPriceLine.lineTitle = "Price";
		cpmPriceLine.xLabels = cpmPriceValue[3];
		cpmPriceLine.value = cpmPriceValue[4];

		// 拼cpcPrice柱状图的数据
		// 计算y轴最大值
		cpcPriceBar.title = "Media Format CPC Price";
		cpcPriceBar.xTitle = "Media Format";
		cpcPriceBar.yMax = cpcPriceValue[0];
		cpcPriceBar.labels = xText;
		cpcPriceBar.value = cpcPriceValue[1];
		//饼状图
		cpcPricePie.title = "Media Format CPC Price";
		cpcPricePie.value= cpcPriceValue[2];
		// 线状图
		cpcPriceLine.title = "Media Format CPC Price";
		cpcPriceLine.xTitle = "Media Format";
		cpcPriceLine.yTitle = "Price";
		cpcPriceLine.yMax = cpcPriceValue[0];
		cpcPriceLine.lineTitle = "Price";
		cpcPriceLine.xLabels = cpcPriceValue[3];
		cpcPriceLine.value = cpcPriceValue[4];
		
		// 拼cpCtr柱状图的数据
		// 计算y轴最大值
		ctrBar.title = "Media Format CTR";
		ctrBar.xTitle = "Media Format";
		ctrBar.yMax = ctrValue[1];
		ctrBar.labels = xText;
		ctrBar.value = ctrValue[0];
		//饼状图
		ctrPie.title = "Media Format CTR";
		ctrPie.value= ctrValue[2];
		// 线状图
		ctrLine.title = "Media Format CTR";
		ctrLine.xTitle = "Media Format";
		ctrLine.yTitle = "Percent";
		ctrLine.yMax = ctrValue[1];
		ctrLine.lineTitle = "Percent";
		ctrLine.xLabels = ctrValue[3];
		ctrLine.value = ctrValue[4];
        total[1] = total[4]!=0?(total[5]/(total[4]*10)):0;
        total[2] = total[4]!=0?(total[0]/(total[4])):0;
		total[3] = total[5]!=0?(total[0]/(total[5])):0;
        tableTotal = "<td align='center'><strong>Total</strong></td>"+
	    "<td align='center'>"+HRMCommon.formatNumber(parseFloat(total[0]).toFixed(0))+"</td>"+
	    "<td align='center'>"+parseFloat(total[2]).toFixed(2)+"</td><td align='center'>"+parseFloat(total[3]).toFixed(2)+"</td><td align='center'>"+parseFloat(total[1]).toFixed(2)+"</td>";
	}
	
	$('#campaignHead').html(tableHead);
	$('#campaignBody').html(tableBody);
	$('#campaignTotal').html(tableTotal);
	
	// 初始化表体下方的文本；
	initTitle();
}