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
var freezePos = [${freezePositions}];

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
	drawLongMarker(ctx);
	drawFreezeMarker(ctx);
}

// ロングを示すマーカー
function drawLongMarker(ctx) {
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

// 凍結ロングを示すマーカー
function drawFreezeMarker(ctx) {
	ctx.strokeStyle = 'black';
	ctx.lineWidth = 1;
	for (var i = 0; i < freezePos.length; i++) {
		var ypos = by + boxSize * (freezePos[i] + 1);
		var xpos = bx + width * boxSize + 20;
		ctx.beginPath();
		ctx.moveTo(xpos, ypos);
		ctx.lineTo(xpos + 20, ypos - 6);
		ctx.lineTo(xpos + 20, ypos + 6);
		ctx.closePath();
		ctx.stroke();
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