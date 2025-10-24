<script setup lang="ts">
import Loader from '@/components/common/Loader.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { isTokenExpired } from '@/helpers/token'
import { useToast } from '@/hooks/useToast'
import { getCredential, getUserInfo, isIdp } from '@/services/AuthService'
import { updatePassword, updateUserInfo } from '@/services/ProfileService'
import type { AxiosError } from 'axios'
import { jwtDecode } from 'jwt-decode'
import { useField, useForm } from 'vee-validate'
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import * as yup from 'yup'

const { t } = useI18n()
const router = useRouter()
const { trigger } = useToast()
const loading = ref(true)
const showPassword = ref(false)

const passwordRules = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).*$/

const personal = ref({
  firstName: '',
  lastName: '',
  email: '',
  id: '',
  idNational: ''
})

const userSchema = yup.object({
  name: yup.string().required('login.validation.required'),
  lastName: yup.string().required('login.validation.required'),
  email: yup.string().required('login.validation.required').email('login.validation.email'),
  idNational: yup.string().test(
    'hasDigit',
    'login.validation.hasDigit',
    function () {
      const orig = (this.originalValue ?? '').toString().trim();
      if (orig.length === 0) return true;
      return /\d/.test(orig);
    })
    .required('login.validation.required'),
})
const passwordSchema = yup.object({
  newPassword: yup.string()
    .required('login.validation.required')
    .matches(
      passwordRules,
      'login.validation.passwordError'
    )
    .min(8, 'login.validation.passwordMin'),
  confirmNewPassword: yup.string()
    .required('login.validation.required')
    .oneOf([yup.ref('newPassword')], 'login.validation.passwordDiff'),
})

const { handleSubmit, values, resetForm, errors } = useForm({ validationSchema: userSchema })

const { value: name, errorMessage: nameError } = useField('name')
const { value: lastName, errorMessage: lastNameError } = useField('lastName')
const { value: email, errorMessage: emailError } = useField('email')
const { value: idNational, errorMessage: idNationalError } = useField('idNational')

const { handleSubmit: submitPassword } = useForm({ validationSchema: passwordSchema })

const { value: password, errorMessage: passwordError } = useField('password')
const { value: newPassword, errorMessage: newPasswordError } = useField('newPassword')
const { value: confirmNewPassword, errorMessage: confirmNewPasswordError } = useField('confirmNewPassword')

const editMode = ref(false)
const accessExpanded = ref(false)

const hasIdp = ref(true)
const changePasswordEnabled = ref(false)
const isLoginGov = ref(sessionStorage.getItem('gov') === 'true')



const enableEdit = () => {

  resetForm({
    values: {
      name: personal.value.firstName,
      lastName: personal.value.lastName,
      email: personal.value.email,
      idNational: personal.value.idNational
    },
    errors: {} // limpa erros
  })

  editMode.value = true
}

const cancelEdit = () => {
  editMode.value = false
}

const savePersonalData = handleSubmit(async (values) => {
  loading.value = true
  try {
    const email = values.email.trim();
    const idNational = values.idNational.trim().replace(/\D/g, '');

    await updateUserInfo(values.name, values.lastName, values.email, email,idNational,personal.value.id)

    personal.value.firstName = values.name
    personal.value.lastName = values.lastName
    personal.value.email = email
    personal.value.idNational = idNational

    trigger(t('messages.profileUpdated'))
  } catch (error) {
    const status = (error as AxiosError).response?.status
    const msg = (error as AxiosError).response?.data as string

    if (status === 403) {
      trigger(t('messages.operationNotAllowed'), 'error')
    } else {
      trigger(t('messages.profileUpdateError'), 'error')
    }

    if (status === 400 && msg && msg.includes('existe')) {
      if(msg.includes('email')){
        trigger(t('messages.invalidEmail'), 'error')
      }else{
        trigger(t('messages.invalidIdNational'), 'error')
      }
    } else {
      trigger(t('messages.registerError'), 'error')
    }
  } finally {
    loading.value = false
    editMode.value = false
  }
})

const saveAccessData = submitPassword(async (values) => {
  loading.value = true
  try {
    await updatePassword(values.newPassword, personal.value.id)
    trigger(t('messages.accessDataUpdated'))
  } catch (error) {
    const status = (error as AxiosError).response?.status

    if (status === 403) {
      trigger(t('messages.operationNotAllowed'), 'error')
    } else {
      trigger(t('messages.accessDataUpdateError'), 'error')
    }
  } finally {
    loading.value = false
    accessExpanded.value = false
  }
})


onMounted(async () => {
  if (isTokenExpired()) {
    localStorage.setItem('keycloak-session', '')
    router.push({ name: 'user-login' })
  }

  const tokenInfo = localStorage.getItem('keycloak-session')
  if (tokenInfo) {
    const token = JSON.parse(tokenInfo)['access_token']
    const decoded = jwtDecode(token)
    if (decoded && decoded.sub) {
      const userData = await getUserInfo(decoded.sub)
      loading.value = false

      personal.value.firstName = userData.firstName
      personal.value.lastName = userData.lastName
      personal.value.email = userData.email

      if(!isLoginGov.value){
        hasIdp.value = await isIdp(userData.email)
        changePasswordEnabled.value = true
      }

      personal.value.id = userData.id
      personal.value.idNational = userData.idNational
    }

  }
})

const goBack = () => {
  window.location.href = `${import.meta.env.VITE_FRONTEND_USR_URL}`
}

</script>

<template>
  <PageHeader class="z-10" />

  <div class="max-w-5xl mx-auto mt-2 px-4">
    <div class="text-xl font-medium mb-2">
      <div @click="goBack" class="flex items-center mt-8 cursor-pointer">
        <v-icon icon="mdi-arrow-left-thick" class="text-teal-darken-2 mr-2 !text-2xl" />
        <p class="mb-1">{{ t('profile.title') }}</p>
      </div>

      <hr />
    </div>

    <div v-if="isLoginGov" class="bg-yellow-100/50 rounded border !p-6 my-4">
      {{ t('profile.ssoLoginMessage')  }}
    </div>

    <!-- Personal Data -->
    <form @submit.prevent="savePersonalData" class="bg-gray-100/50 rounded border !p-6 my-4">
      <div class="text-green-700 font-semibold text-sm mt-0 mb-4 uppercase">
        {{ t('profile.personalData') }}
      </div>

      <div>
        <label class="block text-sm text-gray-500">{{ t('profile.id') }}</label>
        <p>{{ personal.id }}</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <v-text-field v-if="editMode" v-model="name" :error="!!nameError" :error-messages="$t(nameError || '')"
          :label="$t('register.name')" variant="outlined" density="compact" data-testid="first-name" />
        <div v-else>
          <label class="block text-sm text-gray-500">{{ $t('register.name') }}</label>
          <p>{{ personal.firstName }}</p>
        </div>

        <v-text-field v-if="editMode" v-model="lastName" :error="!!lastNameError"
          :error-messages="$t(lastNameError || '')" :label="$t('register.lastName')" variant="outlined"
          density="compact" data-testid="last-name" />
        <div v-else>
          <label class="block text-sm text-gray-500">{{ $t('register.lastName') }}</label>
          <p>{{ personal.lastName }}</p>
        </div>

        <v-text-field v-if="editMode" v-model="idNational" :error="!!idNationalError" :error-messages="$t(idNationalError || '')"
          :label="$t('register.idNational')" variant="outlined" density="compact" data-testid="idNational" />
        <div v-else>
          <label class="block text-sm text-gray-500">{{ $t('register.idNational') }}</label>
          <p>{{ personal.idNational }}</p>
        </div>

        <v-text-field v-if="editMode" v-model="email" :error="!!emailError" :error-messages="$t(emailError || '')"
          :label="$t('register.email')" variant="outlined" density="compact" data-testid="email" />
        <div v-else>
          <label class="block text-sm text-gray-500">{{ $t('register.email') }}</label>
          <p>{{ personal.email }}</p>
        </div>

      </div>


      <div class="mt-6 flex gap-4">
        <v-btn v-show="editMode" rounded class="text-none" color="teal-darken-3" variant="flat" type="submit"
          @click="savePersonalData">
          {{ t('profile.save') }}
        </v-btn>

        <v-btn v-if="editMode" rounded class="text-none" color="teal-darken-3" variant="outlined" @click="cancelEdit">
          {{ t('profile.cancel') }}
        </v-btn>

        <v-btn v-else-if="!hasIdp" variant="flat" @click="enableEdit" rounded class="text-none" color="teal-darken-3"
          data-testid="edit-btn">
          {{ t('profile.edit') }}
        </v-btn>
      </div>
    </form>

    <!-- Access Data -->
    <div class="border rounded" v-if="changePasswordEnabled">
      <div class="flex justify-between items-center px-6 !py-4 cursor-pointer" data-testid="collapse-card"
        @click="accessExpanded = !accessExpanded">
        <div class="text-green-700 font-semibold text-sm m-0 p-0 uppercase">
          {{ t('profile.changePassword') }}
        </div>
        <v-icon :icon="accessExpanded ? 'mdi-chevron-up' : 'mdi-chevron-down'" />
      </div>

      <form @submit.prevent="saveAccessData" v-if="accessExpanded" class="px-6 pt-2 pb-6 space-y-4">
        <div class="w-full grid grid-cols-3 gap-4 mb-4">

          <v-text-field v-model="newPassword" :type="showPassword ? 'text' : 'password'"
            :label="$t('register.newPassword')" variant="outlined" density="compact"
            :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="showPassword = !showPassword" :error="!!newPasswordError"
            :error-messages="$t(newPasswordError || '')" class="h-fit" data-testid="new-password" />

          <v-text-field v-model="confirmNewPassword" type="password" :label="$t('register.confirmPassword')"
            variant="outlined" density="compact" :error="!!confirmNewPasswordError"
            :error-messages="$t(confirmNewPasswordError || '')" class="h-fit" data-testid="confirm-password" />
        </div>

        <v-btn rounded class="text-none" color="teal-darken-3" type="submit" variant="flat">
          {{ t('profile.save') }}
        </v-btn>
      </form>
    </div>
  </div>

  <!-- <Loader v-if="loading" /> -->
</template>
