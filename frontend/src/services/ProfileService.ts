import { API } from '@/helpers/api'
import http from '@/helpers/http'

export const updateUserInfo = async (firstName: string, lastName: string, email: string, username: string,idNational: string, id: string) => {
  const result = await http.put(
    `${API.GENERAL}/updatePerfil`,
    {
      firstName,
      lastName,
      idNational,
      email,
      username,
      id
    },
  )
  return result.data
}

export const updatePassword = async (newPassword: string, id: string) => {
  const result = await http.put(
    `${API.GENERAL}/resetPassword`,
    {
      value: newPassword,
      id
    },
  )
  return result.data
}

