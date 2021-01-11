<#setting number_format="#">
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>LogBox History</title>
<link rel="shortcut icon" href="#"/>
<script type="text/javascript">
	var $contextPath = '${contextPath}';
</script>
<link href="${contextPath}/static/css/base.css" rel="stylesheet" type="text/css" />
<link href="${contextPath}/static/css/jquery-ui-1.8.7.custom.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${contextPath}/static/js/lib/jquery-1.7.1.min.js"></script>
<script type="text/javascript" src="${contextPath}/static/js/lib/jquery-ui-1.8.21.custom.min.js"></script>
<script type="text/javascript" src="${contextPath}/static/js/lib/jquery-ui-timepicker-addon.js"></script>
<script type="text/javascript" src="${contextPath}/static/js/lib/json2.js"></script>
<script type="text/javascript" src="${contextPath}/static/js/app.js"></script>
</head>
<script>
	$(function(){
		$('#startDate').datetimepicker({
		  	dateFormat: 'yy-mm-dd',
	        timeFormat: 'HH:mm:ss'
		});
		
		$('#endDate').datetimepicker({
		    dateFormat: 'yy-mm-dd',
	        timeFormat: 'HH:mm:ss'
		});
	});
</script>
<body>
	<div id="top">
		<label id="title">LogBox (v2.0)</label>
	</div>
	<div id="container">
		<div id="searchBox">
			<form id="searchFrm" action="${contextPath}/application/cluster/log/history/search" method="post">
				<div class="searchCondition">
					<span>
						<label>Cluster Name: </label>
						<input type="text" value="default" name="clusterName"/>
					</span>
					<span>
						<label>Application Name: </label>
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
						<label>Logger Name: </label>
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
						<b>升序</b><input type="radio" value="true" name="asc" checked="true"/>
						<b>降序</b><input type="radio" value="false" name="asc"/>
						<input type="button" id="searchBtn" value="Search It"/>
					</span>
				</div>
				<div class="searchCondition">
					<span style="width: 25%">
						<label>Start Date: </label>
						<input type="text" value="${startDate!}" name="startDate" id="startDate" />
					</span>
					<span style="width: 25%">
						<label>End Date: </label>
						<input type="text" value="${endDate!}" name="endDate" id="endDate" />
					</span>
					<span style="width: 50%">
					</span>
				</div>
			</form>
		</div>
		<div id="logBox" style="height: calc(100% - 150px);">
		</div>
	</div>
	<div id="foot">
		Spring Dessert Series
	</div>
</body>
</html>