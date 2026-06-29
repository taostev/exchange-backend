const TOKEN_KEY = 'exchange_token'
const USER_KEY = 'exchange_user'

export const session = {
  token: () => localStorage.getItem(TOKEN_KEY),
  user: () => {
    try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null') } catch { return null }
  },
  save(data) {
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(data.user))
  },
  updateUser(user) { localStorage.setItem(USER_KEY, JSON.stringify(user)) },
  clear() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  },
}

export async function request(path, options = {}) {
  const headers = { ...(options.headers || {}) }
  const token = session.token()
  if (token) headers.Authorization = `Bearer ${token}`
  if (options.body && !(options.body instanceof FormData)) headers['Content-Type'] = 'application/json'

  const response = await fetch(path, { ...options, headers })
  let payload
  try { payload = await response.json() } catch { throw new Error('服务响应异常，请稍后重试') }
  if (!response.ok || payload.code !== 200) {
    if (payload.code === 401) session.clear()
    throw new Error(payload.msg || '请求失败')
  }
  return payload.data
}

export const api = {
  categories: () => request('/api/categories'),
  items: (params = {}) => request(`/api/items/page?${new URLSearchParams(Object.entries(params).filter(([, v]) => v !== '' && v != null))}`),
  item: (id) => request(`/api/items/${id}`),
  publisher: (id) => request(`/api/items/${id}/publisher`),
  recommend: () => request('/api/recommend'),
  login: (body) => request('/api/user/login', { method: 'POST', body: JSON.stringify(body) }),
  register: (body) => request('/api/user/register', { method: 'POST', body: JSON.stringify(body) }),
  checkUsername: (username) => request(`/api/user/check-username?username=${encodeURIComponent(username)}`),
  profile: () => request('/api/user/profile'),
  updateProfile: (body) => request('/api/user/profile', { method: 'PUT', body: JSON.stringify(body) }),
  publish: (body) => request('/api/items', { method: 'POST', body: JSON.stringify(body) }),
  upload: (body) => request('/api/items/upload', { method: 'POST', body }),
  myItems: (status = '') => request(`/api/items/mine${status ? `?status=${status}` : ''}`),
  itemStatus: (id, status) => request(`/api/items/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }) }),
  favorites: () => request('/api/favorites'),
  favorite: (id) => request(`/api/favorites/${id}`, { method: 'POST' }),
  unfavorite: (id) => request(`/api/favorites/${id}`, { method: 'DELETE' }),
  orders: () => request('/api/orders/mine'),
  createOrder: (body) => request('/api/orders/create', { method: 'POST', body: JSON.stringify(body) }),
  orderAction: (id, action) => request(`/api/orders/${id}/status`, { method: 'PUT', body: JSON.stringify({ action }) }),
  adminStats: () => request('/api/admin/stats'),
  adminUsers: () => request('/api/admin/users'),
  adminUserStatus: (id, status) => request(`/api/admin/users/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }) }),
  adminCategories: () => request('/api/admin/categories'),
  addCategory: (body) => request('/api/admin/categories', { method: 'POST', body: JSON.stringify(body) }),
  updateCategory: (id, body) => request(`/api/admin/categories/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  deleteCategory: (id) => request(`/api/admin/categories/${id}`, { method: 'DELETE' }),
  adminOrders: () => request('/api/admin/orders'),
  adminItemStatus: (id, status) => request(`/api/admin/items/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }) }),
}
