<script lang="ts" setup>
import { defineEmits } from 'vue'
import { useI18n } from 'vue-i18n'

const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()

defineProps<{
  modelValue: string
}>()
</script>

<template>
  <v-text-field :placeholder="t('search.placeholder')" :model-value="modelValue" density="comfortable"
    variant="solo-filled" hide-details class="bg-grey-lighten-4 rounded-lg" flat clearable
    @update:modelValue="emit('update:modelValue', $event)">
    <template #append-inner>
      <v-icon icon="mdi-magnify" class="text-teal-darken-2" />
    </template>
  </v-text-field>
</template>
