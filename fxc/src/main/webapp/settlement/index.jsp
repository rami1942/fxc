<!DOCTYPE html>
<html lang="ja">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection"/>
  <title>決算</title>
</head>
<body>
<p>
前回: ${f:dateFormat(fromDt, "yyyy-MM-dd HH:mm:ss")}<br/>
<br/>
口座残高: ¥${my:commaSep(balance)}(${balanceDiff})<br/>
含み損益: ¥${my:commaSep(profit)}(${profitDiff})<br/>
スワップ: ${my:commaSep(swapPoint)}<br/>
リピート: ${numRepeat}回
</p>

<t:form actionClass="org.dyndns.bluefield.fxc.action.SettlementAction" actionMethod="update" value="${action}">
<input type="submit" value="更新" />
</t:form>

<p>
<a href="../?ak=${accessKey}">戻る</a>
</p>
</body>
</html>