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
                <h3>Welcome ${emailAddress}!</h3>
                <p>Check your email!</p>
            </div>
        </div>

        <div class="col-12">
            <div id="app_register__form">
                <form action="/ui/register/verify/submit" method="POST" class="form">
                    <div class="form-group">
                        <label for="emailAddress">Enter your email address:</label>
                        <input readonly type="text" id="emailAddress" name="emailAddress" value="${emailAddress}">
                    </div>

                    <div class="form-group">
                        <label for="code">Enter your code:</label>
                        <input type="text" id="code" name="code">
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