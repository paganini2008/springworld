<#import "common/page.ftl" as pageToolbar>
<script type="text/javascript">
	$(function(){
		
		TableUtils.initialize(${(page.size)!10});
		
		TableUtils.rowColour();
		
		$('.showLog').click(function(){
			var traceId = $(this).parent().parent().attr("traceId");
			var url = '${contextPath}/job/log/${jobKey!}';
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
<div id="tabContent">
		<table border="0" cellspacing="0" cellpadding="0" class="tblCom">
			<thead>
				<tr>
					<td width="12%" class="tdRight5">
						#
					</td>
					<td width="12%" class="tdLeft5">
						Address
					</td>
					<td class="tdLeft5">
						InstanceId
					</td>
					<td width="6%" class="tdLeft5">
						Running State
					</td>
					<td width="6%" class="tdLeft5">
						Running Result
					</td>
					<td width="6%" class="tdRight5">
						Retries
					</td>
					<td width="12%" class="tdRight5">
						Execution Time
					</td>
					<td width="12%" class="tdRight5">
						Completion Time
					</td>
				</tr>
			</thead>
			<tbody>
				<#if page ?? && page.results?? && page.results? size gt 0>
					<#list page.results as bean>
						<tr traceId="${(bean.traceId? string)!}">
							<td width="12%" class="tdRight5">
							    <a class="showLog" href="javascript:void(0);">${(bean.traceId? string)!}</a>
							</td>
							<td width="12%" class="tdLeft5">
								${(bean.address)!}
							</td>
							<td width="12%" class="tdLeft5">
								${(bean.instanceId)!}
							</td>
							<td width="6%" class="tdLeft5">
								${(bean.runningState)!}
							</td>
							<td class="tdLeft5">
								<#if bean.completed gt 0>
									Completed
								<#elseif bean.failed gt 0>
									Failed
								<#elseif bean.skipped gt 0>
									Skipped
								</#if>
							</td>
							<td width="6%" class="tdRight5">
								${(bean.retries)!}
							</td>
							<td width="12%" class="tdRight5">
								${(bean.executionTime? string('yyyy-MM-dd HH:mm:ss'))!}
							</td>
							<td width="12%" class="tdRight5">
								${(bean.completionTime? string('yyyy-MM-dd HH:mm:ss'))!}
							</td>
						</tr>
					</#list>
				<#else>
					<tr>
						<td colspan="8">
							<p class="tabNoData">
								No data and please search again.
							</p>
						</td>
					</tr>
				</#if>
			</tbody>
		</table>
</div>
		<#if page ?? && page.results?? && page.results? size gt 0>
			<@pageToolbar.page page = page display = 0/> 
		</#if>