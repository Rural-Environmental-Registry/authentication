<script setup lang="ts">
defineProps<{
  title?: string
}>()
</script>

<template>
  <div class="bg-white rounded-lg shadow-md px-8 py-6 mb-6">
    <div v-if="title" class="text-lg font-semibold text-gray-800 mb-8">
      {{ title }}
    </div>
    <slot />
  </div>
</template>

<style scoped>
/* shadow-md no Tailwind equivale a uma leve sombra como na imagem */
</style>
