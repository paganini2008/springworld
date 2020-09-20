<div id="stackTraceBox" style="height: calc(100% - 210px);">
	<#list stackTraceArray as trace>
		<div class="traceItem">
		<pre>
		${(trace.stackTrace ? html)!}
		</pre>
		</div>
	</#list>
</div>