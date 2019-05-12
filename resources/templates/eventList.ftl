<#--@ftlvariable name="data" type="com.example.IndexData" -->
<html>
    <body>
    <p><a href="/protected/route/basic">Back to main page</a></p>
    <p>Register on event</p>
        <ul>
        <#list events as item>
            <li><a href="/protected/route/events/${item.id}">${item.name}</a></li>
        </#list>
        </ul>
    </body>
</html>
