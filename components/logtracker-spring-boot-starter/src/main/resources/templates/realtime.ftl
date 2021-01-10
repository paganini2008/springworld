<#setting number_format="#">
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>LogTracker</title>
<script type="text/javascript">
	var $contextPath = '${contextPath}';
</script>
<link href="${contextPath}/static/css/base.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${contextPath}/static/js/lib/jquery-1.7.1.min.js"></script>
<script type="text/javascript" src="${contextPath}/static/js/lib/json2.js"></script>
</head>
<script>
	$(function(){
	
		setInterval(refresh, 1000);
	});
	
	function refresh(){
		var url = '${contextPath}/application/cluster/log/entry/';
		
		$.get(url, null, function(data){
			var log = '';
			$.each(data.data.results, function(i, item){
				var logEntry = '<div class="logEntry"><pre>';
				logEntry += '<font color="#FF0000"><b>[' + item.clusterName + '-' + item.applicationName + '[host=' + item.host + ', identifier=' + item.identifier + ']]: </b></font>';
				logEntry += item.datetime + ' <b class="' + item.level.toLowerCase() + '">[' + item.level.toUpperCase() + ' ]</b> ' + item.loggerName + ' - ' + item.message;
				if(item.stackTraces.length > 0){
					logEntry += '<br />';
					$.each(item.stackTraces, function(j, stackTrace){
						logEntry += stackTrace + '<br />';
					});
				}
				logEntry += '</pre></div>';
				log += logEntry;
			});
			$('#logBox').html(log);
		});
		
		
	}
</script>
<body>
	<div id="top">
		LogBox
	</div>
	<div id="container">
		<div id="searchBox">
			<form id="searchFrm" action="${contextPath}/application/cluster/log/search" method="post">
				<div class="searchCondition">
					<span>
						<label>ClusterName: </label>
						<input type="text" value="default" name="clusterName"/>
					</span>
					<span>
						<label>ApplicationName: </label>
						<input type="text" value="" name="applicationName"/>
					</span>
					<span>
						<label>Host: </label>
						<input type="text" value="" name="host"/>
					</span>
					<span>
						<label>Identifier: </label>
						<input type="text" value="" name="identifier"/>
					</span>
				</div>
				<div class="searchCondition">
					<span style="width: 50%;">
						<label>LoggerName: </label>
						<input type="text" value="" name="loggerName"/>
					</span>
					<span>
						<label>Level: </label>
						<input type="text" value="" name="level"/>
					</span>
					<span>
						<label>Marker: </label>
						<input type="text" value="" name="marker"/>
					</span>
				</div>
				<div class="searchCondition">
					<span style="width: 75%">
						<label>Keyword: </label>
						<input type="text" value="" name="keyword" id="keyword"/>
					</span>
					<span style="width: 25%">
						<input type="button" id="searchBtn" value="Search It"/>
					</span>
				</div>
			</form>
		</div>
		<div id="logBox">
		</div>
	</div>
	<div id="foot">
	</div>
</body>
</html>