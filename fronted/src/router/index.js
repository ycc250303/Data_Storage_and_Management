import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/movies/complex'
  },
  {
    path: '/movies',
    name: 'Movies',
    component: () => import('@/views/movies/index.vue'),
    meta: { title: '电影查询' }
  },
  {
    path: '/movies/stats',
    name: 'MovieStats',
    component: () => import('@/views/movies/stats.vue'),
    meta: { title: '电影统计' }
  },
  {
    path: '/movies/time',
    name: 'MovieTime',
    component: () => import('@/views/movies/timeQuery.vue'),
    meta: { title: '按时间查询' }
  },
  {
    path: '/movies/genre',
    name: 'MovieGenre',
    component: () => import('@/views/movies/genre.vue'),
    meta: { title: '按类别查询' }
  },
  {
    path: '/movies/complex',
    name: 'MovieComplex',
    component: () => import('@/views/movies/complexQuery.vue'),
    meta: { title: '组合查询' }
  },
  {
    path: '/movies/compare',
    name: 'MovieCompare',
    component: () => import('@/views/movies/compare.vue'),
    meta: { title: '性能对比' }
  },
  {
    path: '/movies/director',
    name: 'MovieDirector',
    component: () => import('@/views/movies/director.vue'),
    meta: { title: '按导演查询' }
  },
  {
    path: '/movies/actor',
    name: 'MovieActor',
    component: () => import('@/views/movies/actor.vue'),
    meta: { title: '按演员查询' }
  },
  {
    path: '/movies/userrating',
    name: 'MovieUserRating',
    component: () => import('@/views/movies/userRatings.vue'),
    meta: { title: '按用户评分查询' }
  },
  {
    path: '/movies/relation',
    name: 'MovieRelation',
    component: () => import('@/views/movies/relation.vue'),
    meta: { title: '演员/导演关系查询' }
  },
  {
    path: '/movies/:id',
    name: 'MovieDetail',
    component: () => import('@/views/movies/detail.vue'),
    props: true,
    meta: { title: '电影详情' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router


