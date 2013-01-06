<!DOCTYPE html>
<html lang="ja">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection"/>
  <title>トラップ</title>

<script type="text/javascript">

function freeze(form, ticketNo) {
	form.ticketNo.value = ticketNo;
	form.submit();
}

</script>
</head>
<body>

<ul>
<li><a href="chart?ak=${accessKey}">チャート</a></li>
<li><a href="position?ak=${accessKey}">ポジション</a></li>
<li><a href="config?ak=${accessKey}">設定</a></li>
</ul>


<c:if test="${my:size(shorts) != numTraps }">
<font color="red">
トラップの本数と本体のサイズが一致していません。チャートは正しく表示されないかも。
</font>
</c:if>

<p>トラップ</p>
<table>
<tr>
<td>
<form action="extendUp">
<input type="submit" value="Extend ↑" />
</form>
</td>
<td>
<form action="shortenUp">
<input type="submit" value="Shorten ↓" />
</form>
</td>
</tr>
</table>
<table border="1">
<c:forEach var="sp" items="${shorts}" varStatus="stat">
<tr>
  <td>${stat.count}</td>
  <td>${f:out(sp.openPrice) }</td>
  <td align="right">${f:out(my:commaSep(eachLots)) }</td>
  <td>
  	<c:if test="${sp.isReal == 1}">◯</c:if>
  	<c:if test="${sp.isReal == 0}">&nbsp;</c:if>
  </td>
</tr>
</c:forEach>
</table>
<table>
<tr>
<td>
<form action="extendDown">
<input type="submit" value="Extend ↓" />
</form>
</td>
<td>
<form action="shortenDown">
<input type="submit" value="Shorten ↑" />
</form>
</td>
</tr>
</table>

<p>本体ロング</p>

<p>
平均建値: ${longAverage}<br/>
トラップ本数: ${numTraps}<br/>
ロスカット: ${lossCutPrice}(${lossCutLevel}%)
</p>
<form action="freezePosition">
<input type="hidden" name="ticketNo" value=""/>
<input type="hidden" name="price" value="" />
<table border="1">
<c:forEach var="lp" items="${longs}" varStatus="stat">
<tr>
  <td>${stat.count}</td>
  <td>${lp.openPrice}</td>
  <td align="right">${lp.lots}</td>
  <td><input type="button" value="戻す" onclick="freeze(this.form, ${lp.ticketNo})"/></td>
</tr>
</c:forEach>
</table>
</form>

</body>
</html>
