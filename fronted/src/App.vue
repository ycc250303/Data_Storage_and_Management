<template>
  <div id="app-root">
    <header class="app-header">
      <div class="brand">
        <div class="logo">DW</div>
        <el-button type="text" plain class="icon-btn header-toggle" @click="toggleSidebar">☰</el-button>
        <div class="title">
          <div class="title-main">数据仓库可视化</div>
          <div class="title-sub">Movie Analytics</div>
        </div>
      </div>
      <div class="header-actions">
        <!-- removed global search per request -->
      </div>
    </header>
    <div class="app-body">
      <aside :class="['side-nav', { collapsed }]">
        <ul>
          <li>
            <router-link to="/movies/complex" class="nav-item" aria-label="组合查询">
              <span class="nav-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <path d="M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h7v7h-7z"/>
                </svg>
              </span>
              <span class="nav-text">组合查询</span>
            </router-link>
          </li>
          <li>
            <router-link to="/movies/relation" class="nav-item" aria-label="关系查询">
              <span class="nav-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2a2 2 0 1 0 0 4 2 2 0 0 0 0-4zM4 12a2 2 0 1 0 0 4 2 2 0 0 0 0-4zM20 12a2 2 0 1 0 0 4 2 2 0 0 0 0-4zM12 8v4M5 14l7-2 7 2" stroke="currentColor" stroke-width="1.2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
              <span class="nav-text">关系查询</span>
            </router-link>
          </li>
          <li>
            <router-link to="/movies/stats" class="nav-item" aria-label="电影统计">
              <span class="nav-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <rect x="3" y="10" width="3" height="8" rx="0.5" />
                  <rect x="9" y="6" width="3" height="12" rx="0.5" />
                  <rect x="15" y="3" width="3" height="15" rx="0.5" />
                </svg>
              </span>
              <span class="nav-text">电影统计</span>
            </router-link>
          </li>
        </ul>
        <div class="side-footer">© 学期项目</div>
      </aside>
      <main class="main-pane">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AppRoot',
  data() {
    return {
      collapsed: false
    }
  },
  methods: {
    toggleSidebar() {
      this.collapsed = !this.collapsed
      try {
        localStorage.setItem('sidebarCollapsed', this.collapsed ? '1' : '0')
      } catch (e) {}
    }
  },
  mounted() {
    try {
      const v = localStorage.getItem('sidebarCollapsed')
      this.collapsed = v === '1'
    } catch (e) {
      this.collapsed = false
    }
  }
}
</script>

<style>
.app-header {
  background: linear-gradient(90deg,#2d8cf0,#57a3f5);
  color: white;
  padding: 12px 24px;
  display:flex;
  align-items:center;
  justify-content:space-between;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
}
.brand { display:flex; align-items:center; gap:12px; }
.logo {
  width:44px; height:44px; border-radius:8px;
  background:rgba(255,255,255,0.15); color:#fff;
  display:flex; align-items:center; justify-content:center; font-weight:700;
  font-size:18px;
}
.title-main { font-size:18px; font-weight:700; }
.title-sub { font-size:12px; opacity:0.9; }
  .header-actions { display:flex; align-items:center; gap:12px; }
  .header-actions .global-search { display:none; }
  .header-toggle { margin-left:10px; color: rgba(255,255,255,0.95); border-radius:8px; padding:6px 8px; background: rgba(255,255,255,0.02); }
  .header-toggle:hover { background: rgba(255,255,255,0.06); }
.app-body { display:flex; min-height:calc(100vh - 68px); }
.side-nav { width:220px; background:#f7f9fc; padding:18px; box-shadow: 1px 0 0 rgba(0,0,0,0.03); display:flex; flex-direction:column; }
.main-pane { flex:1; padding:24px; background:#f0f2f5; min-height:calc(100vh - 68px); }
.side-nav ul { list-style:none; padding:0; margin:0; flex:1; }
.side-nav li { margin-bottom:12px; }
.side-nav .nav-item {
  display:block;
  padding:10px 12px;
  border-radius:8px;
  background: transparent;
  color:#2d6fb5;
  text-decoration:none;
  border:1px solid transparent;
  transition: all .18s ease;
  font-weight:600;
}
.side-nav .nav-item:hover {
  background: rgba(45,140,240,0.06);
  border-color: rgba(45,140,240,0.12);
  box-shadow: 0 4px 10px rgba(13,36,62,0.04);
}
.side-nav .router-link-active {
  background: linear-gradient(90deg,#e6f3ff,#f2fbff);
  border-color: rgba(45,140,240,0.18);
  color:#1b5fa8;
}
.side-footer { font-size:12px; color:#888; margin-top:10px; text-align:center; padding-top:12px; }
.nav-icon { display:inline-flex; align-items:center; justify-content:center; width:36px; height:36px; margin-right:10px; border-radius:8px; background:rgba(45,140,240,0.06); font-size:16px; }
.icon-btn { color: rgba(255,255,255,0.95); border-radius:8px; padding:6px 8px; background: rgba(255,255,255,0.06); }
.icon-btn:hover { background: rgba(255,255,255,0.12); }
.side-nav.collapsed { width:56px; min-width:56px; padding:10px 6px; }
.side-nav.collapsed .nav-item { text-align:center; padding:6px 4px; display:flex; align-items:center; justify-content:center; }
.side-nav.collapsed .nav-item .nav-text { display:none; }
.side-nav.collapsed .nav-icon { margin-right:0; transform: none; width:36px; height:36px; display:inline-flex; align-items:center; justify-content:center; }
.side-nav .nav-item { align-items:center; display:flex; gap:8px; }
.side-nav.collapsed .side-footer { display:none; }
</style>


