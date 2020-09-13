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
			font-family: 微软雅黑, Arial,'Times New Roman';
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
		    width: 100%;
		    background-color: #fff;
		    overflow-y: hidden;
		    overflow-x: hidden;
		    top:50px;
		    position:absolute;
		    z-index: 0;
		    bottom:50px;
		    clear: both;
	    }
	    
	    #left{
	    	width: 15%;
	    	height: 100%;
	    	float: left;
	    }
	    
	    #right{
	    	width: 85%;
	    	height: 100%;
	    	float: left;
	    }
	    
	    #fred2020{
	    	width: 100%;
	    }
	    
</style>
<script type="text/javascript">
	$(function(){
		$('#fred2020').DataTable({
			language: {
			    "emptyTable": "No data available in the table",
			    "info": true,
			    "infoEmpty": "No entries to show",
			    "infoFiltered": " - filtered from _MAX_ records",
			    "infoPostFix": " All records shown are derived from real information.",
			    "thousands": ",",
			    "lengthMenu": "_MENU_ records per page",
			    "loadingRecords": "Loading ...",
			    "processing": "Processing ...",
			    "search": "Search",
			    "zeroRecords": "No records to display",
			    "paginate": {
			        "first": "First page",
                    "last": "Last page",
                    "next": "Next page",
                    "previous": "Previous page"
			     }
			},
			"searching": false,
			"ordering": false,
			"processing": true,
	        "serverSide": true,
	        "ajax": {
	            "url": "${contextPath}/job/detail/select",
	            "type": "POST",
	            "data": function (param) {
                    return param;
                },
                "dataSrc": function (jsonData) {
                    return jsonData.data.data;
                }
	        },
	        "bDestroy":true,
	        "lengthChange": true,
	        "autoWidth": false,
	        "bStateSave": true,
  			"paging": true,
	        "pagingType": "full_numbers",
	        "lengthMenu": [10, 25, 50, 75, 100],
	        "pageLength": 10,
	        "columns": [
	            { "data": "jobId", "title": "ID", "cellType": "td", "width": "5%"},
	            { "data": "clusterName", "title": "Cluster Name", "cellType": "td"},
	            { "data": "groupName", "title": "Group Name", "cellType": "td"},
	            { "data": "jobName", "title": "Job Name", "cellType": "td", "width": "8%"},
	            { "data": "jobClassName", "title": "Job Class Name", "cellType": "td", "width": "20%"},
	            { "data": "description", "title": "Description", "cellType": "td"},
	            { "data": "email", "title": "Email", "cellType": "td"},
	            { "data": "createDate", "title": "Create Date", "cellType": "td", "width": "10%"},
	            { "data": "triggerType", "title": "Trigger Type", "cellType": "td"},
	            { "data": "jobState", "title": "Job State", "cellType": "td"},
	            { "data": null, "title": "", "cellType": "td", "width": "10%"},
	        ],
	        "createdRow": function (row, data, dataIndex) {
               	
            },
	        "columnDefs": [
	            {
	                    "targets": -1,
	                    "data": null,
	                    "defaultContent": "<a>Edit</a><a>Execute</a><a>Delete</a>"
	            }
            ],
	        "headerCallback": function (thead, data, start, end, display) {
	        	console.log('thead ok.')
	        },
	        "footerCallback": function (tfoot, data, start, end, display) {
	        	console.log('footer ok.')
	        },
	        "initComplete": function( settings, json ) {
                console.log('Loading complete.')
            }
		});
	});
	
</script>
<body>
		<div id="top"></div>
		<div id="container">
			<div id="left"></div>
			<div id="right">
				<table id="fred2020" class="display" cellspacing="0" cellpadding="2"></table>
			</div>
		</div>
		<div id="foot"></div>
</body>
</html>