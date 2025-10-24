import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import SearchSelect from '@/components/admin-settings/SearchSelect.vue'

// Mock do useI18n
vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('SearchSelect.vue', () => {
  it('renderiza com o valor inicial', () => {
    const wrapper = mount(SearchSelect, {
      props: {
        modelValue: 'initial'
      }
    })

    const input = wrapper.get('input')
    expect(input.element.value).toBe('initial')
  })

  it('emite evento update:modelValue ao digitar', async () => {
    const wrapper = mount(SearchSelect, {
      props: {
        modelValue: ''
      }
    })

    const input = wrapper.get('input')
    await input.setValue('banana')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0]).toEqual(['banana'])
  })

  it('exibe placeholder traduzido', () => {
    const wrapper = mount(SearchSelect, {
      props: {
        modelValue: ''
      }
    })

    const input = wrapper.get('input')
    expect(input.attributes('placeholder')).toBe('search.placeholder')
  })

  it('renderiza ícone de busca', () => {
    const wrapper = mount(SearchSelect, {
      props: {
        modelValue: ''
      }
    })

    expect(wrapper.html()).toContain('mdi-magnify')
  })
})
