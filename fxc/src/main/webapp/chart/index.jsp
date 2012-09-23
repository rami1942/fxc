<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html lang="ja">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta http-equiv="Content-Language" content="ja" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <meta http-equiv="Content-Script-Type" content="text/javascript" />
    <title>状況</title>

<script type="text/javascript" src="${contextPath}/js/anbt-dashed-line.js"></script>
<script type="text/javascript">

var width = ${numTraps};
var currentPrice = ${currentPricePos};
var basePrice = '${basePrice}';
var baseLine = ${baseLine};
var truePrice = '${truePrice}';
var trueLine = ${trueLine};
var price = [${prices}];
var ispos = [${positions}];
var longPos = [${longPositions}];

var discPosType = new Array(${discPosType} null);
var discPosOP = new Array(${discPosOP} null);
var discPosSL = new Array(${discPosSL} null);
var discPosPrice = new Array(${discPosPrice} null);
var discPosWidth = new Array(${discPosWidth} null);

var under = Math.round(baseLine + 0.5) ;

var bx = 150;
var by = 20;
var boxSize = 40;

window.onload=function() {
	var canvas = document.getElementById('cvs');
	var ctx = canvas.getContext('2d');

	// 建値
	if (baseLine != trueLine) {
		var lineStyle = {
			color: "#ff0000",
			pattern: "*-",
			scale: 15,
			width: 2,
			cap: "butt", // butt, round, square
			join: "bevel" // round, bevel, miter
		};
		var adl = new AnbtDashedLine();
		var v = [[80, by + boxSize * (trueLine + 1)], [bx + boxSize * (width + 1), by + boxSize * (trueLine + 1)]];
		adl.drawDashedPolyLine(ctx, v, lineStyle);

		ctx.font="14px 'Times New Roman'";
		ctx.fillStyle = 'red';
		ctx.fillText(truePrice, 20, by + 5 + boxSize * (trueLine + 1));
	}

	// 仮想建値(価格・横線)
	ctx.strokeStyle='red';
	ctx.lineWidth=2;
	ctx.beginPath();
	ctx.moveTo(80, by + boxSize * (baseLine + 1));
	ctx.lineTo(bx + boxSize * (width + 1), by + boxSize * (baseLine + 1));
	ctx.stroke();
	ctx.font="14px 'Times New Roman'";
	ctx.fillStyle = 'red';
	ctx.fillText(basePrice, 20, by + 5 + boxSize * (baseLine + 1));

	drawBlocks(ctx, width, under);

	// トラップライン
	ctx.lineWidth=4;
	for (var i = 0; i < width; i++) {
		if (ispos[i] == 0) {
			ctx.strokeStyle = 'lime';
		} else {
			ctx.strokeStyle = 'magenta';
		}
		ctx.beginPath();
		ctx.moveTo(bx + boxSize * (width - i - 1), by + boxSize * (i+1) - 1);
		ctx.lineTo(bx + boxSize * (width - i), by + boxSize * (i+1) - 1);
		ctx.stroke();
	}

	// 価格の横線
	ctx.lineWidth=2;
	ctx.strokeStyle='black';
	for (var i = 0; i < width; i+= 2) {
		ctx.beginPath();
		ctx.moveTo(80, by + boxSize * (i + 1));
		ctx.lineTo(120, by + boxSize * (i + 1));
		ctx.stroke();
	}

	// 価格の表示
	ctx.font="16px 'Times New Roman'";
	ctx.fillStyle = 'black';
	for (i = 0; i < width; i += 2) {
		ctx.fillText(price[i/2], 20, by + 5 + boxSize * (i+1));
	}

	drawPriceArrow(ctx, currentPrice);
	drawSideMarker(ctx);

	// 裁量ポジション(ショート)
	for (var i = 0; i < discPosType.length - 1; i++) {
		if (discPosType[i]) {
			drawLongDiscPosition(ctx, i, discPosOP[i], discPosSL[i], discPosWidth[i]);
		} else {
			drawShortDiscPosition(ctx, i, discPosOP[i], discPosSL[i], discPosWidth[i]);
		}
		ctx.font="12px 'Times New Roman'";
		ctx.fillStyle = 'black';
		ctx.textAlign = 'center';
		var offset;
		if (currentPrice < discPosOP[i]) {
			offset = 15;
		} else {
			offset = -10;
		}
		ctx.fillText(discPosPrice[i], bx + (width + 2 + i) * boxSize, by + offset + boxSize * (discPosOP[i] + 1));
	}

	if (discPosType.length > 1) {
		ctx.strokeStyle = 'darkgray';
		ctx.beginPath();
		ctx.moveTo(bx + (width + 2) * boxSize - boxSize / 2, by + boxSize * (currentPrice + 1));
		ctx.lineTo(bx + (width + discPosType.length) * boxSize + boxSize / 2, by + boxSize * (currentPrice + 1));
		ctx.closePath();
		ctx.stroke();
	}
}

function drawLongDiscPosition(ctx, xp, openPrice, slPrice, arrowWidth) {
	var color;
	var slTo;
	if (slPrice != null && currentPrice < openPrice && currentPrice < slPrice) {
		color = 'chocolate';
		slTo = openPrice;
	} else {
		color = 'black';
		slTo = currentPrice;
	}
	if (currentPrice < openPrice && (slPrice == null || slPrice > openPrice)) {
		color = 'red';
	}

	var xpos = bx + (width + 2 + xp) * boxSize;
	var ypos = by + (openPrice + 1) * boxSize;

	// 矢印本体(SL〜建値 or 現在値)
	if (slPrice != null) {
		var lineStyle = {
			color: "darkgray",
			pattern: "*-",
			scale: 10,
			width: 1,
			cap: "butt", // butt, round, square
			join: "bevel" // round, bevel, miter
		};
		var adl = new AnbtDashedLine();
		var slY = by + (slPrice + 1) * boxSize;
		var v = [[xpos, slY], [xpos, ypos]];
		adl.drawDashedPolyLine(ctx, v, lineStyle);
		ctx.strokeStyle = 'darkgray';
		ctx.beginPath();
		ctx.moveTo(xpos - 5, slY - 5);
		ctx.lineTo(xpos + 5, slY + 5);
		ctx.closePath();
		ctx.stroke();
		ctx.beginPath();
		ctx.moveTo(xpos + 5, slY - 5);
		ctx.lineTo(xpos - 5, slY + 5);
		ctx.closePath();
		ctx.stroke();
	}

	// 矢印本体(建値〜現在値)
	ctx.strokeStyle = color;
	ctx.lineWidth = 1;

	ctx.beginPath();
	ctx.moveTo(xpos, ypos);
	ctx.lineTo(xpos, by + (currentPrice + 1) * boxSize + 5);
	ctx.closePath();
	ctx.stroke();

	ctx.beginPath();
	ctx.moveTo(xpos - 7 , ypos);
	ctx.lineTo(xpos + 7, ypos);
	ctx.closePath();
	ctx.stroke();

	// 矢印頭
	var curYPos = by + (currentPrice + 1) * boxSize;
	ctx.fillStyle = color;
	ctx.lineWidth=1;
	ctx.beginPath();
	ctx.moveTo(xpos, curYPos);
	ctx.lineTo(xpos - arrowWidth, curYPos + 20);
	ctx.lineTo(xpos + arrowWidth, curYPos + 20);
	ctx.closePath();
	ctx.fill();
}

function drawShortDiscPosition(ctx, xp, openPrice, slPrice, arrowWidth) {
	var color;
	var slTo;
	if (slPrice != null && currentPrice > openPrice && currentPrice > slPrice) {
		color = 'chocolate';
		slTo = openPrice;
	} else {
		color = 'black';
		slTo = currentPrice;
	}
	if (currentPrice > openPrice && (slPrice == null || slPrice < openPrice)) {
		color = 'red';
	}

	var xpos = bx + (width + 2 + xp) * boxSize;
	var ypos = by + (openPrice + 1) * boxSize;

	// 矢印本体(SL〜建値 or 現在値)
	if (slPrice != null) {
		var lineStyle = {
			color: "darkgray",
			pattern: "*-",
			scale: 10,
			width: 1,
			cap: "butt", // butt, round, square
			join: "bevel" // round, bevel, miter
		};
		var adl = new AnbtDashedLine();
		var slY = by + (slPrice + 1) * boxSize;
		var v = [[xpos, slY], [xpos, ypos]];
		adl.drawDashedPolyLine(ctx, v, lineStyle);
		ctx.strokeStyle = 'darkgray';
		ctx.beginPath();
		ctx.moveTo(xpos - 5, slY - 5);
		ctx.lineTo(xpos + 5, slY + 5);
		ctx.closePath();
		ctx.stroke();
		ctx.beginPath();
		ctx.moveTo(xpos + 5, slY - 5);
		ctx.lineTo(xpos - 5, slY + 5);
		ctx.closePath();
		ctx.stroke();
	}

	// 矢印本体(建値〜現在値)
	ctx.strokeStyle = color;
	ctx.lineWidth = 1;

	ctx.beginPath();
	ctx.moveTo(xpos, ypos);
	ctx.lineTo(xpos, by + (currentPrice + 1) * boxSize - 5);
	ctx.closePath();
	ctx.stroke();

	ctx.beginPath();
	ctx.moveTo(xpos - 7 , ypos);
	ctx.lineTo(xpos + 7, ypos);
	ctx.closePath();
	ctx.stroke();

	// 矢印頭
	var curYPos = by + (currentPrice + 1) * boxSize;
	ctx.fillStyle = color;
	ctx.lineWidth=1;
	ctx.beginPath();
	ctx.moveTo(xpos, curYPos);
	ctx.lineTo(xpos - arrowWidth, curYPos - 20);
	ctx.lineTo(xpos + arrowWidth, curYPos - 20);
	ctx.closePath();
	ctx.fill();
}

// ロングを示すマーカー
function drawSideMarker(ctx) {
	ctx.fillStyle = 'black';
	ctx.lineWidth = 1;
	for (var i = 0; i < longPos.length; i++) {
		var ypos = by + boxSize * (longPos[i] + 1);
		var xpos = bx + width * boxSize + 20;
		ctx.beginPath();
		ctx.moveTo(xpos, ypos);
		ctx.lineTo(xpos + 20, ypos - 6);
		ctx.lineTo(xpos + 20, ypos + 6);
		ctx.closePath();
		ctx.fill();
	}
}

function drawPriceArrow(ctx, currentPrice) {
	// 現在値を示す矢印
	var pos = by + boxSize * (currentPrice + 1);
	ctx.strokeStyle='red';
	ctx.lineWidth=2;
	ctx.beginPath();
	ctx.moveTo(80, pos);
	ctx.lineTo(110, pos);
	ctx.stroke();
	ctx.fillStyle='red';
	ctx.beginPath();
	ctx.moveTo(120, pos);
	ctx.lineTo(100, pos - 6);
	ctx.lineTo(100, pos + 6);
	ctx.closePath();
	ctx.fill();
}

function drawBlocks(ctx, width, under) {
	for (y = 0; y < under; y++) {
		for (x = 0; x < width - y; x++) {
			drawBlock(ctx, x, y, 3);
		}
		for (x = width - y; x < width; x++) {
			drawBlock(ctx, x, y, 1);
		}
	}

	for (y = under; y < width; y++) {
		for (x = 0; x < width - y; x++) {
			drawBlock(ctx, x, y, 2);
		}
	}
}

function drawBlock(ctx, x, y, typ) {
	var fillColor, strokeColor;
	switch(typ) {
	case 1:
		fillColor = 'orange';
		strokeColor = 'chocolate';
		break;
	case 2:
		fillColor = 'cornflowerblue';
		strokeColor = 'darkblue';
		break;
	case 3:
		fillColor = 'rgb(183,251,183)';
		strokeColor = 'darkblue';
		break;
	default:
		return;
	}
	ctx.fillStyle = fillColor;
	ctx.fillRect(bx + boxSize * x, by + boxSize * y, boxSize - 1, boxSize - 1);
	ctx.lineWidth = 2;
	ctx.strokeStyle = strokeColor;
	ctx.strokeRect(bx + boxSize * x, by + boxSize * y, boxSize - 1, boxSize - 1);
}

</script>
  </head>
  <body>
  	<p>
  	  ${currentDatetime}<br/>
  	  現在値: ${currentPrice}<br/>
  	</p>
    <p>
    <a href="../settlement/?ak=${accessKey}">計算</a><br/>
    <a href="../?ak=${accessKey}">ポジション</a><br/>
    </p>
    <canvas id="cvs" width="1000" height="1000"></canvas>
  </body>
</html>