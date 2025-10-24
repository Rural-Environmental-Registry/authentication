<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
  <#if section = "header">
    <h1>Bem-vindo ao Portal do Usuário</h1>
  <#elseif section = "form">
    <div class="custom-box">
        <p>Você está conectado como <strong>${account.username}</strong>.</p>
        <ul>
            <li><a href="${url.passwordUrl}">Alterar Senha</a></li>
            <li><a href="${url.accountUrl}">Dados da Conta</a></li>
            <li><a href="${url.sessionsUrl}">Sessões</a></li>
            <li><a href="${url.logoutUrl}">Sair</a></li>
        </ul>
    </div>
  <#elseif section = "info">
    <p class="info">Este é um painel de conta personalizado.</p>
  </#if>
</@layout.registrationLayout>
