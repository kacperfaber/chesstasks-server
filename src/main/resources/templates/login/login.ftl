<!DOCTYPE html>
<html>
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1" />

    <!-- Base font definitions -->
    <link rel="stylesheet" href="/public/css/fonts.css" type="text/css">

    <!-- Bootstrap -->
    <link rel="stylesheet" href="/public/bootstrap/dist/css/bootstrap.css">

    <!-- login/login.css -->
    <link href="/public/css/login/login.css" type="text/css" rel="stylesheet">

    <!-- some icons -->
    <link rel="stylesheet" href="/public/fonts/icons/css/fontello.css" type="text/css">

</head>
<body>
<div class="container">
    <div class="row">
        <div class="col-12">
            <div id="app_login__header">
                <h3>Welcome back!</h3>
                <p>Please login in...</p>
            </div>
        </div>

        <div class="col-12">
            <div id="app_login__form">
                <form action="/ui/login/submit" method="POST" class="form">
                    <div class="form-group">
                        <label for="login">Enter your login:</label>
                        <input type="text" id="login" name="login" value="${login}">
                    </div>

                    <div class="form-group">
                        <label for="password">Enter your password:</label>
                        <input type="password" id="password" name="password">
                    </div>

                    <#if error>
                        <div class="form-group error">
                            <span>Something went wrong...</span>
                        </div>
                    </#if>

                    <div class="form-group">
                        <input class="form-submit" type="submit" value="SIGN IN">
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
</html>