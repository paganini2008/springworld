<#setting number_format="#">
<#include "head.ftl">
<style type="text/css">

    #searchBox{
    	height: 36px;
    	width: 100%;
    }

	#tabBox {
		height: calc(100% - 36px);
		width: 100%;
	}
	    
	
	#tabContent{
		height: calc(100% - 110px);
	}
	
	#saveBtn{
		width: 160px;
		height: 32px;
		padding: 3px 10px;
		cursor: pointer;
		text-align: center;
		font-weight: bold;
		float: left;
		display: inline-block;
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
	
		$('#saveBtn').click(function(){
			window.location.href= "${contextPath}/job/edit";
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
						<input type="button" value="Create or Update Job" id="saveBtn"></input>
					</form>
				</div>
				<div id="tabBox">
				</div>
			</div>
		</div>
		<#include "foot.ftl">
</body>
</html>