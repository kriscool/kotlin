<#-- @ftlvariable name="data" type="com.example.IndexData" -->
<html>
<body>
${error}
<p><a href="/protected/route/basic">Back to main page</a></p>
<p><label>User login:</label>${login}</p>
<p><label>User address:</label>${address}</p>
<h3><p>Change details</p></h3>
<form action="/protected/route/details" method="post">
    <p>Address:</p>
    <input name="address" type="text">
    <p>New password:</p>
    <input name="pass" type="password">
    <p>Retype new password:</p>
    <input name="pass2" type="password">
    <p>Password:</p>
    <input name="oldPass" type="password">
    <br><br>
    <input type="submit" value="Submit">
</form>

</body>
</html>
