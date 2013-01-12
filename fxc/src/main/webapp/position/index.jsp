<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection"/>
  <title>ポジション</title>

<script type="text/javascript">

function unReserve(form, id) {
	form.id.value = id;
	form.submit();
}

function changeType(form, ticketNo) {
	var sel = form['type'+ticketNo];
	form.ticketNo.value = ticketNo;
	form.selValue.value = sel.value;
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

<t:form actionClass="org.dyndns.bluefield.fxc.action.PositionAction" actionMethod="update" value="${action}">
<input type="submit" value="リセット" />
</t:form>

<hr />

<table>
<tr><td>累積利益</td><td>¥${kkwProfit}</td></tr>
<tr>
<td>確保分:</td>
<td>
<t:form actionClass="org.dyndns.bluefield.fxc.action.PositionAction" actionMethod="setProfitReservation" value="${action}" method="POST" >
<t:input type="text" name="profitReservation"/> <input type="submit" value="変更" />
</t:form>
</td>
</tr>
<tr>
<td>仮想建値割振り額:</td>
<td>
<t:form actionClass="org.dyndns.bluefield.fxc.action.PositionAction" actionMethod="setVirtualPriceReservation" value="${action}" method="POST" >
<t:input type="text" name="virtualPriceReservation"/> <input type="submit" value="変更" /> (￥${my:commaSep(oneLinePrice)}/1段)
</t:form>
</td>
</tr>
</table>

<p>ポジション</p>
<t:form actionClass="org.dyndns.bluefield.fxc.action.PositionAction" actionMethod="setPositionType" value="${action}">

<input type="hidden" name="ticketNo" />
<input type="hidden" name="selValue" />
<input type="hidden" name="ak" value="${accessKey}" />

<table border="1">
<tr>
<th>建値</th><th>SL</th><th>ロット</th><th>SL損益</th><th>現損益</th><th>種別</th>
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
<td align="right">${my:commaSep(d.slProfit)}</td>
<td align="right">${my:commaSep(d.realProfit)}</td>
<td>
<select name="type${d.ticketNo}" onchange="changeType(this.form, ${d.ticketNo})">
<c:if test="${d.isLong}">
	<c:choose>
	<c:when test="${d.posType == 2}">
	    <option value="0"></option>
		<option value="2" selected="selected">複利ロング</option>
		<option value="3">裁量ロング</option>
	    <option value="1">本体ロング</option>
	</c:when>
	<c:when test="${d.posType == 3}">
	    <option value="0"></option>
		<option value="2">複利ロング</option>
		<option value="3" selected="selected">裁量ロング</option>
	    <option value="1">本体ロング</option>
	</c:when>
	<c:otherwise>
	    <option value="0" selected="selected"></option>
		<option value="2">複利ロング</option>
		<option value="3">裁量ロング</option>
	    <option value="1">本体ロング</option>
	</c:otherwise>
	</c:choose>
</c:if>
<c:if test="${! d.isLong}">
	<c:choose>
	<c:when test="${d.posType == 5}">
	    <option value="0"></option>
		<option value="5" selected="selected">出口益S</option>
		<option value="6">固定ショート</option>
		<option value="7">裁量ショート</option>
	</c:when>
	<c:when test="${d.posType == 6}">
	    <option value="0"></option>
		<option value="5">出口益S</option>
		<option value="6" selected="selected">固定ショート</option>
		<option value="7">裁量ショート</option>
	</c:when>
	<c:when test="${d.posType == 7}">
	    <option value="0"></option>
		<option value="5">出口益S</option>
		<option value="6">固定ショート</option>
		<option value="7" selected="selected">裁量ショート</option>
	</c:when>
	<c:otherwise>
	    <option value="0" selected="selected"></option>
		<option value="5">出口益S</option>
		<option value="6">固定ショート</option>
		<option value="7">裁量ショート</option>
	</c:otherwise>
	</c:choose>
</c:if>
</select>
</td>

</tr>
</c:forEach>
</table>
</t:form>
<p>資金調整分</p>
<table border="1">
<tr>
	<th>金額</th>
	<th>用途</th>
	<th>&nbsp;</th>
</tr>
<t:form actionClass="org.dyndns.bluefield.fxc.action.PositionAction" actionMethod="delete" value="${action}">
<input type="hidden" name="id" />
<c:forEach var="rp" items="${reservedProfits}">
<tr>
	<td align="right">${my:commaSep(rp.amount)}</td>
	<td>${f:out(rp.description)}</td>
	<td><input type="button" value="解除" onclick="unReserve(this.form, ${rp.id})" /></td>
</tr>
</c:forEach>
</t:form>
<t:form actionClass="org.dyndns.bluefield.fxc.action.PositionAction" actionMethod="reserve" method="POST" value="${action}">
<tr>
	<td><t:input type="text" name="reserveAmount" /></td>
	<td><t:input type="text" name="reserveDesc" /></td>
	<td><input type="submit" value="登録" /></td>
</tr>
</t:form>
</table>
<p>残額: ¥${my:commaSep(remain)}</p>

<hr />
<p>現在レート: ${currentRate}</p>
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
	<td>
		<c:if test="${lotsLong != null}">
		目安: ${discLongBasePrice}<br/>
		可能量(Lot): ${lotsLong}
		</c:if>
		<c:if test="${lotsLong == null}">-</c:if>
	</td>
	<td>
		<c:if test="${exitRate != null}">
			出口: ${exitRate}<br/>
			<c:if test="${lotsShortExit != null}">
			可能量(Lot): ${lotsShortExit}
			</c:if>
			<c:if test="${lotsShortExit == null}">-</c:if>
		</c:if>
	</td>
	<td>
		<c:if test="${lotsShortVOpenPrice != null}">
		仮想建値:${virtualOpenPrice}<br/>
		可能量(Lot): ${lotsShortVOpenPrice}
		</c:if>
		<c:if test="${lotsShortVOpenPrice == null}">-</c:if>
	</td>
</tr>
</table>

<hr/>
<ul>
<li><a href="../chart?ak=${accessKey}">チャート</a></li>
<li><a href="../?ak=${accessKey}">トラップ</a></li>
<li><a href="../config?ak=${accessKey}">設定</a></li>
</ul>

</body>
</html>