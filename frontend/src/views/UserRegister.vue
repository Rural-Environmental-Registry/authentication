<script setup lang="ts">
import { createUser, getUserInfoByUsername, login } from '@/services/AuthService';
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n';
import * as yup from 'yup'
import { useField, useForm } from 'vee-validate'
import { useRoute, useRouter } from 'vue-router';
import { useToast } from '@/hooks/useToast';
import type { AxiosError } from 'axios';
import PageHeader from '@/components/common/PageHeader.vue';
import { redirectToPortal } from '@/helpers/redirect';
import { updatePassword } from '@/services/ProfileService';

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const { trigger } = useToast()

const showBanner = ref(true)
const showPassword = ref(false)

const loading = ref(false)
const isSetPassword = ref(false)
const idKeycloak = ref('')

const passwordRules = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).*$/

const schema = yup.object({
  name: yup.string().required('login.validation.required'),
  lastName: yup.string().required('login.validation.required'),
  idNational: yup
  .string().test(
    'hasDigit',
    'login.validation.hasDigit',
    function () {
      const orig = (this.originalValue ?? '').toString().trim();
      if (orig.length === 0) return true;
      return /\d/.test(orig);
    }).required('login.validation.required'),
  email: yup.string().required('login.validation.required').email('login.validation.email'),
  password: yup.string()
    .required('login.validation.required')
    .matches(passwordRules, 'login.validation.passwordError')
    .min(8, 'login.validation.passwordMin'),
  confirmPassword: yup.string()
    .required('login.validation.required')
    .oneOf([yup.ref('password')], 'login.validation.passwordDiff'),
})

// useForm + fields
const { handleSubmit } = useForm({ validationSchema: schema })

const { value: name, errorMessage: nameError } = useField('name')
const { value: lastName, errorMessage: lastNameError } = useField('lastName')
const { value: idNational, errorMessage: idNationalError } = useField('idNational')
const { value: email, errorMessage: emailError } = useField('email')
const { value: password, errorMessage: passwordError } = useField('password')
const { value: confirmPassword, errorMessage: confirmPasswordError } = useField('confirmPassword')


onMounted(async () => {
  if(route.query.email) {
    const userData = await getUserInfoByUsername(route.query.email as string)
    if(userData){
      const user = userData[0];
      name.value = user.firstName
      lastName.value = user.lastName
      if(user.attributes && user.attributes.idNational){
        idNational.value = user.attributes.idNational[0];
      }
      email.value = user.username
      idKeycloak.value = user.id
      isSetPassword.value = true
    }
  }
})

const onSubmit = handleSubmit(async (values) => {
  loading.value = true
  try {
    if(!isSetPassword.value){

    const email = values.email.trim();
    const idNational = values.idNational.trim().replace(/\D/g, '');

    const userData = await createUser(email, values.name, values.lastName, values.password,idNational)
    }else{
      await updatePassword(values.password, idKeycloak.value)
    }
    const loginData = await login(values.email, values.password)
    localStorage.setItem('keycloak-session', JSON.stringify(loginData))
    // window.location.href = import.meta.env.VITE_FRONTEND_USR_URL

    redirectToPortal()
  } catch (error) {

    const status = (error as AxiosError).status
    const msg = (error as AxiosError).response?.data as string

    if (status === 400 && msg && msg.includes('existe')) {
      if(msg.includes('email')){
        trigger(t('messages.invalidEmail'), 'error')
      }else{
        trigger(t('messages.invalidIdNational'), 'error')
      }
    } else {
      trigger(t('messages.registerError'), 'error')
    }

    console.log(status, msg, error)
  } finally {
    loading.value = false
  }
})

const redirect = () => {
  router.push({ name: 'user-login' })
}
</script>

<template>
  <div class="min-h-screen flex flex-col">

    <PageHeader class="z-10" />
        <!-- Banner de warning -->
    <div
      v-if="showBanner"
      class="br-message warning mb-4"
      style="padding-top: 0.15rem; padding-bottom: 0.15rem"
    >
      <div class="icon">
  <i class="fas fa-exclamation-triangle fa-lg" aria-hidden="true" style="margin-bottom: 10px;"></i>
      </div>
      <div class="content" role="alert">
        <span class="message-title">{{ t('login.demoBannerTitle') }}</span>
        <span class="message-body">{{ t('login.demoBanner') }}</span>
      </div>
      <div class="close">
        <button class="br-button circle small" type="button" @click="showBanner = false">
          <i class="fas fa-times" aria-hidden="true"></i>
        </button>
      </div>
    </div>
    <div class="flex flex-1">
      <!-- Lado esquerdo com imagem -->
      <div class="w-1/2 flex items-center justify-center bg-[#f8faf9]">
  <img src="@/assets/Logo-RER.png" alt="Login Illustration" class="w-full max-w-sm md:max-w-lg"
          draggable="false" />
      </div>

      <!-- Lado direito com formulário -->
      <div class="w-1/2 bg-[#edf4ed] flex items-center justify-center">
        <form @submit.prevent="onSubmit" class="w-full max-w-md px-4">
          <h2 class="text-3xl font-medium mb-4">{{ $t('register.title') }}</h2>

          <v-text-field v-model="name" :label="$t('register.name')" variant="outlined" density="comfortable" :disabled="isSetPassword"
            class="mb-4" :error="!!nameError" :error-messages="$t(nameError || '')" data-testid="name" />

          <v-text-field v-model="lastName" :label="$t('register.lastName')" variant="outlined" density="comfortable" :disabled="isSetPassword"
            class="mb-4" :error="!!lastNameError" :error-messages="$t(lastNameError || '')" data-testid="lastName" />

          <v-text-field v-model="idNational" :label="$t('register.idNational')" variant="outlined" density="comfortable" :disabled="isSetPassword"
            class="mb-4" :error="!!idNationalError" :error-messages="$t(idNationalError || '')" data-testid="idNational" />

          <v-text-field v-model="email" :label="$t('register.email')" variant="outlined" density="comfortable" :disabled="isSetPassword"
            class="mb-4" :error="!!emailError" :error-messages="$t(emailError || '')" data-testid="email" />

          <v-text-field v-model="password" :type="showPassword ? 'text' : 'password'" :label="$t('register.password')"
            variant="outlined" density="comfortable" :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="showPassword = !showPassword" class="mb-4" :error="!!passwordError"
            :error-messages="$t(passwordError || '')" data-testid="password" />

          <v-text-field v-model="confirmPassword" :label="$t('register.confirmPassword')" type="password"
            variant="outlined" density="comfortable" class="mb-4" :error="!!confirmPasswordError"
            :error-messages="$t(confirmPasswordError || '')" data-testid="confirmPassword" />

          <v-btn rounded :loading="loading" type="submit" color="blue-darken-3" class="text-none w-full mb-4"
            size="large">
            {{ $t('register.submit') }}
          </v-btn>

          <p class="text-sm text-center mt-4">
            {{ $t('register.hasAccount') }}
            <span @click="redirect()" class="text-blue-800 font-medium hover:underline cursor-pointer">
              {{ $t('register.loginLink') }}
            </span>
          </p>
        </form>
      </div>
    </div>
  </div>
</template>
