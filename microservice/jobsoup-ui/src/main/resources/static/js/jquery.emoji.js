(function($) {
	$.fn.emoji = function(options) {
		var defaults = {
			content_el : "#content",
			list : [ {
				name : "QQ表情",
				code : "qq_",
				path : "./face/emoji1/",
				suffix : ".gif",
				max_number : 25
			}, {
				name : "emoji表情",
				code : "em_",
				path : "./face/emoji2/",
				suffix : ".png",
				max_number : 22
			} ]
		};
		var options = $.extend(defaults, options);
		var list = options.list;
		var content_el = options.content_el;
		$(this)
				.click(
						function(e) {
							if ($(".emoji-box").length <= 0) {
								var btnlist = createBtnList(list);
								var emojilist = "";
								for (var i = 0; i < list.length; i++) {
									emojilist = emojilist
											+ createEmojiList(list[i]);
								}
								var total = "<div class='emoji-box'>"
										+ btnlist
										+ emojilist
										+ "</div>";
								$(this).parent().append(total);
								$(this)
										.parent()
										.find(".emoji-btn-box span")
										.click(
												function() {
													var forid = $(this).attr("for");
													$("ul.emoji-ul").hide();
													$("#" + forid).show();
													$(".emoji-box>.emoji-btn-box>span").css("color","#ccc");
													$(this).css("color", "#66B3FF");
													return false;
												});
								$(this).parent().find(".emoji-li").click(
										function() {
											var content = $(this).attr("alt");
											content = replace_em(content);
											var contentvalue = $.trim($(content_el).html());
											$(content_el).html(contentvalue + content);
											$(document).click();
											return false;
										});
								$("ul.emoji-ul").hide();
								$(".emoji-box>.emoji-btn-box>span").first()
										.css("color", "#66B3FF")
								$("#" + list[0].code + "emoji").show();
								var offset = $(this).position();
								$(".emoji-box").css("top",
										offset.top - $(".emoji-box").height());
								$(".emoji-box").css("left", offset.left);
								$(this).parent().find("em.tip").css("top",
										offset.top + $(this).height() - 20)
								$(this).parent().find("em.tip").css("left",
										offset.left + 10)
								$(this).parent().find("em.tip2").css("top",
										offset.top + $(this).height() - 19)
								$(this).parent().find("em.tip2").css("left",
										offset.left + 10)
							} else {
								$(".emoji-box").hide();
								$("em.tip").hide();
								$("em.tip2").hide();
								$(".emoji-box").remove();
								$("em.tip").remove();
								$("em.tip2").remove();
								return false;
							}
							e.stopPropagation();
						});
		$(document).click(function() {
			$(".emoji-box").hide();
			$("em.tip").hide();
			$("em.tip2").hide();
			$(".emoji-box").remove();
			$("em.tip").remove();
			$("em.tip2").remove();
		});
	}
	function createBtnList(arr) {
		var el = "";
		for (var i = 0; i < arr.length; i++) {
			el = el + "<span for='" + arr[i].code + "emoji' class='emoji-btn'>"
					+ arr[i].name + "</span>";
		}
		return "<div class='emoji-btn-box'>" + el + "</div>";
	}
	function createEmojiList(obj) {
		var el = "";
		for (var i = 1; i <= obj.max_number; i++) {
			el = el + "<li class='emoji-li' alt='[" + obj.code + i
					+ "]'><img src='" + obj.path + i + obj.suffix
					+ "' style='max-width:24px;'/></li>";
		}
		return "<ul class='emoji-ul' id='" + obj.code + "emoji'>" + el
				+ "</ul>";
	}
})(jQuery);

function replace_em(em_alt){
	em_alt = em_alt.substr(1, em_alt.length - 1);
	var index = em_alt.indexOf('_');
	if(index > 0){
		var prefix = em_alt.substr(0,index);
		var num = parseInt(em_alt.substr(index+1));
		if(prefix === 'qq'){
			return '<img class="emoji-face" src="../img/emoji/qq2/' + num + '.gif" border="0"/>';
		}
		return '<img class="emoji-face" src="../img/emoji/' + num + '.png" border="0"/>';
	}
}