import { jwtDecode } from 'jwt-decode'
import router from '@/router'
interface JwtPayload {
  exp: number
  resource_access: any
}

export function getAccessToken() {
  const data = localStorage.getItem('keycloak-session')
  const parsedData = data ? JSON.parse(data) : {}

  return parsedData['access_token'] as string
}

export function isTokenExpired(): boolean {

  const tokenInfo = localStorage.getItem('keycloak-session')

  if (!tokenInfo) {
    return true
  }

  const token = JSON.parse(tokenInfo)['access_token']

  const { exp } = jwtDecode<JwtPayload>(token)
  const now = Date.now() / 1000 // em segundos
  return exp < now
}

export function checkIsAdmin() {
  try {

    const tokenInfo = localStorage.getItem('keycloak-session')
    if (!tokenInfo) {
      return true
    }
    const token = JSON.parse(tokenInfo)['access_token']

    const payload = jwtDecode<JwtPayload>(token)

    const realmRoles = payload?.resource_access?.['realm-management']?.roles || []
    const clientRoles = payload?.resource_access?.['car-dpg-app']?.roles || []

    console.log(realmRoles, clientRoles)

    const isAdmin =
      realmRoles.includes('realm-admin') ||
      realmRoles.includes('manage-users') ||
      clientRoles.includes('admin-settings')

    return isAdmin
  } catch (error) {
    console.error('Erro ao verificar token:', error)
    localStorage.removeItem('token')
    router.push({ name: 'login' })
    return false
  }
}
