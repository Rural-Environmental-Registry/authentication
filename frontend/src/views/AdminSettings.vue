<script setup lang="ts">
import AdminCard from '@/components/admin-settings/AdminCard.vue';
import ConfigTable from '@/components/admin-settings/ConfigTable.vue';
import SearchSelect from '@/components/admin-settings/SearchSelect.vue';
import TablePaginator from '@/components/admin-settings/TablePaginator.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import { checkIsAdmin, isTokenExpired } from '@/helpers/token';
import { computed, onMounted, ref, type Ref } from 'vue';
import { useRouter } from 'vue-router';

import axios from 'axios';
import type { TableDataItem } from '@/types/table';
import { parseJsonToTableData } from '@/helpers/table';

const router = useRouter()
const search = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const data: Ref<TableDataItem[]> = ref([])

const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return data.value
    .filter(i => {
      return !search.value || i.fieldName.includes(search.value) || i.source.includes(search.value) || i.component.includes(search.value)
    })
    .slice(start, end)
})

onMounted(async () => {
  if (isTokenExpired() || !checkIsAdmin()) {
    sessionStorage.removeItem('token')
    localStorage.setItem('keycloak-session', '')
    router.push({ name: 'admin-login' })
  }

  axios.get(import.meta.env.VITE_FRONTEND_CONFIG_URL).then(res => {
    console.log('FRONT', res.data)
    data.value = [...data.value, ...parseJsonToTableData(res.data)]
  })

  axios.get(import.meta.env.VITE_BACKEND_CONFIG_URL).then(res => {
    console.log('BACK', res.data)
    data.value = [...data.value, ...parseJsonToTableData(res.data)]
  })

})

</script>

<template>
  <main>
    <PageHeader />

    <div class="my-16 max-w-[1200px] mx-auto">

      <AdminCard :title="$t('search.title')">
        <div class="px-4">
          <SearchSelect v-model="search" />
        </div>
      </AdminCard>

      <AdminCard :title="$t('table.title')">
        <ConfigTable :rows="paginatedItems" />
        <TablePaginator :total="data.length" :currentPage="currentPage" :pageSize="pageSize"
          @update:currentPage="val => currentPage = val" @update:pageSize="val => pageSize = val" />
      </AdminCard>
    </div>

  </main>
</template>
