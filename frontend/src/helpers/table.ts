import type { TableDataItem } from "@/types/table"

export function parseJsonToTableData(json: Record<string, any>): TableDataItem[] {
  const result: TableDataItem[] = []

  const isPrimitive = (val: any): boolean =>
    ['string', 'number', 'boolean'].includes(typeof val)

  const toSafeString = (val: any): string => {
    if (isPrimitive(val)) return String(val)
    try {
      return JSON.stringify(val)
    } catch {
      return '[unserializable]'
    }
  }

  const pushItem = (
    fieldName: string,
    value: any,
    source: string,
    component: string
  ) => {
    result.push({
      fieldName,
      value: toSafeString(value),
      source,
      component
    })
  }

  for (const key in json) {
    const value = json[key]

    if (key === 'default_attributes' && typeof value === 'object' && value !== null) {
      for (const attrKey in value) {
        const attrValue = value[attrKey]
        const source = attrKey.includes('.types')
          ? `types definition of ${attrKey.split('.types')[0]}`
          : 'database'
        pushItem(attrKey, attrValue, source, 'backend')
      }
    } else {
      let source = 'unknown';
      let component = 'backend';

      switch (true) {
        case key.startsWith('backend'):
        case key.startsWith('vite'):
          source = '.env';
          component = 'frontend';
          break;

        case key.startsWith('frontend'):
          source = 'package.json';
          component = 'frontend';
          break;

        case key.startsWith('mapa_'):
          source = 'src/assets/map/constsMap.ts';
          component = 'frontend';
          break;

        case key === 'camadas_mapa':
          source = 'src/assets/map/layers.json';
          component = 'frontend';
          break;

        case key === 'linguagem_padrao':
          source = 'src/config/languages.json.default';
          component = 'frontend';
          break;

        case ['application_name', 'context_path', 'version'].includes(key):
          source = 'application.properties';
          component = 'backend';
          break;

        case key === 'frontend_urls':
          source = 'docker-compose.yaml';
          component = 'backend';
          break;

        case key === 'diff_area':
          source = 'diff_area.json';
          component = 'frontend';
          break;
      }

      pushItem(key, value, source, component)
    }
  }

  return result
}
