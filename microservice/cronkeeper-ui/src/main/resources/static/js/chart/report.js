$contextPath = "http://localhost:8080/mex";
var selectTit = UQJsText.report.selectName;
if(typeof $mwbFlag =="undefined") $mwbFlag = "false";
function init(contextPath) {
	$contextPath = contextPath;
}

function isNotNull(data){
	if(data!==null && data.toString().length>0){
		return true;
	}
	return false;
}

function isNull(data){
	return !isNotNull(data);
}
// 提交之前验证表单
function validateForm(){
	/*
	$('#datepicker_02').next('#dateError').remove();
	$('#datepicker_02').css('color','');
	*/
	var date1 = $.trim($('#datepicker_01').attr('value'));
	var date2 = $.trim($('#datepicker_02').attr('value'));
	var params1 = date1.split('/');
	var params2 = date2.split('/');
	var _date1 = params1[2] + params1[0] + params1[1];
	var _date2 = params2[2] + params2[0] + params2[1];
	var arg1 = parseInt(_date1.replaceAll('/',''));
	var arg2 = parseInt(_date2.replaceAll('/',''));
	if(arg1 > arg2){
		/*
		var t = "<span id='dateError' style='color:red;margin-left:5px;'>End Date cannot be before the Start Date.</span>";
		$(t).insertAfter($('#datepicker_02'));
		$('#datepicker_02').css('color','red');
		*/
		$('#datepicker_02').attr('value',date1);
		return true;
	}
	return true;
}

function seaOver(){
	    var reg = /\s/g;
	    $('.selGoTr').each(function(){
		     var thisOverId = $(this).parent().parent().parent().attr('id');
			var optionType = "";
				if(thisOverId == "OrganOver"){
					optionType = "campaign";
				}else if(thisOverId == "medOver"){
					optionType = "media";
				}else if(thisOverId == "plaOver"){
					optionType = "placement";
				}
			 var btnId = thisOverId + 'Btn';
		     var sel = $(this).find('select:last');
			 sel.find('option').each(function(){                             // 添加新项
				var strOption = $(this).text();
				var strId = $(this).val();
				$('#conItemBox').show();
				if($('#conItemBox a[name="' + thisOverId + '"]').length > 0){
					var flag = true;
					$('#conItemBox a[name="' + thisOverId + '"]').each(function(){
						if($(this).text().replace(reg,'') == strOption.replace(reg,'')){          // 没有重复时列出来
							flag = false;
						}
					})
					if(flag)  $( '<a optionType="' + optionType + '" strId = "' + strId + '" name= ' + thisOverId + '>'  +  strOption + '<span> </span></a>').appendTo($('#conItemBox'));
				}else{
					$( '<a optionType="' + optionType + '" strId = "' + strId + '" name= ' + thisOverId + '>'  +  strOption + '<span> </span></a>').appendTo($('#conItemBox'));
				}
			 })
			 if( $('#conItemBox a[name="' + thisOverId + '"]').length > 0 ){
			      $('#' + btnId ).css('color','red');
			 }
		})
		delTag();
	}

function reflushEvent(){
	$('.filterConditionItems').change(function(){
		var parentNodeId = $(this).val();
		var index = $('.filterConditionItems').index($(this));
		if(index >= $('.filterConditionItems').length - 1){
			return;
		}
		var str = "<option value='0'>" + selectTit + "</option>";
		$('.filterConditionItems:gt('+index+')').html(str);
		if(parentNodeId==0){
			return;
		}
		var levelId = $('.filterConditionItems').eq(index+1).parent().prev().find('input[type="hidden"]').val();
		var url = $contextPath+"/report/dashboard/campaignreport/nodeList"
		$.post(url,{"parentNodeId":parentNodeId,"levelId":levelId},function(data){
				var nodes = eval(data);
				var html = str;
				for(var i in nodes){
					html += "<option value='"+nodes[i]+"'>"+i+"</option>";
				}
				$('.filterConditionItems').eq(index+1).html(html);
			});
	});
	delTag();
}

function campaignBlockCollector(){
	$('#campaignNameSa').empty();
	$('#mediaList').empty();
	if($('#conItemBox').length == 0  || $('#conItemBox').find('a[optiontype="campaign"]').length == 0){
		return;
	}
	var map = new Map();
	var arg;
	$('#conItemBox a[optiontype="campaign"]').each(function(){
		arg = $(this).attr('strid');
		arg = parseInt($.trim(arg));
		map.put(arg,$.trim($(this).text()));
	});
	var h = '';
	var cpnames = map.keys();
	for(var i = 0;i<cpnames.length;i++){
		h += '<option value="' + cpnames[i] + '">';
		h += map.get(cpnames[i])
		h += '</option>';
	}
	$('#campaignNameSa').html(h);
}

function mediaBlockCollector(){
	$('#mediaNameSa').empty();
	if($('#conItemBox').length == 0  || $('#conItemBox').find('a[optiontype="media"]').length == 0){
		return;
	}
	var map = new Map();
	var arg;
	$('#conItemBox a[optiontype="media"]').each(function(){
		arg = $(this).attr('strid');
		arg = parseInt($.trim(arg));
		map.put(arg,$.trim($(this).text()));
	});
	var h = '';
	var cpnames = map.keys();
	for(var i = 0;i<cpnames.length;i++){
		h += '<option value="' + cpnames[i] + '">';
		h += map.get(cpnames[i])
		h += '</option>';
	}
	$('#mediaNameSa').html(h);
}

function loadMediaDefaultData(){
	var $advertiserId = $("select[name='organizationAdvertiserId'] :selected").attr("value");
	var $url = $contextPath + "/report/dashboard/campaignreport/getMediaList";
	var $campaignId = 0;
	if($('#campaignNameSa option').length > 0){
		$campaignId = $('#campaignNameSa :selected').val();
	}
	enableStartAndEndDate(true);
	var param = {
				"advertiserId":$advertiserId,
				"campaignId":$campaignId,
				"startDate":$.trim($('#datepicker_01').val()),
				"endDate":$.trim($('#datepicker_02').val())
			};
	$.post($url,param,function(data){
		enableStartAndEndDate(false);
		if(isNull(data)){
			return;
		}
		var items = eval(data);
		var array = [];
		if($('#targetMediaList').find('option').length > 0){
			$('#targetMediaList').find('option').each(function(j,item){
				array[j] = $(item).text();
			});
		}
		var html = "";
		$.each(items, function(index, item) {
			if(array.contains(item.siteName) == false){
				html += "<option value='" + item.siteId + "'>" + item.siteName
				+ "</option>";
			}
		});
		if(html.length > 0){
			$('#mediaList').html(html);
		}
	});
}

function loadPlmtDefaultData(){
	
	var reportType = $('#reportType').val();
	switch (reportType) {
	case 'Campaign':
		var $advertiserId = $("select[name='organizationAdvertiserId'] :selected").attr("value");
		$.post($contextPath + "/report/dashboard/campaignreport/getMediaList", {
			advertiserId : $advertiserId
		}, function(data) {
			if(isNull(data)){
				return;
			}
			var items = eval(data);
			var html = "<option value='0'>" + selectTit + "</option>";
			$.each(items, function(index, item) {
				html += "<option value='" + item.siteId + "'>" + item.siteName
						+ "</option>";
			});
			$('#selectMedia').html(html);
		});
		break;
	case 'Placement':
		$('#placementList').empty();
		$('#selectChannel').html("<option value='0'>" + selectTit + "</option>");
		var $siteId = $('#currentSiteId').val();
			$.post($contextPath + "/report/dashboard/campaignreport/getChannelList", {
				siteId : $siteId
			}, function(data) {
				if(isNull(data)){
					return;
				}
				var items = eval(data);
				var html = "<option value='0'>" + selectTit + "</option>";
				$.each(items, function(index, item) {
					html += "<option value='" + item.channelId + "'>" + item.channelName
							+ "</option>";
				});
				$('#selectChannel').html(html);
				$('#searchPlacement').click();
			});
		break;
	}
}

function loadOrganizationDefaultData() {
	var a;
	if($('#organizationAdvertiserId').length > 0){
		a = $('#organizationAdvertiserId :selected').val();
	}else if($('#advertiserId').length > 0){
		a = $('#advertiserId').val();
	}
	if (a > 0) {
		processSelectChangeEvent();
	}
}

function loadMediaCategoryDefaultData(){
	var url = $contextPath + "/report/dashboard/mediareport/getMediaCategoryList";
	$.post(url,null,function(data){
		if(isNull(data)){
			return;
		}
		var array = [];
		if($('#targetCategoryList').find('option').length > 0){
			$('#targetCategoryList').find('option').each(function(j,item){
				array[j] = $(item).text();
			});
		}
		var result = eval(data);
		var html = "";
		$.each(result,function(i,item){
			if(array.contains(item.catName) == false){
				html += "<option value='"+item.catId+"'>"+item.catName+"</option>";
			}
		});
		if(html.length > 0){
			$('#categoryList').html(html);
		}
	});
}

function loadDisplayTypeDefaultData(){
	var siteId = 0;
	if($('#mediaNameSa option').length > 0){
		siteId = $('#mediaNameSa :selected').val();
	}
	var url = $contextPath + "/report/dashboard/mediareport/getDisplayTypeList?siteId=" + siteId;
	enableStartAndEndDate(true);
	var startDate = $.trim($('#datepicker_01').val());
	var endDate = $.trim($('#datepicker_02').val());
	url += "&startDate=" + startDate;
	url += "&endDate=" + endDate;
	url += "&advertiserId=" + $('#organizationAdvertiserId :selected').val();
	$.post(url,null,function(data){
		enableStartAndEndDate(false);
		if(isNull(data)){
			return;
		}
		var array = [];
		if($('#targetAdformatList').find('option').length > 0){
			$('#targetAdformatList').find('option').each(function(j,item){
				array[j] = $(item).text();
			});
		}
		var result = eval(data);
		var html = "";
		$.each(result,function(i,item){
			if(array.contains(item.displayName) == false){
				html += "<option value='"+item.displayId+"'>"+item.displayName+"</option>";
			}
		});
		if(html.length > 0){
			$('#adformatList').html(html);
		}
	});
}

/**
 * 当点击删除搜索过滤条件时，级联删除隐藏域的值
 */
function reflashSelectItems(){
	var a="",b="";
	if($('#targetOrganizationList').length>0){
		var len=$('#targetOrganizationList option').length;
		$('#targetOrganizationList option').each(function(j,item){
			a+=$(item).attr('value').replace(/\s/g,'');
			b+=$.trim($(item).text());
			if(j!==len-1){
				a+=",";
				b+=",";
			}
		});
		$('#brandIds').attr('value',a);
		$('#brandNames').attr('value',b);
	}

	if(('#targetCampaignList').length>0){
		a="",b="";
		var len=$('#targetCampaignList option').length;
		
		$('#targetCampaignList option').each(function(j,item){
			a+=$(item).attr('value').replace(/\s/g,'');
			b+=$.trim($(item).text());
			if(j!==len-1){
				a+=",";
				b+=",";
			}
		});
		$('#campaignIdsStr').attr('value',a);
		$('#campaignNames').attr('value',b);
	}

	if($('#targetMediaList').length>0){
		a="",b="";
		var len=$('#targetMediaList option').length;
		
		$('#targetMediaList option').each(function(j,item){
			a+=$(item).attr('value').replace(/\s/g,'');
			b+=$.trim($(item).text());
			if(j!==len-1){
				a+=",";
				b+=",";
			}
		});
		$('#mediaIdsStr').attr('value',a);
		$('#mediaNames').attr('value',b);
	}
	
	if($('#targetPlacementList').length>0){
		a="",b="";
		var len=$('#targetPlacementList option').length;
		
		$('#targetPlacementList option').each(function(j,item){
			a+=$(item).attr('value').replace(/\s/g,'');
			b+=$.trim($(item).text());
			if(j!==len-1){
				a+=",";
				b+=",";
			}
		});
		$('#placementIdsStr').attr('value',a);
		$('#placementNames').attr('value',b);
	}
	
	if($('#targetAdformatList').length > 0){
		a="",b="";
		var len = $('#targetAdformatList option').length;
		$('#targetAdformatList option').each(function(j,item){
			a+=$(item).attr('value').replace(/\s/g,'');
			b+=$.trim($(item).text());
			if(j!==len-1){
				a+=",";
				b+=",";
			}
		});
		$('#adformatIdsStr').attr('value',a);
		$('#adformatNames').attr('value',b);
	}
	
	if($('#targetCategoryList').length > 0){
		a="",b="";
		var len = $('#targetCategoryList option').length;
		$('#targetCategoryList option').each(function(j,item){
			a+=$(item).attr('value').replace(/\s/g,'');
			b+=$.trim($(item).text());
			if(j!==len-1){
				a+=",";
				b+=",";
			}
		});
		$('#platformIdsStr').attr('value',a);
		$('#platformNames').attr('value',b);
	}
	
}

function delTag() {
	$('#conItemBox span').click(
			function() {
				var thisName = $(this).parent().attr('name');
				var htmlTxt = $(this).parent().text();
				var btnId = thisName + 'Btn';
				var reg = /\s/g;
				var strTxt = $(this).parent().text().replace(reg, '');
				var getHideID = function(linkName,htmlTxt){
					switch(linkName){
					    case 'DemograBox':
					    if(htmlTxt.indexOf('-')==-1 && htmlTxt.indexOf('+')==-1){
					    	return 'genderStr';	
					    }else{
					    	return 'periodStr';	
					    }
					    break;
					    
					    case 'GeograBox':
					    return 'geographyStr';
					    break;
					}
					
				}
				$(this).parent().remove();
				$('#' + thisName).find('.selGoTr select:last option').each(
						function() {
							var thisTxt = $(this).text().replace(reg, '');
							if (strTxt == thisTxt) {
								$('#' + thisName).find('.selGoTr select:first')
										.append($(this));
							}
						})
				// 去掉下拉框多选上的选项
				$('#' + thisName).find('input').each(function() {
					var thisVal = $(this).val().replace(reg, '');
					if (strTxt == thisVal) {
						$(this).attr('checked', false);
					}
				})
				
				// 去掉多选 对应 隐藏域上的value
				var hideID = getHideID(thisName,htmlTxt);
				if(hideID){
					var valArr = $('#' + hideID).val().split(',');
					for(var i=0; i<valArr.length; i++){
			            if(htmlTxt.replace(reg,'') == valArr[i].replace(reg,'')){
			                valArr.splice(i,1);
			                break;
			            }
			        }
					var hiddenValNew = valArr.join(',');
			        $('#'+ hideID).val(hiddenValNew);
				}
				if ($('#conItemBox a[name=' + thisName + ']').length == 0) {
					$('#' + btnId).css('color', '');
				}

				if ($('#conItemBox a').length == 0) {
					$('#conItemBox').hide();
				}
				reflashSelectItems();
			})
}

/**
 * 休眠多少秒
 * 
 * @param numberMillis
 *            毫秒
 */
function sleep(numberMillis) {
	var now = new Date();
	var exitTime = now.getTime() + numberMillis;
	while (true) {
		now = new Date();
		if (now.getTime() > exitTime)
			return;
	}
}

/**
 * 加载js文件
 * 
 * @param file
 */
function loadJs(file) {
	var head = $('head').remove('#loadScript');
	$("<scri" + "pt>" + "</scr" + "ipt>").attr({
		src : file,
		type : 'text/javascript',
		id : 'load'
	}).appendTo(head);
}

/**
 * 返回当月最后一天
 * 
 * @param year
 * @param month
 * @returns {Number}
 */
function getCurrentMonthLastDay(year,month){
	var leap=(year % 400 == 0) || (year % 4 == 0 && year % 100 != 0);
	if(month==2){
		if(leap){
			return 29;
		}else{
			return 28;
		}
	}else if(month==4||month==6||month==9||month==11){
		return 30;
	}else{
		return 31;
	}
}

/**
 * 返回对应的季度
 * 
 * @param month
 * @returns
 */
function getQuarter(month){
	if (month >= 1 && month <= 3) {
		quarter = 1;
	} else if (month >= 4 && month <= 6) {
		quarter = 2;
	} else if (month >= 7 && month <= 9) {
		quarter = 3;
	} else if (month >= 10 && month <= 12) {
		quarter = 4;
	}
	return quarter;
}

function toRed(){
	
	var redirectStatus = $('#redirectStatus').val();
	if(redirectStatus == '0'){
		return;
	}
	var t = "";
	var reportType = $('#reportType').val();
	var a = "",b = "";
	var html = "";
	if(reportType == 'Campaign'){
		$('.siteTabCom').find('tbody > tr').each(function(){
			var text = $.trim($(this).find('td:eq(0)').text());
			a += text;
			a += ",";
			var id= $(this).find('td:eq(0)').find('input[type="hidden"]').val().replace(/\s/g,'');
			b += id;
			b +=",";
			
			html += "<option value='"+ id +"'>"+text+"</option>";
		});
		if(html.length > 0){
			$('#targetOrganizationList').html(html);
		}
		if(a.length > 0){
			a = a.substring(0,a.length -1);
			$('#brandNames').attr('value',a);
		}
		if(b.length > 0){
			b = b.substring(0,b.length -1);
			$('#brandIds').attr('value',b);
		}
	}else if(reportType == 'Media'){
		$('.siteTabCom').find('tbody > tr').each(function(){
			var text = $.trim($(this).find('td:eq(1)').text());
			a += text;
			a += ",";
			var id= $(this).find('td:eq(0)').text().replace(/\s/g,'');
			b += id;
			b +=",";
			
			html += "<option value='"+ id +"'>"+text+"</option>";
		});
		if(html.length > 0){
			$('#targetMediaList').html(html);
		}
		if(a.length > 0){
			a = a.substring(0,a.length -1);
			$('#mediaNames').attr('value',a);
		}
		if(b.length > 0){
			b = b.substring(0,b.length -1);
			$('#mediaIdsStr').attr('value',b);
		}
	}
}

$(function() {
	
	var aa= $('.filterConditionItems');
	var _sel = $('#timeType :selected').attr('value');
	if(_sel!=='specificDate'){
		 $('#datepicker_01').attr("disabled",true);
		 $('#datepicker_02').attr("disabled",true);
		 $('#datepicker_01').addClass('inputBg');
		 $('#datepicker_02').addClass('inputBg');
	}else{
		 $('#datepicker_01').attr("disabled",false);
		 $('#datepicker_02').attr("disabled",false);
		 $('#datepicker_01').removeClass("inputBg");
		 $('#datepicker_02').removeClass("inputBg");
	}
	
	var $selectedAdvertiserId = $('#selectedAdvertiserId').attr('value');
	if($selectedAdvertiserId>0){
		$('#organizationAdvertiserId option').each(function(){
			var s=$(this).attr('value');
			if(s==$selectedAdvertiserId){
				$(this).attr('selected','selected');
			}
		});
	}

	
	toRed();
	seaOver();
	
	$('.goToPlacementReport').click(function(){
		
		var $siteId= $(this).parent().parent().prev().text().trim();
		
		var arg = "";
		var argNames = "";
		$('#conItemBox a[optiontype="campaign"]').each(function(){
			
			arg += $(this).attr('strid');
			arg += ",";
			
			argNames += $.trim($(this).text());
			argNames += ",";
		});
		if(arg.length > 0){
			arg = arg.substring(0,arg.length - 1);
		}
		if(argNames.length > 0){
			argNames = argNames.substring(0,argNames.length - 1);
		}
		var url = $contextPath+"/report/dashboard/mediareport/placementreport?siteId="+$siteId + "&brandIds=" + arg;
		url += "&brandNames=" + argNames;
		enableStartAndEndDate(true);
		var startDate = $.trim($('#datepicker_01').val());
	    var endDate = $.trim($('#datepicker_02').val());
		url += "&startDate=" + startDate;
		url += "&endDate=" + endDate;
		url += "&redirect=1";
		
		url += "&isActual=" + $('#spendingType').val();
		url += "&costSystem=" + $('#CostSystem').val();
		url += "&paymentType=" + $('#paymentType').val();
		
		enableStartAndEndDate(false);
		window.location.href = url;
	});
	
	$('#datepicker_02').change(function(){
		var a = $('#datepicker_01').val();
		var b = $('#datepicker_02').val();
		if(a != null && a.length > 0 && b != null && b.length > 0){
			validateForm();
		}
	});

	/***
	 *input添加背景色 
	 **/
	$('#timeType').change(function() {
		var s = $('#datepicker_01').addClass('inputBg').attr("value");
		var t = $('#datepicker_02').addClass('inputBg').attr("value");
		
		$('#datepicker_02').css('color','');
		$('#dateError').remove();
		
		var date = new Date();
		var month=date.getMonth()+1;
		var year=date.getFullYear();
		var d = date.getDate();
		
		var sel = $('#timeType :selected').attr('value');
		var startDate="", endDate="";
		switch (sel) {
		case 'yesterday':
			var today = new Date();
			var yesterday = DateAdd("d",-1,today);   
			startDate = new DateFormat(" MM/dd/yyyy").format(yesterday); 
			endDate =  new DateFormat(" MM/dd/yyyy").format(yesterday); 
			break;
		case 'berforeYesterday':
			var today = new Date();
			var yesterday = DateAdd("d",-2,today);   
			startDate = new DateFormat(" MM/dd/yyyy").format(yesterday); 
			endDate =  new DateFormat(" MM/dd/yyyy").format(yesterday); 
			break;
		case 'thisWeek':
			var today = new Date();
            startDate = new DateFormat(" MM/dd/yyyy").format(showWeekFirstDay(today)); 
            endDate=  new DateFormat(" MM/dd/yyyy").format(showWeekLastDay(today)); 
            break;
		case 'lastWeek':
			var today = new Date();
        	var last7day = DateAdd("d",-6,today);   
            startDate = new DateFormat(" MM/dd/yyyy").format(showWeekFirstDay(last7day)); 
            endDate = new DateFormat(" MM/dd/yyyy").format(showWeekLastDay(last7day)); 
            break;  
		case 'last7Day':
			var today = new Date();
        	var last7day = DateAdd("d",-7,today);   
            startDate = new DateFormat(" MM/dd/yyyy").format(last7day); 
            var today = new Date();
            var last1day = DateAdd("d",-1,today); 
            endDate= new DateFormat(" MM/dd/yyyy").format(last1day); 
            break;            
		case 'specificDate':
			startDate=s;
			endDate=t;
			$('#datepicker_01').removeClass('inputBg');
			$('#datepicker_02').removeClass('inputBg');
			if(!$('#datepicker_01').val()){
				var today = new Date();
				startDate = new DateFormat("MM/dd/yyyy").format(showMonthFirstDay(today));
				endDate = new DateFormat("MM/dd/yyyy").format(today);  
			}
			break;
		case 'thisYear':
			startDate="01/01/"+year;
			endDate=new DateFormat("MM/dd/yyyy").format(today); 
			break;
		case 'lastYear':
			var lastyear = year - 1; 
			startDate="01/01/"+lastyear;
			endDate="12/31/"+lastyear;
			break;			
		case 'thisQuarter':
			var quarter = getQuarter(month);
			if(quarter==1){
				startDate="01/01/"+year;
			}else if(quarter==2){
				startDate="04/01/"+year;
			}else if(quarter==3){
				startDate="07/01/"+year;
			}else if(quarter==4){
				startDate="10/01/"+year;
			}
			if(quarter==1){
				endDate="03/31/"+year;
			}else if(quarter==2){
				endDate="06/30/"+year;
			}else if(quarter==3){
				endDate="09/30/"+year;
			}else if(quarter==4){
				endDate="12/31/"+year;
			}
			break;
		case 'lastQuarter':
			var quarter = getQuarter(month);
			if(quarter==4){
				startDate="07/01/"+year;
				endDate="09/30/"+year;
			}else if(quarter==3){
				startDate="04/01/"+year;
				endDate="06/30/"+year;
			}else if(quarter==2){
				startDate="01/01/"+year;
				endDate="03/31/"+year;
			}else if(quarter==1){
				year = Number(year-1);
				startDate="10/01/"+year;
				endDate="12/31/"+year;
			}
			break;			
		case 'thisMonth':
				var today = new Date();
				startDate = new DateFormat("MM/dd/yyyy").format(showMonthFirstDay(today));
				endDate = new DateFormat("MM/dd/yyyy").format(today);  
				endDate=new DateFormat(" MM/dd/yyyy").format(showMonthLastDay(today));
			break;
		case 'lastMonth':
			var today = new Date();
			var currentMonthDay = DateAdd("m",-1,today); 
			startDate=new DateFormat(" MM/dd/yyyy").format(showMonthFirstDay(currentMonthDay));
			endDate=new DateFormat(" MM/dd/yyyy").format(showMonthLastDay(currentMonthDay));	
			break;
		case 'allTime':
			startDate="";
			endDate="";
			break;			

		}
		
		 $('#datepicker_01').attr("value",startDate);
		 $('#datepicker_02').attr("value",endDate);
		 if(sel !== 'specificDate'){
			 $('#datepicker_01').attr("disabled",true);
			 $('#datepicker_02').attr("disabled",true);
		 }else{
			 $('#datepicker_01').attr("disabled",false);
			 $('#datepicker_02').attr("disabled",false);
		 }
	});
	
	$('#adOverBtn').click(function(){
		overDiv.showBox('adOver',800,90);
		mediaBlockCollector();
		loadDisplayTypeDefaultData();
	});
	
	$('#catOverBtn').click(function(){
		overDiv.showBox('catOver',800,90);
		loadMediaCategoryDefaultData();
	});
	
	$('#OrganOverBtn').click(function(){
		$('#campaignName').val('');
		overDiv.showBox('OrganOver',800,90);
		loadOrganizationDefaultData();
	});
	
	$('#plaOverBtn').click(function(){
		overDiv.showBox('plaOver',820,90);
		campaignBlockCollector();
		$('#placementName').val('');
		loadPlmtDefaultData();
	});
	
	$('#medOverBtn').click(function(){
	    overDiv.showBox('medOver',800,90);
		campaignBlockCollector();
		$('#mediaName').val('');
	    loadMediaDefaultData();
	});

	/** 列出查询条件 * */
	$('.reportOkClickClass').click(function(){
	    var sel = $(this).parent().parent().find('.selGoTr select:last');
		var thisOverId = $(this).parent().parent().attr('id');
		var optionType = "";
		if(thisOverId == "OrganOver"){
			optionType = "campaign";
		}else if(thisOverId == "medOver"){
			optionType = "media";
		}else if(thisOverId == "plaOver"){
			optionType = "placement";
		}
		
		var thisBtnId = thisOverId + 'Btn';
		 var theSpan = $('#'+thisBtnId).next();
		var reg = /\s/g;
		if(sel.find('option').length == 0) { 
		    $('#conItemBox a[name="' + thisOverId + '"]').remove();
		    overDiv.hideBox(thisOverId);
			$('#' + thisBtnId).css('color','');
			theSpan.find('input[type="hidden"]').eq(0).attr('value','');
			theSpan.find('input[type="hidden"]').eq(1).attr('value','');
		    return;
		}
		if($('#conItemBox').css('display') == 'none')
			 $('#conItemBox').show();
		if($('#conItemBox a[name="' + thisOverId + '"]').length > 0){              // 删除多余项
		    $('#conItemBox a[name="' + thisOverId + '"]').each(function(){
				var delFlag = true;
				var strCon = $(this).text().replace(reg,'');
				sel.find('option').each(function(){
					if($(this).text().replace(reg,'') == strCon){
						delFlag = false;
					}
				})
				if(delFlag){
					$(this).remove();
				}
			})
		}
		sel.find('option').each(function(){                             // 添加新项
			var strOption = $(this).text();
			var strId = $(this).val();
			if($('#conItemBox a[name="' + thisOverId + '"]').length > 0){
			    var flag = true;
			    $('#conItemBox a[name="' + thisOverId + '"]').each(function(){
					if($(this).text().replace(reg,'') == strOption.replace(reg,'')){                   // 没有重复时列出来
						flag = false;
					}
				})
				if(flag)  $( '<a optionType="' + optionType + '" strId="' + strId + '" name= ' + thisOverId + '>'  +  strOption + '<span> </span></a>').appendTo($('#conItemBox'));
			}else{
			    $('<a optionType="' + optionType + '" strId="' + strId + '" name= ' + thisOverId + '>'  +  strOption + '<span> </span></a>').appendTo($('#conItemBox'));
			}
		})
		
		if($('#conItemBox a[name="' + thisOverId + '"]').length > 0){
		    $('#' + thisBtnId).css('color','red');
		}
		// var thisOverId = $(this).parent().parent().attr('id');
		var btnId=thisOverId+"Btn";
		 if( $('#conItemBox a[name="' + thisOverId + '"]').length > 0 ){
		      $('#' + btnId ).css('color','red');
		 }
		 
		 var a="",b="";
		 sel.find('option').each(function(){
			 a += $(this).attr('value').replace(/\s/g,'');
			 a += ",";
			 b += $.trim($(this).text());
			 b += ",";
		 });
		 if(a.length > 0){
			 a = a.substring(0,a.length - 1);
			 theSpan.find('input[type="hidden"]').eq(0).attr('value',a);
		 }
		 if(b.length > 0){
			 b = b.substring(0,b.length - 1);
			 theSpan.find('input[type="hidden"]').eq(1).attr('value',b);
		 }
		delTag();
	    overDiv.hideBox(thisOverId);    
	});
	
	/** 处理弹窗中的条件搜索 * */
	/** Organization * */
	$('#organizationAdvertiserId').change(
			function() {
				$('#reSetBtn').click();
				$('#organizationList').html('');
				processSelectChangeEvent();
			});

	$('#searchOrganization').live("click",
			function() {
				$('#organizationList').empty();
				// $('#targetOrganizationList').empty();
				if($('#organizationAdvertiserId').length > 0){
					$advertiserId = $("#organizationAdvertiserId :selected").val();
				}else{
					$advertiserId = $("#advertiserId").val();
				}
				var $campaignName = "";
				if($('#campaignName').length > 0){
					$campaignName=$('#campaignName').val();
				}
				var cn=0;
				$('.filterConditionItems').each(function(){
					if($(this).val()==0){
						cn++;
					}
				});
				if(cn==$('.filterConditionItems').length){
					enableStartAndEndDate(true);
					var param = {
									"nodeId" : $advertiserId,
									"campaignName" : $campaignName,
									"mwb" : $mwbFlag,
									"advertiserId":$advertiserId,
									"startDate":$.trim($('#datepicker_01').val()),
									"endDate":$.trim($('#datepicker_02').val())
								};
					$.post($contextPath
							+ "/report/dashboard/campaignreport/getOrganizationHierarchyList",param, function(data) {
						enableStartAndEndDate(false);
						if(isNull(data)){
							return;
						}
						var items = eval(data);
						var html = "";
						$.each(items, function(index, item) {
							html += "<option value='" + item.campaignId + "'>"
									+ item.campaignName + "</option>";
						});
						$('#organizationList').html(html);
						openOrgWhenMeetPlacementReport();
					});
					
					
				}else{
					var len=$('.filterConditionItems[value!=0]').length-1;
					var sel=$(".filterConditionItems:eq("+len+")");
					var $nodeId = $(sel).attr("value");
					enableStartAndEndDate(true);
					$.post($contextPath
							+ "/report/dashboard/campaignreport/getOrganizationHierarchyList", {
						"nodeType":1,
						"campaignName":$campaignName,
						"nodeId" : $nodeId, 
						"mwb" : $mwbFlag,
						"advertiserId":$advertiserId,
						"startDate":$.trim($('#datepicker_01').val()),
						"endDate":$.trim($('#datepicker_02').val())
					}, function(data) {
						enableStartAndEndDate(false);
						if(isNull(data)){
							return;
						}
						var items = eval(data);
						var html = "";
						$.each(items, function(index, item) {
							html += "<option value='" + item.campaignId + "'>"
									+ item.campaignName + "</option>";
						});
						$('#organizationList').html(html);
						openOrgWhenMeetPlacementReport();
					});
				}
			});
	

	/** Campaign * */
	$('#searchCampaign').click(
			function() {
				$('#targetCampaignList').empty();
				$('#campaignList').empty();
				var $campaignName = $("input[name='campaignName']").val();
				var $campaignAdvertiserId = $(
						"select[name='organizationAdvertiserId'] :selected").attr(
						"value");
				$.post($contextPath
						+ "/report/dashboard/campaignreport/getCampaignList", {
					campaignName : $campaignName,
					advertiserId : $campaignAdvertiserId
				}, function(data) {
					if(isNull(data)){
						return;
					}
					var items = eval(data);
					var html = "";
					$.each(items, function(index, item) {
						html += "<option value='" + item.campaignId + "'>"
								+ item.campaignName + "</option>";
					});
					$('#campaignList').html(html);
				})
			});

	/** Media * */
	$('#searchMedia').click(
			function() {
				// $('#targetMediaList').empty();
				$('#mediaList').empty();
				if($('#campaignNameSa option').length == 0){
					return;
				}
				var $domain = $("input[name='mediaName']").val();
				var $advertiserId;
				if($('#organizationAdvertiserId').length > 0){
					$advertiserId = $('#organizationAdvertiserId :selected').val();
				}else{
					$advertiserId = $('#advertiserId').val();
				}
				var $campaignId = 0;
				if($('#campaignNameSa option').length > 0){
					$campaignId = $('#campaignNameSa :selected').val();
				}
				enableStartAndEndDate(true);
				$.post($contextPath + "/report/dashboard/campaignreport/getMediaList",
						{
							"domain" : $domain,
							"advertiserId" : $advertiserId,
							"campaignId": $campaignId,
							"startDate":$.trim($('#datepicker_01').val()),
							"endDate":$.trim($('#datepicker_02').val())
						}, function(data) {
							enableStartAndEndDate(false);
							if(isNull(data)){
								return;
							}
							var items = eval(data);
							var html = "";
							$.each(items, function(index, item) {
								html += "<option value='" + item.siteId + "'>"
										+ item.siteName + "</option>";
							});
							$('#mediaList').html(html);
						})
			});

	/** Placement * */
	$('#placementAdvertiserId')
			.change(
					function() {
						$('#selectMedia').html("<option value='0'>" + selectTit + "</option>");
						var $advertiserId = $(
								"select[name='placementAdvertiserId'] :selected").attr("value");
						$.post($contextPath+ "/report/dashboard/campaignreport/getMediaList",
										{
											advertiserId : $advertiserId
										},
										function(data) {
											if(isNull(data)){
												return;
											}
											var items = eval(data);
											var html = "<option value='0'>" + selectTit + "</option>";
											$.each(items,function(index,item) {
																html += "<option value='"
																		+ item.siteId
																		+ "'>"
																		+ item.siteName
																		+ "</option>";
															});
											$('#selectMedia').html(html);
										});
					});

	$('#searchPlacement').click(
			function() {
				// $('#targetPlacementList').empty();
				$('#placementList').empty();
				var $placementName = $("input[name='placementName']").val();
				var $reportType = $('#reportType').val();
				var $advertiserId = $('#advertiserId').val();
				switch ($reportType) {
				case 'Campaign':
					var $siteId = $("#selectMedia :selected").val();
					$.post($contextPath
							+ "/report/dashboard/campaignreport/getPlacementList", {
						advertiserId:$advertiserId,
						siteId : $siteId,
						placementName : $placementName,
						reportType:$reportType
					}, function(data) {
						if(isNull(data)){
							return;
						}
						var items = eval(data);
						var html = "";
						$.each(items, function(index, item) {
							html += "<option value='" + item.placementId + "'>"
									+ item.placementName + "</option>";
						});
						$('#placementList').html(html);
					});
					break;
				case 'Placement':
					var $siteId = $("#currentSiteId").val();
					var $channelId = $("#selectChannel :selected").val();
					var $cpId = 0;
					if($('#campaignNameSa').length > 0){
						$cpId = $('#campaignNameSa :selected').val();
					}
					enableStartAndEndDate(true);
					$.post($contextPath
							+ "/report/dashboard/campaignreport/getPlacementList", {
						"advertiserId": $advertiserId,
						"campaignId": $cpId,
						"channelId":$channelId,
						"siteId" : $siteId,
						"placementName" : $placementName,
						"reportType": $reportType,
						"startDate":$.trim($('#datepicker_01').val()),
						"endDate":$.trim($('#datepicker_02').val())
					}, function(data) {
						enableStartAndEndDate(false);
						if(isNull(data)){
							return;
						}
						var items = eval(data);
						var array = [];
						if($('#targetPlacementList').find('option').length > 0){
							$('#targetPlacementList').find('option').each(function(j,item){
								array[j] = $(item).text();
							});
						}
						var html = "";
						$.each(items, function(index, item) {
							if(array.contains(item.placementName) == false){
								html += "<option value='" + item.placementId + "'>"
								+ item.placementName + "</option>";
							}
						});
						$('#placementList').html(html);
					});
					break;
				}
			});
	
	$('#campaignNameSa').change(function(){
		var reportType = $('#reportType').val();
		if(reportType == 'Placement'){
			$('#searchPlacement').click();
		}else if(reportType == 'Media'){
			loadMediaDefaultData();
		}else if(reportType == 'Campaign'){
			loadMediaDefaultData();
		}else if(reportType == 'Screenshot'){
			loadMediaDefaultData();
		}
	});
	
	$('#mediaNameSa').change(function(){
		loadDisplayTypeDefaultData();
	});
	
	$('#selectChannel').change(function(){
		$('#searchPlacement').click();
	});
})

function processSelectChangeEvent(){
	if($('#searchOrganization').next('#currentAdvId').length == 0){
		var hh="<input type='hidden' value='' id='currentAdvId'/>";
		$(hh).insertAfter($('#searchOrganization'));
	}
	var s = "<option value='0'>" + selectTit + "</option>";
	$('.filterConditionItems').html(s);
	var $advertiserId;
	if($('#organizationAdvertiserId').length > 0){
		$advertiserId = $('#organizationAdvertiserId :selected').val();
	}else{
		$advertiserId = $('#advertiserId').val();
	}
	$('#selectedAdvertiserId').attr('value',$advertiserId);
	
	var firstLow = $('#organizationTable tr:first-child');
	if(firstLow.find('#searchOrganization').length == 0){
		var btnObj = $('#searchOrganization').parent().clone();
		$('#searchOrganization').parent().remove();
		btnObj.css('margin-left','10px');
		firstLow.find('td:eq(2)').append(btnObj);
	}
	
	$('#organizationTable tr:gt(0)').remove();
	$('#organizationList').empty();
	var status=0;
	$.post($contextPath
			+ "/report/dashboard/campaignreport/getLevelList",
			{
				advertiserId : $advertiserId
			},
				function(data){
					if(isNull(data)){
						status=1;
						enableStartAndEndDate(true);
						var param = {
								"nodeId" : $advertiserId,
								"mwb" : $mwbFlag,
								"advertiserId" : $advertiserId,
								"startDate":$.trim($('#datepicker_01').val()),
								"endDate":$.trim($('#datepicker_02').val())
							};
						$.post($contextPath
								+ "/report/dashboard/campaignreport/getOrganizationHierarchyList",param, function(data) {
							enableStartAndEndDate(false);
							if(isNull(data)){
								return;
							}
							var items = eval(data);
							var html = "";
							$.each(items, function(index, item) {
								html += "<option value='" + item.campaignId + "'>"
										+ item.campaignName + "</option>";
							});
							$('#organizationList').html(html);
							OrganizationisExist();
							openOrgWhenMeetPlacementReport();
						});
						return;
					}else{
						var items = eval(data);
						var $firstRow=$('#organizationTable tr:first-child');
						var html = "<tr>";
						$.each(items,function(j,item){
							html += "<td id='"+item.parentId+"' class='levelDisplayName textR pr15' style='padding-top: 10px;'>"+item.displayName+":";
							html += "<input type='hidden' value='"+item.levelId+"'/>";
							html += "</td><td style='padding-top: 10px;'>";
							html += "<select style='width: 150px;' class='filterConditionItems'><option value='0'>" + selectTit + "</option></select></td>";
							if((j + 1) % 2 == 0){
								html += "<td>&nbsp;</td></tr><tr>";
							}
						});
						if(html.substring(html.length - 4) == '<tr>'){
							html = html.substring(0,html.length - 4);
						}else if(html.substring(html.length - 5) == '</td>'){
							html += "<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>";
						}
						$(html).insertAfter($firstRow);
						var btnObj = $('#searchOrganization').parent().clone();
						$('#searchOrganization').parent().remove();
						//btnObj.css('margin-left','20px');
						$('#organizationTable tr:last').find('td:last').append(btnObj);
						$('#organizationTable tr:last').find('td:last').css('text-align','right');
					}
					if(status==0){
						
						var $displayName = $('.levelDisplayName:eq(0)').text().replace(":", "").trim();
						$.post($contextPath
								+ "/report/dashboard/campaignreport/getOrganizationList", {
							advertiserId : $advertiserId,
							displayName : $displayName,
							parentId : 0
						}, function(data) {
							
							if(isNull(data)){
								return;
							}
							var items = eval(data);
							var html = "<option value='0'>" + selectTit + "</option>";
							$.each(items, function(index, item) {
								html += "<option value='" + item.nodeId + "'>"
										+ item.nodeName + "</option>";
							});
							$('.filterConditionItems:eq(0)').html(html);
							reflushEvent();
							if($('#organizationList').find('option').length > 0){
								return;
							}
							enableStartAndEndDate(true);
							var param = {
											"nodeId" : $advertiserId,
											"mwb" : $mwbFlag,
											"advertiserId" : $advertiserId,
											"startDate":$.trim($('#datepicker_01').val()),
											"endDate":$.trim($('#datepicker_02').val())
										};
							$.post($contextPath
									+ "/report/dashboard/campaignreport/getOrganizationHierarchyList",param, function(data) {
								enableStartAndEndDate(false);
								if(isNull(data)){
									return;
								}
								var items = eval(data);
								var html = "";
								$.each(items, function(index, item) {
									html += "<option value='" + item.campaignId + "'>"
											+ item.campaignName + "</option>";
								});
								$('#organizationList').html(html);
								OrganizationisExist();
								openOrgWhenMeetPlacementReport();
							});
						});
					}
				});
}

// 处理Organization 框中的条件。左边数据+ 右边数据= 全部数据
 function OrganizationisExist(){
	  $('#organizationList option').each(function(){
		  var leftval=$(this).val();
		  $('#targetOrganizationList option').each(function(){
			  var rightval=$(this).val();
			  if(leftval==rightval){
				  $('#organizationList option[value='+ leftval +']').remove();
			  }
		  })  
	  });
 }
 
 function openOrgWhenMeetPlacementReport(){
	 
	 var reportType = $('#reportType').val();
	 if(reportType !== "Placement"){
		 return;
	 }
	 var cpids = $('#placementCpIds').val();
	 if(cpids == null || cpids.length == 0){
		 return;
	 }
	 var args = cpids.split(',');
	 $('#organizationList option').each(function(){
		if(args.contains($.trim($(this).val())) == false){
			$(this).remove();
		} 
	 });
 }

function highlight(okClick){
	var thisOverId = okClick.parent().parent().attr('id');	
	var btnId=thisOverId+"Btn";
	 if( $('#conItemBox a[name="' + thisOverId + '"]').length > 0 ){
	      $('#' + btnId ).css('color','red');
	 }
}

function setPeriodAndAdvertiser(){
	var $period=$('#timeType :selected').text().trim();
	$("input[name='period']").attr('attr',$period);
	var $name=$('#organizationAdvertiserId :selected').text().trim();
	$("input[name='advertiserName']").attr('attr',$name);
}

function enableStartAndEndDate(flag){
	var reportType = $('#reportType').val();
	if(reportType == 'Placement'){
		return;
	}
	if(flag){
		$('#datepicker_01').attr('disabled',false);
		$('#datepicker_02').attr('disabled',false);
	}else{
		if($('#timeType').val() == "specificDate"){
			$('#datepicker_01').attr('disabled',false);
			$('#datepicker_02').attr('disabled',false);
		}else{
			$('#datepicker_01').attr('disabled',true);
			$('#datepicker_02').attr('disabled',true);
		}
	}
}

var imageStr = "";

var count = 0;

String.prototype.replaceAll  = function(s1,s2){    
	return this.replace(new RegExp(s1,"g"),s2);  
	} 

String.prototype.endWith = function(str) {
	if (str == null || str == "" || this.length == 0
			|| str.length > this.length)
		return false;
	if (this.substring(this.length - str.length) == str)
		return true;
	else
		return false;
	return true;
}

String.prototype.startWith = function(str) {
	if (str == null || str == "" || this.length == 0
			|| str.length > this.length)
		return false;
	if (this.substring(0, str.length) == str)
		return true;
	else
		return false;
	return true;
}

String.prototype.trim = function() {
	return this.replace(/(^\s*)|(\s*$)/g, "");
}
String.prototype.ltrim = function() {
	return this.replace(/(^\s*)/g, "");
}
String.prototype.rtrim = function() {
	return this.replace(/(\s*$)/g, "");
}
