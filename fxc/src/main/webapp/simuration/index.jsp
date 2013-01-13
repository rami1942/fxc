<!DOCTYPE html>
<html lang="ja">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection"/>
  <title>試算</title>
</head>
<body>

<p>
<t:form actionClass="org.dyndns.bluefield.fxc.action.SimurationAction" actionMethod="index" value="${action}" method="GET" >
<input type="hidden" name="ak" value="${accessKey}" />
想定レート:<input type="text" name="targetRate" value="${targetRate}" />
<input type="submit" value="計算" />
</t:form>
</p>

<table border="1">
<tr><th>残高</th><td align="right">${my:commaSep(balance)}</td></tr>
<tr><th>損益</th><td align="right">${my:commaSep(proLossTotal)}</td></tr>
<tr><th>必要証拠金</th><td align="right">${my:commaSep(requiredMargin) }</td></tr>
<tr><th>有効証拠金</th><td align="right">${my:commaSep(balance + proLossTotal)}</td></tr>
<tr><th>余剰証拠金</th><td align="right">${my:commaSep(balance + proLossTotal - requiredMargin)}</td></tr>
<tr><th>証拠金維持率</th><td align="right">
<c:if test="${requiredMargin > 0}">${marginPer}%</c:if>
<c:if test="${requiredMargin == 0}">-</c:if>
</td></tr>
</table>

<br/>
<p>
ロング:ショート = ${longLots}:${shortLots}
</p>

<table border="1">
<tr>
	<th>POS</th><th>SL</th><th>ロット</th><th>損益</th><th>タイプ</th>
</tr>
<c:forEach var="p" items="${positions}">
<c:if test="${p.active}">
<c:set var="bgcolor" value="#FFFFFF"/>
</c:if>
<c:if test="${!p.active}">
<c:set var="bgcolor" value="#808080"/>
</c:if>

<tr>
<td bgcolor="${bgcolor}">
	<c:if test="${p.long}">L</c:if>
	<c:if test="${!p.long}">S</c:if>
	${p.openPrice}
</td>
<td bgcolor="${bgcolor}">
<c:if test="${p.slPrice != null}">${p.slPrice}</c:if>
</td>
<td bgcolor="${bgcolor}">${p.lots}</td>
<td bgcolor="${bgcolor}" align="right">${my:commaSep(p.proLoss)}</td>
<td bgcolor="${bgcolor}">
<c:choose>
<c:when test="${p.posCd ==1}">本体ロング</c:when>
<c:when test="${p.posCd ==2}">複利ロング</c:when>
<c:when test="${p.posCd ==3}">裁量ロング</c:when>
<c:when test="${p.posCd ==4}">ショートトラップ</c:when>
<c:when test="${p.posCd ==5}">出口益S</c:when>
<c:when test="${p.posCd ==6}">固定ショート</c:when>
<c:when test="${p.posCd ==7}">裁量ショート</c:when>
<c:otherwise>${p.posCd}</c:otherwise>
</c:choose>
</td>
</tr>
</c:forEach>
</table>

<hr/>
<ul>
<li><a href="../chart?ak=${accessKey}">チャート</a></li>
<li><a href="../position?ak=${accessKey}">ポジション</a></li>
<li><a href="../?ak=${accessKey}">トラップ</a></li>
<li><a href="../simuration/export?ak=${accessKey}">エクスポート</a></li>
</ul>

</body>
</html>