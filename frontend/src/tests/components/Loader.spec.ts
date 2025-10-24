import { describe, it, expect } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import Loader from '@/components/common/Loader.vue'

describe('Loader.vue', () => {
  it('renderiza corretamente com o v-progress-circular', async () => {
    const wrapper = mount(Loader)

    // Verifica se o container do loader existe
    expect(wrapper.classes()).toContain('fixed')
    expect(wrapper.classes()).toContain('inset-0')
  })
})
