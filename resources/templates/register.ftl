<#-- @ftlvariable name="data" type="com.example.IndexData" -->
<html>
<head>
    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.bundle.min.js" integrity="sha384-xrRywqdh3PHs8keKZN+8zzc5TX0GRTLCcmivcbNJWm2rs5C8PRhcEn3czEjhAO9o" crossorigin="anonymous"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js" integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM" crossorigin="anonymous"></script>
</head>
<body>
<body id="page-top" style="background: linear-gradient(black, white);">

<!-- Navigation -->
<nav class="navbar navbar-expand-lg navbar-light fixed-top py-3" id="mainNav">
    <div class="container">
        <a class="navbar-brand js-scroll-trigger" style="color:white;" href="/">Powrót</a>
        <button class="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse" data-target="#navbarResponsive" aria-controls="navbarResponsive" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
    </div>
</nav>
<header class="masthead">
    <div class="container h-100">
        <div class="row h-100 align-items-center justify-content-center text-center">
            <div class="col-lg-10 align-self-end">
                <form action="/register" method="post" >
                    <p style="color:0000FF;">Nazwa użytkownika:</p>
                    <input name="name" class="form-control" type="text">
                    <p style="color:0000FF;">Adres e-mail:</p>
                    <input name="email" class="form-control" type="text">
                    <p style="color:0000FF;">Hasło:</p>
                    <input name="password" class="form-control" type="password">
                    <input type="submit" class="btn btn-primary" value="Zarejetruj">
                </form>
            </div>
            <div class="col-lg-8 align-self-baseline">
                <#if error?has_content>
                    <div class="alert alert-danger" role="alert">
                        ${error}
                    </div>
                </#if>
            </div>
        </div>
    </div>
</header>


</body>
</html>
