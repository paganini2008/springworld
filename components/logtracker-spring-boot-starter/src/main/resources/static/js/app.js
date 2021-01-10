$.fn.parseForm = function(){
	    var serializeObj = {};
	    var array = this.serializeArray();
	    var str = this.serialize();
	    $(array).each(function(){
	        if(serializeObj[this.name]){
	            if($.isArray(serializeObj[this.name])){
	                serializeObj[this.name].push(this.value);
	            }else{
	                serializeObj[this.name]=[serializeObj[this.name],this.value];
	            }
	        }else{
	            serializeObj[this.name]=this.value; 
	        }
	    });
	    return serializeObj;
	};

$(function(){

	//setInterval(doSearch, 1000);
		
	$('#searchBtn').click(function(){
		doSearch();
		return false;
	});

});
	
function doSearch(){
		var obj = $('#searchFrm');
		var url = obj.attr('action');
		var param = $('#searchFrm').parseForm();
		$.ajax({
			    url: url,
				type:'post',
				contentType: 'application/json;charset=UTF-8',
				dataType:'json',
				data: JSON.stringify(param),
				success: function(data){
					var log = 'No search result';
				    if(data.data.results!=null && data.data.results.length > 0){
				    	log = '';
				    	$.each(data.data.results, function(i, item){
							var logEntry = '<div class="logEntry"><pre>';
							logEntry += '<font color="#FF0000"><b>[' + item.clusterName + '-' + item.applicationName + '[host=' + item.host + ', identifier=' + item.identifier + ']]: </b></font>';
							logEntry += item.datetime + ' <b class="' + item.level.toLowerCase() + '">[' + item.level.toUpperCase() + ' ]</b> ' + item.loggerName + ' - ' + item.message;
							if(item.stackTraces.length > 0){
								logEntry += '<br />';
								$.each(item.stackTraces, function(j, stackTrace){
									logEntry += stackTrace + '<br/>';
								});
							}
							logEntry += '</pre></div>';
							log += logEntry;
						});
				    }
				    $('#logBox').html(log);
				}
			});
}