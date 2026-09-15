import { getAccessToken } from "./token"


export const redirectToPortal = () => {

  const addLang = import.meta.env.VITE_REDIRECT_PARAMS_LANG === 'true'
  const addToken = import.meta.env.VITE_REDIRECT_PARAMS_TOKEN === 'true'

  // VITE_FRONTEND_USR_URL pode ser absoluta (http://host/) ou relativa (/).
  // new URL() exige base para URLs relativas; usamos a origem atual como base
  // para funcionar em qualquer ambiente (local, HCSO, produção) sem hardcode.
  const target = import.meta.env.VITE_FRONTEND_USR_URL as string
  let url = new URL(target, window.location.origin)

  if (addLang) {
    url.searchParams.append('lang', localStorage.getItem('language') as string)
  }
  if (addToken) {
    url.searchParams.append('token', getAccessToken())
  }

  window.location.href = url.toString()
}
