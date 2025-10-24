import { getAccessToken } from "./token"


export const redirectToPortal = () => {

  const addLang = import.meta.env.VITE_REDIRECT_PARAMS_LANG === 'true'
  const addToken = import.meta.env.VITE_REDIRECT_PARAMS_TOKEN === 'true'

  let url = new URL(import.meta.env.VITE_FRONTEND_USR_URL)

  if (addLang) {
    url.searchParams.append('lang', localStorage.getItem('language') as string)
  }
  if (addToken) {
    url.searchParams.append('token', getAccessToken())
  }

  window.location.href = url.toString()
}
