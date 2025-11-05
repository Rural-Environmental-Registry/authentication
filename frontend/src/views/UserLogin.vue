<script setup lang="ts">
import { getCredential, login } from '@/services/AuthService';
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n';
import * as yup from 'yup'
import { useField, useForm } from 'vee-validate'
import { useRouter } from 'vue-router';
import { useToast } from '@/hooks/useToast';
import type { AxiosError } from 'axios';
import PageHeader from '@/components/common/PageHeader.vue';
import { redirectToPortal } from '@/helpers/redirect';

const { t } = useI18n()
const router = useRouter()
const { trigger } = useToast()

const showPassword = ref(false)

const loading = ref(false)

const redirectUri = import.meta.env.VITE_FRONTEND_USR_URL as string

const schema = yup.object({
  email: yup.string().required('login.validation.required').email('login.validation.email'),
  password: yup.string().required('login.validation.required')
})

// useForm + fields
const { handleSubmit } = useForm({ validationSchema: schema })
const { value: email, errorMessage: emailError } = useField('email')
const { value: password, errorMessage: passwordError } = useField('password')

const onSubmit = handleSubmit(async (values) => {
  loading.value = true
  try {

    const havePassword = await getCredential(values.email)
    if(havePassword){
      const user = await login(values.email, values.password)
      console.log('Usuário autenticado:', user)
      localStorage.setItem('keycloak-session', JSON.stringify(user))
      localStorage.setItem('user-email', values.email)

      // window.location.href = import.meta.env.VITE_FRONTEND_USR_URL

      redirectToPortal()

    }else{
        router.push({ name: 'register', query: { email: values.email} },
      )
    }

  } catch (error) {
    const status = (error as AxiosError).response?.status

    if (status === 401) {
      trigger(t('messages.invalidLogin'), 'error')
    } else {
      trigger(t('messages.loginError'), 'error')
    }

  } finally {
    loading.value = false
  }
})

const redirect = () => {
  router.push({ name: 'register' })
}

const govLogin = () => {
  const url = new URL( `${import.meta.env.VITE_KEYCLOAK_API_URL}/realms/car-dpg/protocol/openid-connect/auth`);
  url.searchParams.set("client_id", "api-service");
  url.searchParams.set("response_type", "code");
  url.searchParams.set("scope", "openid");
  url.searchParams.set("redirect_uri", redirectUri);
  url.searchParams.set("kc_idp_hint", "gov-br");
  window.location.href = url.toString()
}
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <PageHeader class="z-10" />
    <div class="flex w-full h-full flex-1">
      <!-- Lado esquerdo com imagem -->
      <div class="w-1/2 flex items-center justify-center bg-[#f8faf9]">
  <img src="@/assets/Logo-RER.png" alt="Login Illustration" class="w-full max-w-sm md:max-w-lg"
          draggable="false" />
      </div>

      <!-- Lado direito com formulário -->
      <div class="w-1/2 bg-[#edf4ed] flex items-center justify-center">
        <form @submit.prevent="onSubmit" class="w-full max-w-md px-4">
          <h2 class="text-3xl font-medium mb-4">{{ $t('login.userTitle') }}</h2>

          <v-text-field v-model="email" :label="$t('login.email')" variant="outlined" density="comfortable" class="mb-4"
            :error="!!emailError" :error-messages="$t(emailError || '')" data-testid="email" />

          <v-text-field v-model="password" :type="showPassword ? 'text' : 'password'" :label="$t('login.password')"
            variant="outlined" density="comfortable" :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="showPassword = !showPassword" class="mb-4" :error="!!passwordError"
            :error-messages="$t(passwordError || '')" data-testid="password" />

          <v-btn rounded :loading="loading" type="submit" color="blue-darken-3" class="text-none w-full mb-4" size="large">
            {{ $t('login.loginButton') }}
          </v-btn>

          <v-btn rounded prepend-icon="mdi-account" @click="govLogin" variant="outlined" color="primary" class="text-none w-full bg-white" size="large">
            {{ $t('login.govLoginButton') }}
          </v-btn>

          <p class="text-sm text-center mt-4">
            {{ $t('login.noAccount') }}
            <span @click="redirect()" class="text-blue-800 font-medium hover:underline cursor-pointer">
              {{ $t('login.registerLink') }}
            </span>
          </p>
        </form>
      </div>
    </div>
  </div>
</template>
