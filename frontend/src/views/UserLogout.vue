<script lang="ts" setup>
import Loader from '@/components/common/Loader.vue'
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter()

const kcLoaded = ref(false);
const govLoaded = ref(false);
const FALLBACK_TIMEOUT = 5000 // Tempo máximo de espera para logout 5 segundos

const idToken = JSON.parse(localStorage.getItem('keycloak-session') || '{}')['id_token'] as string
sessionStorage.removeItem('token');
sessionStorage.removeItem('gov');
localStorage.setItem('keycloak-session', '')

const govUrl = import.meta.env.VITE_GOV_URL
const cardpgUrl = import.meta.env.VITE_FRONTEND_USR_URL
const govClientId = import.meta.env.VITE_GOV_CLIENT_ID

const KC_LOGOUT_URL = `${import.meta.env.VITE_KEYCLOAK_API_URL}/realms/car-dpg/protocol/openid-connect/logout?id_token_hint=${idToken}&post_logout_redirect_uri=${cardpgUrl}&client_id=api-service`
const GOV_LOGOUT_URL = `${govUrl}/logout?post_logout_redirect_uri=${cardpgUrl}&client_id=${govClientId}`

onMounted(() => {
  kcLoaded.value = false
  govLoaded.value = false

  // Timeout de fallback (caso algum iframe nunca carregue)
  setTimeout(() => {
    router.push({ name: 'user-login' })
  }, FALLBACK_TIMEOUT);
});

const checkRedirect = () => {
  if (kcLoaded.value || govLoaded.value)
    setTimeout(() => router.push({ name: 'user-login' }), 500);
};

const onLoadGov = () => {
  govLoaded.value = true;
  checkRedirect();
}

const onLoadKc = () => {
  kcLoaded.value = true;
  checkRedirect();
}
</script>

<template>
  <div>
    <Loader />
  </div>
  <iframe @load="onLoadKc()" id="kc-logout" :src="KC_LOGOUT_URL" class="hidden"></iframe>
  <iframe @load="onLoadGov()" id="gov-logout" :src="GOV_LOGOUT_URL" class="hidden"></iframe>
</template>
