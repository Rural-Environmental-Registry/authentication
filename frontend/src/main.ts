import 'vuetify/styles'
import './assets/main.css'

import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { aliases, mdi } from 'vuetify/iconsets/mdi'
import '@mdi/font/css/materialdesignicons.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import enUS from './locales/en-us.json'
import ptBR from './locales/pt-br.json'
import esES from './locales/es-es.json'
import { createI18n } from 'vue-i18n'

const i18n = createI18n({
  locale: 'en-us',
  fallbackLocale: 'en-us',
  messages: {
    'en-us': enUS,
    'pt-br': ptBR,
    'es-es': esES
  }
})

const vuetify = createVuetify({
  components,
  directives,
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi,
    },
  },
})

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(i18n)
app.use(vuetify)

app.mount('#app')
