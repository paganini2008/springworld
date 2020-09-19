<#setting number_format="#">
<#macro page page display>
<style style="text/css">
	#pageULBox{
		width: 100%;
		height: 30px;
		margin-top: 5px;
	}

	.pageUL {
		float: right;
		margin-right: 10px;
		height: 30px;
		width: auto;
		display: inline-block;
	}
	
	.pageUL li {
		list-style: none;
		float: left;
		display: inline-block;
		line-height: 26px;
		height: 26px;
		width: auto;
		text-align: center;
		margin: 0px 3px;
		float: left;
	}
	
	.pageAction {
		width: 45px;
	}
	
	.pageNumber {
		width: 20px;
	}
	
	.pageShow {
		width: 30px;
	}
	
</style>
<div id="pageULBox">
	<ul class="pageUL">  <!-- 翻页-->
		  <li>
		  		当页显示&nbsp;<b>${page.results?size}</b>条记录&nbsp;&nbsp;
		  		总共:&nbsp;<b>${page.rows}</b>条记录/<b>${page.totalPages}</b>页
		  </li>
		  <li class="pageAction">
		  		<a href="javascript:void(0);" onclick="javascript:goToPage(${page.firstPage})">首页</a>
		  </li>
	      <li class="pageAction">
	      		<a id="pageForPrev" href="javascript:void(0);" onclick="javascript:goToPage(${page.previousPage})">上一页</a>
	      </li>
		  <#if page.pageNos?? && page.pageNos?size gt 0>
		      <#list page.pageNos as pageNo>
		      		<li class="pageNumber">
		      			<#if pageNo == page.page>
		      				${pageNo}
		      			<#else>
		      				<a href="javascript:void(0);" onclick="javascript:goToPage(${pageNo})">${pageNo}</a>
		      			</#if>
		      		</li>
			  </#list>
		  </#if>
		  <#if page.page != page.totalPages>
		       <li class="pageAction">
		       		<a id="pageForNext" href="javascript:void(0);" onclick="javascript:goToPage(${page.nextPage})">下一页</a>
		       </li>
	      </#if>
	       <li class="pageAction">
	       		<a href="javascript:void(0);" onclick="javascript:goToPage(${page.totalPages})">末页</a>
	       </li>
	       <li>
	       		<input type="text" value="${page.page}" id="pageNoValue" style="width:40px;padding-left: 3px;"/>
	       </li>
	       <li class="pageGo">
	       		<input onclick="javascript:goToPage(-1)" type="button" value="Go" style="width: 50px; padding: 0px 10px;cursor: pointer;"/>
	       </li>
	       <#if display == 1>
	       		<li class="pageShow"> <a <#if page.size == 10>class="hoverLink" </#if> href="javascript:void(0);" onclick="javascript:setPageSize(10);"><span>10行</span></a></li>
	            <li class="pageShow"> <a <#if page.size == 50>class="hoverLink" </#if> href="javascript:void(0);" onclick="javascript:setPageSize(50);"><span>50行</span></a></li>
	            <li class="pageShow"> <a <#if page.size == 100>class="hoverLink" </#if> href="javascript:void(0);" onclick="javascript:setPageSize(100);"><span>100行</span></a></li>
		   </#if>
	</ul>
</div>
<script type="text/javascript">

	function setPageSize(pageSize){
	    var date = new Date();   
	    date.setTime(date.getTime() + (30 * 24 * 60 * 60 * 1000));
		document.cookie = "DATA_LIST_SIZE=" + pageSize + ";expires=" + date.toGMTString() + ";path=/";
		$('#pageNo').val(1);
		$(".pageForm").submit();
	}
	
	function goToPage(pageNo) {
		if(pageNo == -1){
			pageNo = $('#pageNoValue').val();
			if(pageNo == 0){
				pageNo = 1;
			}
		}
		var currentPageNo = ${page.page};
		if(pageNo == currentPageNo) {
			return;
		}
		var totalPages = ${page.totalPages};
		if(eval(pageNo) > eval(totalPages)){
			pageNo = totalPages;
		}
		if(pageNo == 0){
			pageNo = 1;
		}
		$("#pageNo").val(pageNo);
		$(".pageForm").submit();
	}
</script> 
</#macro>