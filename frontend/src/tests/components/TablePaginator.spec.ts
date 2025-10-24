import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import TablePaginator from '@/components/admin-settings/TablePaginator.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('TablePaginator.vue', () => {
  const mountComponent = (props = {}) => {
    return mount(TablePaginator, {
      props: {
        total: 50,
        currentPage: 2,
        pageSize: 10,
        ...props
      }
    })
  }

  it('renderiza corretamente os elementos básicos', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('pagination.show')
    expect(wrapper.text()).toContain('pagination.of')
    expect(wrapper.text()).toContain('pagination.items')
    expect(wrapper.text()).toContain('pagination.page')
  })

  it('calcula corretamente o intervalo de itens', () => {
    const wrapper = mountComponent({ currentPage: 2, pageSize: 10, total: 50 })
    expect(wrapper.text()).toContain('11–20')
  })

  it('emite update:currentPage ao clicar no botão anterior', async () => {
    const wrapper = mountComponent({ currentPage: 2 })
    await wrapper.find('[data-testid="arrow-left"]').trigger('click')
    expect(wrapper.emitted('update:currentPage')![0]).toEqual([1])
  })

  it('não emite update:currentPage se já na primeira página', async () => {
    const wrapper = mountComponent({ currentPage: 1 })
    await wrapper.find('[data-testid="arrow-left"]').trigger('click')
    expect(wrapper.emitted('update:currentPage')).toBeUndefined()
  })

  it('emite update:currentPage ao clicar no botão próximo', async () => {
    const wrapper = mountComponent({ currentPage: 2, pageSize: 10, total: 50 })
    await wrapper.find('[data-testid="arrow-right"]').trigger('click')
    expect(wrapper.emitted('update:currentPage')![0]).toEqual([3])
  })

  it('emite update:pageSize ao mudar no seletor', async () => {
    const wrapper = mountComponent()
    const select = wrapper.findAllComponents({ name: 'VSelect' })[0]
    await select.vm.$emit('update:modelValue', 20)
    expect(wrapper.emitted('update:pageSize')![0]).toEqual([20])
  })

  it('emite update:currentPage ao mudar de página no seletor', async () => {
    const wrapper = mountComponent()
    const select = wrapper.findAllComponents({ name: 'VSelect' })[1]
    await select.vm.$emit('update:modelValue', 3)
    expect(wrapper.emitted('update:currentPage')![0]).toEqual([3])
  })

  it('usa pageSizes customizadas se passadas', () => {
    const wrapper = mountComponent({ pageSizes: [10, 25, 100] })
    const select = wrapper.findAllComponents({ name: 'VSelect' })[0]
    expect((select.props('items') as number[])).toEqual([10, 25, 100])
  })
})
