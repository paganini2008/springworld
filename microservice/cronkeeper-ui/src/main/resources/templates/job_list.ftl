<#import "common/page.ftl" as pageToolbar>
<script type="text/javascript">
	$(function(){
		
		TableUtils.initialize(${(page.size)!10});
		
		TableUtils.rowColour();
		
	})
</script>
<div id="tabContent">
		<table border="0" cellspacing="0" cellpadding="0" class="tblCom">
			<thead>
				<tr>
					<td width="3%" class="tdRight5">
						#
					</td>
					<td width="6%" class="tdLeft5">
						Cluster Name
					</td>
					<td width="8%" class="tdLeft5">
						Group Name
					</td>
					<td width="8%" class="tdLeft5">
						Job Name
					</td>
					<td width="20%" class="tdLeft5">
						Job Class Name
					</td>
					<td class="tdLeft5">
						Description
					</td>
					<td width="10%" class="tdLeft5">
						Email
					</td>
					<td width="3%" class="tdLeft5">
						Retries
					</td>
					<td width="10%" class="tdLeft5">
						Create Date
					</td>
					<td width="6%" class="tdLeft5">
						Job State
					</td>
					<td width="6%" class="tdLeft5">
						Trigger Type
					</td>
					<td width="10%" class="tdLeft5">
						Action
					</td>
				</tr>
			</thead>
			<tbody>
				<#if page ?? && page.results?? && page.results? size gt 0>
					<#list page.results as bean>
						<tr>
							<td width="3%" class="tdRight5">
							    ${(page.page - 1) * (page.size) + (bean_index + 1)}
							</td>
							<td width="6%" class="tdLeft5">
								${(bean.jobKey.clusterName)!}
							</td>
							<td width="8%" class="tdLeft5">
								${(bean.jobKey.groupName)!}
							</td>
							<td width="8%" class="tdLeft5">
								${(bean.jobKey.jobName)!}
							</td>
							<td width="15%" class="tdLeft5">
								${(bean.jobKey.jobClassName)!}
							</td>
							<td class="tdLeft5">
								${(bean.description)!}
							</td>
							<td width="10%" class="tdLeft5">
								${(bean.email)!}
							</td>
							<td width="3%" class="tdLeft5">
								${(bean.retries)!}
							</td>
							<td width="10%" class="tdLeft5">
								${(bean.createDate? string('yyyy-MM-dd HH:mm:ss'))!}
							</td>
							<td width="6%" class="tdLeft5">
								${(bean.jobRuntime.jobState.repr)!}
							</td>
							<td width="6%" class="tdLeft5">
								${(bean.jobTriggerDetail.triggerType.repr)!}
							</td>
							<td width="10%" class="tdLeft5">
								<a href="${contextPath}/job/detail/${(bean.jobKey.identifier ? html)!}">Detail</a>
								<a href="">Run</a>
								<a href="">Delete</a>
							</td>
						</tr>
					</#list>
				<#else>
					<tr>
						<td colspan="12">
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