/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import * as redirect from '@/helpers/redirect'
import * as token from '@/helpers/token'
import * as utils from '@/helpers/utils'
import * as jwtDecode from 'jwt-decode'

vi.mock('jwt-decode', () => ({
  jwtDecode: vi.fn()
}))

vi.mock('@/router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    default: {
      ...actual,
      useRouter: vi.fn().mockReturnValue({ push: vi.fn() }),
      useRoute: vi.fn(),
      createRouter: vi.fn(),
      createWebHistory: vi.fn(),
      push: vi.fn()
    },
  }
})

// Remove global jwtDecode mock. Use per-test vi.mock below.
// Remove global localStorage stub. Use per-test vi.stubGlobal below.

// --- redirect.ts ---
describe('redirectToPortal', () => {
	const originalLocation = window.location

	beforeEach(() => {
		// eslint-disable-next-line @typescript-eslint/ban-ts-comment
		// @ts-expect-error
		delete window.location
		window.location = { href: '' } as any
		vi.stubGlobal('localStorage', {
			getItem: vi.fn((key) => {
				if (key === 'lang') return 'pt-br'
				return null
			})
		})
		vi.stubGlobal('import.meta', {
			env: {
				VITE_REDIRECT_PARAMS_LANG: 'true',
				VITE_REDIRECT_PARAMS_TOKEN: 'true',
				VITE_FRONTEND_USR_URL: 'https://portal.example.com'
			}
		})

		vi.spyOn(token, 'getAccessToken').mockReturnValue('mock-token')
	})



	afterEach(() => {
		window.location = originalLocation as any
		vi.restoreAllMocks()
	})
	it('should redirect with lang and token params', () => {
		redirect.redirectToPortal()
		expect(window.location.href).toContain('https://inovacao.dataprev.gov.br/rechml')
		expect(window.location.href).toContain('lang=pt-br')
		expect(window.location.href).toContain('token=mock-token')
	})
})

// --- token.ts ---
describe('getAccessToken', () => {
	it('should return access token from localStorage', () => {
		vi.stubGlobal('localStorage', {
			getItem: vi.fn(() => JSON.stringify({ access_token: 'abc123' }))
		})
		expect(token.getAccessToken()).toBe('abc123')
	})
})

describe('isTokenExpired', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

	it('should return true if no token', () => {
		vi.stubGlobal('localStorage', { getItem: vi.fn(() => null) })
		expect(token.isTokenExpired()).toBe(true)
	})
	it('should return true if token is expired', () => {
    (jwtDecode.jwtDecode as any).mockReturnValueOnce({ exp: 1000000000, resource_access: {} })
		const expiredToken =
			'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.' +
			'eyJleHAiOjEwMDAwMDAwLCJyZXNvdXJjZV9hY2Nlc3MiOnt9fQ.' +
			'signature'
		vi.stubGlobal('localStorage', {
			getItem: vi.fn(() => JSON.stringify({ access_token: expiredToken }))
		})
		expect(token.isTokenExpired()).toBe(true)
	})
	it('should return false if token is valid', () => {
    (jwtDecode.jwtDecode as any).mockReturnValueOnce({ exp: Math.floor(Date.now() / 1000) + 1000, resource_access: {} })

		vi.stubGlobal('localStorage', {
			getItem: vi.fn(() => JSON.stringify({ access_token: 'valid-token' }))
		})
		expect(token.isTokenExpired()).toBe(false)
	})
})

describe('checkIsAdmin', () => {

  beforeEach(() => {
    vi.clearAllMocks()
  })
	it('should return true if admin role present', () => {
    (jwtDecode.jwtDecode as any).mockReturnValueOnce({
        exp: Math.floor(Date.now() / 1000) + 1000,
				resource_access: {
					'realm-management': { roles: ['realm-admin'] },
					'car-dpg-app': { roles: [] }
				}
    })
		vi.stubGlobal('localStorage', {
			getItem: vi.fn(() => JSON.stringify({ access_token: 'admin-token' }))
		})
		expect(token.checkIsAdmin()).toBe(true)
	})
	it('should return false if no admin role', () => {
    (jwtDecode.jwtDecode as any).mockReturnValueOnce({
        exp: Math.floor(Date.now() / 1000) + 1000,
				resource_access: {
					'realm-management': { roles: [] },
					'car-dpg-app': { roles: [] }
				}
    })
		vi.stubGlobal('localStorage', {
			getItem: vi.fn(() => JSON.stringify({ access_token: 'user-token' }))
		})

		expect(token.checkIsAdmin()).toBe(false)
	})
	it('should handle error and remove token', () => {
    (jwtDecode.jwtDecode as any).mockRejectedValueOnce(new Error('bad token'))
		const removeItem = vi.fn()
		vi.stubGlobal('localStorage', {
			getItem: vi.fn(() => '{'),
			removeItem
		})

		expect(token.checkIsAdmin()).toBe(false)
		expect(removeItem).toHaveBeenCalledWith('token')
	})
})

// --- utils.ts ---
describe('cn', () => {
	it('should merge tailwind classes', () => {
		expect(utils.cn('bg-red-500', 'text-white')).toContain('bg-red-500')
	})
	it('should handle conditional classes', () => {
		expect(utils.cn('foo', false && 'bar')).toContain('foo')
	})
})

describe('valueUpdater', () => {
	it('should update ref value with function', () => {
		const ref = { value: 1 }
		utils.valueUpdater((v: number) => v + 1, ref as any)
		expect(ref.value).toBe(2)
	})
	it('should update ref value with direct value', () => {
		const ref = { value: 1 }
		utils.valueUpdater(5, ref as any)
		expect(ref.value).toBe(5)
	})
})
