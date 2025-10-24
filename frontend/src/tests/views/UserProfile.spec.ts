import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import UserProfile from '@/views/UserProfile.vue'
import { updatePassword, updateUserInfo } from '@/services/ProfileService'
import { getUserInfo, isIdp } from '@/services/AuthService'
import { nextTick } from 'vue'

vi.mock('@/components/common/PageHeader.vue', () => ({
  default: { template: '<div />' }
}))

vi.mock('@/components/common/Loader.vue', () => ({
  default: { template: '<div />' }
}))

vi.mock('@/helpers/token', () => ({
  isTokenExpired: () => false
}))

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
  useRoute: vi.fn()
}))

const mockTrigger = vi.fn()
vi.mock('@/hooks/useToast', () => ({
  useToast: () => ({ trigger: mockTrigger })
}))

vi.mock('@/helpers/token', () => ({
  isTokenExpired: vi.fn(() => false),
  checkIsAdmin: vi.fn(() => true)
}))


describe('UserProfile.vue', () => {
  localStorage.setItem('keycloak-session', '{"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJwb0dfLVR4T3R4UmhEZEFPYW9OREtrTUxnWlFQY3FESUlDaWN1RmRCSGxJIn0.eyJleHAiOjE3NTY3NjYwNjIsImlhdCI6MTc1NjczMDA2MywiYXV0aF90aW1lIjoxNzU2NzMwMDYyLCJqdGkiOiIyZmMzNGI5YS1iNTIwLTQxMDEtYTE3MS1kZjExNTUyMjQ5ZTciLCJpc3MiOiJodHRwczovL2lub3ZhY2FvLmRhdGFwcmV2Lmdvdi5ici9yZWNobWwva2V5Y2xvYWsvcmVhbG1zL2Nhci1kcGciLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiN2UzMmFmYjItOWMxZS00MzU0LWJjZWQtZjcxNzgzZWQ4NTI4IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYXBpLXNlcnZpY2UiLCJzaWQiOiI3NGIyMWMyOC00NmNiLTQxMmEtYTg3OS02ZjcwMTdiMWIwODIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIi8qIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy1jYXItZHBnIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiaWRfbmF0aW9uYWwiOiI4NzUxMDYwNjA0NyIsIm5hbWUiOiJNZXUgTm9tZSIsInByZWZlcnJlZF91c2VybmFtZSI6ImFuZGVyc29uLmFyam9uYUBhY2FkLnB1Y3JzLmJyIiwiZ2l2ZW5fbmFtZSI6Ik1ldSIsImZhbWlseV9uYW1lIjoiTm9tZSIsImVtYWlsIjoiYW5kZXJzb24uYXJqb25hQGFjYWQucHVjcnMuYnIifQ.EypeSCu1g4fRXrSSGCDhLUa6EXJTxnu2n2AvtoSsILjqdusYAmUMN8181pQ2D5QnwHgFtddoLKJaktArnabyFWEjBoi1D2G9SeJSFCAocXz08FW0Z4gVDmuNRS07fCrHQpCCkjpk5lX7Gd86lyJfIVMUq6L8dHtUGqiBBdKK6q_-1JCJjuPfJypKTrHlyxYUBd_rvns3lbhfcpUhineO29qngouM5zOOy9rXbxgVynl2V09y-wIxlVDBiD21b3yOqxelZKr7IRvtqrhZUrfpUnNG_U3Z32wijtxmlnN3woyIntKldDkg1BWyXAooQsnZnQAgWZvhwBXGnksgxcd3VA","expires_in":35999,"refresh_expires_in":35999,"refresh_token":"eyJhbGciOiJIUzUxMiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJjMzhlNWJjMS0zMjIzLTQ4OWQtOWQ5Zi0yZmM5ZmUyMzBlOTAifQ.eyJleHAiOjE3NTY3NjYwNjIsImlhdCI6MTc1NjczMDA2MywianRpIjoiMzEwOTE0ZjktOTI0MS00ZjQ5LTk0MjgtNDgyNjVkYjIxYjJhIiwiaXNzIjoiaHR0cHM6Ly9pbm92YWNhby5kYXRhcHJldi5nb3YuYnIvcmVjaG1sL2tleWNsb2FrL3JlYWxtcy9jYXItZHBnIiwiYXVkIjoiaHR0cHM6Ly9pbm92YWNhby5kYXRhcHJldi5nb3YuYnIvcmVjaG1sL2tleWNsb2FrL3JlYWxtcy9jYXItZHBnIiwic3ViIjoiN2UzMmFmYjItOWMxZS00MzU0LWJjZWQtZjcxNzgzZWQ4NTI4IiwidHlwIjoiUmVmcmVzaCIsImF6cCI6ImFwaS1zZXJ2aWNlIiwic2lkIjoiNzRiMjFjMjgtNDZjYi00MTJhLWE4NzktNmY3MDE3YjFiMDgyIiwic2NvcGUiOiJvcGVuaWQgc2VydmljZV9hY2NvdW50IHdlYi1vcmlnaW5zIHJvbGVzIHByb2ZpbGUgYWNyIGJhc2ljIGVtYWlsIn0.UXoQW1jRMAnq_Z2UnL_gRwEbIMTLwyK947-51dUnHor-MnVkFlDbzDol-aj822gGErzaZ-VXadZQ57B_CcNHZg","token_type":"Bearer","id_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJwb0dfLVR4T3R4UmhEZEFPYW9OREtrTUxnWlFQY3FESUlDaWN1RmRCSGxJIn0.eyJleHAiOjE3NTY3NjYwNjIsImlhdCI6MTc1NjczMDA2MywiYXV0aF90aW1lIjoxNzU2NzMwMDYyLCJqdGkiOiJlMjY0YjY1ZS02ZjkyLTRlYTItYjZjNy0wODlmZWYyNGEzZGQiLCJpc3MiOiJodHRwczovL2lub3ZhY2FvLmRhdGFwcmV2Lmdvdi5ici9yZWNobWwva2V5Y2xvYWsvcmVhbG1zL2Nhci1kcGciLCJhdWQiOiJhcGktc2VydmljZSIsInN1YiI6IjdlMzJhZmIyLTljMWUtNDM1NC1iY2VkLWY3MTc4M2VkODUyOCIsInR5cCI6IklEIiwiYXpwIjoiYXBpLXNlcnZpY2UiLCJzaWQiOiI3NGIyMWMyOC00NmNiLTQxMmEtYTg3OS02ZjcwMTdiMWIwODIiLCJhdF9oYXNoIjoiajhEZmY4SDdyc0JfZ1hQbzRoWjBMQSIsImFjciI6IjEiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImlkX25hdGlvbmFsIjoiODc1MTA2MDYwNDciLCJuYW1lIjoiTWV1IE5vbWUiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhbmRlcnNvbi5hcmpvbmFAYWNhZC5wdWNycy5iciIsImdpdmVuX25hbWUiOiJNZXUiLCJmYW1pbHlfbmFtZSI6Ik5vbWUiLCJlbWFpbCI6ImFuZGVyc29uLmFyam9uYUBhY2FkLnB1Y3JzLmJyIn0.dd8b-7hU2dUoarYjiSkIZjh8JLbudW8r-eZsPYfRwK7XqENPvPFEjC5tgQMWBEM8jkDLJrfCf_ipLYkBkRVhjjWxP6VCYrS-rLDaXJrSD8CKbLnuALLxxw5gA_Fv_uV7PM7LmHWJ0ZYikOtb-fAEcp0hZHNSIkvcQRLKjKidbXmTFh71At76bUzKUTZi6jp6Dx-NLb8THnS3ILftvGTFYa-jNkecSEBlEBoV75yXjk7P8JLGJq9KYVUzL14kc-ThpX5PtZFavC3pt48bnYmjN1Wzmnden-HRgjVLCYJ-5SZIm0MZhOyHEKGJ0THak6oDTKNbhCkRFVfa4okh4t3yNQ","not-before-policy":0,"session_state":"74b21c28-46cb-412a-a879-6f7017b1b082","scope":"openid profile email"}')
  beforeEach(() => {
    vi.clearAllMocks()

    sessionStorage.setItem('gov', 'false')

    vi.mocked(getUserInfo).mockResolvedValue({
      firstName: 'Maria',
      lastName: 'Silva',
      email: 'maria@email.com',
      id: '123',
      idNational: '123',
      email_verified: true,
      name: '',
      preferred_username: ''
    })
    vi.mocked(isIdp).mockResolvedValueOnce(false)
  })

  it('monta corretamente e carrega os dados do usuário', async () => {
    const wrapper = mount(UserProfile)
    await flushPromises()

    await vi.waitFor(() => {
      expect(wrapper.text()).toContain('Maria')
      expect(wrapper.text()).toContain('Silva')
      expect(wrapper.text()).toContain('maria@email.com')
    })
  })

  it('ativa modo de edição e salva dados pessoais', async () => {
    const wrapper = mount(UserProfile)
    await flushPromises()

    await wrapper.find('[data-testid="edit-btn"]').trigger('click')

    await flushPromises()
    await nextTick()

    await wrapper.get('[data-testid="first-name"] input').setValue('João')
    await wrapper.get('[data-testid="last-name"] input').setValue('Silva')
    await wrapper.get('[data-testid="email"] input').setValue('joao@email.com')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    await vi.waitFor(() => {
      expect(updateUserInfo).toHaveBeenCalledWith('João', 'Silva', 'joao@email.com', 'joao@email.com', '123', '123')
      expect(mockTrigger).toHaveBeenCalledWith('messages.profileUpdated')
    })
  })

  it('exibe erro ao falhar atualização dos dados pessoais', async () => {
    vi.mocked(updateUserInfo).mockRejectedValueOnce(new Error('Falha'))

    const wrapper = mount(UserProfile)
    await flushPromises()

    await wrapper.find('[data-testid="edit-btn"]').trigger('click')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    await vi.waitFor(() => {
      expect(mockTrigger).toHaveBeenCalledWith('messages.profileUpdateError', 'error')
    })
  })

it('salva nova senha com sucesso', async () => {
  const wrapper = mount(UserProfile)
  await flushPromises()

  await wrapper.find('[data-testid="collapse-card"]').trigger('click') // expande

  await flushPromises()
  await nextTick()

  await wrapper.get('[data-testid="new-password"] input').setValue('Senha@2024')
  await wrapper.get('[data-testid="confirm-password"] input').setValue('Senha@2024')
  await wrapper.findAll('form')[1].trigger('submit.prevent')
  await flushPromises()

  await vi.waitFor(() => {
    expect(updatePassword).toHaveBeenCalledWith('Senha@2024', '123')
    expect(mockTrigger).toHaveBeenCalledWith('messages.accessDataUpdated')
    expect(localStorage.getItem('keycloak-session')).contain('access_token')
  })
})

  it('exibe erro ao falhar atualização da senha', async () => {
    vi.mocked(updatePassword).mockRejectedValueOnce(new Error('Erro'))

    const wrapper = mount(UserProfile)
    await flushPromises()

    await wrapper.find('[data-testid="collapse-card"]').trigger('click') // expande
    await wrapper.get('[data-testid="new-password"] input').setValue('Senha@2024')
    await wrapper.get('[data-testid="confirm-password"] input').setValue('Senha@2024')
    await wrapper.findAll('form')[1].trigger('submit.prevent')
    await flushPromises()

    await vi.waitFor(() => {
      expect(mockTrigger).toHaveBeenCalledWith('messages.accessDataUpdateError', 'error')
    })
  })
})
