// vitest.setup.ts
import { vi } from 'vitest'

import { config } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { aliases, mdi } from 'vuetify/iconsets/mdi'
import { createUser } from '@/services/AuthService'

const i18n = createI18n({
  legacy: false, // ✅ importante
  locale: 'en',
  fallbackLocale: 'en'
})

const vuetify = createVuetify({
  components,
  directives
})

global.ResizeObserver = require('resize-observer-polyfill')

config.global.plugins = [i18n, vuetify]

// Toast
vi.mock('@/hooks/useToast', () => ({
  useToast: () => ({
    trigger: vi.fn()
  })
}))

// AuthService
vi.mock('@/services/AuthService', () => ({
  login: vi.fn(),
  createUser: vi.fn(),
  getUserInfo: vi.fn(),
  getTokenFromCode: vi.fn(),
  getCredential: vi.fn(),
  isIdp: vi.fn()
}))

vi.mock('@/services/ProfileService', () => ({
  updateUserInfo: vi.fn(),
  updatePassword: vi.fn()
}))

// Mock global do vue-router
const mockPush = vi.fn()
vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => ({ push: mockPush })
  }
})
