//国际化
var colName    = UQJsText.flashshow.colName;
var reportName = UQJsText.flashshow.reportName;
var labelName = UQJsText.flashshow.labelName;
var contextPath = "http://localhost:8080/mex";
String.prototype.trim = function() {
	return this.replace(/(^\s*)|(\s*$)/g, "");
}
var jsonresult;

var jsonresult2;

// 分隔符
var separator="分隔符";

function setContextPath(_contextPath) {
	contextPath = _contextPath;
}

function giveData() {
	return   JSON.stringify(jsonresult);
}

function giveData2() {
	return JSON.stringify(jsonresult2);
}

function isNotNull(data){
	if(data!=null&&data.toString().length>0){
		return true
	}
	return false;
}

function isNull(data){
	return !isNotNull(data);
}

function sleep(numberMillis) {
	var now = new Date();
	var exitTime = now.getTime() + numberMillis;
	while (true) {
		now = new Date();
		if (now.getTime() > exitTime)
			return;
	}
}

function changeData(divId,json) {
	var tmp = findSWF(divId);
	tmp.load(json);
}

function findSWF(movieName) {
	if (window[movieName] != null) 
		return window[movieName];
	return document[movieName];
}

function initSelectTarget(){
	
	var $type = $("input[name='actualType']").attr("value");
	var html="";
	if($type==3){
		html=getSelectItemSet("doubleBar");
	}else if($type=='rtb'){
		return;
	}else{
		html=getSelectItemSet("bar");
	}
	$('#sel_target').html(html);
	
	
	
}

function changeSelectTarget(type){
	var	html=getSelectItemSet(type);
	$('#sel_target').html(html);
}

function totalSummary(){
	if($('#actualType').val() == '3'){
		return;
	}
	var str = "";
	$('.siteTabCom > tbody').find('tr:last').find('td').each(function(i,t){
		str += $(t).text().replace(/\s/g,'');
		str += '|';
	});
	if(str.length > 0){
		str = str.substring(0,str.length - 1);
		$('input[name="totalSummary"]').attr('value',str);
	}
}

function returnMap(type){
	var map = new Map();
	switch(type){
	case 'bar':
		map.put(colName[0],"3,Spend");
		map.put(colName[1],"4,1,Impression");
		map.put(colName[2],"6,2,Click");
		map.put("CTR","12,5,CTR");
		map.put("UV","8,3,UV");
		map.put("Visits","10,4,Visits");
		map.put("CPM","14,6,CPM");
		map.put("CPC","16,7,CPC");
		map.put("CPUV","18,8,CPUV");
		map.put("CPV","20,9,CPV");
		break;
	case 'pie':
		map.put(colName[0],"3,Spend");
		map.put(colName[1],"4,1,Impression");
		map.put(colName[2],"6,2,Click");
		map.put("UV","12,5,UV");
		map.put("Visits","10,4,Visits");
		break;
	case 'doublePie':
		map.put(colName[1],"4,1,Impression");
		map.put(colName[2],"6,2,Click");
		map.put("UV","12,5,UV");
		map.put("Visits","10,4,Visits");
		break;
	case 'doubleBar':
		map.put(colName[1],"4,1,Impression");
		map.put(colName[2],"6,2,Click");
		map.put("CTR","12,5,CTR");
		map.put("UV","8,3,UV");
		map.put("Visits","10,4,Visits");
		map.put("CPM","14,6,CPM");
		map.put("CPC","16,7,CPC");
		map.put("CPUV","18,8,CPUV");
		map.put("CPV","20,9,CPV");
		break;
	}
	return map;
}

var colorSets = ["#f75959","#f8b556","#e8f15b","#89e967","#67e9e1","#7d75e9","#d373de"];

function getSelectItemSet(type){
	var map = returnMap(type);
	var array = map.keys();
	var html = "";
	var arg,n,m;
	for(var j = 0; j < array.length; j++){
		arg = map.get(array[j]) + "";
		n = arg.substring(0,arg.lastIndexOf(","));
		m = arg.substring(arg.lastIndexOf(",") + 1);
		html+="<option value='" + n + "' flag='" + m + "'>" + array[j] + "</option>";
	}
	return html;
}

function changeTitle(){
	$('#actualVSBudgetMark').remove();
	var $type=$('#actualType').attr('value');
	var $report=$('#reportType').attr('value');
	var html="";
	switch ($report) {
	case 'Campaign':
		html+= reportName[0];
		break;
	case 'Media':
		html+= reportName[1];
		break;
	case 'Placement':
		html+= reportName[2];
		break;
	case 'Benchmark':
		html+= reportName[3];
		break;
	case 'rtbCampaign':
		html+= reportName[4];
		break;
	}
	html += "&nbsp;&nbsp;&nbsp;&nbsp;";
	html += reportName[8];
	switch ($type) {
	case '1':
		html+= reportName[5];
		break;
	case '2':
		html+= reportName[6];
		break;
	case '3':
		html+= reportName[7];
		html+="<span id='actualVSBudgetMark' style='width:10px;height:10px;font-weight:normal'>&nbsp;&nbsp;&nbsp;&nbsp;(<span class='redMark'></span>&nbsp;"+ reportName[5] +"&nbsp;&nbsp;<span class='blueMark'></span>&nbsp;"+ reportName[6] +")</span>";
		break;
	case 'rtb':
		break;
	}
	$('#reTit').html(html);
}

var len=0;
$(function() {
	
	changeTitle();
	
	$('#benchmarkReportSearchBtn').click(function(){
		var result=validateForm();
		if(result==true){
			var advertiserId = $('#organizationAdvertiserId :selected').val();
			$('#advertiserId').attr('value',advertiserId);
			$('#benchmarkReportForm').attr("action",
					contextPath + "/report/dashboard/benchmarkreport");
			$('#benchmarkReportForm').submit();
			changeTitle();
		}
	});
	
	$('#placementReportSearchBtn').click(
			function() {
				var result=validateForm();
				if(result==true){
					var siteId=$('#currentSiteId').attr("value");
					var advertiserId = $('#organizationAdvertiserId :selected').val();
					$('#advertiserId').attr('value',advertiserId);
						$('#placementReportForm').attr("action",
								contextPath + "/report/dashboard/mediareport/placementreport");
						$('#placementReportForm').submit();
						changeTitle();
					
				}
			})

	$('#campaignReportSearchBtn').click(
			function() {
				var result=validateForm();
				if(result==true){
					var advertiserId = $('#organizationAdvertiserId :selected').val();
					$('#advertiserId').attr('value',advertiserId);
					$('#campaignReportForm').attr("action",
							contextPath + "/report/dashboard/campaignreport");
					$('#campaignReportForm').submit();
					changeTitle();
				}
			});

	$('#mediaReportSearchBtn').click(
			function() {
				var result=validateForm();
				if(result==true){
					var advertiserId = $('#organizationAdvertiserId :selected').val();
					$('#advertiserId').attr('value',advertiserId);
					$('#mediaReportForm').attr("action",
							contextPath + "/report/dashboard/mediareport");
					$('#mediaReportForm').submit();
					changeTitle();
				}
			});

/*	$('#hourlyReportSearchBtn').click(
			function() {
				var result=validateForm();
				if(result==true){
					var advertiserId = $('#organizationAdvertiserId :selected').val();
					$('#advertiserId').attr('value',advertiserId);
					$('#searchId').attr('value',1);
					$('#hourlyReportForm').attr("action",
							contextPath + "/report/dashboard/hourlyreport/campaign");
					$('#hourlyReportForm').submit();
				}
			});*/
	
	$('#sel_target').change(function() {
		$('#showFlaBox').find('#toImage').find('li:eq(1)').css('display','');
		var $type = $("input[name='actualType']").attr("value");
		var $text = $.trim($('#sel_target :selected').text());
		var $index = $('#toImage li.hov').index();
		var arr = [];
		if ($type == 3) {
			arr = returnMap("doublePie").keys();
		} else {
			arr = returnMap("pie").keys();
		}
		if(arr.contains($text) == false){
			$('#showFlaBox').find('#toImage').find('li:eq(1)').css('display','none');
		}else{
			$('#showFlaBox').find('#toImage').find('li:eq(1)').css('display','');
		}
		
		if ($type == 3) {
			if($index == 0 || $('#showFlaBox').find('#toImage').find('li:eq(1)').css('display') == 'none'){
				image2("doubleBar");
			}else{
				image2("doublePie");
			}
		}else{
			if($index == 0 || $('#showFlaBox').find('#toImage').find('li:eq(1)').css('display') == 'none'){
				image2("bar");
			}else{
				image2("pie");
			}
		}
		
	});

	$('#createBar').click(function() {
		var $type = $("input[name='actualType']").attr("value");
		if ($type == 3) {
			image2("doubleBar");
		} else {
			image2("bar");
		}
	});

	$('#createPie').click(function() {
		var $type = $("input[name='actualType']").attr("value");
		if ($type == 3) {
			image2("doublePie");
		} else {
			image2("pie");
		}
	});
	

	// 发送 邮件  中 的  include me 事件 ,点击添加当前登录用户的email到收件人列表
	$('#inemail').click(function(){
		 var tos= $('#emailTos').attr('value').replace(/\s/g,'');
		 var  useremail= $('#useremail').val();
		 var emailto; 
		 if(tos.indexOf(useremail)==-1){
		 	 if(tos!=""&&tos.charAt(tos.length-1) == ','&&tos.charAt(tos.length-1) != '，'){
			   emailto=tos+useremail+",";
			 }else if(tos!=""&&tos.charAt(tos.length-1) != ','&&tos.charAt(tos.length-1) != '，'){
			   emailto=tos+","+useremail+",";
			 }else if(tos!=""&&tos.charAt(tos.length-1) == '，'&&tos.charAt(tos.length-1) != ',') {
				  emailto=tos+useremail+",";
			 }else{
				 emailto=useremail+",";
			 }
			   $('#emailTos').attr('value',emailto);
		 }else{
			 return ;
		 }
	
	});
	
	
	
	// 发送Email
	$('#gotoSendMail').click(function(){
		$('#noemail').hide();
		$('#erroremail').hide();
		var tos= $('#emailTos').attr('value').replace('，',',').replace('/\s/g','');
		if(tos.charAt(tos.length-1) == ','){
			tos = tos.substring(0,tos.length-1);
		}
		if(tos==""){
			$('#noemail').show();
			return;
		}else{
			$('#noemail').hide();
		}
		var emails =tos.split(',');
		var checkEmailOK=true;
		for(var l=0;l<emails.length;l++){
			checkEmailOK=ReportUtils.checkEmail(emails[l]);
			if(tos!=""&&!checkEmailOK){
				$('#erroremail').show();
				return;
			}else{
				$('#erroremail').hide();
			}
		}
		
		var title=$('#emailTitle').attr('value');
		var comments=$('#emailComments').attr('value');
		var type=0;
		$("input[name='mail_atta']").each(function(){
			if($(this).attr('checked')==true){
				type=$(this).val();
			}
		});
		$('#emTos').attr('value',tos);
		$('#emTitle').attr('value',title);
		$('#emComments').attr('value',comments);
		totalSummary();
		var isEmpty= $('#resultEmptyFlag').attr('value');
		if(type>0&&isEmpty==0){
			
			if(type==1){
				PDF();
			}
			
			$('#emAttachment').attr('value',type);
		}
		var $form=$('.reportForm');
		var $action = "";
		if($form.attr('id')=='benchmarkReportForm'){
			$action =contextPath + "/report/dashboard/benchmarkreport/sendEmail";
		}else if($form.attr('id')=='campaignReportForm'){
			$action =contextPath + "/report/dashboard/campaignreport/sendEmail";
		}else if($form.attr('id')=='placementReportForm'){
			var $siteId=$('#currentSiteId').attr('value');
			$action =contextPath + "/report/dashboard/placementreport/sendEmail?siteId="+$siteId;
		}else if($form.attr('id')=='mediaReportForm'){
			$action =contextPath + "/report/dashboard/mediareport/sendEmail";
		}else if($form.attr("id") == 'hourlyReportForm'){
			$action = contextPath + "/report/dashboard/hourlyreport/sendEmail";
		}
		
		if(isNotNull($action)){
			
			$form.attr('action',$action);
			$form.submit();
		}
		overDiv.hideBox('emailBox');
		overDiv.waitShow();
		$('#hiddenLoadingTextField').show().focus();
		$('#hiddenLoadingTextField').blur(function(){
		    $('#hiddenLoadingTextField').hide();
			overDiv.waitHide();
		});
	})
	
	
	/*
	 * 生成Excel报表
	 */
	$('#exportToExcel').click(function() {
		var isEmpty= $('#resultEmptyFlag').attr('value');
		if(isEmpty==1){
			return;
		}
		overDiv.waitShow();
		$('#hiddenLoadingTextField').show().focus();
		$('#hiddenLoadingTextField').blur(function(){
		    $('#hiddenLoadingTextField').hide();
			overDiv.waitHide();
		});
		
		var $form = $('.reportForm');
		var $action = "";
		if ($form.attr("id") == 'campaignReportForm') {
			$action = contextPath + "/report/dashboard/campaignreport/exportToExcel";
		} else if ($form.attr("id") == 'mediaReportForm') {
			$action = contextPath + "/report/dashboard/mediareport/exportToExcel";
		}else if($form.attr("id") == 'placementReportForm'){
			$action = contextPath + "/report/dashboard/mediareport/placementreport/exportToExcel";
		}else if($form.attr("id") == 'benchmarkReportForm'){
			$action = contextPath + "/report/dashboard/benchmarkreport/exportToExcel";
		}else if($form.attr("id") == 'hourlyReportForm'){
			$action = contextPath + "/report/dashboard/hourlyreport/exportToExcel";
		}
		
		totalSummary();
		$form.attr("action", $action);
		$form.submit();
	});
	


	/*
	 * 生成PDF报表
	 */
	$('#exportToPDF').click(
			function() {
				var isEmpty= $('#resultEmptyFlag').attr('value');
				if(isEmpty==1){
					return;
				}
	
				
				totalSummary();
				PDF();
				
				overDiv.waitShow();
				$('#hiddenLoadingTextField').show().focus();
				$('#hiddenLoadingTextField').blur(function(){
				    $('#hiddenLoadingTextField').hide();
					overDiv.waitHide();
				});
				
				
				$form=$('.reportForm');
				 	if($form.attr('id')=='mediaReportForm'){
				 		$form.attr('action',contextPath+"/report/dashboard/mediareport/exportToPDF");
				 	}else if($form.attr('id')=='campaignReportForm'){
				 		$form.attr('action',contextPath+"/report/dashboard/campaignreport/exportToPDF");
				 	}else if($form.attr('id')=='placementReportForm'){
				 		$form.attr('action',contextPath+"/report/dashboard/mediareport/placementreport/exportToPDF");
				 	}else if($form.attr('id')=='benchmarkReportForm'){
				 		$form.attr('action',contextPath+"/report/dashboard/benchmarkreport/exportToPDF");
				 	}
				 $form.submit();
				 
				$('#showFlaBox').find('.FlashClass').remove();
				$('#showFlaBox').find('.clear0').remove();
				$('#showFlaBox').find('object').remove();
					$("<div class='clear0'></div><div class='FlashClass' id='FlashDiv'></div>").insertAfter($('#toImage'));
					var $flag = $("input[name='resultEmptyFlag']").attr("value");
					var $type = $("input[name='actualType']").attr("value");
					if($flag==0){
						if($type==3){
							$('#sel_target option:eq(1)').attr('selected','selected');
							image2('doubleBar');
						}else{
							image2('bar');
						}
					}
			});
			
		initSelectTarget();

})

function PDF(){
	
	$('#exportPDFImage > img').remove();
	$('#exportPDFImage > div').remove();
	
	var $type = $("input[name='actualType']").attr("value");
	var $n=14;
	if($type==3){
		$n=18;
	}else if($type=='rtb'){
		$n=9;
	}
	
	for(var k=1;k<=$n;k++){
		$('#exportPDFImage').append("<div id='divBar"+k+"'></div>");
	}
	
	var arr1 = createAllBar();
	var arr2= createAllPie();
	var array1=arr1.split(separator);
	var array2=arr2.split(separator);
	var i=0;
	// 遍历柱状图字符串数组
	for(;i<array1.length;i++){
		var json=array1[i];
		if(isNull(json)){
			continue;
		}
		changeData('FlashDiv', json);
		OFC.jquery.rasterize("FlashDiv","divBar"+(i+1));
	}
	// 遍历饼图字符串数组
	var array_other=[];
	var l=0;
	for(i=0;i<array2.length;i++){
		var json=array2[i];
		if(isNull(json)){
			continue;
		}
		if($type==3&&json.indexOf('Uniqlick')>=0){
			var _tmp=json.split('Uniqlick');
			changeData('FlashDiv', _tmp[0]);
			OFC.jquery.rasterize("FlashDiv","divBar"+(i+array1.length+1));
			array_other[l++]= _tmp[1];
		}else{
			changeData('FlashDiv', json);
			OFC.jquery.rasterize("FlashDiv","divBar"+(i+array1.length+1));
		}
	}
	if(array_other.length>0){
		var t= array1.length+array2.length;
		for(var q=0;q<array_other.length;q++){
			var json=array_other[q];
			changeData('FlashDiv', json);
			OFC.jquery.rasterize("FlashDiv","divBar"+(t+q+1));
		}
	}
	var $imgBinaryStr="";
	$('#exportPDFImage > img').each(function(j,item){
		$imgBinaryStr+=$(item).attr('src');
		if(j!==$('.exportPDFImage > img').length-1){
			$imgBinaryStr+="Uniqlick";
		}
	})
	
	$('#binaryImages').attr('value',$imgBinaryStr);
	$('#exportPDFImage').css('display','none');
}

var length=0;

function createAllPie() {
	var indexArray = [ colName[0], colName[1], colName[2], "UV" ];
	var $type = $("input[name='actualType']").attr("value");
	var dataString ="";
	var a = "";
	for ( var i = 0; i <indexArray.length; i++) {
		if ($type == 3) {
			a = image3("doublePie", indexArray[i], i +1);
		} else {
			a = image3("pie", indexArray[i], i+1 );
		}
		dataString += a;
		if(i!=indexArray.length-1){
			dataString+=separator;
		}
		len++;
	}
	return dataString;
}

var globalMap;

function createAllBar() {
	$('.FlashClass').remove();
	var indexArray = [];
	var $type = $("input[name='actualType']").attr("value");
	if($type==3){
		globalMap = returnMap('doubleBar');
		indexArray = [ colName[1], colName[2], "CTR","UV", "Visits", "CPM", "CPC", "CPUV", "CPV" ];
	}else{
		globalMap = returnMap('bar');
		indexArray = [ colName[0], colName[1], colName[2], "CTR","UV", "Visits", "CPM", "CPC", "CPUV", "CPV" ];
	}
	var dataString ="";
	var a = "";
	for ( var i = 0; i < indexArray.length; i++) {
		if ($type == 3) {
			a = image3("doubleBar", indexArray[i], i + 1);
		} else {
			a = image3("bar", indexArray[i], i + 1);
		}
		if(isNull(a)){
			continue;
		}
		dataString += a;
		if(i!=indexArray.length-1){
			dataString+=separator;
		}
		len++;
	}
	return dataString;
}

function image3(type, index, flag) {
	var $selText = index;
	var $type = $("input[name='actualType']").attr("value");
	var ulToggle = $('#toImage');
	ulToggle.show().find('li').removeClass('hov');
	if (type == 'doubleBar') {
		if ($selText == colName[0]) {
			return "";
		}
	}
	if (type == 'pie') {
		if ($selText !== colName[0] && $selText !== colName[1]
				&& $selText !== colName[2] && $selText !== 'UV') {
			return "";
		}
	}
	if (type == 'doublePie') {
		if ($selText !== colName[1] && $selText !== colName[2]
				&& $selText !== 'UV') {
			return "";
		}
	}
	var n = 0;
	var w=0;
	var $reportType=$('#reportType').val();
	if($reportType.trim()=='Media'|| $reportType.trim() == 'Placement'||$reportType.trim()=='Benchmark'){
		w=1;
	}
	var numReg = /^([\-]?[0-9])|([\-]?[0-9]+\.?[0-9]+)$/;
	var $data = "", $data2 = "", $data3 = "", $campNames = "", arg = "", $actualName = "";
	switch ($type) {
	case '1':
		$actualName = labelName[0];
		$('#campaignTable_act tr:first-child').find("td").each(
				function(i, item) {
					var t = $.trim($(item).attr('flag'));
					if (t.indexOf($selText) >= 0) {
						n = i + 1;
						return false;
					}
				});
		$('#campaignTable_act > tbody').find("tr").each(function() {
			if($(this).hasClass('totalTr')){
				return false;
			}
			$(this).find("td").each(function(j, column) {
				if (j == w) {
					arg = $(column).find('a').text();
					if (arg !== null && arg.trim().length > 0) {
						$campNames += arg.trim() + separator;
					} else {
						$campNames += "Untitled" + separator;
					}
				} else if (j + 1 == n) {
					arg = $(column).text();
					if (arg !== null && arg.trim().length > 0) {
						arg = ReportUtils.getValue(arg.trim());
						$data += arg + separator;
					} else {
						$data += "0" + separator;
					}
				}
			});
		});
		break;
	case '2':
		$actualName = labelName[1];
		ulToggle.show().find('li').removeClass('hov');
		ulToggle.find('li:eq(1)').addClass('hov');
		$('#campaignTable_bud tr:first-child').find("td").each(
				function(i, item) {
					var t = $.trim($(item).attr('flag'));
					if (t.indexOf($selText) >= 0) {
						n = i + 1;
						return false;
					}
				});
		$('#campaignTable_bud > tbody').find("tr").each(function() {
			if($(this).hasClass('totalTr')){
				return false;
			}
			$(this).find("td").each(function(j, column) {
				if (j == w) {
					arg = $(column).find('a').text();
					if (arg !== null && arg.trim().length > 0) {
						$campNames += arg.trim() + separator;
					} else {
						$campNames += "Untitled" + separator;
					}
				} else if (j + 1 == n) {
					arg = $(column).text();
					if (arg !== null && arg.trim().length > 0) {
						arg = ReportUtils.getValue(arg.trim());
						$data += arg + separator;
					} else {
						$data += "0" + separator;
					}
				}
			});
		});
		break;
	case '3':
		$actualName = labelName[2];
		var $arg = globalMap.get($selText).split(",");
		n = $arg[0];
		$('#campaignTable_all > tbody').find("tr").each(function() {
			$(this).find("td").each(function(j, column) {
				if (j == w) {
					arg = $(column).find('a').text();
					if (arg !== null && arg.trim().length > 0) {
						$campNames += arg.trim() + separator;
					} else {
						$campNames += "Untitled" + separator;
					}
				}
			});
		});
		$('#campaignTable_all > tbody tr:even').each(function() {
			$(this).find("td").each(function(j, column) {
				if (j + 1 == n) {
					arg = $(column).text();
					if (arg !== null && arg.trim().length > 0) {
						arg = ReportUtils.getValue(arg.trim());
						$data3 += arg + separator;
					} else {
						$data3 += "0" + separator;
					}
				}
			});
		});

		$('#campaignTable_all > tbody tr:odd').each(function() {
			$(this).find("td").each(function(j, column) {
				if (j + 1 == $arg[1]) {
					arg = $(column).text();
					if (arg !== null && arg.trim().length > 0) {
						arg = ReportUtils.getValue(arg.trim());
						$data2 += arg + separator;
					} else {
						$data2 += "0" + separator;
					}
				}
			});
		});
		$data3 = $data3.substring(0, $data3.length - 3);
		$data2 = $data2.substring(0, $data2.length - 3);
		break;
	}
	$campNames = $campNames.substring(0, $campNames.length - 3);
	if ($type !== '3') {
		$data = $data.substring(0, $data.length - 3);
		if ($data.split(separator).length !== $campNames.split(separator).length) {
			return;
		}
	} else {
		if ($data3.split(separator).length !== $data2.split(separator).length) {
			return;
		}
	}
	var $url = "", $div = "";
	var jsonTmp, jsonString;
	var tmpVar=0;
	switch (type) {
	case 'doublePie':
		var values = [];
		var dsum_1 = 0;
		var d = $data3.split(separator);
		$.each(d, function(j, t) {
			if(numReg.test(t) == false){
				values[j] = '';
			}else{
				values[j] = parseFloat(t);
				dsum_1 += values[j];
			}
		});

		var values2 = [];
		var dsum_2 = 0;
		d = $data2.split(separator);
		$.each(d, function(j, t) {
			if(numReg.test(t) == false){
				values2[j] = '';
			}else{
				values2[j] = parseFloat(t);
				dsum_2 += values2[j];
			}
		});

		var labels = [];
		d = $campNames.split(separator);
		var y = "";
		for ( var j = 0; j < d.length; j++) {
			if (j % 2 != 0) {
				continue;
			} else {
				y += d[j];
			}
			if (j !== d.length - 2) {
				y += "&";
			}
		}
		d = y.split("&");
		$.each(d, function(j, t) {
			labels[j] = t;
		});
		var realVal = [];
		for ( var j = 0; j < labels.length; j++) {
			var val = "";
			var per = "";
			if(dsum_1 !== 0){
				if(numReg.test(values[j]) == true){
					val = parseFloat(values[j]);
					per = val.div(dsum_1).mul(100).toFixed(2) + "%";
				}
			}
			var item = {
					"label" : per,
					"value" : val,
					"font-size" : 11,
					"text":labels[j],
					"tip":labels[j] + "<br>" + (numReg.test(val) == true ? formatMoney(val,0):'')
			}
			realVal[j] = item;
		}
		JSONBuilder.setType("");
		jsonTmp = JSONBuilder.pie(realVal, $selText + "--"
				+ $actualName.split('vs')[0].trim());

		jsonString = JSON.stringify(jsonTmp);
		
		realVal = [];
		for ( var j = 0; j < labels.length; j++) {
			var val = "";
			var per = "";
			if(dsum_1 !== 0){
				if(numReg.test(values2[j]) == true){
					val = parseFloat(values2[j]);
					per = val.div(dsum_2).mul(100).toFixed(2) + "%";
				}
			}
			var item = {
					"label" : per,
					"value" : val,
					"font-size" : 11,
					"text":labels[j],
					"tip":labels[j] + "<br>" + (numReg.test(val) == true ? formatMoney(val,0):'')
			}
			realVal[j] = item;
		}
		

		jsonTmp = JSONBuilder.pie(realVal, $selText + "--"
				+ $actualName.split('vs')[1].trim());
		
		jsonString += "Uniqlick" + JSON.stringify(jsonTmp);
		
		break;
	case 'doubleBar':
		var values = [];
		var d = $data3.split(separator);
		var max = 0;
		$.each(d, function(j, t) {
			if(t.toString()!=='-'){
				values[j] = parseFloat(t);
				tmpVar = parseFloat(t);
				if(tmpVar.toString()=='NaN'){
					tmpVar = 0 ;
				}
				max = max >= tmpVar ? max : tmpVar;
			}else{
				values[j] = null;
			}
		});
		var values2 = [];
		d = $data2.split(separator);
		$.each(d, function(j, t) {
			values2[j] = parseFloat(t);
			tmpVar = parseFloat(t);
			if(tmpVar.toString()=='NaN'){
				tmpVar=0;
			}
			max = max >= tmpVar ? max : tmpVar;
		});
		var labels = [];
		d = $campNames.split(separator);
		var y = "";
		for ( var j = 0; j < d.length; j++) {
			if (j % 2 != 0) {
				continue;
			} else {
				y += d[j];
			}
			if (j !== d.length - 2) {
				y += "&";
			}
		}
		d = y.split("&");
		$.each(d, function(j, t) {
			var qq={
				"text":t,
				"rotate":340
			};
			labels[j] = qq;
		});
		jsonTmp = JSONBuilder.doubleBar($selText, values, values2, labels, max);
		jsonString = JSON.stringify(jsonTmp);
		break;
	case 'bar':
		var values = [];
		var d = $data.split(separator);
		var max = 0;
		$.each(d, function(j, t) {
			if(numReg.test(t) == true){
				values[j] = parseFloat(t);
				tmpVar = parseFloat(t);
				max = max >= tmpVar ? max : tmpVar;
			}else{
				values[j]='';
			}
		});
		var labels = [];
		d = $campNames.split(separator);
		$.each(d, function(j, t) {
			var qq={
				"text":t,
				"rotate":340
			};
			labels[j] = qq;
		});
		
		var realVal = [];
		var g = 0;
		for(var j=0;j<values.length;j++){
			var q={
				"top":values[j],
				"colour":colorSets[g++]
			}
			if(g == colorSets.length){
				g = 0;
			}
			realVal[j] = q;
		}
		
		jsonTmp = JSONBuilder.bar($selText, $actualName, realVal, labels, max);
		jsonString = JSON.stringify(jsonTmp);
		break;
	case 'pie':
		ulToggle.show().find('li').removeClass('hov');
		ulToggle.find('li:eq(1)').addClass('hov');
		var values = [];
		var d = $data.split(separator);
		var e = $campNames.split(separator);

		var dsum = 0;
		for(var j = 0;j < d.length;j++){
			if(numReg.test(d[j]) == false){
				continue;
			}
			dsum += parseFloat(d[j]);
		}
		var e = $campNames.split(separator);

		for ( var j = 0; j < d.length; j++) {
			var val = "";
			var per = "";
			if(dsum !== 0){
				if(numReg.test(d[j]) == true){
					val = parseFloat(d[j]);
					per = val.div(dsum).mul(100).toFixed(2) + "%";
				}
			}
			var item = {
				"label" : per,
				"value" : val,
				"font-size" : 11,
				"text":e[j],
				"tip":e[j] + "<br>" + (numReg.test(val) == true ? formatMoney(val,0):'')
			}
			values[j] = item;
		}
		JSONBuilder.setType("");
		jsonTmp = JSONBuilder.pie(values, $selText + "--" + $actualName);
		jsonString = JSON.stringify(jsonTmp);
		break;
	}
	return jsonString;
}

function image2(type) {
	var $selText = $("#sel_target :selected").text();
	var oldSelText = "";
	$("#sel_target option").each(function(){
		if($.trim($(this).text()) == $selText){
			oldSelText = $selText;
			$selText = $(this).attr("flag");
			return false;
		}
	});
	var $type = $("input[name='actualType']").attr("value");
	var ulToggle = $('#toImage');
	$('#showFlaBox').find('.FlashClass').remove();
	$('#showFlaBox').find('.clear0').remove();
	$('#showFlaBox').find('object').remove();
	$('#outerFlashFrame').css('height','auto');
	if (type == 'doubleBar') {
		if ($selText == 'Spend') {
			return;
		}
	}
	if (type == 'pie') {
		if ($selText !== 'Spend' && $selText !== 'Impression'
				&& $selText !== 'Click' && $selText !== 'UV' && $selText !== 'Visits') {
			return;
		}
	}
	if (type == 'doublePie') {
		if ($selText !== 'Impression' && $selText !== 'Click'
				&& $selText !== 'UV' && $selText !== 'Visits') {
			return;
		}
	}
	var n = 0;
	var w = 0;
	var reportType = $("input[name='reportType']").attr("value");
	if (reportType.trim() == 'Media' || reportType.trim() == 'Placement'||reportType.trim()=='Benchmark') {
		w = 1;
	}
	var $data = "", $data2 = "", $data3 = "", $campNames = "", arg = "", $actualName = "";
	var numReg = /^([\-]?[0-9])|([\-]?[0-9]+\.?[0-9]+)$/;
	switch ($type) {
	case '1':
		$actualName = labelName[0];
		$('#campaignTable_act tr:first-child').find("td").each(function(i, item) {
					var t = $.trim($(item).attr('flag'));
					if (t.indexOf($selText) >= 0) {
						n = i + 1;
						return false;
					}
				});
		$('#campaignTable_act > tbody').find("tr").each(function() {
			if($(this).hasClass('totalTr')){
				return false;
			}
			$(this).find("td").each(function(j, column) {
				if (j == w) {
					if(reportType.toLowerCase() == 'placement'){
						arg = $(column).find('span').text();
					}else{
						arg = $(column).find('a').text();
					}
					if (arg !== null && arg.trim().length > 0) {
						$campNames += arg.trim() + separator;
					} else {
						$campNames += "Untitled" + separator;
					}
				} else if (j + 1 == n) {
					arg = $(column).text();
					if (arg !== null && arg.trim().length > 0) {
						arg = ReportUtils.getValue(arg.trim());
						$data += arg + separator;
					} else {
						$data += "0" + separator;
					}
				}
			});
		});
		break;
	case '2':
		$actualName = labelName[1];
		ulToggle.show().find('li').removeClass('hov');
		ulToggle.find('li:eq(1)').addClass('hov');
		$('#campaignTable_bud tr:first-child').find("td").each(
				function(i, item) {
					var t = $.trim($(item).attr('flag'));
					if (t.indexOf($selText) >= 0) {
						n = i + 1;
						return false;
					}
				});
		$('#campaignTable_bud > tbody').find("tr").each(function() {
			if($(this).hasClass('totalTr')){
				return false;
			}
			$(this).find("td").each(function(j, column) {
				if (j == w) {
					if(reportType.toLowerCase() == 'placement'){
						arg = $(column).find('span').text();
					}else{
						arg = $(column).find('a').text();
					}
					if (arg !== null && arg.trim().length > 0) {
						$campNames += arg.trim() + separator;
					} else {
						$campNames += "Untitled" + separator;
					}
				} else if (j + 1 == n) {
					arg = $(column).text();
					if (arg !== null && arg.trim().length > 0) {
						arg = ReportUtils.getValue(arg.trim());
						$data += arg + separator;
					} else {
						$data += "0" + separator;
					}
				}
			});
		});
		break;
	case '3':
		$actualName = labelName[2];
		var $arg = $("select[name='sel_target'] :selected").attr("value")
				.split(",");
		n = $arg[0];
		$('#campaignTable_all > tbody').find("tr").each(function() {
			$(this).find("td").each(function(j, column) {
				if (j == w) {
					if(reportType.toLowerCase() == 'placement'){
						arg = $(column).find('span').text();
					}else{
						arg = $(column).find('a').text();
					}
					if (arg !== null && arg.trim().length > 0) {
						$campNames += arg.trim() + separator;
					} else {
						$campNames += "Untitled" + separator;
					}
				}
			});
		});
		$('#campaignTable_all > tbody tr:even').each(function() {
			$(this).find("td").each(function(j, column) {
				if (j + 1 == n) {
					arg = $(column).text();
					if (arg !== null && arg.trim().length > 0) {
						arg = ReportUtils.getValue(arg.trim());
						$data3 += arg + separator;
					} else {
						$data3 += "0" + separator;
					}
				}
			});
		});
		$('#campaignTable_all > tbody tr:odd').each(function() {
			$(this).find("td").each(function(j, column) {
				if (j + 1 == $arg[1]) {
					arg = $(column).text();
					if (arg !== null && arg.trim().length > 0) {
						arg = ReportUtils.getValue(arg.trim());
						$data2 += arg + separator;
					} else {
						$data2 += "0" + separator;
					}
				}
			});
		});
		$data3 = $data3.substring(0, $data3.length - 3);
		$data2 = $data2.substring(0, $data2.length - 3);
		break;
	}
	$campNames = $campNames.substring(0, $campNames.length - 3);
	if ($type !== '3') {
		$data = $data.substring(0, $data.length - 3);
		if ($data.split(separator).length !== $campNames.split(separator).length) {
			return;
		}
	} else {
		if ($data3.split(separator).length !== $data2.split(separator).length) {
			return;
		}
	}
	var $url = "", $div = "";
	var tmpVar=0;
	JSONBuilder.setType('notToPDF');
	if(oldSelText == 'CTR'){
		oldSelText += "(%)";
	}
	switch (type) {
	case 'doublePie':
		$('#outerFlashFrame').css('height','640px');
		$("<div class='clear0'></div><div class='FlashClass' id='FlashDiv'></div><div class='FlashClass' id='FlashDiv2'></div>")
				.insertAfter($('#toImage'));
		ulToggle.show().find('li').removeClass('hov');
		ulToggle.find('li:eq(1)').addClass('hov');

		var values = [];
		var dsum_1 = 0;
		var d = $data3.split(separator);
		$.each(d, function(j, t) {
			if(numReg.test(t) == false){
				values[j] = '';
			}else{
				values[j] = parseFloat(t);
				dsum_1 += values[j];
			}
		});

		var values2 = [];
		var dsum_2 = 0;
		d = $data2.split(separator);
		$.each(d, function(j, t) {
			if(numReg.test(t) == false){
				values2[j] = '';
			}else{
				values2[j] = parseFloat(t);
				dsum_2 += values2[j];
			}
		});

		var labels = [];
		d = $campNames.split(separator);
		var y = "";
		for ( var j = 0; j < d.length; j++) {
			if (j % 2 != 0) {
				continue;
			} else {
				y += d[j];
			}
			if (j !== d.length - 2) {
				y += "&";
			}
		}
		d = y.split("&");
		$.each(d, function(j, t) {
			labels[j] = t;
		});
		
		var realVal = [];
		for ( var j = 0; j < labels.length; j++) {
			var val = "";
			var per = "";
			if(dsum_1 !== 0){
				if(numReg.test(values[j]) == true){
					val = parseFloat(values[j]);
					per = val.div(dsum_1).mul(100).toFixed(2) + "%";
				}
			}
			var item = {
					"label" : per,
					"value" : val,
					"font-size" : 11,
					"text":labels[j],
					"tip":labels[j] + "<br>" + (numReg.test(val) == true ? formatMoney(val,0):'')
			}
			realVal[j] = item;
		}
		jsonresult = JSONBuilder.pie(realVal, oldSelText + "--"
				+ $actualName.split('vs')[0].trim());
		swfobject.embedSWF(contextPath
				+ "/resources/chart/swf/open-flash-chart-legend.swf", "FlashDiv",
				"100%", "300", "9.0.0", "#FFFFFF", {
					"get-data" : "giveData"
				},{wmode:"opaque"});
		
		realVal = [];
		for ( var j = 0; j < labels.length; j++) {
			var val = "";
			var per = "";
			if(dsum_1 !== 0){
				if(numReg.test(values2[j]) == true){
					val = parseFloat(values2[j]);
					per = val.div(dsum_2).mul(100).toFixed(2) + "%";
				}
			}
			var item = {
					"label" : per,
					"value" : val,
					"font-size" : 11,
					"text":labels[j],
					"tip":labels[j] + "<br>" + (numReg.test(val) == true ? formatMoney(val,0):'')
			}
			realVal[j] = item;
		}
		
			jsonresult2 = JSONBuilder.pie(realVal, oldSelText + "--"
					+ $actualName.split('vs')[1].trim());
			swfobject.embedSWF(contextPath
					+ "/resources/chart/swf/open-flash-chart-legend.swf", "FlashDiv2",
					"100%", "300", "9.0.0", "#FFFFFF", {
						"get-data" : "giveData2"
					},{wmode:"opaque"});
		break;
	case 'doubleBar':
		ulToggle.show().find('li').removeClass('hov');
		ulToggle.find('li:eq(0)').addClass('hov');
		$("<div class='clear0'></div><div class='FlashClass' id='FlashDiv'></div>").insertAfter(
				$('#toImage'));

		var values = [];
		var d = $data3.split(separator);
		var max = 0;
		$.each(d, function(j, t) {
			if(t.toString()!=='-'){
				values[j] = parseFloat(t);
				tmpVar = parseFloat(t);
				max = max >= tmpVar ? max : tmpVar;
			}else{
				values[j]=null;
			}
		});
		var values2 = [];
		d = $data2.split(separator);
		$.each(d, function(j, t) {
			if(t.toString()!=='-'){
				values2[j] = parseFloat(t);
				tmpVar = parseFloat(t);
				max = max >= tmpVar ? max : tmpVar;
			}else{
				values2[j]=null;
			}
		});
		var labels = [];
		d = $campNames.split(separator);
		var y = "";
		for ( var j = 0; j < d.length; j++) {
			if (j % 2 != 0) {
				continue;
			} else {
				y += d[j];
			}
			if (j !== d.length - 2) {
				y += separator;
			}
		}
		d = y.split(separator);
		$.each(d, function(j, t) {
			var q = {
				"text" : t,
				"rotate" : 340
			}
			labels[j] = q;
		});
		jsonresult = JSONBuilder.doubleBar(oldSelText, values, values2, labels,
				max);
		swfobject.embedSWF(contextPath
				+ "/resources/chart/swf/open-flash-chart-SimplifiedChinese.swf", "FlashDiv",
				"100%", "300", "9.0.0", "#FFFFFF", {
					"get-data" : "giveData"
				},{wmode:"opaque"});
		break;
	case 'bar':

		$("<div class='clear0'></div><div class='FlashClass' id='FlashDiv'></div>").insertAfter(
				$('#toImage'));
		ulToggle.show().find('li').removeClass('hov');
		ulToggle.find('li:eq(0)').addClass('hov');
		var values = [];
		var d = $data.split(separator);
		var max = 0;
		$.each(d, function(j, t) {
			if(numReg.test(t) == true){
				values[j] = parseFloat(t);
				tmpVar = parseFloat(t);
				max = max >= tmpVar ? max : tmpVar;
			}else{
				values[j]='';
			}
		});
		var labels = [];
		d = $campNames.split(separator);
		$.each(d, function(j, t) {
			var q = {
				"text" : t.trim(),
				"rotate" : 340
			}
			labels[j] = q;
		});
		
		var realVal = [];
		var g = 0;
		for(var j=0;j<values.length;j++){
			var q={
				"top":values[j],
				"colour":colorSets[g++]
			}
			if(g == colorSets.length){
				g = 0;
			}
			realVal[j] = q;
		}
		
		jsonresult = JSONBuilder
				.bar(oldSelText, $actualName, realVal, labels, max);
		swfobject.embedSWF(contextPath
				+ "/resources/chart/swf/open-flash-chart-SimplifiedChinese.swf", "FlashDiv",
				"100%", "300", "9.0.0", "#FFFFFF", {
					"get-data" : "giveData"
				},{wmode:"opaque"});
		break;
	case 'pie':

		$("<div class='clear0'></div><div class='FlashClass' id='FlashDiv'></div>").insertAfter(
				$('#toImage'));
		ulToggle.show().find('li').removeClass('hov');
		ulToggle.find('li:eq(1)').addClass('hov');
		var values = [];
		var d = $data.split(separator);
		
		var dsum = 0;
		for(var j = 0;j < d.length;j++){
			if(numReg.test(d[j]) == false){
				continue;
			}
			dsum += parseFloat(d[j]);
		}
		var e = $campNames.split(separator);

		for ( var j = 0; j < d.length; j++) {
			var val = "";
			var per = "";
			if(dsum !== 0){
				if(numReg.test(d[j]) == true){
					val = parseFloat(d[j]);
					per = val.div(dsum).mul(100).toFixed(2) + "%";
				}
			}
			var item = {
				"label" : per,
				"value" : val,
				"font-size" : 11,
				"text":e[j],
				"tip":e[j] + "<br>" + (numReg.test(val) == true ? formatMoney(val,0):'')
			}
			values[j] = item;
		}
		jsonresult = JSONBuilder.pie(values, oldSelText + "--" + $actualName);
		swfobject.embedSWF(contextPath
				+ "/resources/chart/swf/open-flash-chart-legend.swf", "FlashDiv",
				"100%", "300", "9.0.0", "#FFFFFF", {
					"get-data" : "giveData"
				},{wmode:"opaque"});
		
		break;
	}

}

/**
 * json对象转字符串
 * 
 * @param jsonObj
 * @returns
 */
function getJsonString(jsonObj) {
	var sA = [];
	(function(o) {
		var isObj = true;
		if (o instanceof Array) {
			isObj = false;
		} else {
			if (typeof o != "object") {
				if (typeof o == "string") {
					sA.push("\"" + o + "\"");
				} else {
					sA.push(o);
				}
				return;
			}
		}
		sA.push(isObj ? "{" : "[");
		for ( var i in o) {
			if (o.hasOwnProperty(i) && i != "prototype") {
				if (isObj) {
					sA.push(i + ":");
				}
				arguments.callee(o[i]);
				sA.push(",");
			}
		}
		sA.push(isObj ? "}" : "]");
	})(jsonObj);
	return sA.slice(0).join("").replace(/,\}/g, "}").replace(/,\]/g, "]");
}

var imageStr = "";

var count = 0;
