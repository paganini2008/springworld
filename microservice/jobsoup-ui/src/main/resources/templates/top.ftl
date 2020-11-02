<script type="text/javascript">
	$(function(){
		$('#currentClusterName').change(function(){
			var clusterName = $(this).val();
			var url = '${contextPath}/index?clusterName=' + clusterName;
			window.location.href = url;
		});
	});
	
</script>
<a id="logoText" href="https://github.com/paganini2008/springworld.git" target="_blank">JobStorm</a>
<a href="https://github.com/paganini2008/springworld.git"><img style="position: absolute; top: 0; left: 0; border: 0; z-index: 9999;" src="https://camo.githubusercontent.com/82b228a3648bf44fc1163ef44c62fcc60081495e/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f6c6566745f7265645f6161303030302e706e67" alt="Fork me on GitHub" data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_left_red_aa0000.png"></a>
<select id="currentClusterName" name="clusterName">
	<#if clusterNames?? && clusterNames? size gt 0>
		<#list clusterNames as theName>
			<option value="${theName!}" <#if theName == currentClusterName>selected="true"</#if>>${theName!}</option>
		</#list>
	</#if>
	<option value="sample1" <#if currentClusterName='sample1'>selected="true"</#if>>sample1</option>
	<option value="sample2" <#if currentClusterName='sample2'>selected="true"</#if>>sample2</option>
</select>