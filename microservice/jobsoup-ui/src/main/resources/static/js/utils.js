String.prototype.startWith = function(str) {
	var reg = new RegExp("^" + str);
	return reg.test(this);
}
String.prototype.endWith = function(str) {
	var reg = new RegExp(str + "$");
	return reg.test(this);
}

Array.prototype.contains = function(element) {
	for ( var i = 0; i < this.length; i++) {
		if (this[i] == element) {
			return true;
		}
	}
	return false;
};

Array.prototype.uniquelize = function() {
	var ra = new Array();
	for ( var i = 0; i < this.length; i++) {
		if (!ra.contains(this[i])) {
			ra.push(this[i]);
		}
	}
	return ra;
};

Array.prototype.insertAfter = function(index, value) {
	var a = this.slice(0, index);
	var b = this.slice(index);
	if (Object.prototype.toString.call(value) == "[object Array]") {
		for (var i = 0; i < value.length; i++) {
			a.push(value[i]);
		}
	} else {
		a.push(value);
	}
	return (a.concat(b));
};

function randomInt(from,to){
	return parseInt(Math.random() * (to - from) + from);
}

function randomString(n){
	return Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, n);
}

(function ($, undefined) {
    $.fn.getCursorPosition = function () {
        var el = $(this).get(0);
        var pos = 0;
        if ('selectionStart' in el) {
            pos = el.selectionStart;
        } else if ('selection' in document) {
            el.focus();
            var Sel = document.selection.createRange();
            var SelLength = document.selection.createRange().text.length;
            Sel.moveStart('character', -el.value.length);
            pos = Sel.text.length - SelLength;
        }
        return pos;
    }
})(jQuery);

var Utils = {
		
	openPopLoading: function(){
		if($('.pop-cover').length == 0){
			var html = '<div class="pop-cover">';
			html += '<div class="pop-body2">';
			html += '<img src="' + $contextPath + '/static/img/pop-loading1.gif" border="0" />';
			html += '</div></div>';
			$(html).appendTo($('#container'));
		}
		$('.pop-cover').fadeIn(600);
		
		setTimeout(function(){
			Utils.closePopCover();
		},60000);
	},
		
	openPopConfirm: function(text){
			if($('.pop-cover').length > 0){
				$('.pop-cover').remove();
			}
			var html = '<div class="pop-cover">';
			html += '<div class="pop-body">';
			html += '<div class="pop-top">';
			html += '<span class="pop-title">温馨提示</span>';
			html += '<span class="pop-close" title="关闭"></span>';
			html += '</div>';
			html += '<div class="pop-title-line"></div>';
			html += '<div class="pop-content"><span class="pop-warn"></span><span class="pop-text">'+text+'</span></div>';
			html += '</div></div>';
			$(html).prependTo($('#container'));
			
			$('.pop-close,.pop-confirm').click(function(){
				Utils.closePopCover();
			});
			
			$('.pop-cover').fadeIn(600);
			
			if(text){
				console.log('消息提示: ' + text);
			}
	},
	
	closePopCover: function(){
		if($('.pop-cover').length > 0){
			$('.pop-cover').fadeOut(600,function(){
				$(this).remove();
			});
		}
	},
		
	getFlashObject: function(){
		if(typeof window.ActiveXObject != "undefined"){
		    return new ActiveXObject("ShockwaveFlash.ShockwaveFlash");
		}else{
		    return navigator.plugins['Shockwave Flash'];
		}
	},
		
	flashChecker: function(){
			var hasFlash = 0;　　
		    var flashVersion = 0;
		    if(document.all) {
		        var swf = new ActiveXObject('ShockwaveFlash.ShockwaveFlash');
		        if(swf) {
		            hasFlash = 1;
		            VSwf = swf.GetVariable("$version");
		            flashVersion = parseInt(VSwf.split(" ")[1].split(",")[0]);
		        }
		    } else {
		        if(navigator.plugins && navigator.plugins.length > 0) {
		            var swf = navigator.plugins["Shockwave Flash"];
		            if(swf) {
		                hasFlash = 1;
		                var words = swf.description.split(" ");
		                for(var i = 0; i < words.length; ++i) {
		                    if(isNaN(parseInt(words[i]))){
		                        continue;
		                    }
		                    flashVersion = parseInt(words[i]);
		                }
		            }
		        }
		    }
		    return {
		        f: hasFlash,
		        v: flashVersion
		    };
	}
}

/**
 * 
 * JavaScript 版Map
 * 
 * @returns {Map}
 */
function Map() {
	this.elements = new Array();

	// 获取MAP元素个数
	this.size = function() {
		return this.elements.length;
	};

	// 判断MAP是否为空
	this.isEmpty = function() {
		return (this.elements.length < 1);
	};

	// 删除MAP所有元素
	this.clear = function() {
		this.elements = new Array();
	};

	// 向MAP中增加元素（key, value)
	this.put = function(_key, _value) {
		this.elements.push({
			key : _key,
			value : _value
		});
	};

	// 删除指定KEY的元素，成功返回True，失败返回False
	this.remove = function(_key) {
		var bln = false;
		try {
			for (i = 0; i < this.elements.length; i++) {
				if (this.elements[i].key == _key) {
					this.elements.splice(i, 1);
					bln = true;
				}
			}
		} catch (e) {
			bln = false;
		}
		return bln;
	};

	// 获取指定KEY的元素值VALUE，失败返回NULL
	this.get = function(_key) {
		try {
			var result = null;
			for (i = 0; i < this.elements.length; i++) {
				if (this.elements[i].key == _key) {
					result = this.elements[i].value;
				}
			}
			return result;
		} catch (e) {
			return null;
		}
	};

	this.getAll = function(_key) {
		try {
			var result = [];
			var j = 0;
			for (i = 0; i < this.elements.length; i++) {
				if (this.elements[i].key == _key) {
					result[j++] = this.elements[i].value;
				}
			}
			return result;
		} catch (e) {
			return null;
		}
	}

	// 设置MAP中指定KEY元素的值VALUE, 失败返回NULL
	this.set = function(_key, _value) {
		try {
			this.remove(_key);
			this.put(_key, _value);
		} catch (e) {
			return null;
		}
	};

	// 获取指定索引的元素（使用element.key，element.value获取KEY和VALUE），失败返回NULL
	this.element = function(_index) {
		if (_index < 0 || _index >= this.elements.length) {
			return null;
		}
		return this.elements[_index];
	};

	// 判断MAP中是否含有指定KEY的元素
	this.containsKey = function(_key) {
		var bln = false;
		try {
			for (i = 0; i < this.elements.length; i++) {
				if (this.elements[i].key == _key) {
					bln = true;
				}
			}
		} catch (e) {
			bln = false;
		}
		return bln;
	};

	// 判断MAP中是否含有指定VALUE的元素
	this.containsValue = function(_value) {
		var bln = false;
		try {
			for (i = 0; i < this.elements.length; i++) {
				if (this.elements[i].value == _value) {
					bln = true;
				}
			}
		} catch (e) {
			bln = false;
		}
		return bln;
	};

	// 获取MAP中所有VALUE的数组（ARRAY）
	this.values = function() {
		var arr = new Array();
		for (i = 0; i < this.elements.length; i++) {
			arr.push(this.elements[i].value);
		}
		return arr;
	};

	// 获取MAP中所有KEY的数组（ARRAY）
	this.keys = function() {
		var arr = new Array();
		for (i = 0; i < this.elements.length; i++) {
			arr.push(this.elements[i].key);
		}
		return arr;
	};
}