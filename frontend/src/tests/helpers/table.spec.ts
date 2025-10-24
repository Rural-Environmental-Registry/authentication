// src/tests/parseJsonToTableData.spec.ts
import { describe, it, expect } from 'vitest'
import { parseJsonToTableData } from '@/helpers/table'

describe('parseJsonToTableData', () => {
  it('converte valores primitivos para strings', () => {
    const input = {
      application_name: 'MyApp',
      version: 123,
      enabled: true
    }

    const out = parseJsonToTableData(input)
    const map = Object.fromEntries(out.map((i) => [i.fieldName, i]))

    expect(map.application_name.value).toBe('MyApp')
    expect(map.version.value).toBe('123') // number -> string
    expect(map.enabled.value).toBe('true') // boolean -> string

    // sources according to rules
    expect(map.application_name.source).toBe('application.properties')
    expect(map.application_name.component).toBe('backend')
  })

  it('stringifica objetos e arrays (serializáveis)', () => {
    const input = {
      settings: { theme: 'dark', size: 'L' },
      items: [1, 2, 3]
    }

    const out = parseJsonToTableData(input)
    const map = Object.fromEntries(out.map((i) => [i.fieldName, i]))

    expect(map.settings.value).toBe(JSON.stringify({ theme: 'dark', size: 'L' }))
    expect(map.items.value).toBe(JSON.stringify([1, 2, 3]))

    // defaults for source/component when no rule applies
    expect(map.settings.source).toBe('unknown')
    expect(map.settings.component).toBe('backend')
  })

  it('tratamento de objetos não serializáveis (circular) -> "[unserializable]"', () => {
    const circular: any = {}
    circular.self = circular

    const out = parseJsonToTableData({ circ: circular })
    expect(out).toHaveLength(1)
    expect(out[0].fieldName).toBe('circ')
    expect(out[0].value).toBe('[unserializable]')
  })

  it('expande default_attributes e detecta .types', () => {
    const input = {
      default_attributes: {
        'User.types': { role: 'admin' },
        'plainAttr': 'val'
      }
    }

    const out = parseJsonToTableData(input)
    // deve criar dois itens: 'User.types' e 'plainAttr'
    const byName = Object.fromEntries(out.map((i) => [i.fieldName, i]))

    expect(byName['User.types']).toBeDefined()
    expect(byName['User.types'].source).toBe('types definition of User')
    expect(byName['User.types'].component).toBe('backend')
    expect(byName['User.types'].value).toBe(JSON.stringify({ role: 'admin' }))

    expect(byName['plainAttr']).toBeDefined()
    expect(byName['plainAttr'].source).toBe('database')
    expect(byName['plainAttr'].component).toBe('backend')
    expect(byName['plainAttr'].value).toBe('val')
  })

  it('mapeia chaves que começam com backend ou vite para .env / frontend', () => {
    const input = {
      backend_api_key: 'abc',
      vite_base: '/app/'
    }

    const out = parseJsonToTableData(input)
    const map = Object.fromEntries(out.map((i) => [i.fieldName, i]))

    expect(map.backend_api_key.source).toBe('.env')
    expect(map.backend_api_key.component).toBe('frontend')

    expect(map.vite_base.source).toBe('.env')
    expect(map.vite_base.component).toBe('frontend')
  })

  it('mapeia frontend* para package.json e component frontend', () => {
    const input = {
      frontend_setting: 'v'
    }

    const out = parseJsonToTableData(input)
    expect(out[0].source).toBe('package.json')
    expect(out[0].component).toBe('frontend')
  })

  it('mapeia mapa_*, camadas_mapa e linguagem_padrao corretamente', () => {
    const input = {
      mapa_abc: 'x',
      camadas_mapa: 'layers',
      linguagem_padrao: 'pt-BR'
    }

    const out = parseJsonToTableData(input)
    const map = Object.fromEntries(out.map((i) => [i.fieldName, i]))

    expect(map.mapa_abc.source).toBe('src/assets/map/constsMap.ts')
    expect(map.mapa_abc.component).toBe('frontend')

    expect(map.camadas_mapa.source).toBe('src/assets/map/layers.json')
    expect(map.camadas_mapa.component).toBe('frontend')

    expect(map.linguagem_padrao.source).toBe('src/config/languages.json.default')
    expect(map.linguagem_padrao.component).toBe('frontend')
  })

  it('mapeia frontend_urls', () => {
    const out = parseJsonToTableData({ frontend_urls: ['a'] })
    expect(out[0].source).toBe('package.json')
    expect(out[0].component).toBe('frontend')
  })

  it('mapeia diff_area para diff_area.json (frontend)', () => {
    const out = parseJsonToTableData({ diff_area: { x: 1 } })
    expect(out[0].source).toBe('diff_area.json')
    expect(out[0].component).toBe('frontend')
  })

  it('campo sem regra cai em unknown / backend', () => {
    const out = parseJsonToTableData({ X_SOMETHING: 1 })
    expect(out[0].source).toBe('unknown')
    expect(out[0].component).toBe('backend')
  })
})
