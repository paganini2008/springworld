<#setting number_format="#">
<#include "head.ftl">
<style type="text/css">

    #searchBox{
    	height: 60px;
    	width: 100%;
    	clear: both;
    }

	#tabBox {
		height: auto;
		width: 100%;
		position: relative;
		bottom: 5px;
	}
	    
	
	#tabContent{
		height: auto;
	}
	
	#saveBtn{
		width: calc(100% - 5px);
		height: 36px;
		line-height: 36px;
		padding: 2px 10px;
		cursor: pointer;
		text-align: center;
		font-weight: bold;
		float: left;
		display: inline-block;
		margin: 10px auto;
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
					<form class="pageForm" id="searchForm" action="${contextPath}/job">
						<input type="hidden" value="${(page.page)!}" name="page" id="pageNo"/>
						<input type="button" value="Create Your Job" id="saveBtn"></input>
					</form>
				</div>
				<div id="tabBox">
				</div>
			</div>
		</div>
		<#include "foot.ftl">
</body>
</html>