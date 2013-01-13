<!DOCTYPE html>
<html lang="ja">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection"/>
  <title>エクスポート</title>
</head>
<body>

<table>
<c:forEach var="p" items="${exportPos}">
<tr>
<td>
<c:if test="${p.long}">L</c:if>
<c:if test="${!p.long}">S</c:if>
</td>
<td>${p.openPrice}</td>
<td>${p.lots}</td>
<td>${p.swapPoint}</td>
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

