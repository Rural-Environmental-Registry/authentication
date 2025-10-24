<#-- login.ftl -->
<html>
<head>
<link rel="stylesheet" href="${url.resourcesPath}/css/style.css" />
</head>
<body>
  <div class="login-container">
    <h1>Bem-vindo ao Keycloak personalizado 🚀</h1>
    <form id="kc-form-login" action="${url.loginAction}" method="post">
      <input type="text" name="username" placeholder="Usuário"/>
      <input type="password" name="password" placeholder="Senha"/>
      <button type="submit">Entrar</button>
    </form>
  </div>
</body>
</html>
