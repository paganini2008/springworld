<#setting number_format="#">
<#include "head.ftl">
<style type="text/css">

	#tabBox {
		width: 100%;
		height: 100%;
	}
	    
	
	#tabContent{
		height: calc(100% - 110px);
	}
	    
</style>
<script type="text/javascript" src="${contextPath}/static/js/common.js"></script>
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
<#import "common/page.ftl" as pageToolbar>
<body>
		<div id="top">
			<#include "top.ftl">
		</div>
		<div id="container">
			<div id="left">
				<#include "nav.ftl">
			</div>
			<div id="right">
				<div id="searchBox">
					<form class="pageForm" id="searchForm" method="post" action="${contextPath}/job">
						<input type="hidden" value="${(page.page)!}" name="page" id="pageNo"/>
					</form>
				</div>
				<div id="tabBox">
				</div>
			</div>
		</div>
		<#include "foot.ftl">
</body>
</html>