<#--@ftlvariable name="data" type="com.example.IndexData" -->
<html>
<body>
<p><a href="/protected/route/basic">Back to main page</a></p>


<table>
    <tr>
        <td>Nazwa:</td>
        <td>${item.name}<br></td>
    </tr>
    <tr>
        <td>Opis: <br></td>
        <td>${item.desc}</td>
    </tr>
    <tr>
        <td>Data:</td>
        <td>${item.date}</td>
    </tr>
</table>
<p><a href="/protected/route/events/${item.id}/register">Register on event</a></p>

</body>
</html>
