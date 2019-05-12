<#-- @ftlvariable name="data" type="com.example.IndexData" -->
<html>
<body>
<p>Hello ${user.name}</p>

<#if user.event??>
<p>Already you are signed in on event: <a href="/protected/route/events/${user.event.id}">${user.event.name}</a>&nbsp;<a href="/protected/route/events/${user.event.id}/unsubscribe">Unsubscribe</a> </p>
</#if>

<a href="/protected/route/events">Sign in on event</a>
<a href="/protected/route/details">Change details</a>

</body>
</html>
