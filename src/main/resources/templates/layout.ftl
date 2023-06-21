<#import "navbar.ftl" as navbar>
<#import "sidenav.ftl" as sidenav>
<#macro layout>

    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-9ndCyUaIbzAi2FUVXJi0CjmCapSmO7SnpJef0486qhLnuZ2cdeRhO02iuK6FUUVM" crossorigin="anonymous">
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js" integrity="sha384-geWF76RCwLtnZ8qwWowPQNguL3RmwHVBC9FhGdlKrxdiJJigb/j/68SIy3Te4Bkz" crossorigin="anonymous"></script>

        <link href="../public/fonts/icons/css/fontello.css" type="text/css" rel="stylesheet"/>

        <link href="../public/css/style.css" type="text/css" rel="stylesheet"/>
    </head>

    <body>

    <@navbar.navbar></@navbar.navbar>

    <div class="app">
        <@sidenav.sidenav></@sidenav.sidenav>

        <div id="app_container">
            <#nested>
        </div>
    </div>

    <script src="../public/js/script.js"></script>
    </body>
    </html>

</#macro>

