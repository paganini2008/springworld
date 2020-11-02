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
		
		$('.tblCom').colResizable({
			  liveDrag:true, 
	          gripInnerHtml:"<div class='grip'></div>", 
	          draggingClass:"dragging", 
	          resizeMode:'fit'
		});
		
	})
</script>
<div id="tabContent">
		<table border="0" cellspacing="0" cellpadding="0" class="tblCom" width="100%">
			<thead>
				<tr>
					<td width="3%" class="tdRight5">
						#
					</td>
					<td class="tdLeft5">
						Address
					</td>
					<td class="tdLeft5">
						InstanceId
					</td>
					<td class="tdLeft5">
						Running State
					</td>
					<td class="tdLeft5">
						Retries
					</td>
					<td class="tdLeft5">
						Execution Time
					</td>
					<td class="tdLeft5">
						Completion Time
					</td>
				</tr>
			</thead>
			<tbody>
				<#if page ?? && page.results?? && page.results? size gt 0>
					<#list page.results as bean>
						<tr traceId="${(bean.traceId? string)!}">
							<td width="3%" class="tdLeft5">
							    <a class="showLog" href="javascript:void(0);">${(page.page - 1) * (page.size) + (bean_index + 1)}</a>
							</td>
							<td class="tdLeft5">
								${(bean.address)!}
							</td>
							<td class="tdLeft5">
								${(bean.instanceId)!}
							</td>
							<td class="tdLeft5">
								${(bean.runningState.repr)!}
							</td>
							<td class="tdLeft5">
								${(bean.retries)!}
							</td>
							<td class="tdLeft5">
								${(bean.executionTime? string('yyyy-MM-dd HH:mm:ss'))!}
							</td>
							<td class="tdLeft5">
								${(bean.completionTime? string('yyyy-MM-dd HH:mm:ss'))!}
							</td>
						</tr>
					</#list>
				<#else>
					<tr>
						<td colspan="7">
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