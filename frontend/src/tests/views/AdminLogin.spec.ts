import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AdminLogin from '@/views/AdminLogin.vue'
import { login } from '@/services/AuthService'
import { flushPromises } from '@vue/test-utils'

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

// vi.mock('@/services/AuthService', () => ({
//   login: vi.fn().mockImplementation(() => ({} as any))
// }))

const mockPush = vi.fn()
vi.mock('vue-router', () => {
  return {
    useRouter: () => ({ push: mockPush }),
    useRoute: vi.fn()
  }
})

const mockTrigger = vi.fn()
vi.mock('@/hooks/useToast', () => ({
  useToast: () => ({ trigger: mockTrigger })
}))

describe('AdminLogin.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('monta corretamente', () => {
    const wrapper = mount(AdminLogin)
    expect(wrapper.exists()).toBe(true)
  })

  it('realiza login com sucesso e redireciona', async () => {
    vi.mocked(login).mockResolvedValueOnce('1234' as any)
    const wrapper = mount(AdminLogin)

    await wrapper.get('[data-testid="email"] input').setValue('admin@example.com')
    await wrapper.get('[data-testid="password"] input').setValue('Test@123')
    await wrapper.find('form').trigger('submit.prevent')

    await flushPromises()

    await vi.waitFor(() => {
      expect(login).toHaveBeenCalledWith('admin@example.com', 'Test@123')
      expect(localStorage.getItem('keycloak-session')).toContain('1234')
      expect(mockPush).toHaveBeenCalledWith({ name: 'home' })
      expect(mockTrigger).not.toHaveBeenCalled()
    })
  })

  it('exibe toast de erro se status for 401', async () => {
    const error = {
      response: {
        status: 401
      }
    }
    vi.mocked(login).mockRejectedValueOnce(error)

    const wrapper = mount(AdminLogin)
    await wrapper.get('[data-testid="email"] input').setValue('admin@example.com')
    await wrapper.get('[data-testid="password"] input').setValue('Test@123')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    await vi.waitFor(() => {
      expect(login).toHaveBeenCalledWith('admin@example.com', 'Test@123')
      expect(mockTrigger).toHaveBeenCalledWith('messages.invalidLogin', 'error')
      expect(mockPush).not.toHaveBeenCalled()
    })

  })

  it('exibe toast de erro genérico para outros erros', async () => {
    vi.mocked(login).mockRejectedValueOnce({ response: { status: 500 } })

    const wrapper = mount(AdminLogin)
    await wrapper.get('[data-testid="email"] input').setValue('admin@example.com')
    await wrapper.get('[data-testid="password"] input').setValue('Test@123')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    await vi.waitFor(() => {
      expect(mockTrigger).toHaveBeenCalledWith('messages.loginError', 'error')
    })
  })
})
