<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="ja">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection"/>
  <title>Position</title>
</head>
<body>

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
  <td>${f:out(eachLots) }</td>
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
トラップ本数: ${numTraps}
</p>
<table border="1">
<c:forEach var="lp" items="${longs}" varStatus="stat">
<tr>
  <td>${stat.count }</td>
  <td>${lp.openPrice}</td>
  <td>${lp.lots}</td>
</tr>
</c:forEach>
</table>

<c:if test="${freezes.size() > 0 }">
<p>凍結ポジション</p>
<table border="1">
<c:forEach var="fp" items="${freezes}" varStatus="stat">
<tr>
	<td>${stat.count}</td>
	<td>${fp.openPrice}</td>
	<td>${fp.lots}</td>
</tr>
</c:forEach>
</table>
</c:if>

<ul>
<li><a href="chart">チャート</a></li>
<li><a href="config">設定</a></li>
</ul>
</body>
</html>
