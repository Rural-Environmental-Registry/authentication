<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  total: number
  currentPage: number
  pageSize: number
  pageSizes?: number[]
}>()

const emit = defineEmits<{
  (e: 'update:currentPage', value: number): void
  (e: 'update:pageSize', value: number): void
}>()

const { t } = useI18n()
const pageSizes = props.pageSizes || [5, 10, 20, 30]

const totalPages = computed(() =>
  Math.max(Math.ceil(props.total / props.pageSize), 1)
)

const firstItem = computed(() =>
  props.total === 0 ? 0 : (props.currentPage - 1) * props.pageSize + 1
)

const lastItem = computed(() =>
  Math.min(props.currentPage * props.pageSize, props.total)
)

function prevPage() {
  if (props.currentPage > 1) emit('update:currentPage', props.currentPage - 1)
}

function nextPage() {
  if (props.currentPage < totalPages.value)
    emit('update:currentPage', props.currentPage + 1)
}
</script>

<template>
  <div class="d-flex justify-space-between align-center px-4 pt-4 pb-2 text-sm">

    <div class="flex items-center">
      <div class="d-flex align-center gap-2 border-r-1 border-gray-300 mr-4">
        <span class="mr-2">{{ t('pagination.show') }}</span>
        <v-select :items="pageSizes" :model-value="props.pageSize" variant="solo" density="compact" hide-details
          style="max-width: 100px" @update:model-value="val => emit('update:pageSize', val)" flat />
      </div>

      <div class="text-center flex-grow-1">
        {{ firstItem }}–{{ lastItem }} {{ t('pagination.of') }} {{ props.total }} {{ t('pagination.items') }}
      </div>
    </div>


    <div class="d-flex align-center gap-2">
      <div class="flex items-center border-gray-300 border-r-1 mr-2">
        <span class="mr-2">{{ t('pagination.page') }}</span>
        <v-select :items="Array.from({ length: totalPages }, (_, i) => i + 1)" :model-value="props.currentPage"
          variant="solo" flat density="compact" hide-details style="max-width: 80px"
          @update:model-value="val => emit('update:currentPage', val)" />
      </div>

      <v-icon icon="mdi-chevron-left" color="success" :disabled="props.currentPage <= 1" @click="prevPage"
        class="cursor-pointer" data-testid="arrow-left" />
      <v-icon icon="mdi-chevron-right" color="success" :disabled="props.currentPage >= totalPages" @click="nextPage"
        class="cursor-pointer" data-testid="arrow-right" />
    </div>
  </div>
</template>
