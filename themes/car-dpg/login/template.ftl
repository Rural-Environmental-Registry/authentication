<#-- template.ftl: layout base para o tema -->
<#macro layout>
<!DOCTYPE html>
<html lang="${locale.currentLanguageTag}">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${msg("loginTitle")}</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/style.css">
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" type="image/x-icon" />
</head>
<body>
    <div class="page-container">
        <header>
            <img src="${url.resourcesPath}/img/logo.png" alt="Logo" class="logo">
        </header>

        <main>
            <#nested>
        </main>

        <footer>
            <p>&copy; ${.now?string("yyyy")} Sua Empresa. Todos os direitos reservados.</p>
        </footer>
    </div>
</body>
</html>
</#macro>

<#-- Adicione isto no final -->
<#macro registrationLayout displayInfo=true displayMessage=true>
    <@layout>
        <#nested>
    </@layout>
</#macro>
