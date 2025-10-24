import { describe, expect, it, vi, beforeEach } from 'vitest'
import http from '@/helpers/http'
import { API } from '@/helpers/api'
// at the top of your test file
vi.unmock('@/services/AuthService')
import { login, createUser, getUserInfo, getUserInfoByUsername, getTokenFromCode, getCredential, isIdp } from '@/services/AuthService'
import type { UserInfo } from '@/types/user'
import type { TokenResponse } from '@/services/AuthService'

vi.mock('@/helpers/http')

describe('AuthService', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  describe('login', () => {
    it('should login successfully with valid credentials', async () => {
      const mockResponse: TokenResponse = {
        access_token: 'mock-token',
        expires_in: 300,
        'not-before-policy': 0,
        refresh_expires_in: 1800,
        refresh_token: 'mock-refresh-token',
        scope: 'openid',
        session_state: 'mock-session',
        token_type: 'Bearer'
      }

      vi.mocked(http.post).mockResolvedValueOnce({ data: mockResponse })

      const result = await login('test@example.com', 'password123')

      expect(http.post).toHaveBeenCalledWith(
        `${API.PROTOCOL}/openid-connect/token`,
        expect.any(URLSearchParams),
        expect.any(Object)
      )
      expect(result).toEqual(mockResponse)
    })

    it('should throw error when login fails', async () => {
      const mockError = new Error('Login failed')
      vi.mocked(http.post).mockRejectedValueOnce(mockError)

      await expect(login('test@example.com', 'wrong-password')).rejects.toThrow()
    })
  })

  describe('createUser', () => {
    it('should create user successfully', async () => {
      const mockResponse = { id: 'user-123' }
      vi.mocked(http.post).mockResolvedValueOnce({ data: mockResponse })

      const result = await createUser(
        'test@example.com',
        'John',
        'Doe',
        'password123',
        '12345678900'
      )

      expect(http.post).toHaveBeenCalledWith(
        API.REGISTER,
        {
          firstName: 'John',
          lastName: 'Doe',
          idNational: '12345678900',
          email: 'test@example.com',
          value: 'password123'
        }
      )
      expect(result).toEqual(mockResponse)
    })

    it('should throw error when user creation fails', async () => {
      const mockError = new Error('User creation failed')
      vi.mocked(http.post).mockRejectedValueOnce(mockError)

      await expect(
        createUser('test@example.com', 'John', 'Doe', 'password123', '12345678900')
      ).rejects.toThrow()
    })
  })

  describe('getUserInfo', () => {
    it('should get user info successfully', async () => {
      const mockUserInfo: UserInfo = {
        id: 'user-123',
        email: 'test@example.com',
        email_verified: true,
        firstName: 'John',
        lastName: 'Doe',
        name: 'John Doe',
        preferred_username: 'johndoe',
        idNational: '12345678900'
      }

      vi.mocked(http.get).mockResolvedValueOnce({ data: mockUserInfo })

      const result = await getUserInfo('user-123')

      expect(http.get).toHaveBeenCalledWith(`${API.SEARCH}/user-123`)
      expect(result).toEqual(mockUserInfo)
    })

    it('should throw error when getting user info fails', async () => {
      const mockError = new Error('Failed to get user info')
      vi.mocked(http.get).mockRejectedValueOnce(mockError)

      await expect(getUserInfo('invalid-id')).rejects.toThrow()
    })
  })

  describe('getUserInfoByUsername', () => {
    it('should get user info by username successfully', async () => {
      const mockUserInfo: UserInfo = {
        id: 'user-123',
        email: 'test@example.com',
        email_verified: true,
        firstName: 'John',
        lastName: 'Doe',
        name: 'John Doe',
        preferred_username: 'johndoe',
        idNational: '12345678900'
      }

      vi.mocked(http.get).mockResolvedValueOnce({ data: mockUserInfo })

      const result = await getUserInfoByUsername('johndoe')

      expect(http.get).toHaveBeenCalledWith(`${API.SEARCH_BY_USERNAME}/johndoe`)
      expect(result).toEqual(mockUserInfo)
    })

    it('should throw error when getting user info by username fails', async () => {
      const mockError = new Error('Failed to get user info')
      vi.mocked(http.get).mockRejectedValueOnce(mockError)

      await expect(getUserInfoByUsername('invalid-username')).rejects.toThrow()
    })
  })

  describe('getTokenFromCode', () => {
    it('should get token from code successfully', async () => {
      const mockTokenResponse: TokenResponse = {
        access_token: 'mock-token',
        expires_in: 300,
        'not-before-policy': 0,
        refresh_expires_in: 1800,
        refresh_token: 'mock-refresh-token',
        scope: 'openid',
        session_state: 'mock-session',
        token_type: 'Bearer'
      }

      vi.mocked(http.post).mockResolvedValueOnce({ data: mockTokenResponse })

      const result = await getTokenFromCode('auth-code-123')

      expect(http.post).toHaveBeenCalledWith(
        `${API.GENERAL}/auth/callback`,
        {},
        { params: { code: 'auth-code-123' } }
      )
      expect(result).toEqual(mockTokenResponse)
    })

    it('should throw error when getting token from code fails', async () => {
      const mockError = new Error('Failed to get token')
      vi.mocked(http.post).mockRejectedValueOnce(mockError)

      await expect(getTokenFromCode('invalid-code')).rejects.toThrow()
    })
  })

  describe('getCredential', () => {
    it('should get credential successfully', async () => {
      const mockCredential = { credential: 'mock-credential' }
      vi.mocked(http.get).mockResolvedValueOnce({ data: mockCredential })

      const result = await getCredential('johndoe')

      expect(http.get).toHaveBeenCalledWith(`${API.CREDENTIAL}/johndoe`)
      expect(result).toEqual(mockCredential)
    })

    it('should throw error when getting credential fails', async () => {
      const mockError = new Error('Failed to get credential')
      vi.mocked(http.get).mockRejectedValueOnce(mockError)

      await expect(getCredential('invalid-username')).rejects.toThrow()
    })
  })

  describe('isIdp', () => {
    it('should check idp successfully', async () => {
      const mockResponse = { isIdp: true }
      vi.mocked(http.get).mockResolvedValueOnce({ data: mockResponse })

      const result = await isIdp('johndoe')

      expect(http.get).toHaveBeenCalledWith(`${API.IDP}/johndoe`)
      expect(result).toEqual(mockResponse)
    })

    it('should throw error when checking idp fails', async () => {
      const mockError = new Error('Failed to check idp')
      vi.mocked(http.get).mockRejectedValueOnce(mockError)

      await expect(isIdp('invalid-username')).rejects.toThrow()
    })
  })
})
