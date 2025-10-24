
import { API } from '@/helpers/api'
import http from '@/helpers/http'
import type { UserInfo } from '@/types/user'

export interface TokenResponse {
  access_token: string
  expires_in: number
  'not-before-policy': number
  refresh_expires_in: number
  refresh_token: string
  scope: string
  session_state: string
  token_type: string
}

export const login = async (email: string, password: string) => {
  const params = new URLSearchParams()
  params.append('username', email)
  params.append('password', password)
  params.append('client_id', 'car-dpg-app')
  params.append('client_secret', 'S49U5h9kswLxS2xnRDOxn1WLWvnh5b15')
  params.append('grant_type', 'password')
  params.append('scope', 'openid')

  try {
    const response = await http.post<TokenResponse>(
      `${API.PROTOCOL}/openid-connect/token`,
      params,
      {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      }
    )

    console.log('Token:', response.data)
    return response.data
  } catch (error) {
    console.error('Erro ao autenticar com Keycloak', error)
    throw error
  }
}

export const createUser = async (
  email: string,
  firstName: string,
  lastName: string,
  password: string,
  idNational: string
) => {
  try {
    const response = await http.post(
      API.REGISTER,
      {
        firstName: firstName,
        lastName: lastName,
        idNational: idNational,
        email: email,
        value: password
      },
    )

    console.log('Usuário criado com sucesso:', response.data)
    return response.data
  } catch (error) {
    console.error('Erro ao criar usuario com Keycloak', error)
    throw error
  }
}

export const getUserInfo = async (id : string) => {
  try {
    const response = await http.get<UserInfo>(
     API.SEARCH + '/' + id,
    )

    return response.data
  } catch (error) {
    throw error
  }
}

export const getUserInfoByUsername = async (username : string) => {
  try {
    const response = await http.get(
     API.SEARCH_BY_USERNAME + '/' + username,
    )

    return response.data
  } catch (error) {
    throw error
  }
}

export const getTokenFromCode = async (code: string) => {
  try {
    const response = await http.post<TokenResponse>(
      `${API.GENERAL}/auth/callback`, {}, { params: { code } }
    )

    return response.data
  } catch (error) {
    throw error
  }
}


export const getCredential = async (username : string) => {
  try {
    const response = await http.get(
     API.CREDENTIAL + '/' + username,
    )

    return response.data
  } catch (error) {
    throw error
  }
}

export const isIdp = async (username : string) => {
  try {
    const response = await http.get(
     API.IDP + '/' + username,
    )

    return response.data
  } catch (error) {
    throw error
  }
}
