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
            <button class="btn" id="app_navbar__menu_button">
                <span class="icon icon-menu"></span>
            </button>
        </div>

        <div id="app_sidenav">
            <div id="app_sidenav__current_user">
                <!-- TODO: Add something here -->
            </div>

            <div id="app_sidenav__links">
                <a href="/ui/home" data-href="/ui/home" class="sidenav-link">
                    <span class="icon icon-home"></span>
                    <span>HOME</span>
                </a>
            </div>

            <div id="app_sidenav__blur"></div>
        </div>

        <div id="app_content">
            <#nested>
        </div>
    </div>

    <script type="module" src="/public/js/script.js"></script>
    </body>
    </html>

</#macro>

