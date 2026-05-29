import { defineStore } from 'pinia'

export const usePostStore = defineStore('post', {
  state: () => ({
    list: [],
    total: 0,
    page: 1,
    size: 10,
    keyword: '',
    boardId: null,
  }),
  actions: {
    setList({ list = [], total = 0 }) {
      this.list = list
      this.total = total
    },
    setFilters({ page, size, keyword, boardId }) {
      if (typeof page !== 'undefined') this.page = page
      if (typeof size !== 'undefined') this.size = size
      if (typeof keyword !== 'undefined') this.keyword = keyword
      if (typeof boardId !== 'undefined') this.boardId = boardId
    },
    resetFilters() {
      this.page = 1
      this.size = 10
      this.keyword = ''
      this.boardId = null
    },
  },
})
