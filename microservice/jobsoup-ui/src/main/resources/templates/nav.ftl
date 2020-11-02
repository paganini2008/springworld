<script type="text/javascript">
	$(function(){
		$('#navBox li').hover(function(){
			$(this).css({'background-color':'#F0F0F0','border-radius':'8px'});
		}, function(){
			$(this).css({'background-color':'', 'border-radius':''});
		});
	});
</script>
<style type="text/css">
	#navBox li a {
		color: #000;
	}
</style>
<ul id="navBox">
	<li><a href="${contextPath}/index">Job Management</a></li>
	<li><a href="javascript:void(0);">Job Report</a></li>
	<li><a href="javascript:void(0);">Job Tools</a></li>
	<li><a href="javascript:void(0);">Quick Start</a></li>
</ul>