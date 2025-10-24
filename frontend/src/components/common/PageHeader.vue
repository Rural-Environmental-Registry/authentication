<script setup lang="ts">
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/common/select'
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { faGlobe, faUser, faChevronDown } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import DropdownMenu from './dropdown-menu/DropdownMenu.vue';
import DropdownMenuTrigger from './dropdown-menu/DropdownMenuTrigger.vue';
import AvatarImage from './avatar/AvatarImage.vue';
import AvatarFallback from './avatar/AvatarFallback.vue';
import DropdownMenuContent from './dropdown-menu/DropdownMenuContent.vue';
import DropdownMenuLabel from './dropdown-menu/DropdownMenuLabel.vue';
import DropdownMenuSeparator from './dropdown-menu/DropdownMenuSeparator.vue';
import DropdownMenuItem from './dropdown-menu/DropdownMenuItem.vue';
import Avatar from './avatar/Avatar.vue';

const mainFront = ref<string>(import.meta.env.VITE_FRONTEND_USR_URL as string);
const showDropdownMenu = ref(false)

const { locale } = useI18n()
const router = useRouter()
const route = useRoute()

onMounted(() => {
  console.log(locale.value)
  localStorage.setItem('language', locale.value)

  showDropdownMenu.value = route.name === 'home' || route.name === 'user-profile'
})

const selected = ref(locale.value)

watch(selected, (lang) => {
  locale.value = lang
  localStorage.setItem('language', lang)
})

const logout = async () => {
  if (route.name === 'home') {
    sessionStorage.removeItem('token');
    localStorage.setItem('keycloak-session', '')
    router.push({ name: 'admin-login' })
  } else {
    router.push({ name: 'logout' })
  }
}

</script>

<template>
  <div class="bg-white">
    <nav class="w-full !shadow-md">
      <div class="flex justify-between p-3">
        <a :href="mainFront">
          <div class="pr-2 pl-2">
            <img class="h-10 object-cover" src="@/assets/govbr-logo-large.png" alt="gov.br logo" />
          </div>
        </a>
        <div class="flex items-center gap-3 pr-4">
          <div class="">
            <Select v-model="selected">
              <SelectTrigger class="w-[200px] rounded !border-gray-600/80">
                <FontAwesomeIcon :icon="faGlobe" style="color: #A9A9A9" class="cursor-pointer" />
                <SelectValue :placeholder="{
                  'en-us': 'English (USA)',
                  'pt-br': 'Portuguese (Brazil)',
                  'es-es': 'Spanish (Spain)'
                }[selected]" />
              </SelectTrigger>
              <SelectContent class="bg-white rounded">
                <SelectItem class="p-1 !cursor-pointer hover:bg-gray-200/50" value="en-us">English (USA)</SelectItem>
                <SelectItem class="p-1 !cursor-pointer hover:bg-gray-200/50" value="pt-br">Portuguese (Brazil)
                </SelectItem>
                <SelectItem class="p-1 !cursor-pointer hover:bg-gray-200/50" value="es-es">Spanish (Spain)</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <DropdownMenu v-if="showDropdownMenu">
            <DropdownMenuTrigger>
              <div class="flex gap-2 items-center">
                <Avatar class="bg-[#fdfaef]">
                  <AvatarImage src="" alt="@unovue" />
                  <AvatarFallback>
                    <FontAwesomeIcon :icon="faUser" style="color: #baf2dd;" size="3x" class="max-h-[36px]" />
                  </AvatarFallback>
                </Avatar>
                <FontAwesomeIcon :icon="faChevronDown" style="color: #baf2dd;" size="sm" />
              </div>
            </DropdownMenuTrigger>
            <DropdownMenuContent class="bg-white">
              <DropdownMenuLabel>{{ $t("homepage.avatar.title") }}</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <!-- <RouterLink to="/profile">
                <DropdownMenuItem class="hover:bg-gray-200/50 cursor-pointer">
                  {{ $t("homepage.avatar.profile") }}
                </DropdownMenuItem>
              </RouterLink> -->
              <DropdownMenuItem data-testid="logout-btn" @click="logout()" class="hover:bg-gray-200/50 cursor-pointer">
                {{ $t("homepage.avatar.logout") }}
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </nav>
  </div>
</template>
