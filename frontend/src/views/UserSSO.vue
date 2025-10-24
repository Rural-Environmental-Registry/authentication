<script setup lang="ts">
import { onMounted } from 'vue'

import { useToast } from '@/hooks/useToast'
import { getTokenFromCode } from '@/services/AuthService'
import { redirectToPortal } from '@/helpers/redirect'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { t } = useI18n()

onMounted(async () => {
  const code = route.query.code as string
  const iss = decodeURIComponent(route.query.iss as string)

  if (!code || !iss) {
    toast.trigger(t('messages.sso.invalidParams'), 'error')
    router.push({ name: 'login' })
    return
  }

  try {
    const user = await getTokenFromCode(code)
    localStorage.setItem('keycloak-session', JSON.stringify(user))


    // window.location.href = import.meta.env.VITE_FRONTEND_USR_URL
    sessionStorage.setItem('gov', 'true')
    redirectToPortal()
  } catch (error) {
    toast.trigger(t('messages.sso.errorLogin'), 'error')

    router.push({ name: 'login' })
  }
})
</script>

<template>
  <div class="flex items-center justify-center h-screen">
    <v-progress-circular indeterminate color="primary" size="48" />
  </div>
</template>
