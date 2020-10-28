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
		width: 90px;
		height: 32px;
		padding: 3px 10px;
		cursor: pointer;
		text-align: center;
		font-weight: bold;
		float: left;
		display: inline-block;
	}
	    
</style>
<link href="${contextPath}/static/css/json-editor.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${contextPath}/static/js/common.js"></script>
<script type="text/javascript" src="${contextPath}/static/js/lib/jquery-json-editor.js"></script>
<script type="text/javascript">
    
	$(function(){
	
		var example = JSON.parse('${jobDefinition}');
	
		$('#editor').jsonEditor(example, { change: function() {}
	    });
	    
	    $('#saveBtn').click(function(){
	    	var url = '${contextPath}/job/save';
	    	$.ajax({
			    url: url,
				type:'post',
				contentType: "application/json; charset=utf-8",
				dataType:'json',
				data: JSON.stringify(example),
				success: function(data){
				    if(data.success){
				    	alert('保存成功');
				    	window.location.href = '${contextPath}/job';
				    }else{
				    	alert('保存失败：' + data.msg);
				    }
				}
			});
	    });
	});
	
</script>
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
					<input type="button" value="Save Job" id="saveBtn"></input>
				</div>
				<div id="editor" class="json-editor"></div>
				<input type="hidden" id="json"></input>
			</div>
		</div>
		<#include "foot.ftl">
</body>
</html>