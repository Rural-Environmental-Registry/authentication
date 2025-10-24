import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import UserRegister from '@/views/UserRegister.vue'
import { createUser, login } from '@/services/AuthService'

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

describe('UserRegister.vue', () => {
  beforeEach(async () => {
    vi.clearAllMocks()
    localStorage.clear()

    const useRoute = (await import('vue-router')).useRoute as any
      useRoute.mockReturnValue({
        query: {
          email: ''
        }
      })

    })

  it('monta corretamente', () => {
    const wrapper = mount(UserRegister)
    expect(wrapper.exists()).toBe(true)
  })

  it('realiza cadastro com sucesso e redireciona para portal', async () => {
    vi.mocked(login).mockResolvedValueOnce({ token: 'abcd1234' } as any)
    vi.mocked(createUser).mockResolvedValueOnce({} as any)

    const wrapper = mount(UserRegister)

    await wrapper.get('[data-testid="name"] input').setValue('Carla')
    await wrapper.get('[data-testid="lastName"] input').setValue('Santos')
    await wrapper.get('[data-testid="idNational"] input').setValue('3213221')
    await wrapper.get('[data-testid="email"] input').setValue('carla@example.com')
    await wrapper.get('[data-testid="password"] input').setValue('Abc123@#')
    await wrapper.get('[data-testid="confirmPassword"] input').setValue('Abc123@#')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    await vi.waitFor(() => {
      expect(createUser).toHaveBeenCalledWith(
        'carla@example.com',
        'Carla',
        'Santos',
        'Abc123@#',
        '3213221',
      )
      expect(login).toHaveBeenCalledWith('carla@example.com', 'Abc123@#')
      expect(localStorage.getItem('keycloak-session')).toContain('abcd1234')
    })
  })

  it('exibe erro se e-mail já cadastrado', async () => {
    vi.mocked(createUser).mockRejectedValueOnce({
      status: 400,
      response: { data: 'username already exists' }
    })

    const wrapper = mount(UserRegister)

    await wrapper.get('[data-testid="email"] input').setValue('jaexiste@email.com')
    await wrapper.get('[data-testid="name"] input').setValue('Ana')
    await wrapper.get('[data-testid="lastName"] input').setValue('Silva')
    await wrapper.get('[data-testid="password"] input').setValue('Abc123@#')
    await wrapper.get('[data-testid="confirmPassword"] input').setValue('Abc123@#')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    await vi.waitFor(() => {
      expect(mockTrigger).toHaveBeenCalledWith('messages.registerError', 'error')
    })
  })

  it('exibe erro genérico se status não for 400', async () => {
    vi.mocked(createUser).mockRejectedValueOnce({
      status: 500,
      response: { data: 'Server error' }
    })

    const wrapper = mount(UserRegister)

    await wrapper.get('[data-testid="email"] input').setValue('erro@email.com')
    await wrapper.get('[data-testid="name"] input').setValue('Lucas')
    await wrapper.get('[data-testid="lastName"] input').setValue('Ferreira')
    await wrapper.get('[data-testid="password"] input').setValue('Abc123@#')
    await wrapper.get('[data-testid="confirmPassword"] input').setValue('Abc123@#')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    await vi.waitFor(() => {
      expect(mockTrigger).toHaveBeenCalledWith('messages.registerError', 'error')
    })
  })

  it('redireciona ao clicar em "Já tem uma conta?"', async () => {
    const wrapper = mount(UserRegister)
    await wrapper.find('span.cursor-pointer').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'user-login' })
  })
})
