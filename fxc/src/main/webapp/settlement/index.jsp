<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection"/>
  <title>決算</title>

<script type="text/javascript">

function unReserve(form, id) {
	form.id.value = id;
	form.submit();
}

</script>

</head>
<body>
<c:import url="/common/errors.jsp"/>
<c:import url="/common/notice.jsp"/>

<p>
前回: ${f:dateFormat(fromDt, "yyyy-MM-dd HH:mm:ss")}<br/>
<br/>
口座残高: ￥${my:commaSep(balance)}(${balanceDiff})<br/>
含み損益: ￥${my:commaSep(profit)}(${profitDiff})<br/>
スワップ: ${my:commaSep(swapPoint)}<br/>
リピート: ${numRepeat}回
</p>

<t:form actionClass="org.dyndns.bluefield.fxc.action.SettlementAction" actionMethod="update" value="${action}">
<input type="submit" value="リセット" />
</t:form>

<hr />

<table>
<tr><td>累積利益</td><td>¥${kkwProfit}</td></tr>
<tr>
<td>基準日時:</td>
<td>
<t:form actionClass="org.dyndns.bluefield.fxc.action.SettlementAction" actionMethod="setBaseDt" value="${action}" method="POST">
<t:input type="text" name="baseDt" /> <input type="submit" value="変更" />
</t:form>
</td>
</tr>
<tr>
<td>確保分:</td>
<td>
<t:form actionClass="org.dyndns.bluefield.fxc.action.SettlementAction" actionMethod="setProfitReservation" value="${action}" method="POST" >
<t:input type="text" name="profitReservation"/> <input type="submit" value="変更" />
</t:form>
</td>
</tr>
<tr>
<td>仮想建値割振り額:</td>
<td>
<t:form actionClass="org.dyndns.bluefield.fxc.action.SettlementAction" actionMethod="setVirtualPriceReservation" value="${action}" method="POST" >
<t:input type="text" name="virtualPriceReservation"/> <input type="submit" value="変更" /> (￥${my:commaSep(oneLinePrice)}/1段)
</t:form>
</td>
</tr>
</table>

<p>SL確定益</p>
<table border="1">
<tr>
<th>建値</th><th>SL</th><th>ロット</th><th>証拠金</th><th>SL損益</th>
</tr>
<c:forEach var="d" items="${discs}">
<tr>
<td>
	<c:if test="${d.isLong}">L</c:if>
	<c:if test="${!d.isLong}">S</c:if>
	${d.openPrice}
</td>
<td>
	<c:if test="${d.slPrice != null}">${d.slPrice}</c:if>
	<c:if test="${d.slPrice == null}">-</c:if>
</td>
<td>${d.lots}</td>
<td>
	<c:if test="${d.margin != null}">
		${my:commaSep(d.margin)}
	</c:if>
	<c:if test="${d.margin == null}">-</c:if>
</td>
<td>${my:commaSep(d.profit)}</td>
</tr>
</c:forEach>
</table>

<p>調整分</p>
<table border="1">
<tr>
	<th>金額</th>
	<th>用途</th>
	<th>&nbsp;</th>
</tr>
<t:form actionClass="org.dyndns.bluefield.fxc.action.SettlementAction" actionMethod="delete" value="${action}">
<input type="hidden" name="id" />
<c:forEach var="rp" items="${reservedProfits}">
<tr>
	<td align="right">${my:commaSep(rp.amount)}</td>
	<td>${f:out(rp.description)}</td>
	<td><input type="button" value="解除" onclick="unReserve(this.form, ${rp.id})" /></td>
</tr>
</c:forEach>
</t:form>
<t:form actionClass="org.dyndns.bluefield.fxc.action.SettlementAction" actionMethod="reserve" method="POST" value="${action}">
<tr>
	<td><t:input type="text" name="reserveAmount" /></td>
	<td><t:input type="text" name="reserveDesc" /></td>
	<td><input type="submit" value="登録" /></td>
</tr>
</t:form>
</table>
<p>残額: ¥${my:commaSep(remain)}</p>

<hr />
<table border="1">
<tr>
	<th>出口益S</th>
	<th>確保益L</th>
	<th>確保益S(SL出口)</th>
	<th>確保益S(SL建値)</th>
</tr>
<tr>
<td>
可能額: ${shAmount}<br/>
可能量(Lot): ${hedgeLots}
</td>
<td>&nbsp;</td>
<td>
<c:if test="${lotsShortExit != null}">
可能量(Lot): ${lotsShortExit}
</c:if>
<c:if test="${lotsShortExit == null}">-</c:if>
</td>
<td>&nbsp;</td>
</tr>
</table>

<p>
<a href="../chart?ak=${accessKey}">チャート</a><br/>
<a href="../?ak=${accessKey}">ポジション</a><br/>
</p>
</body>
</html>