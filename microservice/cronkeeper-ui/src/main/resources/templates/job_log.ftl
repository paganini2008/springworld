<#setting number_format="#">
<style type="text/css">
	#logBox {
		height: 100%;
	}
</style>
<script type="text/javascript">
	$(function(){
		
		$('.showError').click(function(){
			var traceId = $(this).parent().attr("traceId");
			var url = '${contextPath}/job/error/${jobKey!}';
			$.ajax({
			    url: url,
				type:'get',
				dataType:'html',
				data: {
					"traceId": traceId
				},
				success: function(data){
				    $('#tabBox').html(data);
				}
			});
		});
		
	})
</script>
<div id="logBox">
	<#list logs as log>
		<div class="logItem" traceId="${(log.traceId? string)!}">
			<#if log.level == 'ERROR'>
				${(log.createDate ? string('yyyy-MM-dd HH:mm:ss'))!} [<a class="showError" href="javascript:void(0);">ERROR</a>&nbsp;&nbsp;] - ${(log.log ? html)!}
			<#else>
				${(log.createDate ? string('yyyy-MM-dd HH:mm:ss'))!} [${(log.level ? upper_case)!}&nbsp;&nbsp;] - ${(log.log ? html)!}
			</#if>
		</div>
	</#list>
</div>
		