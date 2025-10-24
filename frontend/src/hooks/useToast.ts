import { ref } from 'vue'

const show = ref(false)
const message = ref('')
const color = ref<'success' | 'error' | 'warning' | 'info'>('success')

export function useToast() {
  const trigger = (msg: string, type: typeof color.value = 'success') => {
    message.value = msg
    color.value = type
    show.value = true
  }

  return {
    show,
    message,
    color,
    trigger
  }
}
