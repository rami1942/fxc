<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ja" lang="ja">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta http-equiv="Content-Language" content="ja" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <meta http-equiv="Content-Script-Type" content="text/javascript" />
    <title>Canvas</title>
<script type="text/javascript">

var width = 7;
var under = 4;

var price = ['83.450', '83.050', '82.650', '82.250'];

var bx = 150;
var by = 100;
var boxSize = 40;

window.onload=function() {
	var canvas = document.getElementById('cvs');
	var ctx = canvas.getContext('2d');

	drawBlocks(ctx, width, under);

	for (var i = 0; i < width; i+= 2) {
		ctx.beginPath();
		ctx.moveTo(80, by + boxSize * (i + 1));
		ctx.lineTo(120, by + boxSize * (i + 1));
		ctx.stroke();
	}

	ctx.font="16px 'Times New Roman'";
	ctx.fillStyle = 'black';
	for (i = 0; i < width; i += 2) {
		ctx.fillText(price[i/2], 20, 100 + 5 + 40 * (i+1));
	}
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
    <canvas id="cvs" width="1000" height="1000"></canvas>
  </body>
</html>