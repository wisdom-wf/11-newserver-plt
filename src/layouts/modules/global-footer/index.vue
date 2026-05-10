<script setup lang="ts">
import { useRouter } from 'vue-router';

defineOptions({
  name: 'GlobalFooter'
});

const router = useRouter();
</script>

<template>
  <DarkModeContainer class="h-full flex-center">
    <span class="text-[12px] text-[#9ca3af]">
      Copyright MIT © 2024 智慧居家养老服务管理平台
      <span class="mx-2 text-[#9ca3af]">|</span>
      <a
        href="javascript:;"
        class="text-[#1E3A5F] hover:text-[#2563eb] cursor-pointer"
        @click="router.push('/builtin/rd-report')"
      >
        研发报告
      </a>
    </span>
  </DarkModeContainer>
</template>

<style scoped></style>
