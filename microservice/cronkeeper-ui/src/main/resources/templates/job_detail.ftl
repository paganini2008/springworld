<#setting number_format="#">
<#include "head.ftl">
<style type="text/css">

	#tabBox {
		width: 100%;
		height: 100%;
	}
	
	#tabContent{
		height: calc(100% - 210px);
	}
	
	#jobDetail div {
		clear: both;
		height: 32px;
		line-height: 32px;
		width: 100%;
	}
	    
	#jobDetail div label{
		width: 170px;
		height: 32px;
		line-height: 32px;
		display: inline-block;
		text-align: right;
		padding-right: 5px;
		float: left;
		vertical-align: top;
		font-weight: bold;
	}
	
	#jobDetail div span{
		display: inline-block;
		float: left;
		text-align: left;
		height: 32px;
		line-height: 32px;
		width: auto;
	}
	    
</style>
<script type="text/javascript">
	$(function(){
		$('#searchForm').submit(function(){
			var obj = $(this);
			var url = obj.attr('action');
			$.ajax({
			    url: url,
				type:'post',
				dataType:'html',
				data: obj.serialize(),
				success: function(data){
				    $('#tabBox').html(data);
				}
			});
			return false;
		});
	
		onLoad();
	});
	
	function onLoad(){
		$('#searchForm').submit();
	}
	
</script>
<script type="text/javascript" src="${contextPath}/static/js/common.js"></script>
<body>
		<div id="top">
			<#include "top.ftl">
		</div>
		<div id="container">
			<div id="left">
				<#include "nav.ftl">
			</div>
			<div id="right">
				<div id="jobDetail">
					<div class="jobKey">
						<label>Cluster Name:</label>
						<span id="clusterName">${(jobDetail.jobKey.clusterName)!}</span>
						<label>Group Name:</label>
						<span id="groupName">${(jobDetail.jobKey.groupName)!}</span>
						<label>Job Name:</label>
						<span id="jobName">${(jobDetail.jobKey.jobName)!}</span>
						<label>Job Class Name:</label>
						<span id="jobClassName">${(jobDetail.jobKey.jobClassName)!}</span>
					</div>
					<#assign triggerType = jobDetail.jobTriggerDetail.triggerType.value!>
					<#assign tdObj = jobDetail.jobTriggerDetail.triggerDescriptionObject>
					<div class="triggerDetail">
						<label>Trigger Type:</label>
						<span id="triggerType">${(jobDetail.jobTriggerDetail.triggerType.repr)!}</span>
						<label>Start Date:</label>
						<span id="triggerStartDate">${(jobDetail.jobTriggerDetail.startDate?string('yyyy-MM-dd HH:mm:ss'))!'-'}</span>
						<label>End Date:</label>
						<span id="triggerEndDate">${(jobDetail.jobTriggerDetail.endDate?string('yyyy-MM-dd HH:mm:ss'))!'-'}</span>
						
						<#if triggerType == 1>
							<label>Trigger Description:</label>	
							<span id="triggerDescription">
								${(tdObj.cron?string)!}
							</span>
						<#elseif triggerType == 2>
							<label>Trigger Description:</label>	
							<span id="triggerDescription">
								${(tdObj.periodic?string)!}
							</span>
						</#if>
					</div>
					<#if triggerType == 3>
						<div><label>Trigger Description:</label>
						<pre id="triggerDescription" style="width: calc(100% - 170px);float: left;">${(tdObj.serial?string?trim)!}</pre>
						</div>
					</#if>
					<div class="jobRuntime">
						<label>Job State:</label>
						<span id="jobState">${(jobDetail.jobRuntime.jobState.repr)!}</span>
						<label>Last Running State:</label>
						<span id="lastRunningState">${(jobDetail.jobRuntime.lastRunningState.repr)!}</span>
						<label>Last Execution Time:</label>
						<span id="lastExecutionTime">${(jobDetail.jobRuntime.lastExecutionTime?string('yyyy-MM-dd HH:mm:ss'))!}</span>
						<label>Last Completion Time:</label>
						<span id="lastCompletionTime">${(jobDetail.jobRuntime.lastCompletionTime?string('yyyy-MM-dd HH:mm:ss'))!}</span>
					</div>
				</div>
				<div id="searchBox" style="clear: both;">
					<form class="pageForm" id="searchForm" method="post" action="${contextPath}/job/trace">
						<input type="hidden" value="${(page.page)!}" name="page" id="pageNo"/>
						<input type="hidden" value="${(jobDetail.jobKey.identifier)!}" name="jobKey"/>
					</form>
				</div>
				<div id="tabBox">
				</div>
			</div>
		</div>
		<#include "foot.ftl">
</body>
</html>