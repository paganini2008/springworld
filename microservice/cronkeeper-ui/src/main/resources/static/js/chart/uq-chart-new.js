(function(){
    var UQChart = window.UQChart = UQChart||{};
    UQChart.otherClass = UQChart.otherClass||{};

    /**
    数据保留二份，一份是obj,data最原始的数据，json结构
    一份是过滤后(%$￥-等字符)的，存放在原始的json数据结构里
    原始数据保存在obj.strData.left/ obj.strData.right,显示的时候来这里取
    **/
    //canvas基本类,rectS边框的间距

    UQChart.otherClass.BaseCanvas = function(obj){
        var t     = this;
        t.obj     = obj;
        t.id      = obj.id;
        t.data    = obj.data;
        // t.canBox  = UQChart.util.getObj(this.id);
        t.rectS   = 10;
        t.divBox  = $('#'+t.id);
		t.width   = t.divBox.width();
        t.height  = parseInt(t.divBox.attr('oldHei'))||t.divBox.height();
        t.divBox.css({'width':t.width});
		
		//空数据处理
		
		try{
			 if(obj.type !== 'Overlap'){
				 var mData     = obj.data.mainData.dataObj;
				 var leftData  = mData.leftData;
				 var rightData = mData.rightData;
				 if(leftData && leftData.dataAry && leftData.dataAry[0].length === 0){
					 leftData.dataAry = [[0]]; 
				 }
				 if(rightData && rightData.dataAry && rightData.dataAry[0].length === 0){
					 rightData.dataAry = [[0]]; 
				 }
			 }
		}catch(e){
			
		}
		
	
        canvasInit();

        //初始化，canvas的大小，取ctx
        function canvasInit(){
			var divBox = t.divBox;
			var canBox = null;
			var html   = '<canvas width="'+t.width+'" height="'+t.height+'" id="'+ t.id +'_cav" >' +
                             
                         '</canvas>';
			divBox.html(html).css('position','relative');
			divBox.attr('oldHei')? divBox.css('height',divBox.attr('oldHei')):'';
            canBox = t.canBox   = divBox.find('canvas');
            divBox.css('position','relative');
            t.ctx      = canBox[0].getContext('2d');
			
        }
    }

    //双Y坐标轴类,绘制坐标轴,【单Y，双Y】
    //实例参数解释，axisW坐标轴的长; axisRect坐标轴的四个角的坐标; axisXAry,X轴上数据，lineTipAry,axisXAry,很重要的一个参数，存储需要显示的点的坐标
    UQChart.otherClass.DrawAxis = function(type){
        var t        = this;
        var width    = t.width;
        var height   = t.height;
        var ctx      = t.ctx;
        var util     = UQChart.util;
        var mData    = t.data.mainData;
        var bData    = t.data.base;
        var leftData = mData.dataObj.leftData;
        t.mData      = mData;
        t.bData      = bData;
        t.axisW      = 0;
        t.axisH      = 0;
        t.leftY      = null;
        t.rightY     = null;
        t.axisRect   = [];
        t.axisXAry   = [];
        t.lineTipAry = [];
        t.barTipAry  = [];
        t.barTipTit  = [];
        t.step       = bData.step?bData.step:5;
        t.rotate     = bData.rotate? 360-parseInt(bData.rotate):0;

        drawAxisInit();

        //此处标记是否走底位,leftLow或rightLow来标记
        function drawAxisInit(){
            var leftAry   = leftData.dataAry;
            var leftLow   = typeof leftData.lowPos === 'undefined'? false:true;
            var leftPoint = leftData.hasPoint;
            var leftCol   = leftData.colorAry;
            var leftTit   = leftData.titleAry;
            t.setYSide    = setYSide(leftAry,"leftY",Boolean(leftLow),leftPoint);

            addColTit(leftAry,leftCol,leftTit);

            if(type === "double"){
                var rightData  = t.mData.dataObj.rightData;
                var rightAry   = rightData.dataAry;
                var rightLow   = typeof rightData.lowPos === 'undefined'? false:true;

                var rightPoint = rightData.hasPoint;
                var rightCol   = rightData.colorAry;
                var rightTit   = rightData.titleAry;
                addColTit(rightAry,rightCol,rightTit);
                setYSide(rightAry,"rightY",Boolean(rightLow),rightPoint);
            }else{
                setYSide([],"rightY",false,false);
            }


            drawXYLine();

            drawYNum("leftY");

            if(type === "double"){
                drawYNum("rightY");
            }
			
            writeYname();
        }

        //自动补全缺省的颜色和标题,根据RGB颜色计算方法，每种颜色加100
        function addColTit(ary,col,tit){
            var leftAry = leftData.dataAry;
            var leftCol = leftData.colorAry;
            var leftTit = leftData.titleAry;

            //RGB 颜色，每个颜色加，100，变成新的颜色
            var translate = function(str){
                str = parseInt(str,16) + 100;
                str = str > 255? str-255:str;
                str = str.toString(16);
                if(str.length === 0) str = '0' + str;
                return str;
            }

            var addColor = function(colorStr){
                var newColor = '';
                colorStr = colorStr.replace('#','');
                for(var i=0; i<3; i++){
                    newColor += translate(colorStr.slice(i*2,2*(i+1)));
                }
                newColor  = "#" + newColor;
                return newColor;
            }

            var len = ary.length;
            if(col.length<len || tit.length<len){
                for(var i=0; i<len; i++){
                    if(!col[i]){
                       if(i === 0) col[i-1] = "#ac0000";
                       col[i] =  addColor(col[i-1]);
                    }
                    if(!tit[i]){
                       tit[i] = "0" + (i+1);
                    }
                }
            }
        }

        //计算Y轴离2边的边距，最大值，步长,每步的数值
        //lowPos 是标识是否有高低位区别
        function setYSide(yAry,sideY,lowPos,hasPoint,notZeroY){
            //var that      = this;
            var maxVal    = 0;
            var stepNum   = 0;
            var axisMax   = 0;
            var toSide    = 0;
            var axisMaxStr= 0;
            var leftDist  = 0;
            var step      = t.step;
            var strBox    = 60;
			var nameAry   = mData.yNameList;
			
			//100%的特殊设置
			try{
				var leftMax  = UQChart.setDataOk(t,'t.mData.dataObj.leftData.maxPNum');
				var rightMax = UQChart.setDataOk(t,'t.mData.dataObj.rightData.maxPNum');
				if((sideY === 'leftY' && parseInt(leftMax) === 100) || (sideY === 'rightY' && parseInt(rightMax) === 100) ){
					 leftDist = 75;
					 toSide   = (sideY === 'leftY')?leftDist:(t.width - leftDist);
					 t[sideY] = {
						"stepNum":100/step,
						"axisMax":100,
						"toSideNum":toSide
					 }
					 return;
				}
			}catch(e){
				
			}
			

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
                        "toSideNum":t.width - 55
                    }
                }
            }else{
            	t[sideY] = getYStep(yAry,sideY,hasPoint);
            }
			
			if(!nameAry || (nameAry.length ===1 && nameAry[0].trim() ==='')){
			    if(sideY === 'leftY'){
                    t[sideY]['toSideNum'] -= 30;
                }else{
                    t[sideY]['toSideNum'] += 30;
                }
		    }
			
        }
		
		function getYStep(yAry,sideY,hasPoint){
			var stepNum,startNum,axisMax,axisMaxStr,axisMaxStrBox,leftDist,toSide;
			var notZeroY  = t.mData.dataObj[sideY.slice(0,sideY.length-1)+'Data'].notZeroY;
			var maxVal    = util.getAryMaxValue(yAry);
			var step      = t.step;
            var diff      = 0;
				
			//获取步长，最大值的通用方法
			var getValue = function(){
			    stepNum     =  maxVal/step;
				if(typeof lowPos !== 'undefined')  stepNum = stepNum*3;
				stepNum     = getStepNum(stepNum,hasPoint);
				axisMax     = stepNum*step;
				axisMaxStr  = util.getSplitNum(axisMax);	
			}
			
			if(notZeroY){
				var numAry     =  util.concatAry(yAry);
				var concatAry  =  util.cloneAry(numAry);
				var yArySort   =  concatAry.sort(function(a,b){ return a-b});
				var minNum    =  yArySort[0];
				var maxNum    =  yArySort[yArySort.length-1];

				diff      =  maxNum - minNum === 0? maxNum:maxNum - minNum;
				stepNum   =  diff/(step - 0.5);
				stepNum   =  stepNum === 0? 1:stepNum;
				
				if(typeof lowPos !== 'undefined')  stepNum = stepNum*3;
				stepNum   =  getStepNum(stepNum,hasPoint);
				startNum  =  minNum - stepNum*0.5;
				if(startNum < 0){
				    startNum = 0;	
					getValue();
				}else{
					axisMax   =  stepNum*step + startNum;
				    axisMaxStr=  util.getSplitNum(axisMax);
				}
			}else{
				getValue();
			}

			if(axisMaxStr.indexOf('.') > -1){
				axisMaxStr = axisMaxStr.split('.')[0] + '.' + axisMaxStr.split('.')[1].slice(0,2);
			}
			
			axisMaxStrBox = $('<span style="font-size:12px;">' + axisMaxStr + '</span>');
			leftDist      =  axisMaxStrBox.appendTo($(document.body)).width() + 45;
			toSide        = (sideY === 'leftY')?leftDist:(t.width - leftDist);
			axisMaxStrBox.remove();
			
			return {
				"startNum":startNum||0,
				"stepNum":stepNum,
				"axisMax":axisMax,
				"toSideNum":toSide
			}
		}

        //绘制Y轴名字
        function writeYname(){
            var nameAry  = mData.yNameList||[];
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
               ctx.font      = "13px Arial,Helvetica,sans-serif";
               strWidth      = ctx.measureText(str).width;
               wTop          = (height-strWidth)/2 + strWidth*0.45;
               //计算方法，offLeft + 3是正常偏移位置，(strWidth-14)/2是旋转后位置，+8/-4是微调位置
               wLeft = i===0? (3 - (strWidth-14)/2 + 8):( width - 24-(strWidth-14)/2 - 4);
               $('<div class="bodyUqNewDiv" style="-webkit-transform:rotate(-90deg); -moz-transform:rotate(-90deg); transform:rotate(-90deg); font-size:12px; color:#417dd7; position:absolute; width:'+ strWidth +'px; height:14px; top:'+ wTop +'px; left:'+ wLeft +'px;">' + str + '</div>').appendTo(t.divBox);
            }
            //ctx.rotate(-Math.PI/2);
        }

        //步长的算法
        //1、 有小数点的情况，保留2位；
        // 2、整数的情况  1、10以内的去Math.ceil() 2、100以内取5或0，结尾的整数，大于自身，3,100以上的取0结尾的整数，大于自身
        function getStepNum(stepLong,hasPoint){
            if(hasPoint){
                var str   = stepLong.toString();
                if(str.indexOf('.') > -1){
					stepLong  = Number(Number(stepLong).toFixed(2)) + 0.01;
					
                }else{
                    stepLong  = Number(stepLong) + 0.01;
                }
            }else{
                if(stepLong>100){
                    stepLong = parseInt(stepLong/10 + 1) * 10;

                }else if(stepLong>10){

                    stepLong = Math.ceil(stepLong).toString();
                    if(parseInt(stepLong.slice(1,2)) > 5){
                         stepLong = parseInt(parseInt(stepLong)/10 + 1) * 10;
                    }else{
                         stepLong = parseInt(stepLong.slice(0,1))*10 + 5;
                    }
                }else{
                    stepLong = Math.ceil(stepLong);
                }
            }
			
            return stepLong;
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
                if(type === 'single' && i===3) break;
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
            var htmlObj     = null;
            var htmlCss     = '';
            var rotateVal   = '';
            var moevHei     = 0;
            var moveWid     = 0;
            var degree      = 0;
            var widAry      = [];
            ctx.strokeStyle = "#945532";
            ctx.fillStyle   = "#000000";
            ctx.font        = "12px Arial,Helvetica,sans-serif";
            ctx.lineWidth   = 2;
            rotateVal       =  'rotate(' + t.rotate+ 'deg)';
            degree          =  (360- t.rotate)*Math.PI/180;
            for(var i=0,len=nameAry.length; i<len; i++){
				if(nameAry[i] !== ''){
					ctx.beginPath();
					ctx.moveTo(x,y);
					ctx.lineTo(x,y+5);
					ctx.stroke();
					ctx.closePath();
					curText  = nameAry[i];
					htmlObj  = $('<div style="font-size:12px; position:absolute; z-index:1; left:' + x + 'px; top:' + (y+8) + 'px;">' + curText + '</div>');
					htmlObj.appendTo($(document.body));
					curWidth = htmlObj.width();
					htmlObj.appendTo(t.divBox);
					widAry.push(curWidth);
					htmlObj.css('left',x-curWidth/2);
					if(t.rotate){
						moveWid      =  curWidth*Math.cos(degree)/2;
						moveHei      =  curWidth*Math.sin(degree)/2 -4;
						htmlObj.css({'top':y+8+moveHei,'left':x-curWidth/2-moveWid,'-o-transform':rotateVal,'-webkit-transform':rotateVal,'-moz-transform':rotateVal}) ;
					}
				}
                t.axisXAry.push(x);
                x += stepW;
            }
            if(t.rotate){
                var maxWidth   = util.getMaxVal(widAry);
                maxWidth       = maxWidth*Math.sin(degree);
                console.log('maxWidth=' + maxWidth);
                t.divBox.css('height',maxWidth + height).attr('oldHei',height);
            }
        }

        //画小方框，标识颜色图示
        function drawBlock(){
            //var leftLen = mData.dataObj.leftData.length;
            var toLeft    = t.axisRect[0].x;
            var toTop     = t.axisRect[0].y;
            var x         = toLeft;
            var y         = toTop - 22;
            var textW     = 0;
            var leftData  = mData.dataObj.leftData;
            var rightData = mData.dataObj.rightData;

            //绘制小方块的具体方法
            var drawFn    = function(titleAry,colorAry){
                for(var i=0,len=titleAry.length; i<len; i++){
                    ctx.fillStyle = colorAry[i];
                    ctx.fillRect(x,y,10,10);
                    //写上标题
                    x = x + 15;
                    y = y + 9;
                    ctx.fillStyle = "#000000";
                    ctx.font = '12px Arial,Helvetica,sans-serif';
                    ctx.fillText(titleAry[i],x,y);
                    textW = ctx.measureText(titleAry[i]).width;
                    y = y - 9;
                    x = x + textW + 20;
                }
            }

            //左边数据块，右边数据块
            drawFn(leftData.titleAry,leftData.colorAry);
            if(rightData){
                drawFn(rightData.titleAry,rightData.colorAry);
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
			var notZeroY    = t.mData.dataObj[direction.slice(0,direction.length-1)+'Data'].notZeroY;
			
            ctx.strokeStyle = "#945532";
            ctx.fillStyle   = "#000000";
            ctx.font        = "12px Arial,Helvetica,sans-serif";
            ctx.lineWidth   = 2;
            if(direction === "leftY"){
                ctx.textAlign = "right";
            }else{
                ctx.textAlign = "left";
                x += 5;
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
                
				if(notZeroY){
				    yText =  i*stepNum + obj.startNum;
					yText = util.getSplitNum(yText);
				}else{
					yText = util.getSplitNum(i*stepNum);
				}

                if(yText.toString().indexOf('.')>-1){
                	yText = Number(yText.replace(/\,/g,'')).toFixed(2);
                	yText = util.getSplitNum(yText);
                }
				
				
                if(direction === "leftY"){
                    txtOffSet = x-12;
                    if(!mData.dataObj.leftData.hasPoint){
                        yText = yText.split('.')[0];
                    }
                    if(mData.dataObj.leftData.addStr){
                        yText += mData.dataObj.leftData.addStr;
                    }
                }else{
                    txtOffSet = x+7;
                    if(!mData.dataObj.rightData.hasPoint){
                        yText = yText.split('.')[0];
                    }
                    if(mData.dataObj.rightData.addStr){
                        yText += mData.dataObj.rightData.addStr;
                    }
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
        //set Title 的时候注意是否要小数点
        //index 数据的多维数据的顺序
        //sideY,是'leftY'或'rightY'

        t.getAxis = function(title,sideY,ary,chartType,hasPoint,index){
            var newAry    = [];
            var sideObj   = t[sideY];
            var axisH     = t.axisH;
            var axisMax   = sideObj.axisMax;
            var toBom     = t.axisRect[1].y;
            var splitFn   = UQChart.util.getSplitNum;
            var titList   = [];

            var showNum   = '';
            var side      = sideY.replace('Y','');
            var strData   = t.obj.strData[side];
			var notZeroY  = t.mData.dataObj[sideY.slice(0,sideY.length-1)+'Data'].notZeroY;
			

            for(var i=0,len=ary.length; i<len; i++){
                var newObj    = {};
                //检查不是数字的情况
                if( isNaN(Number(strData[index][i]))){
                    showNum = strData[index][i];
                }else{
                    showNum  = splitFn(ary[i]);
                    if(!hasPoint) showNum = showNum.split(".")[0];
                }

                newObj.x     = t.axisXAry[i];            
				
                
				if(notZeroY){
					newObj.y  = Math.round( toBom - (ary[i]- sideObj.startNum)*axisH/(axisMax - sideObj.startNum) );
				}else{
					newObj.y  = Math.round(toBom - (ary[i]*axisH)/axisMax);
				}

                //存储带有数值的Tip,自动添加小数
                newObj.title = (title+'<br/><b>' + showNum +'</b>');

                if(chartType === "line"){
                    t.lineTipAry.push([newObj.x-4,newObj.x+4,newObj.y-4,newObj.y+4,newObj.title]);
                }else if(chartType === "bar"){
                    titList.push(newObj.title);
                }

                newObj.color = '';
                newAry.push(newObj);
            }

            if(chartType === "bar"){
                t.barTipTit.push(titList);
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
	   var divBox   = t.divBox;
	   if(data.color&&data.color.length===3){
		   color    = data.color;
	   }
	   /*
	   var pBox     =  $('<div style="position:relative;"></div>');
	   pBox.insertBefore($('#'+ id));
	   $('#'+ id).appendTo(pBox);
	   */
	   t.divBox.find('div').remove().css('position','relative');
	   
	   
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
			   $('<p style="position:absolute; font-size:12px;  z-index:2; top:'+ (_y*i + 34) +'px; left:'+ (_x + 5) +'px; display:block;  width:10px; height:10px; background:'+ color[i] + ';"> </p>').appendTo(divBox);
			   $('<div style="position:absolute;  font-size:12px;  text-align:left; top:'+ (_y*i + 30) +'px; left:'+ (_x + 20) +'px;">'+ _text + ' </div>').appendTo(divBox);
		   }
	   }
	
	   //注册事件
	   function evt(){
		   $("#"+id).mousemove(function(e){
			   var curObj  = $(this);
			   var offset  = curObj.offset();
			   var _x      = e.pageY - offset.top;
			   var _y      = e.pageX - offset.left;
	
			   //鼠标在矩形在域内
		   })
	   }
	
	}

    /***对html对象的简单扩展,以及其他通用功能***/
    UQChart.util = {
        filter:function(ary){
            var reg    = /[$￥%-]/g;
            var newAry = [];
            for(var i=0,len=ary.length; i<len; i++){
                newAry[i] = ary[i];
                if(typeof ary[i] === 'string'){
                   ary[i]    = Number(ary[i].replace(reg,''));
                }
            }
            return newAry;
        },
		cloneAry:function(ary){
		    var newAry = [];
			for(var i=0,len=ary.length; i<len; i++){
			    newAry[i] = ary[i]	
			}
			return newAry;
		},
		concatAry:function(yAry){
			var newAry = [];
			if(yAry.length>1){
                newAry    = yAry[0];
                for(var i=1,len = yAry.length; i<len; i++){
                    newAry  = newAry.concat(yAry[i]);
                }
            }else{
                newAry = yAry[0];
            }
			
			return newAry;
		},
        //获取（可能的几个）数组中最大的数
        getAryMaxValue:function(yAry){
            var util      = this;
            var maxNum    = 0;
			
            newAry     =  util.concatAry(yAry);
            maxNum     =  util.getMaxVal(newAry);
			
            if(maxNum === 0){
                maxNum = 10;
            }
            return maxNum;
        },
        getMaxVal:function(ary){
			var maxVal = Math.max.apply(this,ary);
            return maxVal;
        },
        //获取一个数的三位分割,如120030,为120,030,自动加小数的过程
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
                newStr = Number(num).toFixed(2);
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

            //获取父节点
            t.parent = function(){
                var parent = t.parentNode;
                return parent && parent.nodeType !== 11 ? parent : null;
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

    // 柱状图，折线,混合图
    UQChart.otherClass.DMixInit = function(){
        var t          = this;
        var mData      = t.mData;
        var dataObj    = mData.dataObj;
        var leftAry    = dataObj.leftData.dataAry;
        var rigAry     = dataObj.rightData.dataAry;

        var oneTitAry  = dataObj.leftData.titleAry;
        var twoTitAry  = dataObj.rightData.titleAry;
        var ctx        = t.ctx;
        var type       = [dataObj.leftData.typeAry[0],dataObj.rightData.typeAry[0]];
        var oneDataAry = [];
        var twoDataAry = [];
        var direcAry   = ["leftY","rightY"];
        var pointList  = [dataObj.leftData.hasPoint,dataObj.rightData.hasPoint];

        init();
        //这里的leftAry,righAry是多维数组
        function init(){
            var barColAry  = mData.dataObj.leftData.colorAry;
            var lineColAry = mData.dataObj.rightData.colorAry;
            var objAry     = [];

            oneDataAry     = leftAry;
            twoDataAry     = rigAry;

            //调换数据，颜色顺序
            if(type[1] === 'bar'){
                var tempAry = barColAry;
                var tempTit = oneTitAry;
                oneDataAry  = rigAry;
                twoDataAry  = leftAry;
                direcAry    = ["rightY","leftY"];
                pointList   = [dataObj.rightData.hasPoint,dataObj.leftData.hasPoint];
                barColAry   = lineColAry;
                lineColAry  = tempAry;
                oneTitAry = twoTitAry;
                twoTitAry = tempTit;
            }

            //先画状图，再画折线图，防止折线图被挡住在在面
            //计算【柱状图】数据
            for(var i=0,oneLen=oneDataAry.length; i<oneLen; i++){
                objAry = t.getAxis(oneTitAry[i],direcAry[0],oneDataAry[i],'bar',pointList[0],i);
                UQChart.otherClass.Drawbar.call(t,objAry,barColAry[i],oneLen,i,t.barTipTit[i]);
            }

            //计算【折线图】数据
            for(var j=0,twoLen=twoDataAry.length; j<twoLen; j++){
                objAry  = t.getAxis(twoTitAry[j],direcAry[1],twoDataAry[j],'line',pointList[1],j);
                UQChart.otherClass.Drawline.call(t,objAry,lineColAry[j]);
            }

            UQChart.otherClass.Evt.call(t);
        }
    }

    //绘制柱状图，独立的类,柱状图的个数以x轴的数组为准
    UQChart.otherClass.Drawbar = function(ary,color,dimension,index,titleAry){


        var t          = this;
        var mData      = t.mData;
        var barNum     = mData.xNameList.length;
        var barWid     = 0;
        var axisH      = t.axisH;
        var ctx        = t.ctx;

        //构造函数
        init();

        function init(){
            if(barNum === 0) return;
            barWid         = t.data.base.barWidth||parseInt(t.axisW/barNum);
            
            //柱状图的宽度，单个的最大宽度不能大于100，多为柱图单个宽度不能大于80
            if(dimension > 1){
               var singWid = parseInt(barWid/dimension*0.85);
               barWid      = singWid > 80?80:singWid;
            }else{
               barWid      = (barWid*0.7 >100)? 100:barWid*0.7;
            }
            //存储柱图的宽度
            if(!t.barWid){
               t.barWid = barWid;
            }

            for(var i=0; i<barNum; i++){
                drawingBar(i,color,dimension,index);
            }
        }


        //绘制柱状图
        function drawingBar(i,color,dimension,index){
            var x,y,width,height;
            if(i>ary.length-1) return;
            ctx.fillStyle = color;
            x             = ary[i].x-barWid/2;
            y             = ary[i].y;
            width         = barWid;
            height        = axisH-(ary[i].y-t.axisRect[0].y);
            if(height === 0){
			    height = 1;	
			    y -= 1;
			}
            console.log('height=' + height);
            if(dimension === 1){
                ctx.fillRect(x,y-1,width,height);
            }else{
                x = ary[i].x-barWid*dimension/2 + index*barWid;
                ctx.fillRect(ary[i].x-barWid*dimension/2 + index*barWid,y-1,width,height);
            }


            t.barTipAry.push([x, y, x + barWid,y+height,titleAry[i]]);
            ctx.beginPath();
        }
    }


    // 【单】Y轴,折线、柱状混合图
    UQChart.otherClass.SmixInit = function(){
        var t          = this;
        var leftData   = t.mData.dataObj.leftData;
        var leftAry    = leftData.dataAry;
        var newLeftAry = [];
        var newRigAry  = [];
        var leftTit    = leftData.titleAry;
        var leftType   = leftData.typeAry;
        var leftCol    = leftData.colorAry;
        var ctx        = t.ctx;
        var hasPoint   = leftData.hasPoint;


        init();

        //初始化,先画柱状图，再画折线图
        function init(){
            var dimension = 0;
            var len       = leftAry.length;
            var objAry    = null;
            var barIndex  = 0;
            for(var i=0; i<len; i++){
                if(leftType[i] === 'bar'){
                    dimension += 1;
                }
            }

            //先画柱状图 ,次出的维度与当前barIndex是bar的需要提出来
            for(i=0; i<len; i++){
                if(leftType[i] === 'bar'){
                    objAry = t.getAxis(leftTit[i],'leftY',leftAry[i],'bar',hasPoint,i);
                    UQChart.otherClass.Drawbar.call(t,objAry,leftCol[i],dimension,barIndex,t.barTipTit[barIndex]);
                    barIndex += 1;
                }
            }

            //在画折线图
            for(i=0; i<len; i++){
                if(leftType[i] === 'line'){
                    objAry = t.getAxis(leftTit[i],'leftY',leftAry[i],'line',hasPoint,i);
                    UQChart.otherClass.Drawline.call(t,objAry,leftCol[i]);
                }
            }
            //注册事件
            UQChart.otherClass.Evt.call(t);
        }
    }

    // 【单】Y轴,柱状图
    UQChart.otherClass.SbarInit = function(){
        var t          = this;
        var leftData   = t.mData.dataObj.leftData;
        var leftAry    = leftData.dataAry;
        var newLeftAry = [];
        var newRigAry  = [];
        var leftTit    = leftData.titleAry;
        var leftCol    = leftData.colorAry;
        var ctx        = t.ctx;
        var hasPoint   = leftData.hasPoint;

        init();

        //初始化,计算【柱状图】
        function init(){
            for(var i=0,len=leftAry.length; i<len; i++){
                objAry = t.getAxis(leftTit[i],'leftY',leftAry[i],'bar',hasPoint,i);
                UQChart.otherClass.Drawbar.call(t,objAry,leftCol[i],len,i,t.barTipTit[i]);
            }
            UQChart.otherClass.Evt.call(t);
        }
    }


    // 【单】Y轴,折线图
    UQChart.otherClass.SlineInit = function(){
        var t          = this;
        var leftData   = t.mData.dataObj.leftData;
        var leftAry    = leftData.dataAry;
        var leftTit    = leftData.titleAry;
        var leftCol    = leftData.colorAry;
        var hasPoint   = leftData.hasPoint;
        var ctx        = t.ctx;

        init();

        //初始化
        function init(){
            var newObj   = null;
            for(var i=0,len=leftAry.length; i<len; i++){

                newObj = t.getAxis(leftTit[i],'leftY',leftAry[i],'line',hasPoint,i);
                UQChart.otherClass.Drawline.call(t,newObj,leftCol[i]);
            }
            UQChart.otherClass.Evt.call(t);
        }
    }


    // 【双】Y轴,左右双折线图
    UQChart.otherClass.DlineInit = function(){
        var t          = this;
        var mData      = t.mData;
        var dataObj    = mData.dataObj;
        //var bData    = t.bData;
        var leftAry    = dataObj.leftData.dataAry;
        var rigAry     = dataObj.rightData.dataAry;
        var newLeftAry = [];
        var newRigAry  = [];
        var leftTit    = dataObj.leftData.titleAry;
        var rightTit   = dataObj.rightData.titleAry;
        var leftCol    = dataObj.leftData.colorAry;
        var rightCol   = dataObj.rightData.colorAry;
        var ctx        = t.ctx;
        var pointList  = [dataObj.leftData.hasPoint,dataObj.rightData.hasPoint];

        //js执行
        init();

        function init(){
            var newAry   = [];
            var drawLine = function(ary,direction,titleAry,hasPoint,colorAry){
                for(var i=0,len=ary.length; i<len; i++){
                    newAry[i] = t.getAxis(titleAry[i],direction,ary[i],'line',hasPoint,i);
                    UQChart.otherClass.Drawline.call(t,newAry[i],colorAry[i]);
                }
            }

            drawLine(leftAry,"leftY",leftTit,pointList[0],leftCol);
            drawLine(rigAry,"rightY",rightTit,pointList[1],rightCol);
            UQChart.otherClass.Evt.call(t);
        }

    }

    //绘制折线图，独立的类
    UQChart.otherClass.Drawline = function(ary,color){
        var t        = this;
        var len      = ary.length;
        var ctx      = t.ctx;
        var radius   = 4;
        var x,y;
        if(len ===0) return;
        ctx.lineWidth= 2;

        //对应的x轴点,画折线
        for(var j=0; j<len; j++){
            ctx.shadowBlur   = 0;
            ctx.strokeStyle  = color;
            if(j === 0){
                ctx.beginPath();
                ctx.moveTo(ary[0].x,ary[0].y);
            }else{
                ctx.lineTo(ary[j].x,ary[j].y);
            }
            ctx.stroke();
            if(j === len-1) ctx.closePath();
        }

        //画点,操作
        for(var i=0; i<len; i++){
            ctx.shadowBlur  = 3;
            ctx.shadowColor = 'rgba(64, 64, 64, 0.5)';
            ctx.fillStyle   = color;
            ctx.beginPath();
            x = ary[i].x;
            y = ary[i].y;
            ctx.arc(x,y,radius,0,Math.PI*2,true);
            ctx.fill();
            ctx.closePath();
        }
    }

    // 显示文字提示，事件类，有提示框出现
    UQChart.otherClass.Evt = function(obj){
        var t          = this;
        var id         = t.id;
        var canvasObj  = t.canBox;
        var offset     = canvasObj.offset();
        var lineTipAry = t.lineTipAry;
        var barTipAry  = t.barTipAry;
        var overId     = id +"_over";
        var chartType  = t.mData.dataObj.leftData.typeAry;

        if(t.bData.doubleY){
            chartType  = chartType.concat(t.mData.dataObj.rightData.typeAry);
        }

        //构造函数
        init();

        function init(){
            lineEvt();
            barEvt();
        }

        //折线图漂浮层，显示提示,注册事件，
        function lineEvt(){
            //鼠标位置
            var x,y;
            var showBox    = null;
            var overId     = overId + '_line';
            canvasObj.mousemove(function(e){
                var width = 0;
                var left  = 0;
                $('#'+overId).hide();
                x = e.pageX - offset.left;
                y = e.pageY - offset.top;

                for(var i=0,len=lineTipAry.length; i<len; i++){
                    if(x>=lineTipAry[i][0] && x<=lineTipAry[i][1] && y>=lineTipAry[i][2] && y<=lineTipAry[i][3]){
                        if($('#'+overId).length > 0){
                            width =  $('#'+overId).width();
                            left  =  (x-width/2-3);
                            $('#'+overId).css({'top':(y-50),'left':left}).html(lineTipAry[i][4]).show();
                        }else{
                            showBox = $('<div id="' + overId + '" class="canvasOver" style="position:absolute; background:#fffeed; padding:6px; border-radius:5px; border:1px solid #c0c0c0; position:absolute; z-index:10; font-size:12px; top:'+ (y-50) +'px;left:'+ x +'px;">'+ lineTipAry[i][4] +'</div>');
                            showBox.appendTo(canvasObj.parent()).show();
                            width =  $('#'+overId).width();
                            left  =  (x-width/2-3);
                            showBox.css('left',left);
                            return false;
                        }
                    }else{
                        //$('#'+overId).hide();
                    }
                }
            })
            canvasObj.mouseout(function(e){
                $('#'+overId).remove();
            })
        }

        //柱状图的注册事件
        function barEvt(){

            //鼠标位置
            var x,y;
            var showBox    = null;
            var overId     = overId + '_bar';
            canvasObj.mousemove(function(e){
                $('#'+overId).hide();
                x = e.pageX - offset.left;
                y = e.pageY - offset.top;

                for(var i=0,len=barTipAry.length; i<len; i++){
                    if(x>barTipAry[i][0] && y>barTipAry[i][1] && x<barTipAry[i][2] && y < barTipAry[i][3]){

                        var barWid = barTipAry[i][2] - barTipAry[i][0];
                        var barY   = barTipAry[i][1] - 50;
                        var barX   = barTipAry[i][0] + barWid/2;
                        if($('#'+overId).length > 0){
                            width =  $('#'+overId).width();
                            left  =  x - width/2 - 4;
                            $('#'+overId).css({'top':(barY),'left':left}).html(barTipAry[i][4]).show();
                        }else{
                            showBox = $('<div id="' + overId + '" class="canvasOver" style="position:absolute; background:#fffeed; padding:6px; border-radius:5px; border:1px solid #c0c0c0; position:absolute; z-index:10; font-size:12px; top:'+ (barY) +'px;left:'+ x +'px;">'+ barTipAry[i][4] +'</div>');
                            showBox.appendTo(canvasObj.parent()).show();
                            width =  $('#'+overId).width();
                            left  =  x - width/2 - 4;
                            showBox.css('left',left);
                            return false;
                        }
                    }
                }
            });

            canvasObj.mouseout(function(e){
                $('#'+overId).remove();
            })
        }
    }
	
	//是否需要画一条平均线
	UQChart.drawAverLine = function(t){
		//有才执,行hasAverLine:true
		//leftY = {"stepNum":1,"axisMax":123,"toSideNum":50}
		if(t.bData.hasAverLine){
			var leftY  = t.leftY;
			var yArr   = t.mData.dataObj.leftData.dataAry[0];
			var averVal= 0;
			var ctx    = t.ctx;
			var startDot = {};
			var endDot   = {};
			var getAverVal = function(arr){
			    var total = 0;
				var len   = arr.length
				for(var i=0; i<len; i++){
				    total += Number(arr[i]);
				}
				averVal = total/len;
				return averVal;
			}
			
			var drawLine = function(startDot,endDot){
				ctx.strokeStyle = "#ffcc00";
				ctx.lineWidth   = 1;
				ctx.shadowBlur  = 0;
				ctx.beginPath();
				ctx.moveTo(startDot.x,startDot.y);
				ctx.lineTo(endDot.x,endDot.y);
				ctx.stroke();
			}
			
			averVal = getAverVal(yArr);
			if(averVal === 0) return;
			startDot.x = leftY.toSideNum;
			
			startDot.y = t.axisH - t.axisH*averVal/leftY.axisMax + t.axisRect[0].y;
			endDot.x   = startDot.x + t.axisW;
			endDot.y   = startDot.y;

			drawLine(startDot,endDot);
			
	    }
		
	}


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
    //此处的call方法能实现神奇的继承功能
    UQChart.chartClass = {
        Overlap:function(obj){
            var other = UQChart.otherClass;
            var t     = this;
			
            other.BaseCanvas.call(t,obj);
            other.OverlapInit.call(t);
        },
        SlineChart:function(obj){
            var other = UQChart.otherClass;
            var t     = this;
            other.BaseCanvas.call(t,obj);
            other.DrawAxis.call(t,"single");
            other.SlineInit.call(t);
        },
        SbarChart:function(obj){
            var other = UQChart.otherClass;
            var t     = this;
            other.BaseCanvas.call(t,obj);
            other.DrawAxis.call(t,"single");
            other.SbarInit.call(t);
        },
        SmixChart:function(obj){
            var other = UQChart.otherClass;
            var t     = this;
            other.BaseCanvas.call(t,obj);
            other.DrawAxis.call(t,"single");
            other.SmixInit.call(t);
        },
        DlineChart:function(obj){
            var other = UQChart.otherClass;
            var t     = this;
            other.BaseCanvas.call(t,obj);
            other.DrawAxis.call(t,"double");
            other.DlineInit.call(t);
        },
        DmixChart:function(obj){
            var other = UQChart.otherClass;
            var t     = this;
            other.BaseCanvas.call(t,obj);
            other.DrawAxis.call(t,"double");
            other.DMixInit.call(t);
			UQChart.drawAverLine(t);
			
        }
    }

    UQChart.util.checkFn = function(obj,type){
        if(!obj.id || !obj.data) return false;
        try{
           document.createElement('canvas').getContext('2d');
        }
        catch(e){
        	//alert('ie~~~');
            //UQChart.VML(obj,type);
            return false;
        }
        return true;
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

        ctx.font      = '14px  Arial,Helvetica,sans-serif';
        ctx.fillStyle = "#000";
        textW         = ctx.measureText(wText).width;
        startX        = (boxWid - textW) / 2;
        startY        = (boxHei - 14) / 2 + 14;
        ctx.fillText(wText,startX,startY);
    }

    //生成图表的【统一调用】的方法
    //1)检查下，canvas不支持转到IE的vml方法  2) 智能获取图形的类型，检查数据是否合法  3) 生成新图表
    UQChart.myNew = function(obj){
        var type     = '';
        var newChart = null;
        type         = UQChart.getType(obj);
        if(!UQChart.util.checkFn(obj,type)) return false;
		
        newChart     = new UQChart.chartClass[type](obj);
    }

    //智能获取图表类型
    UQChart.getType     = function(obj){
        var chartType   = '';
        var data        = obj.data;
		var type        = obj.type?obj.type:false;
		if(type) return type;
        try{
            var axisY    = data.base.doubleY;
            var leftData = data.mainData.dataObj.leftData;
            var leftDList= leftData.dataAry;
            var leftType = leftData.typeAry;
            var typeOne  = leftType[0];
            var mix      = false;

            var flagMIX  = function (str,typeList){
                for(var i=1,len=typeList.length; i<len; i++){
                    if(str !== typeList[i]){
                        chartType = true;
                        return chartType;
                    }
                }
                return false;
            }


            //过滤获取新数据
            var oneAry     = [];
            var getNewData = function(sideData,side){
                for(var i=0,len=sideData.length; i<len; i++){
                    oneAry     = sideData[i];
                    obj.strData[side].push(UQChart.util.filter(oneAry));
                }
            }

            //过滤后的数据放在此处,很重要
            obj.strData = {
                left:[],
                right:[]
            }
            getNewData(leftDList,'left');

            if(axisY){
                var rightData = data.mainData.dataObj.rightData;
                var rigType   = rightData.typeAry;
                var typeList  = leftType.concat(rigType);
                getNewData(rightData.dataAry,'right');
                mix           = flagMIX(typeOne,typeList);
                chartType     = mix?'Dmix':'D'+typeOne.toLowerCase();;
            }else{
                mix           = flagMIX(typeOne,leftType);
                chartType     = mix?'Smix':'S'+typeOne.toLowerCase();;
            }
            return chartType + 'Chart';
        }catch(e){
            //数据格式错误提示错误提示
            UQChart.util.showWrong(obj);
        }
    }
	
	//obj,str
	//t,"t.mData.dataObj.rightData.maxPNum"
    //var leftMax  = UQChart.setDataOk(t,'t.mData.dataObj.leftData.maxPNum');
	UQChart.setDataOk = function(obj,str){
		var arr    = str.split('.');
		var i      = 1;

        var oneObj = obj;
		for(var i=1; i<arr.length; i++){
		   if(!oneObj[arr[i]]){
              oneObj[arr[i]] = {};
			  oneObj = oneObj[arr[i]];
		   }
		}

		return obj;
		
	}


    //【双】Y轴,柱状折线混合图
    UQChart.dMixChart = function(obj){
        var newChart = new UQChart.chartClass.DmixChart(obj);
        return true;
    }

    //双圆重叠图,2个圆看重叠情况
    UQChart.overLapChart = function(obj){
        var newChart = new UQChart.chartClass.Overlap(obj);
        return true;
    }
})();

