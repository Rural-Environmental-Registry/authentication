import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { flushPromises } from '@vue/test-utils'
import UserLogin from '@/views/UserLogin.vue'
import { login, getCredential } from '@/services/AuthService'

vi.mock('yup', async () => {
  const actual = await vi.importActual('yup')
  return {
    ...actual,
    object: () => ({
      shape: () => ({
        validate: () => Promise.resolve(),
        validateSync: () => {},
        __isYupSchema__: true
      })
    })
  }
})

vi.mock('@/components/common/PageHeader.vue', () => ({
  default: { template: '<div />' }
}))

vi.mock('@/helpers/redirect', () => ({
  redirectToPortal: vi.fn()
}))

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
  useRoute: vi.fn()
}))

const mockTrigger = vi.fn()
vi.mock('@/hooks/useToast', () => ({
  useToast: () => ({ trigger: mockTrigger })
}))

describe('UserLogin.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('monta corretamente', () => {
    const wrapper = mount(UserLogin)
    expect(wrapper.exists()).toBe(true)
  })

  it('realiza login com sucesso e redireciona para portal', async () => {
    vi.mocked(login).mockResolvedValueOnce({ token: '1234' } as any)
    vi.mocked(getCredential).mockResolvedValueOnce(true)

    const wrapper = mount(UserLogin)

    await wrapper.get('[data-testid="email"] input').setValue('user@example.com')
    await wrapper.get('[data-testid="password"] input').setValue('Test@123')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    await vi.waitFor(() => {
      expect(login).toHaveBeenCalledWith('user@example.com', 'Test@123')
      expect(localStorage.getItem('keycloak-session')).toContain('1234')
      expect(localStorage.getItem('user-email')).toBe('user@example.com')
    })
  })

  it('exibe toast de erro se status for 401', async () => {
    const error = { response: { status: 401 } }
    vi.mocked(login).mockRejectedValueOnce(error)
    vi.mocked(getCredential).mockResolvedValueOnce(true)


    const wrapper = mount(UserLogin)

    await wrapper.get('[data-testid="email"] input').setValue('user@example.com')
    await wrapper.get('[data-testid="password"] input').setValue('Test@123')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    await vi.waitFor(() => {
      expect(mockTrigger).toHaveBeenCalledWith('messages.invalidLogin', 'error')
    })
  })

  it('exibe toast de erro genérico se status for diferente de 401', async () => {
    const error = { response: { status: 500 } }
    vi.mocked(login).mockRejectedValueOnce(error)
    vi.mocked(getCredential).mockResolvedValueOnce(true)


    const wrapper = mount(UserLogin)

    await wrapper.get('[data-testid="email"] input').setValue('user@example.com')
    await wrapper.get('[data-testid="password"] input').setValue('Test@123')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    await vi.waitFor(() => {
      expect(mockTrigger).toHaveBeenCalledWith("messages.loginError", 'error')
    })
  })

  it('chama redirecionamento para cadastro ao clicar em link', async () => {
    const wrapper = mount(UserLogin)
    await wrapper.find('span.cursor-pointer').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'register' })
  })
})
