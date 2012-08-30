<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="ja" xml:lang="ja">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="Content-Style-Type" content="text/css" />
  <meta http-equiv="Content-Script-Type" content="text/JavaScript" />
  <link href="${contextPath}/css/default.css" rel="stylesheet" type="text/css" media="screen,projection" charset="utf-8" />
  <title>設定</title>
</head>
<body>
<c:import url="/common/errors.jsp"/>
<c:import url="/common/notice.jsp"/>

<t:form action="update" value="${action}">
<table border="1">
<tr>
	<th>トラップ通貨数</th>
	<td><t:input type="text" name="lots" /></td>
</tr>
<tr>
	<th>トラップ幅(銭)</th>
	<td><t:input type="text" name="trapWidth" /></td>
</tr>
</table>
<input type="submit" value="設定" />
</t:form>

</body>
</html>
