<#macro mainLayout bodyClass="" active="">
    <!DOCTYPE html>
    <html lang="pt-br">
    <head>
        <meta charset="UTF-8">
        <title><#nested "title">Keycloak</#nested></title>
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link rel="stylesheet" href="${url.resourcesPath}/css/login.css">
        <link rel="icon" href="${url.resourcesPath}/img/favicon.ico">
    </head>
    <body class="${bodyClass}">
        <div id="kc-header">
            <div id="kc-header-wrapper">
                <h1><a href="${url.loginAction}">Minha Aplicação</a></h1>
            </div>
        </div>

        <div id="kc-content">
            <div id="kc-content-wrapper">
                <#nested>
            </div>
        </div>

        <div id="kc-footer">
            <div id="kc-footer-wrapper">
                <ul>
                    <li><a href="#">Suporte</a></li>
                    <li><a href="#">Privacidade</a></li>
                </ul>
            </div>
        </div>
    </body>
    </html>
</#macro>
