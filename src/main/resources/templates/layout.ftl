<#import "navbar.ftl" as navbar>
<#import "sidenav.ftl" as sidenav>
<#macro layout>

    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        <!-- Base font definitions -->
        <link rel="stylesheet" href="/public/css/fonts.css" type="text/css">

        <link href="../public/bootstrap/dist/css/bootstrap.css" rel="stylesheet" type="text/css">

        <link href="../public/fonts/icons/css/fontello.css" type="text/css" rel="stylesheet"/>

        <link href="../public/css/style.css" type="text/css" rel="stylesheet"/>
    </head>

    <body>

    <div id="app">
        <div id="app_navbar">
            <button id="app_navbar__menu_button">
                <span class="icon icon-menu"></span>
            </button>
        </div>
    </div>

    <script src="../public/js/script.js"></script>
    </body>
    </html>

</#macro>

