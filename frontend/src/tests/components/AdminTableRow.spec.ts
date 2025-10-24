import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AdminTableRow from '@/components/admin-settings/AdminTableRow.vue'

describe('AdminTableRow.vue', () => {
  const props = {
    fieldName: 'frontend_url (CORS)',
    value: 'https://inovacao.dataprev.gov.br/',
    source: 'docker-compose.yaml',
    component: 'backend',
  }

  it('renderiza os dados corretamente nas colunas', () => {
    const wrapper = mount(AdminTableRow, { props })

    const cells = wrapper.findAll('td')
    expect(cells).toHaveLength(4)
    expect(cells[0].text()).toBe(props.fieldName)
    expect(cells[1].text()).toBe(props.value)
    expect(cells[2].text()).toBe(props.source)
    expect(cells[3].text()).toBe(props.component)
  })

  it('tem a estrutura correta de linha e classes', () => {
    const wrapper = mount(AdminTableRow, { props })

    expect(wrapper.element.tagName).toBe('TR')
    expect(wrapper.classes()).toContain('text-sm')
    expect(wrapper.classes()).toContain('border-b-1')
    expect(wrapper.classes()).toContain('border-gray-300')
  })
})
