<script type="text/javascript">
	$(function(){
		$('#currentClusterName').change(function(){
			var clusterName = $(this).val();
			var url = '${contextPath}/index?clusterName=' + clusterName;
			window.location.href = url;
		});
	});
	
</script>
<img src="${contextPath}/static/img/spring-logo.jpg" style="height: 70px;width: 220px;float: left; display: inline-block;">
<label id="logoText">
	Cronkeeper UI
</label>
<select id="currentClusterName" name="clusterName">
	<#if clusterNames?? && clusterNames? size gt 0>
		<#list clusterNames as theName>
			<option value="${theName!}" <#if theName == currentClusterName>selected="true"</#if>>${theName!}</option>
		</#list>
		<option value="sample1" <#if currentClusterName='sample1'>selected="true"</#if>>sample1</option>
		<option value="sample2" <#if currentClusterName='sample2'>selected="true"</#if>>sample2</option>
	</#if>
</select>