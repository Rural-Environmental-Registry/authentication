<script setup lang="ts">
import AdminTableRow from "./AdminTableRow.vue";
import { useI18n } from "vue-i18n";

const props = defineProps<{
  rows: {
    fieldName: string;
    value: string;
    source: string;
    component: string;
  }[];
}>();

const { t } = useI18n();
</script>

<template>
  <table class="min-w-full border-collapse">
    <thead class="!bg-green-200/75 text-left">
      <tr class="!text-green-900">
        <th class="!font-semibold !text-sm !px-4 !py-4 !text-green-900 !bg-green-200/75">{{ t('table.headers.field') }}</th>
        <th class="!font-semibold !text-sm !px-4 !py-4 !text-green-900 !bg-green-200/75">{{ t('table.headers.value') }}</th>
        <th class="!font-semibold !text-sm !px-4 !py-4 !text-green-900 !bg-green-200/75">{{ t('table.headers.source') }}</th>
        <th class="!font-semibold whitespace-nowrap !text-sm px-4 !py-4 !text-green-900 !bg-green-200/75">{{ t('table.headers.component') }}</th>
      </tr>
    </thead>
    <tbody>
      <AdminTableRow v-for="(row, index) in props.rows" :key="index" :fieldName="row.fieldName" :value="row.value"
        :source="row.source" :component="row.component" />
    </tbody>
  </table>
</template>
