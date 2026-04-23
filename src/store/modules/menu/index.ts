import { defineStore } from 'pinia';
import { ref } from 'vue';
import { fetchGetUserMenus } from '@/service/api';
import { SetupStoreId } from '@/enum';

export const useMenuStore = defineStore(SetupStoreId.Menu, () => {
  const menus = ref<Api.Menu.MenuItem[]>([]);
  const menuTree = ref<Api.Menu.MenuTree[]>([]);
  const loaded = ref(false);

  async function getUserMenus() {
    if (loaded.value) return;

    const { data, error } = await fetchGetUserMenus();
    if (!error) {
      menuTree.value = data || [];
      loaded.value = true;
    }
  }

  function resetStore() {
    menus.value = [];
    menuTree.value = [];
    loaded.value = false;
  }

  return {
    menus,
    menuTree,
    loaded,
    getUserMenus,
    resetStore
  };
});
