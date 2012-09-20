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
<t:input type="text" name="virtualPriceReservation"/> <input type="submit" value="変更" />
</t:form>
</td>
</tr>
</table>

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
<p>
ショートヘッジ可能額: ${shAmount}<br/>
ヘッジ可能量(Lot): ${hedgeLots}
</p>

<p>
<a href="../?ak=${accessKey}">戻る</a>
</p>
</body>
</html>