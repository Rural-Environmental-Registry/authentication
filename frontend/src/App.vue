<script setup lang="ts">
import { RouterView } from 'vue-router'
import router from './router';
import { useI18n } from 'vue-i18n';
import { onMounted, ref, watch } from 'vue';
import { useToast } from './hooks/useToast';

const { t, locale } = useI18n()
const { show, message, color } = useToast()
const currentTitle = ref('')

onMounted(() => {
  locale.value = localStorage.getItem('language') || 'en-us'
})


router.afterEach((to) => {
  const defaultTitle = 'RER'
  currentTitle.value = to.meta.title as string
  document.title = `${defaultTitle} ${t(to.meta.title as string)}`
})

watch(locale, () => {
  const defaultTitle = 'RER'
  document.title = `${defaultTitle} ${t(currentTitle.value)}`
});

</script>

<template>
  <v-snackbar v-model="show" :color="color" timeout="4000" location="top right" class="z-[9999]">
    {{ message }}
  </v-snackbar>

  <RouterView />
</template>

<style lang="scss">
html {
  overflow: auto;
}
</style>
