<!DOCTYPE html>
<html>
	<body>
		<div>
			<table border="0" cellspacing="0" cellpadding="0" style="font-family:Open Sans, sans-serif; font-size:14px " width="100%">
				<tbody>
					<tr>
						<td>
							<b>ClusterName: </b>${(jobKey.clusterName)!}
						</td>
						<td>
							<b>GroupName: </b>${(jobKey.groupName)!}
						</td>
					<tr>
					<tr>
						<td>
							<b>JobName: </b>${(jobKey.jobName)!}
						</td>
						<td>
							<b>JobClassName: </b>${(jobKey.jobClassName)!}
						</td>
					</tr>
					<tr>
						<td>
							<b>TraceId: </b>${traceId!}
						</td>
						<td>
							<b>StartDate: </b>${startDate!}
						</td>
					<tr>
					<tr>
						<td>
							<b>RunningState: </b>${runningState!}
						</td>
						<td>
							<b>Attachment: </b>${attachment!}
						</td>
					</tr>
				</tbody>
			</table>
		</div>
		<div>
			<#if stackTraceArray?? && stackTraceArray ? size gt 0>
				<#list stackTraceArray as trace>
					<p>
						<pre>
							${(trace.stackTrace ? html)!}
						</pre>
					</p>
				</#list>
			</#if>
		</div>
	</body>
</html>
