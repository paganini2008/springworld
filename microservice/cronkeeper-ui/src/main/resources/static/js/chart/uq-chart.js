var UQChart = UQChart||{};
UQChart.otherClass = UQChart.otherClass||{};
UQChart.isIE = function(){
	var flag = (navigator.userAgent.indexOf('MSIE')>-1)? true:false;
	return flag;
}


//canvas基本类
UQChart.otherClass.BaseCanvas = function(obj){
	var t     = this;
	t.obj     = obj;
	t.id      = obj.id;
	t.data    = obj.data;
	t.canBox  = UQChart.util.getObj(this.id);
	t.rectS   = 10;
	canvasInit();
	//初始化，canvas的大小，取ctx
	function canvasInit(){
		try{
			var canBox = t.canBox;
			t.width    = canBox.Wid();
			t.height   = canBox.Hei();
			canBox.setAttr({"width":t.width-2,"height":t.height});
			t.ctx      = canBox.getContext('2d');
		}catch(e){
			return;
		}
	}
}

//双Y坐标轴类,绘制坐标轴,单Y，双Y
//实例参数解释，axisW坐标轴的长; axisRect坐标轴的四个角的坐标; axisXAry,X轴上数据，showDotAry,很重要的一个参数，存储需要显示的点的坐标
UQChart.otherClass.DrawAxis = function(type){
    var t        = this;
	var width    = t.width;
	var height   = t.height;
	var ctx      = t.ctx;
	var util     = UQChart.util;
	var mData    = t.data.mainData;
	var bData    = t.data.base;
	t.mData      = mData;
	t.bData      = bData;
	t.axisW      = 0;
	t.axisH      = 0;
	t.leftY      = null;
	t.rightY     = null;
	t.axisRect   = [];
	t.axisXAry   = [];
	t.showDotAry = [];
	t.step       = bData.step?bData.step:5;

	drawAxisInit();
	
	function drawAxisInit(){
		var leftAry  = mData.dataObj.leftData;
		setYSide(leftAry,"leftY");
		if(type === "double"){
			var rightAry = mData.dataObj.rightData;
			setYSide(rightAry,"rightY");
		}

		drawXYLine();

		drawYNum("leftY");
		if(type === "double"){
			drawYNum("rightY");
		}
		writeYname();
	}

	//计算Y轴离2边的边距，最大值，步长,每步的数值
	function setYSide(yAry,sideY){
		var that      = this;
		var maxVal    = 0;
		var stepNum   = 0;
		var axisMax   = 0;
		var toSide    = 0;
		var axisMaxStr= 0;
		var leftDist  = 0;
		var step      = t.step;

		//某边没有数据时,新的设置
		if(yAry.length === 0){
			if(sideY === 'leftY'){
				t[sideY] = {
					"stepNum":1,
					"axisMax":step,
					"toSideNum":55
				}
			}else{
				t[sideY] = {
					"stepNum":1,
					"axisMax":step,
					"toSideNum":width - 55
				}
			}	
		}else{
			
			maxVal      =  util.getAryMaxValue(yAry);
			if(sideY !== 'leftY') maxVal = lowY(maxVal);
			stepNum     = getStepNum(maxVal,step);
			if(!stepNum) stepNum = 5;
			axisMax     = stepNum*step;
			axisMaxStr  = util.getSplitNum(axisMax);
			leftDist    = axisMaxStr.length*5+60;
			toSide      = (sideY === 'leftY')?leftDist:(t.width - leftDist);

			t[sideY] = {
				"stepNum":stepNum,
				"axisMax":axisMax,
				"toSideNum":toSide
			}
	    }	
	}
	
	//右边Y轴的数据只在1/that[sideY].axisMax/average一下的高度活动，低位活动,左边的平均值
	function lowY(maxVal){
		if(maxVal=== 10) return maxVal;
		var yData    = mData.dataObj.leftData;
		var allNum   = 0;
		var n        = 0;
		var average  = 0;
		var multiple = 0;
		if(yData.length ===0 ) return maxVal;

		for(var i=0,len=yData.length; i<len; i++){
			for(var j=0,leng=yData[i].length;j<leng; j++){
				if(parseInt(yData[i][j]).toString() !== 'NaN'){
					allNum += parseInt(yData[i][j]);
					n++;
				}
			}
		}

		average  = parseInt(allNum/n);
		multiple = parseInt(t["leftY"].axisMax)/average;
		if(multiple<3) multiple = 3;
		maxVal   = multiple*maxVal;
		return maxVal;
	}

	//绘制Y轴名字
	function writeYname(){
	    var nameAry  = mData.yNameList;
		var str      = '';
		var strWidth = 0;
		var offset   = $('#'+t.id).offset();
		var offTop   = offset.top;
		var offLeft  = offset.left;
		var wTop     = 0;
		var wLeft    = 0;
		
		for(var i=0,len=nameAry.length; i<len; i++){
		   str           = nameAry[i];
		   ctx.fillStyle = "#1752ab";
		   ctx.font      = "13px sans-serif";
		   strWidth      = ctx.measureText(str).width;
		   wTop          = offset.top +(height-strWidth)/2 + strWidth*0.45;
		   //计算方法，offLeft + 3是正常偏移位置，(strWidth-14)/2是旋转后位置，+8/-4是微调位置
		   wLeft = i===0? (offLeft + 3 - (strWidth-14)/2 + 8):(offLeft + width - 24-(strWidth-14)/2 - 4);
		   $('<div class="bodyUqNewDiv" style="-webkit-transform:rotate(-90deg); -moz-transform:rotate(-90deg); transform:rotate(-90deg); font-size:12px; color:#417dd7; position:absolute; width:'+ strWidth +'px; height:14px; top:'+ wTop +'px; left:'+ wLeft +'px;">' + str + '</div>').appendTo($(document.body));
		}
		//ctx.rotate(-Math.PI/2);
	}

	//计算Y轴每一步的长度,传入一个最大值
	function getStepNum(maxVal,step){
		if(maxVal === 0 ) return 0;
		var str      = '';
		var stepNum  =  maxVal /step;
		if(stepNum>=1){
			str       = Math.floor(stepNum).toString();
			stepNum = (parseInt(str.slice(0,1)) + 1)*Math.pow(10,str.length-1);
		}else{
		    str       = stepNum.toString();
			var end   = str.match(/[^(0|\.)]/).index+1;
			var num   = parseInt(str.slice(end-1,end));
			stepNum   = (num ===9)? parseInt(str.slice(0,end-2) + '1',10) : parseInt(str.slice(0,end-1) + (num+1).toString());
		}
		return stepNum;
	}

	//绘制x,y坐标轴主线
    function drawXYLine(){
        var ary     = [];
        //坐标轴位置,待调整
		var toTop   = 40;
		var toLeft  = t.leftY.toSideNum;
		var toBom   = height - 28;
        var toRight = t.rightY.toSideNum;
		t.axisW     = toRight - toLeft;
		t.axisH     = toBom - toTop;
		t.axisRect  = [{x:toLeft,y:toTop},{x:toLeft,y:toBom},{x:toRight,y:toBom},{x:toRight,y:toTop}];
		ary         = t.axisRect;

		drawAxis(ary);
		drawXname();
		
		if(bData.bgGrid){
		    drawBg(ary);
		}
		//绘制标题颜色示意图
		if(bData.legend){
			drawBlock();
		}
	}

	//绘制坐标轴,此处才是真正的绘制，用 html5的js API
    function drawAxis(ary){
		//坐标轴颜色,与系统以前风格一致
		ctx.strokeStyle = "#945532";
		ctx.lineWidth   = 2;
		ctx.beginPath();
		ctx.moveTo(ary[0].x,ary[0].y);
		for(var i=1,len=ary.length; i<len; i++){
            ctx.lineTo(ary[i].x,ary[i].y);
		}
		ctx.stroke();
	}

	//绘制X坐标轴上的对应点,名字
	function drawXname(){
		var axisW    = t.axisW;
		var nameAry  = t.data.mainData.xNameList;
		var blockNum = nameAry.length - 1;
		var stepW    = axisW/(blockNum+1);
		var x        = stepW/2 + t.axisRect[0].x;
		var y        = t.axisRect[1].y;

		var curText     = '';
		var curWidth    = 0;
		ctx.strokeStyle = "#945532";
		ctx.fillStyle   = "#000000";
		ctx.font        = "12px sans-serif";
		ctx.lineWidth   = 2;
		for(var i=0,len=nameAry.length; i<len; i++){
			ctx.beginPath();
		    ctx.moveTo(x,y);
			ctx.lineTo(x,y+5);
			ctx.stroke();
            ctx.closePath();
			curText  = nameAry[i];
			curWidth = ctx.measureText(curText).width;
			ctx.fillText(curText,x-curWidth/2,y+18);
			t.axisXAry.push(Math.round(x));
			x += stepW;
		}
	}

    //画小方框，标识颜色图示
	function drawBlock(){
		var leftLen = mData.dataObj.leftData.length;
		var colorAry= mData.color;
		var titleAry= mData.title;
		var toLeft  = t.axisRect[0].x;
		var toTop   = t.axisRect[0].y;
		var x       = toLeft;
		var y       = toTop - 22;
		var text    = titleAry[0];
		var textW   = 0;
		for(var i=0,len=colorAry.length; i<len; i++){
			//画矩形方块
			ctx.fillStyle = colorAry[i];
		    ctx.fillRect(x,y,10,10);
			//写上标题
			x = x + 15;
			y = y + 9;
			ctx.fillStyle = "#000000";
			ctx.font = '12px 宋体';
			ctx.fillText(titleAry[i],x,y);
			textW = ctx.measureText(titleAry[i]).width;
			y = y - 9;
			x = x + textW + 20;
		}
	}

	//绘制y轴上的数据
	function drawYNum(direction){
		var obj         = t[direction];
		var stepNum     = obj.stepNum;
		var step        = t.step;
		var x           = obj.toSideNum;
		var y           = t.axisRect[1].y;
		var yStepNum    = Math.floor(t.axisH/step);
		var yText       = '';
		
		ctx.strokeStyle = "#945532";
		ctx.fillStyle   = "#000000";
		ctx.font        = "12px sans-serif"
		ctx.lineWidth   = 2;
		if(direction === "leftY"){
			ctx.textAlign = "right";
		}else{
			ctx.textAlign = "left";
		    x+=5;
		}
		y = t.axisRect[1].y;
		var txtOffSet = 0;
		
		for(var i=0; i<step+1; i++){
			ctx.beginPath();
		    ctx.moveTo(x,y);
			ctx.lineTo(x-5,y);
			y -= yStepNum;
			ctx.stroke();
			ctx.closePath();
			yText = util.getSplitNum(i*stepNum);

			if(direction === "leftY"){
				txtOffSet = x-12;
			}else{
				txtOffSet = x+7;
			}
			ctx.fillText(yText,txtOffSet,y+yStepNum+3);
		}
	}

	//绘制背景坐标轴网格背景
	function drawBg(ary){
		var gNum = 40;
		var x    = ary[0].x + gNum;
		var y    = ary[1].y ;
		var xEnd = ary[2].x;
		var yEnd = ary[0].y;
		var xAry = t.axisXAry;

		//背景网格颜色
		ctx.strokeStyle = "#f3ede3";
		ctx.lineWidth   = 1;
		
		//X轴画线条
		var x = 0;
		var y = ary[1].y - 1;
		for(var i=0; i<xAry.length; i++){
			x = xAry[i];
		    ctx.beginPath();
			ctx.moveTo(x,y);
			ctx.lineTo(x,y-t.axisH);
			ctx.stroke();
			ctx.closePath();
		}
		
		//Y轴画线条
		var yStepNum = t.axisH/t.step;
		x = ary[0].x + 1;
		y = ary[1].y - yStepNum +1;
		for(var j=0; j<t.step-1; j++){
		    ctx.beginPath();
			ctx.moveTo(x,y);
			ctx.lineTo(x+t.axisW-2,y);
			ctx.stroke();
			ctx.closePath();
			y -= yStepNum;
		}
	}

	//取得某数组数据，对应的页面坐标轴,此方法算是坐标轴的公用方法
	t.getAxis = function(title,sideY,ary){
		var newAry  = [];
		var sideObj = t[sideY];
		var axisH   = parseInt(t.axisH);
		var axisMax = parseInt(sideObj.axisMax);
		var toBom   = t.axisRect[1].y;
		var splitFn = UQChart.util.getSplitNum;
		for(var i=0,len=ary.length; i<len; i++){
			var newObj = {};
			newObj.x   = t.axisXAry[i];
			newObj.y   = Math.round(toBom - parseInt(ary[i])/axisMax*axisH);
			t.showDotAry.push([newObj.x-4,newObj.x+4,newObj.y-4,newObj.y+4,title+'<br/><b>' + splitFn(ary[i])+'</b>']);
		    newAry.push(newObj);
		}
		return newAry;
	}
}

//Overlap的具体实现
UQChart.otherClass.OverlapInit= function(){
   var t        = this;
   var data     = t.data;
   var width    = t.width;
   var height   = t.height;
   var canBox   = t.canBox;
   var ctx      = t.ctx;
   var id       = t.id;
   var numAry   = data.num;
   var titAry   = data.title;
   var rateText = data.rateText;
   var sqrtAry  = [];
   var ratioAry = [];
   var cirCenter= [];
   var rectObj  = [];
   var mySqrt   = function(n){return Math.sqrt(n)}
   var startXNum= Math.round(width*2/3);
   var r1,r2,overR;

   var color    = ["#ee352c","#ffd800","#2564e3"];
   var that     = this;
   if(data.color&&data.color.length===3){
	   color    = data.color;
   }
   var pBox     =  $('<div style="position:relative;"></div>');
   pBox.insertBefore($('#'+ id));
   $('#'+ id).appendTo(pBox);
   getRaduis();
   drawCircle();
   drawTit();
   evt();

   //计算半径r1,r2,ovreR,根据位置坐标等，高度，最大不超过height的2/3,长度，不超过width的2/3*2/3,
   function getRaduis(){
	   var _rNum = 0;
	   var _R    = 0;
	   var _bigN = 0;
	   //var toFixed(n) = function(n){return n.toFixed()}
	   for(var i=0,len=numAry.length; i<len; i++){
		   sqrtAry[i] = mySqrt(parseInt(numAry[i])).toFixed(3);
	   }
	   for(i=0,len=sqrtAry.length; i<len;i++){
		   ratioAry[i]= (i===len-1)?1:Number((sqrtAry[i]/sqrtAry[len-1]).toFixed(2));
		   _rNum += ratioAry[i];
	   }
	   overR = Number((2/3*startXNum/_rNum).toFixed(2));
	   if(ratioAry[0] > ratioAry[1]){
		   _R    = ratioAry[0]*overR;
		   _bigN = 0;
	   }else{
		   _R    = ratioAry[1]*overR;
		   _bigN = 1;
	   }
	   if(2*_R >4/5*height){
		   _R    = Number((height*2/5).toFixed(2));
		   overR = Number((_R/ratioAry[_bigN]).toFixed(2));
		   if(_bigN===0){
			   r1 =  _R;
			   r2 =  Number((overR* ratioAry[1]).toFixed(2));
		   }else{
			   r1 =  Number((overR* ratioAry[1]).toFixed(2));
			   r2 =  _R;
		   }
	   }
   }

   //根据半径绘圆
   function drawCircle(){
	   var _y       = Math.round(height/2);
	   var _toStart = Math.round((startXNum - 2*r1 - 2*r2 + overR)/2);
	   var _x1      = width - startXNum + r1 + _toStart;
	   var _x2      = _x1 + r1 + r2 - overR;
	   var _xAry    = [_x1,_x2];
	   var _rAry    = [r1,r2];
	   var bigR     = r1>r2? r1:r2;

	   //2个矩形的4个角
	   var _rectX1,_rectX2,_rectY1,_rectY2;
	   _rectX1      = _x1 - bigR;
	   _rectX2      = _x2 + bigR;
	   _rectY1      = _y - bigR;
	   _rectY2      = _y + bigR;

	   //圆心的位置
	   cirCenter    = [{x:_x1,y:_y},{x:_x2,y:_y}];
	   rectObj      = [{x:_rectX1,y:_rectY1},{x:_rectX2,y:_rectY1},{x:_rectX2,y:_rectY2},{x:_rectX1,y:_rectY2}];

	   for(var j=0;j<3;j++){
		   var n = j;
		   if(j===1){
			  ctx.fillStyle = color[2];
		   }else if(j===2){
			  ctx.fillStyle = color[1];
		   }else{
			  ctx.fillStyle = color[0];
		   }
		   ctx.beginPath();
		   if(j===1){
			  ctx.globalCompositeOperation = "source-atop";
		   }else if(j===2){
			  ctx.globalCompositeOperation = "destination-over";
		   }
		   if(j===2){
			  n = 1;
		   }
		   ctx.arc(_xAry[n],_y,_rAry[n],0,2*Math.PI,false);
		   ctx.closePath();
		   ctx.fill();
	   }
   }

   function drawTit(){
	   var _x    = 20;
	   var _y    = 45;
	   var _side = that.rectS;
	   var _text = '';
	   for(var i=0;i<3;i++){
		   _text = rateText + '   ' + (numAry[2]/numAry[i]*100).toFixed(2) + '% <br/>' + titAry[i];
		   if(i === 2){
			   _text =  titAry[i];
		   }
		   $('<p style="position:absolute;  z-index:2; top:'+ (_y*i + 34) +'px; left:'+ (_x + 5) +'px; display:block;  width:10px; height:10px; background:'+ color[i] + ';"> </p>').appendTo(pBox);
		   $('<div style="position:absolute; text-align:left; top:'+ (_y*i + 30) +'px; left:'+ (_x + 20) +'px;">'+ _text + ' </div>').appendTo(pBox);
		   /*
		   ctx.fillStyle = color[i];
		   ctx.fillRect(_x,_y,_side,_side);

		   ctx.fillStyle = "#292929";
		   ctx.font = "12px serif";
		   
		   
		   if(i!==2){
			   $('<div style="position:absolute; top:'+ _y+'; left:'+ (_x+5) +'"> '+ rateText + '<br/>' +rateText+ ' </div>').appendTo(pBox);
			   _y += 25;
			  // ctx.fillText(rateText + '   ' + (numAry[2]/numAry[i]*100).toFixed(2) + '%',_x+15,_y);

		   }else{
			   ctx.fillText(titAry[i],_x+15,_y+9);
		   }
		   
		   _y += 15;
		   */
	   }
   }

   //注册事件
   //用jquery 做，以后在修改
   function evt(){
	   $("#"+id).mousemove(function(e){
		   var curObj  = $(this);
		   var offset  = curObj.offset();
		   var _x      = e.pageY - offset.top;
		   var _y      = e.pageX - offset.left;

		   //鼠标在矩形在域内
		   /*
		   if(_x>rectObj[0].x && _x<rectObj[1].x && _y>rectObj[0].y && _y<rectObj[2].y){

		   }
		   var imgData = ctx.getImageData(_x,_y,_x+1,_y +1);
		   var pixels  = imgData.data;
		   for(var i=0,len=pixels.length; i<len; i++){
				pixels[i*4]   = 255-pixels[i*4];    //红
				pixels[i*4+1] = 255-pixels[i*4+1];  //绿
				pixels[i*4+2] = 255-pixels[i*4+2];  //蓝
		   }
		   */

	   })

	   /*
	   var evtO = UQChart.util.evt;
	   canBox.onmouseover = function(e){

		   var e      = evtO.getEvent(e);
		   var obj    = evtO.getTarget(e);
		   var objoff = obj.getEOffset(e);
	   }

	   canBox.addEvt("mousemove",function(e){
		   var e      = canBox.getE(e);
		   var offset = canBox.getEOffset(e);

		   var left   = offset.left;
		   var top    = offset.top;
		   var imgData= ctx.getImageData(left,top,left+1,top+1);
		   var pixels = imgData.data;

		   for(var i=0,len=pixels.length; i<len; i++){
				pixels[i*4] = 255-pixels[i*4];    //红
				pixels[i*4+1] = 255-pixels[i*4+1];  //绿
				pixels[i*4+2] = 255-pixels[i*4+2];  //蓝
		   }
	   })
	   */
   }

}




/***对html对象的简单扩展,以及其他通用功能***/
UQChart.util = {
	//获取（可能的几个）数组中最大的数
	getAryMaxValue:function(yAry){
		var newAry    = [];
		var maxNum    = 0;
		if(yAry.length>1){
			newAry    = yAry[0];
            for(var i=1,len = yAry.length; i<len; i++){
                newAry  = newAry.concat(yAry[i]);
            }
		}else{
			newAry = yAry[0];
		}
		maxNum =  this.getMaxVal(newAry);
		if(maxNum===0){
		    maxNum = 10;	
		}
		return maxNum;
	},
	getMaxVal:function(ary){

		var maxVal = Number(ary[0]);
		if(ary.length>1){
			for(var i=1,len=ary.length; i<len; i++){
				ary[i] = Number(ary[i]);
				if(ary[i]>maxVal){
					maxVal = ary[i];
				}
			}
		}
		
		return maxVal;
	},
	//获取一个数的三位分割,如120030,为120,030
	getSplitNum:function(num){
		var str    = num.toString();
		var strAry = str.split('.');
		var newStr = '';
		var strLen = strAry[0].length;

		if(strLen>3){
		   	var stepLen  = Math.ceil(strLen/3);
			var splitStr = ',';
			var startPos = 0;

			for(var i=0; i<stepLen; i++){
				if(i=== stepLen-1)  splitStr = '';
				startPos = strLen-3*(i+1);
				if(startPos<0) startPos = 0;
				newStr = splitStr + str.slice(startPos,strLen-3*i) + newStr;
			}

			if(strAry.length>1){
				newStr = newStr + '.' + strAry[1];
			}else{
			    newStr = newStr + '.00';
			}	
		}else{
			newStr = str;
		}
		return newStr;
	},
    getObj:function(id){
		var obj = document.getElementById(id);
		this.AddClass.call(obj);
	    return obj;
    },
	AddClass:function(){
		var t = this;

		//设置属性的方法
		t.setAttr = function(){
			var args = arguments;
			if(args.length === 2){
			   	t.setAttribute(args[0],args[1]);
			}else if(args.length === 1 && typeof args === "object"){
			    for(var k in args[0]){
				    t.setAttribute(k,args[0][k]);
			    }
			}
		}

		//获取样的方法
		t.getStyle = function(name){
		    if(t.style[name]){
			    return t.style[name];
			}else if(t.currentStyle){
			    return t.currentStyle[name];
			}else if(document.defaultView&&document.defaultView.getComputedStyle){
			    name  = name.replace(/([A-Z])/g,"-$1");
				name  = name.toLowerCase();
				var s = document.defaultView.getComputedStyle(this,"");
				return s&&s.getPropertyValue(name);
			}else{
			    return null;
			}
		}

		//设置样式,可单独设置，也可传对象{}
		t.setStyle = function(){
			var arg = arguments;
			var old;
			if(arg.length === 2){
				old =  t.getStyle[arg[0]];
			    t.style[arg[0]] = arg[1];
				return old;
			}else if(arg.length === 1 && typeof arg[0] === "object"){
			    var str = '';
				old = {}
				for(var k in arg[0]){
					str += k +":"+ arg[0][k] +";";
					old[k] = arg[0][k];
				}
				t.style.cssText += ";" + str;
				return old;
			}else{
			    return null;
			}
		}

		//获取样式长宽的数值
		var getStyNum = function(name){
			var str = t.getStyle(name);
			var num = 0;
			if(str.indexOf("%")>-1){
				num = t.offsetWidth;
			}else{
				num = parseInt(str.replace('px',''));
			}
		    return num;
		}

		//获取元素全高度(显示或隐藏)
		t.Hei = function(){
		    if(t.getStyle("display")!=="none"){
			    return getStyNum("height")||t.offsetHeight;
			}else{
			    var old = t.setStyle({display:"block",visibility:"hidden",position:"absolute"});
				var h   = t.clientHeight || getStyNum("height");
				t.setStyle(old);
				return h;
			}
		}

		//获取元素全长度(显示或隐藏)
		t.Wid = function(){
		    if(t.getStyle("display")!=="none"){
			    return getStyNum("width")||t.offsetWidth;
			}else{
			    var old = t.setStyle({display:"block",visibility:"hidden",position:"absolute"});
				var w   = t.clientWidth || getStyNum("width");
				t.setStyle(old);
				return w;
			}
		}

		//获取光标相对于当前元素的位置
		t.getEOffset = function(e){
			var  obj = {};
			obj.left = (e&&e.layerX) || window.event.offsetX;
			obj.top  = (e&&e.layerY) || window.event.offsetY;
			return obj;
		}


		var evtO = UQChart.util.evt;
		//添加事件
		t.addEvt = function(type,handler){
			evtO.addHandler(t,type.handler);
		}

		//移出事件
		t.removeEvt = function(type, handler){
			evtO.removeHandler(t,type.handler);
		}

		//获得事件
		t.getE = function(e){
			evtO.getEvent(e);
		}

		//获取事件对象
		t.getEObj = function(e){
			evtO.getTarget(e);
		}
	},
	evt:{
	    addHandler : function(element, type, handler){
			if(element.addEventListener){
				element.addEventListener(type,handler,false);
			}else if(element.attachEvent){
				element.attachEvent("on" + type, handler);
			}else{
				elmenet["on" + type] = handler;
			}
		},
		removeHandler : function(element, type, handler){
			if(element.removeEventListener){
				element.removeEventListener(type, handler, false);
			}else if(element.detachEvent){
				element.detachEvent("on"+type, handler);
			}else{
				element["on"+type] = null;
			}
		},
		getEvent : function(event){
			return event ? event : window.event;
		},
		getTarget : function(event){
			return event.target || event.srcElement;
		},
		preventDefault : function(event){
			if(event.preventDefault){
				event.preventDefault();
			}else{
				event.returnValue = false;
			}
		},
		stopPropagation : function(event){
			if(event.stopPropagation){
				event.stopPropagation();
			}else{
				event.cancelBubble = true;
			}
		}
	}
}

//双Y轴，不同类型的图表画法，line、bar 等
UQChart.otherClass.DlbInit = function(){
    var t          = this;
	var mData      = t.mData;
	var dataObj    = mData.dataObj;
	//var bData      = t.bData;
	var leftAry    = dataObj.leftData;
	var rigAry     = dataObj.rightData;
	
	var titAry     = mData.title;
	var ctx        = t.ctx;
	var type       = mData.chartType;
	var oneDataAry = [];
	var twoDataAry = [];
	/*
	var newLeftAry = [];
	var newRigAry  = [];
	*/
	
	init();
	
	function init(){
	    if(type.right&&type.right === 'bar'){
		    oneDataAry = rigAry;
			twoDataAry = leftAry;
	    }else{
			oneDataAry = leftAry;
			twoDataAry = rigAry;
		}
		for(var i=0,oneLen=oneDataAry.length; i<oneLen; i++){
			
			newLeftAry[i] = t.getAxis(titAry[i],'leftY',leftAry[i]);
		}

		for(var j=0,rigLen=rigAry.length; j<rigLen; j++){
			newRigAry[j]  = t.getAxis(titAry[j+leftLen],'rightY',rigAry[j]);
		}
		
		
    }
	
	
	
	
}

//双Y轴折线图的主要实现部分
UQChart.otherClass.DlineInit = function(){
	var t          = this;
	var mData      = t.mData;
	var dataObj    = mData.dataObj;
	//var bData      = t.bData;
	var leftAry    = dataObj.leftData;
	var rigAry     = dataObj.rightData;
	var newLeftAry = [];
	var newRigAry  = [];
	var titAry     = mData.title;
	var ctx        = t.ctx;

	//js执行
	init();

	function init(){
		
		for(var i=0,leftLen=leftAry.length; i<leftLen; i++){
			newLeftAry[i] = t.getAxis(titAry[i],'leftY',leftAry[i]);
		}

		for(var j=0,rigLen=rigAry.length; j<rigLen; j++){
			newRigAry[j]  = t.getAxis(titAry[j+leftLen],'rightY',rigAry[j]);
		}
		animate(newLeftAry,newRigAry);
		canvasEvt(t.id);
	}

	//画线动画
	function animate(newLeftAry,newRigAry){
		var FnTimes  = 1;
		//画折线图独立的方法
		var drawLine = function(dataAry){
			//if(dataAry.length ===0) return;
		    var colorAry = mData.color;
			var len      = dataAry[0].length;
			var radius   = 4;
			var x,y;
			var curCol   = '';
			ctx.lineWidth= 2;

			for(var j=0,inLen = dataAry.length; j<inLen; j++){
				curCol = FnTimes === 1?  colorAry[j]:colorAry[leftAry.length+j];
				//对应的x轴点,画折线
				ctx.shadowBlur = 0;
				ctx.strokeStyle = curCol;
				ctx.beginPath();
				ctx.moveTo(dataAry[j][0].x,dataAry[j][0].y);
				for(var i=1;i<len;i++){
					ctx.lineTo(dataAry[j][i].x,dataAry[j][i].y);
				}
				ctx.stroke();
				ctx.closePath();

				//画点,操作
				ctx.shadowBlur  = 3;
			    ctx.shadowColor = 'rgba(64, 64, 64, 0.5)';
				ctx.fillStyle   = curCol;
				ctx.beginPath();
				x = dataAry[j][0].x;
				y = dataAry[j][0].y;
				ctx.arc(x,y,radius,0,Math.PI*2,true);
				ctx.fill();
				ctx.closePath();
				for(var i=1;i<len;i++){
					ctx.beginPath();
					x = dataAry[j][i].x;
					y = dataAry[j][i].y;
					ctx.arc(dataAry[j][i].x,dataAry[j][i].y,radius,0,Math.PI*2,true);
					ctx.fill();
					ctx.closePath();
				}
			}
			FnTimes++;
		}
        if(newLeftAry.length >0 ) drawLine(newLeftAry);
		if(newRigAry.length >0 ) drawLine(newRigAry);
		//drawLine(newRigAry);
	}



	//canvas注册事件,显示漂浮的div
	function canvasEvt(){
		var id         = t.id;
		var canvasObj  = $('#'+id);
		var offset     = canvasObj.offset();
		var showDot    = t.showDotAry;
		canvasObj.unbind('');
		var x,y;
		canvasObj.mousemove(function(e){
			$('.canvasOver').hide();
			x = e.pageX - offset.left;
			y = e.pageY - offset.top;
			for(var i=0,len=showDot.length; i<len; i++){
				if(x>=showDot[i][0] && x<=showDot[i][1] && y>=showDot[i][2] && y<=showDot[i][3]){
					if($('#canvasOver').length > 0){
						var width =  $('#canvasOver').width();
					    var left  =  (e.pageX-width/2-3);
					    $('#canvasOver').css({'top':(e.pageY-50),'left':left}).html(showDot[i][4]).show();
					}else{
						showBox = $('<div id="canvasOver" class="canvasOver" style=" background:#fffeed; padding:6px; border-radius:5px; border:1px solid #c0c0c0; position:absolute; z-index:10; top:'+(e.pageY-50)+'px;left:'+(e.pageX-30)+'px;">'+ showDot[i][4] +'</div>');
						showBox.appendTo($('body')).show();
						var width =  $('#canvasOver').width();
						showBox.css('left',(e.pageX-width/2-3));
						return false;
					}
				}
			}
		})
		canvasObj.mouseout(function(e){
			$('#canvasOver').hide();
		})
	}
}

//String扩展，颜色转换
/*RGB颜色转为16进制的 hex*/
String.prototype.colorHex = function(){
	var reg = /^#([0-9a-fA-f]{3}|[0-9a-fA-f]{6})$/;
	var that = this;
	if(/^(rgb|RGB)/.test(that)){
		var aColor = that.replace(/(?:\(|\)|rgb|RGB)*/g,"").split(",");
		var strHex = "#";
		for(var i=0; i<aColor.length; i++){
			var hex = Number(aColor[i]).toString(16);
			if(hex === "0"){
				hex += hex;
			}
			strHex += hex;
		}
		if(strHex.length !== 7){
			strHex = that;
		}
		return strHex;
	}else if(reg.test(that)){
		var aNum = that.replace(/#/,"").split("");
		if(aNum.length === 6){
			return that;
		}else if(aNum.length === 3){
			var numHex = "#";
			for(var i=0; i<aNum.length; i+=1){
				numHex += (aNum[i]+aNum[i]);
			}
			return numHex;
		}
	}else{
		return that;
	}
};

//16进制的颜色的转为,#rgb格式
String.prototype.colorRgb = function(){
	var reg = /^#([0-9a-fA-f]{3}|[0-9a-fA-f]{6})$/;
	var sColor = this.toLowerCase();
	if(sColor && reg.test(sColor)){
		if(sColor.length === 4){
			var sColorNew = "#";
			for(var i=1; i<4; i+=1){
				sColorNew += sColor.slice(i,i+1).concat(sColor.slice(i,i+1));
			}
			sColor = sColorNew;
		}
		var sColorChange = [];
		for(var i=1; i<7; i+=2){
			sColorChange.push(parseInt("0x"+sColor.slice(i,i+2)));
		}
		return "RGB(" + sColorChange.join(",") + ")";
	}else{
		return sColor;
	}
};
//参数ary,数组，每个元素是obj,将多个obj融合成一个新的obj,返回newObj
UQChart.conObj = function(ary){
	if(!ary) return false;
	if(ary.length===1) {
	   return ary[0]
	}else{
	   var newObj = {};
	   var k      = '';
	   for(var i=0,len=ary.length; i<len; i++){
		   for(k in ary[i]){
			  if(ary[i].hasOwnProperty(k)){
				  newObj[k] = ary[i][k];
			  }
		   }
	   }
	   return newObj;
	}
}

//Overlap,2个圆圈重合的饼图
//DlineChart,双折线
//此处的call方法能实现神奇的继承功能
UQChart.chartClass = {
  	Overlap:function(obj){
		var other = UQChart.otherClass;
		var t     = this;
		other.BaseCanvas.call(t,obj);
		other.OverlapInit.call(t);
	},
	DlineChart:function(obj){
		var other = UQChart.otherClass;
		var t     = this;
		other.BaseCanvas.call(t,obj);
		other.DrawAxis.call(t,"double");
		other.DlineInit.call(t);
	}
}

UQChart.util.checkFn = function(obj){
	if(!obj.id || !obj.data) return false;
	try{
		UQChart.util.getObj(obj.id).getContext('2d');
	}
	catch(e){
		return false;
	}
	return true;
}

//overLap数据检查
UQChart.util.dLineCheckFn = function(obj){
	var d = obj.data
	if(d.base&&d.mainData&&d.mainData.title&&d.mainData.xNameList&&d.mainData.dataObj&&d.mainData.dataObj.leftData&&d.mainData.dataObj.rightData ){
		
		return true;
	}else{
	    return false;
	}
}

//overLap数据检查
UQChart.util.overLapCheckFn = function(obj){
	var d = obj.data
	if(d.title&&d.num&&d.rateText&&d.title.length === 3){
	    return true;
	}else{
	    return false;
	}
}

UQChart.util.showWrong = function(obj){
	var canBox    = UQChart.util.getObj(obj.id);
	var ctx       = canBox.getContext('2d');
	var wText     = 'There is something wrong with the data, check it please.';
	var textW     = 0;
	var boxWid    = canBox.Wid();
	var boxHei    = canBox.Hei();
	var startX    = 0;
	var startY    = 0;
	canBox.setAttr({"width":boxWid-2,"height":boxHei});

	ctx.font      = '14px serif';
	ctx.fillStyle = "#000";
	textW         = ctx.measureText(wText).width;
	startX        = (boxWid - textW) / 2;
	startY        = (boxHei - 14) / 2 + 14;
	ctx.fillText(wText,startX,startY);
}

//实例化封装
UQChart.dLineChart = function(obj){
	if(this.isIE()) return;
	$('.dLineChart').remove();
	/*
	if(!UQChart.util.checkFn(obj)) return false;
	if(!UQChart.util.dLineCheckFn(obj)){
	    UQChart.util.showWrong(obj);
	    return false;
	}
	*/
	$('.bodyUqNewDiv').remove();
   	var newChart = new UQChart.chartClass.DlineChart(obj);
	return true;
}

UQChart.overLapChart = function(obj){
	if(this.isIE()) return;
	if(!UQChart.util.checkFn(obj)) return false;
	if(!UQChart.util.overLapCheckFn(obj)){
		UQChart.util.showWrong(obj);
	    return false;
	};
	$('.bodyUqNewDiv').remove();
   	var newChart = new UQChart.chartClass.Overlap(obj);
	return true;
}

