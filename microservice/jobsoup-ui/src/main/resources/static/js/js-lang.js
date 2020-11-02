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
// 重写Javascript 的toFixed方法
// Number.prototype.toFixed = function(d) {
// return fixed(this, d);
// }

function fixed(value, d) {
	var s = value + "";
	if (!d)
		d = 0;
	if (s.indexOf(".") == -1)
		return value + "";// s+=".";
	// s+=new Array(d+1).join("0");
	if (new RegExp("^(-|\\+)?(\\d+(\\.\\d{0," + (d + 1) + "})?)\\d*$").test(s)) {
		var s = "0" + RegExp.$2, pm = RegExp.$1, a = RegExp.$3.length, b = true;
		if (a == d + 2) {
			a = s.match(/\d/g);
			if (parseInt(a[a.length - 1]) > 4) {
				for ( var i = a.length - 2; i >= 0; i--) {
					a[i] = parseInt(a[i]) + 1;
					if (a[i] == 10) {
						a[i] = 0;
						b = i != 1;
					} else
						break;
				}
			}
			s = a.join("").replace(new RegExp("(\\d+)(\\d{" + d + "})\\d$"),
					"$1.$2");
		}
		if (b)
			s = s.substr(1);
		for ( var i = s.length - 1; i > s.indexOf("."); i--) {
			if (s.substr(i, 1) == "0")
				s = s.substr(0, s.length - 1);
			else
				break;
		}
		return (pm + s).replace(/\.$/, "");
	}
	return value + "";
}

// 除法函数，用来得到精确的除法结果
// 说明：javascript的除法结果会有误差，在两个浮点数相除的时候会比较明显。这个函数返回较为精确的除法结果。
// 调用：accDiv(arg1,arg2)
// 返回值：arg1除以arg2的精确结果
function accDiv(arg1, arg2) {
	var t1 = 0, t2 = 0, r1, r2;
	t1 = arg1.toString().indexOf('.') > -1 ? arg1.toString().split(".")[1].length : 0;
	t2 = arg2.toString().indexOf('.') > -1 ? arg2.toString().split(".")[1].length : 0;
	with (Math) {
		r1 = Number(arg1.toString().replace(".", ""));
		r2 = Number(arg2.toString().replace(".", ""));
		return (r1 / r2) * pow(10, t2 - t1);
	}
}

Number.prototype.div = function(arg) {
	return accDiv(this, arg);
}

// 乘法函数，用来得到精确的乘法结果
// 说明：javascript的乘法结果会有误差，在两个浮点数相乘的时候会比较明显。这个函数返回较为精确的乘法结果。
// 调用：accMul(arg1,arg2)
// 返回值：arg1乘以arg2的精确结果
function accMul(arg1, arg2) {
	var m = 0, s1 = arg1.toString(), s2 = arg2.toString();
	m += s1.indexOf('.') > -1?s1.split(".")[1].length:0;
	m += s2.indexOf('.') > -1?s2.split(".")[1].length:0;
	
	return Number(s1.replace(".", "")) * Number(s2.replace(".", ""))
			/ Math.pow(10, m);
}

Number.prototype.mul = function(arg) {
	return accMul(arg, this);
}

// 加法函数，用来得到精确的加法结果
// 说明：javascript的加法结果会有误差，在两个浮点数相加的时候会比较明显。这个函数返回较为精确的加法结果。
// 调用：accAdd(arg1,arg2)
// 返回值：arg1加上arg2的精确结果
function accAdd(arg1, arg2) {
	var r1, r2, m;
	r1 = arg1.toString().indexOf('.') > -1?arg1.toString().split(".")[1].length : 0;
	r1 = arg2.toString().indexOf('.') > -1?arg2.toString().split(".")[1].length : 0;
	
	m = Math.pow(10, Math.max(r1, r2))
	return (arg1 * m + arg2 * m) / m;
}

Number.prototype.add = function(arg) {
	return accAdd(arg, this);
};

// 减法函数
function accSub(arg1, arg2) {
	var r1, r2, m, n;
	r1 = arg1.toString().indexOf('.')>-1? arg1.toString().split(".")[1].length: 0;
	r2 = arg2.toString().indexOf('.')>-1? arg2.toString().split(".")[1].length: 0;
	m = Math.pow(10, Math.max(r1, r2));
	// last modify by deeka
	// 动态控制精度长度
	n = (r1 >= r2) ? r1 : r2;
	return ((arg2 * m - arg1 * m) / m).toFixed(n);
}
// /给number类增加一个sub方法，调用起来更加方便
Number.prototype.sub = function(arg) {
	return accSub(arg, this);
};

//格式化Money
function formatMoney(s, n) {
	n = n >= 0 && n <= 20 ? n : 2;
	s = parseFloat((s + "").replace(/[^\d\.-]/g, "")).toFixed(n) + "";
	var f = false;
	if(s.substring(0,1) == '-'){
		s = s.substring(1);
		f = true;
	}
	var a = "", r = "";
	if (s.indexOf(".") > 0) {
		a = s.split(".")[0];
		r = "." + s.split(".")[1];
	} else {
		a = s;
	}
	var l = a.split("").reverse();
	var t = "";
	for (i = 0; i < l.length; i++) {
		t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : "");
	}
	t = t.split("").reverse().join("") + r;
	if(f == true){
		t = "-" + t;
	}
	return t;
}

// 还原Money：
function restoreMoney(s) {
	// s=s.replaceAll(',','');
	return parseFloat(s.replace(/[^\d\.-]/g, ""));
}

Array.prototype.each = function(fn) {
	fn = fn || Function.K;
	var a = [];
	var args = Array.prototype.slice.call(arguments, 1);
	for ( var i = 0; i < this.length; i++) {
		var res = fn.apply(this, [ this[i], i ].concat(args));
		if (res != null) {
			a.push(res);
		}
	}
	return a;
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

Array.prototype.contains = function(element) {
	for ( var i = 0; i < this.length; i++) {
		if (this[i] == element) {
			return true;
		}
	}
	return false;
};

/*
Array.prototype.insertAfter = function(index, value) {
	var a = this.slice(0, index);
	var b = this.slice(index);
	if (Object.prototype.toString.call(value) == "[object Array]") {
		for ( var i = 0; i < value.length; i++) {
			a.push(value[i]);
		}
	} else {
		a.push(value);
	}
	return (a.concat(b));
};

Array.prototype.removeAfter = function(index, type) {
	var a = this.slice(0, index);
	var b = this.slice(index);
	a.pop();
	if (type) {
		return a;
	}
	return (a.concat(b));
};
*/

Array.prototype.remove = function(a) {
	var n = -1;
	var l = this.length;
	for (var i = 0; i < l; i++) {
		if(this[i] == a){
			n = i;
			break;
		}
	}
	if(n == -1){
		return this;
	}
	return this.slice(0, n).concat(this.slice(n + 1, l));
};

Array.prototype.union = function(a) {
	if (Object.prototype.toString.call(a) == "[object Array]"){
		return this.concat(a).uniquelize();
	}
};

Array.prototype.unionAll = function(a) {
	if (Object.prototype.toString.call(a) == "[object Array]"){
		return this.concat(a).sort();
	}
};

Array.prototype.complement = function(a) {
	if (Object.prototype.toString.call(a) == "[object Array]"){
		return this.union(a).minus(this.intersect(a));
	}
};

Array.prototype.intersect = function(a) {
	if (Object.prototype.toString.call(a) == "[object Array]"){
		return this.uniquelize().each(function(o) {
			return a.contains(o) ? o : null;
		});
	}
};
Array.prototype.minus = function(a) {
	if (Object.prototype.toString.call(a) == "[object Array]"){
		return this.uniquelize().each(function(o) {
			return a.contains(o) ? null : o;
		});
	}
};

function isURL(url) {
	var strRegex = "^((https|http|ftp|rtsp|mms)://)"
			+ "(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?"
			+ "(([0-9]{1,3}\.){3}[0-9]{1,3}" + "|" + "([0-9a-z_!~*'()-]+\.)*"
			+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\." + "[a-z]{2,6})"
			+ "(:[0-9]{1,4})?" + "((/?)|"
			+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
	var re = new RegExp(strRegex);
	if (re.test(url.toLowerCase())) {
		return true;
	} else {
		return false;
	}
}
