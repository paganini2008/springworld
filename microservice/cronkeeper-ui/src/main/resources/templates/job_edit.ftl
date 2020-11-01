<#setting number_format="#">
<#include "head.ftl">
<style type="text/css">

    #searchBox{
    	height: 60px;
    	width: 100%;
    	clear: both;
    }

	#tabBox {
		width: 100%;
		height: auto;
	}
	
	#tabContent{
		height: auto;
	}
	
	#saveBtn {
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
	
	#jsonBox {
		width: 100%;
		clear: both;
		text-align: left;
	}
	
	#jsonEditor {
		width: 100%;
	}
	
	#jsonData {
		width: 100%;
		display: none;
	}
	
	#editBtn {
		background-color: #97CBFF;
		display: inline-block;
		height: 32px;
		line-height: 32px;
		color: #fff;
		text-align: center;
		font-weight: bold;
		padding: 2px 10px;
		border-radius: 8px;
		cursor: pointer;
	}

	    
</style>
<link href="${contextPath}/static/css/json-editor.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${contextPath}/static/js/common.js"></script>
<script type="text/javascript" src="${contextPath}/static/js/lib/jquery-json-editor.js"></script>
<script type="text/javascript">
    
	var dataJson; 
    
	$(function(){
	
		var example = JSON.parse('${jobDefinition}');
		$('#jsonData > pre').html(JSON.stringify(JSON.parse('${jobDefinition}'), null, 4));
	
		$('#jsonEditor').jsonEditor(example, { change: function(data) {
				dataJson = JSON.stringify(data);
				$('#jsonData > pre').html(JSON.stringify(JSON.parse(dataJson), null, 4));
			}
	    });
	    
	    $('#saveBtn').click(function(){
	    	var url = '${contextPath}/job/save';
	    	$.ajax({
			    url: url,
				type:'post',
				contentType: "application/json; charset=utf-8",
				dataType:'json',
				data: dataJson,
				success: function(data){
				    if(data.success){
				    	alert('Save OK');
				    	window.location.href = '${contextPath}/job';
				    }else{
				    	alert('Oh, no. Reason: ' + data.msg);
				    }
				}
			});
	    });
	    
	    $('#editBtn').toggle(function(){
	    	$(this).html('Edit Json');
	    	$('#jsonData').show();
	    	$('#jsonEditor').hide();
	    },function(){
	    	$(this).html('Show Json');
	    	$('#jsonData').hide();
	    	$('#jsonEditor').show();
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
					<input type="button" value="Save Your Job" id="saveBtn"></input>
				</div>
				<div id="jsonBox">
					<div style="width: 100%; height: 36px; line-height: 36px; text-align: left;"><span id="editBtn">Show Json</span></div>
					<div id="jsonEditor" class="json-editor"></div>
					<div id="jsonData">
						<pre></pre>
					</div>
				</div>
			</div>
		</div>
		<#include "foot.ftl">
</body>
</html>