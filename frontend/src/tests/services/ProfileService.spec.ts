import { describe, it, expect, vi, beforeEach } from 'vitest'
import http from '@/helpers/http'
import { API } from '@/helpers/api'
vi.unmock('@/services/ProfileService')
import { updateUserInfo, updatePassword } from '@/services/ProfileService'

vi.mock('@/helpers/http')

describe('ProfileService', () => {
	beforeEach(() => {
		vi.resetAllMocks()
	})

	describe('updateUserInfo', () => {
		it('should update user info successfully', async () => {
			const mockResponse = { success: true }
			vi.mocked(http.put).mockResolvedValueOnce({ data: mockResponse })

			const result = await updateUserInfo(
				'John', 'Doe', 'john@example.com', 'johndoe', '12345678900', 'user-123'
			)

			expect(http.put).toHaveBeenCalledWith(
				`${API.GENERAL}/updatePerfil`,
				{
					firstName: 'John',
					lastName: 'Doe',
					idNational: '12345678900',
					email: 'john@example.com',
					username: 'johndoe',
					id: 'user-123'
				}
			)
			expect(result).toEqual(mockResponse)
		})

		it('should throw error when update fails', async () => {
			const mockError = new Error('Update failed')
			vi.mocked(http.put).mockRejectedValueOnce(mockError)

			await expect(
				updateUserInfo('John', 'Doe', 'john@example.com', 'johndoe', '12345678900', 'user-123')
			).rejects.toThrow()
		})
	})

	describe('updatePassword', () => {
		it('should update password successfully', async () => {
			const mockResponse = { success: true }
			vi.mocked(http.put).mockResolvedValueOnce({ data: mockResponse })

			const result = await updatePassword('newpass123', 'user-123')

			expect(http.put).toHaveBeenCalledWith(
				`${API.GENERAL}/resetPassword`,
				{
					value: 'newpass123',
					id: 'user-123'
				}
			)
			expect(result).toEqual(mockResponse)
		})

		it('should throw error when password update fails', async () => {
			const mockError = new Error('Password update failed')
			vi.mocked(http.put).mockRejectedValueOnce(mockError)

			await expect(updatePassword('newpass123', 'user-123')).rejects.toThrow()
		})
	})
})
