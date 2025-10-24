<#import "template.ftl" as layout>
<@layout.mainLayout active="account" bodyClass="account">

<h1>${msg("accountTitle")}</h1>

<div class="content-area">
    <ul class="nav-tabs">
        <li class="${activeTab == "personal-info"?then("active", "")}">
            <a href="${url.accountUrl}">${msg("personalInfo")}</a>
        </li>
        <li class="${activeTab == "password"?then("active", "")}">
            <a href="${url.passwordUrl}">${msg("password")}</a>
        </li>
        <li class="${activeTab == "authenticator"?then("active", "")}">
            <a href="${url.totpUrl}">${msg("authenticator")}</a>
        </li>
        <li class="${activeTab == "sessions"?then("active", "")}">
            <a href="${url.sessionsUrl}">${msg("sessions")}</a>
        </li>
    </ul>

    <div class="tab-content">
        <#-- O conteúdo das abas virá de templates filhos -->
        <#nested "content">
    </div>
</div>

</@layout.mainLayout>
