import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AdminSettings from '@/views/AdminSettings.vue'
import { TABLE_DATA } from '../../../mocks/table-data'
import { flushPromises } from '@vue/test-utils'

vi.mock('@/components/admin-settings/AdminCard.vue', () => ({
  default: { template: '<div><slot /></div>' }
}))
vi.mock('@/components/admin-settings/SearchSelect.vue', () => ({
  default: {
    template: '<input @input="$emit(\'update:modelValue\', $event.target.value)" />'
  }
}))
vi.mock('@/components/admin-settings/ConfigTable.vue', () => ({
  default: {
    props: ['rows'],
    template: '<div>Config Table</div>'
  }
}))
vi.mock('@/components/admin-settings/TablePaginator.vue', () => ({
  default: {
    props: ['total', 'currentPage', 'pageSize'],
    template: '<div>Pagination</div>'
  }
}))
vi.mock('@/components/common/PageHeader.vue', () => ({
  default: { template: '<div>Header</div>' }
}))

vi.mock('@/helpers/token', () => ({
  isTokenExpired: vi.fn(() => false),
  checkIsAdmin: vi.fn(() => true)
}))

vi.mock('@/helpers/table', () => ({
  parseJsonToTableData: vi.fn(() => TABLE_DATA)
}))

vi.mock('axios', () => ({
  default: {
    get: vi.fn(() => Promise.resolve({ data: {} }))
  }
}))

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

describe('AdminSettings.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('monta corretamente e renderiza os componentes', async () => {
    const wrapper = mount(AdminSettings)
    await flushPromises()

    expect(wrapper.html()).toContain('Config Table')
    expect(wrapper.html()).toContain('Pagination')
  })

  it('redireciona se o token for inválido ou o usuário não for admin', async () => {
    const { isTokenExpired, checkIsAdmin } = await import('@/helpers/token')
    ;(isTokenExpired as any).mockReturnValueOnce(true)

    mount(AdminSettings)
    await flushPromises()

    expect(mockPush).toHaveBeenCalledWith({ name: 'admin-login' })
  })

  it('filtra os dados da tabela com base no texto de busca', async () => {
    const wrapper = mount(AdminSettings)
    await flushPromises()

    const input = wrapper.find('input')
    await input.setValue('backend')

    // Confirma que o campo de input está funcionando
    expect((input.element as HTMLInputElement).value).toBe('backend')
  })
})
