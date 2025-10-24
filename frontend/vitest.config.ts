import { fileURLToPath } from 'node:url'
import { mergeConfig, defineConfig, configDefaults, UserConfigFn } from 'vitest/config'
import viteConfig from './vite.config'

const vitestConfig: UserConfigFn = (env) => mergeConfig(
  viteConfig(env),
  defineConfig({
    test: {
      globals: true,
      server: {
        deps: {
          inline: ['vuetify']
        }
      },
      environment: 'jsdom',
      exclude: [...configDefaults.exclude, 'e2e/**'],
      root: fileURLToPath(new URL('./', import.meta.url)),
      setupFiles: './src/tests/vitest.setup.ts',
      coverage: {
        reporter: ['text', 'lcov'],
        include: [
          'src/views/**/*.{ts,vue}',
          'src/helpers/**/*.{ts,vue}',
          'src/services/**/*.{ts,vue}',
          'src/components/**/*.{ts,vue}',
        ],
        exclude: [
          'src/components/common/select/**/*.{ts,vue}',
          'src/components/common/avatar/**/*.{ts,vue}',
          'src/components/common/dropdown-menu/**/*.{ts,vue}',
          'src/main.ts',
          'src/router/**',
          'src/locales/**',
          'src/stores/**',
          '**/*.d.ts',
          'src/tests/**'
        ]
      }
    },
    // css: {
    //   preprocessorOptions: {
    //     css: {}
    //   }
    // }
  }),
);

export default vitestConfig;
