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
	
		//setInterval(refresh, 2000);
		refresh();
	});
	
	function refresh(){
		var url = '${contextPath}/application/cluster/log/entry/';
		
		$.get(url, null, function(data){
			var s = '';
			$.each(data.data.results, function(i, item){
				s += 'Message: ' + item['message'];
				s += ', Reason: ' + item['reason'];
				
			});
			$('#logBox').html(s);
		});
		
		
	}
</script>
<body>
	<div id="top">
		LogTracker
	</div>
	<div id="container">
		<div id="searchBox">
			<form id="searchFrm" action="${contextPath}/application/cluster/log/search" method="post">
				<div>
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
				<div>
					<span>
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
				<div>
					<span>
						<label>Keyword: </label>
						<input type="text" value="" name="keyword"/>
					</span>
					<span>
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