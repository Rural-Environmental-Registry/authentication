import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import PageHeader from '@/components/common/PageHeader.vue'
import { useI18n } from 'vue-i18n'

// Mock vue-router
vi.mock('vue-router', () => ({
  RouterLink: {
    template: '<a><slot /></a>',
  },
}))

const mockPush = vi.fn()
vi.mock('vue-router', () => {
  return {
    useRouter: () => ({ push: mockPush }),
    useRoute: vi.fn().mockReturnValue({ name: 'home' })
  }
})

// Mock select-related components
vi.mock('@/components/common/select', () => ({
  Select: {
    template: '<div><slot /></div>',
    props: ['modelValue'],
  },
  SelectTrigger: {
    template: '<div><slot /></div>',
  },
  SelectValue: {
    template: '<span />',
    props: ['placeholder'],
  },
  SelectItem: {
    template: '<div><slot /></div>',
    props: ['value'],
  },
  SelectContent: {
    template: '<div><slot /></div>',
  },
}))
vi.mock('@/components/common/avatar/Avatar.vue', () => ({ default: { template: '<div><slot /></div>' } }))
vi.mock('@/components/common/avatar/AvatarImage.vue', () => ({ default: { template: '<div></div>' } }))
vi.mock('@/components/common/avatar/AvatarFallback.vue', () => ({ default: { template: '<div></div>' } }))
vi.mock('@/components/common/dropdown-menu/DropdownMenu.vue', () => ({ default: { template: '<div><slot /></div>' } }))
vi.mock('@/components/common/dropdown-menu/DropdownMenuTrigger.vue', () => ({ default: { template: '<div><slot /></div>' } }))
vi.mock('@/components/common/dropdown-menu/DropdownMenuContent.vue', () => ({ default: { template: '<div><slot /></div>' } }))
vi.mock('@/components/common/dropdown-menu/DropdownMenuItem.vue', () => ({ default: { template: '<div @click="$emit(\'click\')"><slot /></div>' } }))
vi.mock('@/components/common/dropdown-menu/DropdownMenuSeparator.vue', () => ({ default: { template: '<div></div>' } }))
vi.mock('@/components/common/dropdown-menu/DropdownMenuLabel.vue', () => ({ default: { template: '<div><slot /></div>' } }))

// Mock FontAwesomeIcon
vi.mock('@fortawesome/vue-fontawesome', () => ({
  FontAwesomeIcon: {
    template: '<i />',
    props: ['icon'],
  },
}))

describe('PageHeader.vue', () => {
  beforeEach(() => {
    // vi.clearAllMocks()
    // localStorage.clear()
  })

  it('renderiza corretamente', () => {
    const wrapper = mount(PageHeader)
    expect(wrapper.find('img[alt="gov.br logo"]').exists()).toBe(true)
  })

  it('define o locale inicial no localStorage ao montar', () => {
    mount(PageHeader)
    expect(localStorage.getItem('language')).toBe('en') // depende da linguagem inicial
  })

  it('atualiza o locale quando selected é alterado', async () => {
    const wrapper = mount(PageHeader)
    const vm = wrapper.vm as any

    // Simula a troca de idioma
    vm.selected = 'es-es'
    await wrapper.vm.$nextTick()

    expect(localStorage.getItem('language')).toBe('es-es')
  })

  it('limpa localStorage/sessionStorage e redireciona para admin-login ao fazer logout na rota home', async () => {
    const wrapper = mount(PageHeader)

    await vi.waitFor(async () => {
      const logoutButton = await wrapper.get('[data-testid="logout-btn"]')
      await logoutButton.trigger('click')

      expect(localStorage.getItem('keycloak-session')).toBe('')
      expect(sessionStorage.getItem('token')).toBeNull()
      expect(mockPush).toHaveBeenCalledWith({ name: 'admin-login' })
    })

  })
})
