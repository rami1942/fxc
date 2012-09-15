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

前回: ${f:dateFormat(fromDt, "yyyy-MM-dd HH:mm:ss")}<br/>
口座残高: ${my:commaSep(balance)}(${balanceDiff})<br/>
含み損益: ${my:commaSep(profit)}(${profitDiff})<br/>

</body>
</html>