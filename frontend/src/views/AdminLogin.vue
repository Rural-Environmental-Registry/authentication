<script setup lang="ts">
import { login } from '@/services/AuthService';
import { ref } from 'vue'
import { useI18n } from 'vue-i18n';
import * as yup from 'yup'
import { useField, useForm } from 'vee-validate'
import { useRouter } from 'vue-router';
import type { AxiosError } from 'axios';
import { useToast } from '@/hooks/useToast';
import PageHeader from '@/components/common/PageHeader.vue';

const { t } = useI18n()
const { trigger } = useToast()
const showPassword = ref(false)

const loading = ref(false)

const schema = yup.object({
  email: yup.string().required('login.validation.required').email('login.validation.email'),
  password: yup.string().required('login.validation.required')
})

// useForm + fields
const router = useRouter()
const { handleSubmit } = useForm({ validationSchema: schema })
const { value: email, errorMessage: emailError } = useField('email')
const { value: password, errorMessage: passwordError } = useField('password')

const onSubmit = handleSubmit(async (values) => {
  try {
    const user = await login(values.email, values.password)
    localStorage.setItem('keycloak-session', JSON.stringify(user))
    router.push({ name: 'home' })
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
</script>

<template>
  <div class="min-h-screen flex flex-col">

    <PageHeader class="z-10" />
    <div class="flex flex-1">
      <!-- Lado esquerdo com imagem -->
      <div class="w-1/2 flex items-center justify-center bg-[#f8faf9]">
  <img src="@/assets/Logo-RER.png" alt="Login Illustration" class="w-full max-w-sm md:max-w-lg"
          draggable="false" />
      </div>

      <!-- Lado direito com formulário -->
      <div class="w-1/2 bg-[#edf4ed] flex items-center justify-center">
        <form @submit.prevent="onSubmit" class="w-full max-w-md px-4">
          <h2 class="text-3xl font-medium mb-4">{{ $t('login.title') }}</h2>

          <v-text-field v-model="email" :label="$t('login.email')" variant="outlined" density="comfortable" class="mb-4"
            :error="!!emailError" :error-messages="$t(emailError || '')" data-testid="email" />

          <v-text-field v-model="password" :type="showPassword ? 'text' : 'password'" :label="$t('login.password')"
            variant="outlined" density="comfortable" :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="showPassword = !showPassword" class="mb-6" :error="!!passwordError"
            :error-messages="$t(passwordError || '')" data-testid="password" />

          <v-btn rounded :loading="loading" type="submit" color="blue-darken-3" data-testid="submit" class="text-none w-full mb-4" size="large">
            {{ $t('login.loginButton') }}
          </v-btn>

          <!-- <v-btn variant="outlined" color="primary" class="w-full" size="large">
          {{ $t('login.govLoginButton') }}
        </v-btn> -->
        </form>
      </div>
    </div>
  </div>
</template>
