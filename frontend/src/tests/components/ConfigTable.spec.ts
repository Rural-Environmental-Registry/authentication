import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AdminTable from '@/components/admin-settings/ConfigTable.vue'

// Mock do componente filho
vi.mock('@/components/admin-settings/AdminTableRow.vue', () => ({
  default: {
    name: 'AdminTableRow',
    props: ['fieldName', 'value', 'source', 'component'],
    template: '<tr><td>{{ fieldName }}</td><td>{{ value }}</td><td>{{ source }}</td><td>{{ component }}</td></tr>',
  }
}))

// Mock i18n
vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('AdminTable.vue', () => {
  const mockRows = [
    {
      fieldName: 'frontend_url (CORS)',
      value: 'https://inovacao.dataprev.gov.br/',
      source: 'docker-compose.yaml',
      component: 'backend',
    },
    {
      fieldName: 'backend_version',
      value: '0.0.1-SNAPSHOT',
      source: 'build.gradle',
      component: 'backend',
    }
  ]

  let wrapper: ReturnType<typeof mount>

  beforeEach(() => {
    wrapper = mount(AdminTable, {
      props: { rows: mockRows }
    })
  })

  it('renderiza os cabeçalhos traduzidos corretamente', () => {
    const headers = wrapper.findAll('thead th')
    expect(headers).toHaveLength(4)
    expect(headers[0].text()).toBe('table.headers.field')
    expect(headers[1].text()).toBe('table.headers.value')
    expect(headers[2].text()).toBe('table.headers.source')
    expect(headers[3].text()).toBe('table.headers.component')
  })

  it('renderiza um AdminTableRow para cada item em rows', () => {
    const rows = wrapper.findAll('tbody tr')
    expect(rows).toHaveLength(mockRows.length)
    expect(rows[0].text()).toContain(mockRows[0].fieldName)
    expect(rows[1].text()).toContain(mockRows[1].fieldName)
  })

  it('usa as props corretas para AdminTableRow', () => {
    const rows = wrapper.findAll('tbody tr')
    expect(rows[0].html()).toContain(mockRows[0].value)
    expect(rows[0].html()).toContain(mockRows[0].source)
    expect(rows[0].html()).toContain(mockRows[0].component)
  })
})
