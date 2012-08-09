<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="ja" xml:lang="ja">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection" charset="utf-8" />
  <title>Position</title>
</head>
<body>

<p>トラップ</p>
<input type="button" value="Extend ↑" />
<table border="1">
<c:forEach var="sp" items="${shorts}">
<tr>
  <td>S</td>
  <td>${f:out(sp.openPrice) }</td>
  <td>${f:out(eachLots) }</td>
  <td><input type="button" value="Delete" /></td>
</tr>
</c:forEach>
</table>
<input type="button" value="Extend ↓" />

<p>本体ロング</p>

<p>
平均建値: ${longAverage}<br/>
トラップ本数: ${numTraps}
</p>
<table border="1">
<c:forEach var="lp" items="${longs}">
<tr>
  <td>L</td>
  <td>${lp.openPrice}</td>
  <td>${lp.lots}</td>
</tr>
</c:forEach>
</table>
<ul>
<li><a href="config">設定</a></li>
</ul>
</body>
</html>
