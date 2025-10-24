import axios from 'axios'
import router from '@/router'

const http = axios.create()

http.interceptors.request.use(config => {
  const data = localStorage.getItem('keycloak-session')
  const parsedData = data ? JSON.parse(data) : {}

  if (parsedData && parsedData['access_token']) {
    config.headers.Authorization = `Bearer ${parsedData['access_token']}`
  }

  return config
}, error => {
  return Promise.reject(error)
})

http.interceptors.response.use(
  response => response,
  error => {
    // const router = useRouter()
    if (error.response?.status === 401) {
      // 🔐 Limpar token
      localStorage.removeItem('keycloak-session')

      // 🔁 Redirecionar para login
      router.push({ name: 'user-login' }) // ou `'/login'`

      // 🛑 Opcional: mostrar alerta
      console.warn('Sessão expirada. Redirecionando para login.')
    }

    return Promise.reject(error)
  }
)

export default http
