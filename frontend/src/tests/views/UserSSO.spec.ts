// src/tests/views/UserSSO.spec.ts

import { mount } from '@vue/test-utils'
import UserSSO from '@/views/UserSSO.vue'
import { describe, it, vi, expect, beforeEach } from 'vitest'
import { flushPromises } from '@vue/test-utils'

// Mocks globais
const mockPush = vi.fn()
const mockTrigger = vi.fn()
const mockGetToken = vi.fn()

vi.mock('vue-router', () => {
  return {
    useRouter: () => ({ push: mockPush }),
    useRoute: vi.fn()
  }
})

vi.mock('@/hooks/useToast', () => ({
  useToast: () => ({ trigger: mockTrigger })
}))

vi.mock('@/services/AuthService', () => ({
  getTokenFromCode: vi.fn((code) => mockGetToken(code))
}))

vi.mock('@/helpers/redirect', () => ({
  redirectToPortal: vi.fn()
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

beforeEach(() => {
  vi.clearAllMocks()
  localStorage.clear()
})

describe('UserSSO.vue', () => {
  it('✅ redireciona ao portal com código válido', async () => {
    mockGetToken.mockResolvedValue({ token: 'ok' })

    const useRoute = (await import('vue-router')).useRoute as any
    useRoute.mockReturnValue({
      query: {
        code: 'mock-code',
        iss: encodeURIComponent('http://localhost')
      }
    })

    mount(UserSSO)
    await flushPromises()

    expect(localStorage.getItem('keycloak-session')).toContain('ok')
  })

  it('❌ mostra erro se código estiver ausente', async () => {
    const useRoute = (await import('vue-router')).useRoute as any
    useRoute.mockReturnValue({
      query: {
        iss: encodeURIComponent('http://localhost')
        // code ausente
      }
    })

    mount(UserSSO)
    await flushPromises()

    expect(mockTrigger).toHaveBeenCalledWith('messages.sso.invalidParams', 'error')
    expect(mockPush).toHaveBeenCalledWith({ name: 'login' })
  })
})
