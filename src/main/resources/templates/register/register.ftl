<!DOCTYPE html>
<html>
<head>

    <meta name="viewport" content="width=device-width, initial-scale=1" />

    <!-- Base font definitions -->
    <link rel="stylesheet" href="/public/css/fonts.css" type="text/css">

    <!-- Bootstrap -->
    <link rel="stylesheet" href="/public/bootstrap/dist/css/bootstrap.css">

    <!-- register/register.css -->
    <link href="/public/css/register/register.css" type="text/css" rel="stylesheet">

    <!-- some icons -->
    <link rel="stylesheet" href="/public/fonts/icons/css/fontello.css" type="text/css">

</head>
<body>
<div class="container">
    <div class="row">
        <div class="col-12">
            <div id="app_register__header">
                <h3>Welcome!</h3>
                <p>You can register using this form...</p>
            </div>
        </div>

        <div class="col-12">
            <div id="app_register__form">
                <form action="/ui/register/submit" method="POST" class="form">
                    <div class="form-group">
                        <label for="username">Enter your username:</label>
                        <input type="text" id="username" name="username" value="${username}">
                    </div>

                    <div class="form-group">
                        <label for="emailAddress">Enter your email address:</label>
                        <input type="email" id="emailAddress" name="emailAddress" value="${emailAddress}">
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
                        <input class="form-submit" type="submit" value="REGISTER IN">
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
</html>