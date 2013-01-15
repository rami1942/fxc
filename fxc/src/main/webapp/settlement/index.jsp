<!DOCTYPE html>
<html lang="ja">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection"/>
  <title>日々サマリ</title>
</head>
<body>

<t:form actionClass="org.dyndns.bluefield.fxc.action.SettlementAction" actionMethod="index" value="${action}" method="GET" >
<input type="hidden" name="ak" value="${accessKey}" />
日付:<input type="text" name="date" value="${date}" />
<input type="submit" value="表示" />
</t:form>

<table border="1">
<tr>
<th>POS</th><th>ロット</th><th>CLOSE時刻</th><th>損益</th><th>スワップ</th><th>種別</th>
</tr>
<c:forEach var="p" items="${history}">
<tr>
<td>
	<c:if test="${p.posCd == 0}">L</c:if>
	<c:if test="${p.posCd != 0}">S</c:if>
	${p.openPrice}
</td>
<td>${p.lots}</td>
<td>${p.closeDt}</td>
<td align="right">${my:commaSep(p.profit)}</td>
<td align="right">${my:commaSep(p.swapPoint)}</td>
<td>
<c:choose>
<c:when test="${p.posCd == 0 && p.magicNo != 0 }">ショートトラップ</c:when>
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

</body>
</html>
