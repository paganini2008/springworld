<#setting number_format="#">
<#include "head.ftl">
<style type="text/css">
		*{
        	margin: 0px;
	        padding: 0px;
        }
        
        body {
            margin: 0px;
	        padding: 0px;
	        width: 100%;
	        height: 100%;
			text-align: center;
			background-color: #fff;
			font-style: normal;
			font-size: 14px;
			font-family: "Helvetica Neue",Helvetica,Arial,sans-serif;
			color: #000;
		}
		
		#top{
			height:50px;
		    line-height:50px;
		    background-color: #fff;
		    width: 100%;
		    position:absolute;
		    z-index:5;
		    top:0;
		    text-align:center;
		}
		
		#foot{
			height:50px;
		    line-height:50px;
		    background-color: #fff;
		    width: 100%;
		    position:absolute;
		    z-index:200;
		    bottom:0;
		    text-align:center;
		}
        
        #container {
		    width: calc(100% - 20px);
		    background-color: #fff;
		    overflow-y: hidden;
		    overflow-x: hidden;
		    top:50px;
		    bottom:50px;
		    left: 10px;
		    right: 10px;
		    position:absolute;
		    z-index: 0;
		    clear: both;
	    }
	    
	    #left{
	    	width: 15%;
	    	height: 100%;
	    	float: left;
	    }
	    
	    #right{
	    	width: calc(85% - 5px);
	    	height: 100%;
	    	float: left;
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
<body>
		<div id="top"></div>
		<div id="container">
			<div id="left"></div>
			<div id="right">
				<div id="searchBox">
					<form class="pageForm" id="searchForm" method="post" action="${contextPath}/job">
						<input type="hidden" value="${(page.page)!}" name="page" id="pageNo"/>
						<select id="clusterName" name="clusterName">
							<#if clusterNames?? && clusterNames? size gt 0>
								<#list clusterNames as clusterName>
									<option value="${clusterName!}">${clusterName!}</option>
								</#list>
							</#if>
						</select>
					</form>
				</div>
				<div id="tabBox">
				</div>
			</div>
		</div>
		<div id="foot"></div>
</body>
</html>