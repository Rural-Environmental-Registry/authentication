<script setup lang="ts">
defineProps<{
  fieldName: string;
  value: string;
  source: string;
  component: string;
}>();
</script>

<template>
  <tr class="text-sm border-b-1 border-gray-300">
    <td class="px-4 py-4">{{ fieldName }}</td>
    <td class="px-4 py-4 break-all">{{ value }}</td>
    <td class="px-4 py-4">{{ source }}</td>
    <td class="px-4 py-4">{{ component }}</td>
  </tr>
</template>
