import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AdminCard from '@/components/admin-settings/AdminCard.vue'

describe('AdminCard.vue', () => {
  it('renderiza o conteúdo do slot', () => {
    const wrapper = mount(AdminCard, {
      slots: {
        default: '<p>Conteúdo de teste</p>',
      },
    })

    expect(wrapper.text()).toContain('Conteúdo de teste')
  })

  it('exibe o título quando a prop "title" é passada', () => {
    const wrapper = mount(AdminCard, {
      props: {
        title: 'Título de Teste',
      },
    })

    expect(wrapper.text()).toContain('Título de Teste')
    expect(wrapper.find('div.text-lg.font-semibold').exists()).toBe(true)
  })

  it('não renderiza o título quando a prop "title" não é fornecida', () => {
    const wrapper = mount(AdminCard)
    expect(wrapper.find('div.text-lg.font-semibold').exists()).toBe(false)
  })
})
