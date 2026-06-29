import { useEffect, useMemo, useState } from 'react'
import { Link, NavLink, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom'
import {
  ArrowRight, BookOpen, Box, Check, ChevronLeft, ChevronRight, CircleUserRound,
  Clock3, Edit3, Heart, LayoutDashboard, LogOut, Menu, MessageCircleMore,
  PackageCheck, Plus, Search, ShieldCheck, ShoppingBag, Sparkles, UploadCloud,
  UserRound, Users, X, XCircle, Zap
} from 'lucide-react'
import { api, session } from './api'

const itemStatus = { 1: '可交换', 2: '交换中', 3: '已换出', 4: '已下架' }
const orderStatus = { 0: '待确认', 1: '交换中', 2: '已拒绝', 3: '已完成', 4: '已取消' }
const categoryEmoji = ['⌁', '◫', '✦', '◌', '⌂', '◇']
const recommendSourceMeta = {
  llm: { label: 'AI 智能推荐', kicker: 'POWERED BY GEMINI', title: 'AI 为你挑选', tone: 'llm' },
  hot: { label: '热门推荐', kicker: 'HOT PICKS', title: '大家都在看', tone: 'hot' },
  fallback: { label: '个性推荐', kicker: 'JUST FOR YOU', title: '今天，遇见这些', tone: 'fallback' },
}

function imageOf(item) {
  const value = item?.images?.split(',').find(Boolean)
  return value || ''
}

function formatDate(value) {
  if (!value) return '刚刚'
  return new Intl.DateTimeFormat('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
}

function App() {
  const [user, setUser] = useState(session.user())
  const [authOpen, setAuthOpen] = useState(false)
  const [toast, setToast] = useState(null)
  const [mobileOpen, setMobileOpen] = useState(false)
  const location = useLocation()

  useEffect(() => setMobileOpen(false), [location.pathname])
  useEffect(() => {
    if (!toast) return
    const timer = setTimeout(() => setToast(null), 2600)
    return () => clearTimeout(timer)
  }, [toast])

  const notify = (message, type = 'success') => setToast({ message, type })
  const requireAuth = (callback) => user ? callback?.() : setAuthOpen(true)

  return (
    <div className="app-shell">
      <Header user={user} onAuth={() => setAuthOpen(true)} onLogout={() => { session.clear(); setUser(null); notify('已安全退出') }} mobileOpen={mobileOpen} setMobileOpen={setMobileOpen} />
      <main>
        <Routes>
          <Route path="/" element={<Home user={user} />} />
          <Route path="/market" element={<Market />} />
          <Route path="/item/:id" element={<ItemDetail user={user} requireAuth={requireAuth} notify={notify} />} />
          <Route path="/publish" element={<Protected user={user} openAuth={() => setAuthOpen(true)}><Publish notify={notify} /></Protected>} />
          <Route path="/favorites" element={<Protected user={user} openAuth={() => setAuthOpen(true)}><Favorites notify={notify} /></Protected>} />
          <Route path="/orders" element={<Protected user={user} openAuth={() => setAuthOpen(true)}><Orders user={user} notify={notify} /></Protected>} />
          <Route path="/profile" element={<Protected user={user} openAuth={() => setAuthOpen(true)}><Profile user={user} setUser={setUser} notify={notify} /></Protected>} />
          <Route path="/admin" element={<Protected user={user} admin openAuth={() => setAuthOpen(true)}><Admin notify={notify} /></Protected>} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
      <Footer />
      {authOpen && <AuthModal onClose={() => setAuthOpen(false)} onLogin={(data) => { session.save(data); setUser(data.user); setAuthOpen(false); notify(`欢迎回来，${data.user.nickname || data.user.username}`) }} notify={notify} />}
      {toast && <div className={`toast ${toast.type}`}><span>{toast.type === 'success' ? <Check size={16} /> : <XCircle size={16} />}</span>{toast.message}</div>}
    </div>
  )
}

function Header({ user, onAuth, onLogout, mobileOpen, setMobileOpen }) {
  return <header className="site-header">
    <div className="nav-wrap">
      <Link className="brand" to="/"><span className="brand-mark"><Sparkles size={20} /></span><span>拾光<em>交换所</em></span></Link>
      <nav className={mobileOpen ? 'open' : ''}>
        <NavLink to="/">首页</NavLink><NavLink to="/market">发现好物</NavLink>
        {user && <NavLink to="/favorites">我的关注</NavLink>}
        {user && <NavLink to="/orders">交换记录</NavLink>}
        {user?.role === 1 && <NavLink to="/admin">管理台</NavLink>}
      </nav>
      <div className="nav-actions">
        {user ? <>
          <Link to="/publish" className="button button-dark button-small"><Plus size={16} />发布物品</Link>
          <div className="user-menu">
            <Link to="/profile" className="avatar">{(user.nickname || user.username || '我').slice(0, 1)}</Link>
            <div className="user-pop"><strong>{user.nickname || user.username}</strong><span>{user.role === 1 ? '平台管理员' : '交换会员'}</span><Link to="/profile"><UserRound size={15} />个人中心</Link><button onClick={onLogout}><LogOut size={15} />退出登录</button></div>
          </div>
        </> : <button className="button button-dark button-small" onClick={onAuth}>登录 / 注册</button>}
        <button className="mobile-menu" onClick={() => setMobileOpen(!mobileOpen)}>{mobileOpen ? <X /> : <Menu />}</button>
      </div>
    </div>
  </header>
}

function Home({ user }) {
  const [recommend, setRecommend] = useState({ records: [], source: '', reason: '' })
  const [categories, setCategories] = useState([])
  const [recommendLoading, setRecommendLoading] = useState(true)
  const [keyword, setKeyword] = useState('')
  const navigate = useNavigate()
  useEffect(() => {
    setRecommendLoading(true)
    Promise.all([api.recommend(), api.categories()])
      .then(([r, c]) => {
        setRecommend({ records: r.records || [], source: r.source || 'hot', reason: r.reason || '' })
        setCategories(c || [])
      })
      .catch(() => setRecommend({ records: [], source: '', reason: '' }))
      .finally(() => setRecommendLoading(false))
  }, [user?.id])
  const recommendMeta = recommendSourceMeta[recommend.source] || recommendSourceMeta.fallback
  const search = (e) => { e.preventDefault(); navigate(`/market?keyword=${encodeURIComponent(keyword)}`) }
  return <>
    <section className="hero">
      <div className="hero-copy">
        <span className="eyebrow"><Zap size={14} />让闲置重新发光</span>
        <h1>交换不需要价格，<br /><i>只需要刚刚好。</i></h1>
        <p>把搁置的物品交给真正需要的人，也许你寻找的惊喜，正躺在另一位同学的书桌上。</p>
        <form className="hero-search" onSubmit={search}><Search size={20} /><input value={keyword} onChange={e => setKeyword(e.target.value)} placeholder="搜搜相机、教材、耳机……" /><button>去发现</button></form>
        <div className="hero-facts"><span><b>0</b> 元交易</span><span><b>∞</b> 种可能</span><span><b>校内</b> 面交</span></div>
      </div>
      <div className="hero-visual">
        <div className="visual-card card-a"><div className="mock-photo camera">◉</div><span>胶片相机</span><small>想换一本摄影集</small></div>
        <div className="visual-card card-b"><div className="mock-photo books">BOOKS</div><span>专业课教材</span><small>学长的通关秘籍</small></div>
        <div className="orbit orbit-one">↗</div><div className="orbit orbit-two">♡</div>
        <div className="hero-note">“旧物不是终点，<br />是下一段故事的开场。”</div>
      </div>
    </section>
    <section className="marquee"><div>交换 · 分享 · 循环 · 遇见 · 让每件物品继续被喜欢 · 交换 · 分享 · 循环 · 遇见</div></section>
    <section className="section">
      <div className="section-head"><div><span className="kicker">EXPLORE BY CATEGORY</span><h2>从兴趣开始逛</h2></div><Link to="/market">查看全部 <ArrowRight size={17} /></Link></div>
      <div className="category-grid">
        {categories.slice(0, 6).map((c, i) => <Link key={c.categoryId} to={`/market?categoryId=${c.categoryId}`} className={`category-card tone-${i % 4}`}><span className="category-icon">{categoryEmoji[i]}</span><div><strong>{c.name}</strong><small>去淘一淘</small></div><ArrowRight size={18} /></Link>)}
        {!categories.length && [1,2,3,4].map((x, i) => <div className="category-card skeleton" key={x}><span /><div /></div>)}
      </div>
    </section>
    <section className="section featured">
      <div className="section-head">
        <div>
          <span className="kicker">{recommendMeta.kicker}</span>
          <h2>{recommendMeta.title}</h2>
        </div>
        <Link to="/market">继续逛逛 <ArrowRight size={17} /></Link>
      </div>
      <RecommendBanner source={recommend.source} reason={recommend.reason} label={recommendMeta.label} tone={recommendMeta.tone} loading={recommendLoading} loggedIn={!!user} />
      {recommendLoading ? <CardSkeleton /> : <div className="item-grid">{recommend.records.map(item => <ItemCard key={item.itemId} item={item} aiPick={recommend.source === 'llm'} />)}{!recommend.records.length && <Empty text="还没有上架的物品，来做第一个分享者吧" />}</div>}
    </section>
    <section className="story-strip">
      <div><span className="story-number">01</span><h3>发布一件闲置</h3><p>拍张照片，告诉大家它的故事和你的交换愿望。</p></div>
      <div><span className="story-number">02</span><h3>遇见心仪好物</h3><p>收藏、沟通，拿出你愿意交换的物品发出邀请。</p></div>
      <div><span className="story-number">03</span><h3>完成一次交换</h3><p>校内见面，双方确认，让好东西继续流动。</p></div>
    </section>
  </>
}

function Market() {
  const location = useLocation(), navigate = useNavigate()
  const query = useMemo(() => new URLSearchParams(location.search), [location.search])
  const [categories, setCategories] = useState([])
  const [data, setData] = useState({ records: [], total: 0, current: 1 })
  const [loading, setLoading] = useState(true)
  const [keyword, setKeyword] = useState(query.get('keyword') || '')
  const categoryId = query.get('categoryId') || '', page = Number(query.get('page') || 1)
  useEffect(() => { api.categories().then(setCategories).catch(() => {}) }, [])
  useEffect(() => { setLoading(true); api.items({ page, size: 8, categoryId, keyword: query.get('keyword') || '' }).then(setData).finally(() => setLoading(false)) }, [location.search])
  const setQuery = (key, value) => { const q = new URLSearchParams(location.search); value ? q.set(key, value) : q.delete(key); if (key !== 'page') q.delete('page'); navigate(`/market?${q}`) }
  return <div className="page-wrap">
    <div className="market-heading"><span className="eyebrow">CAMPUS TREASURE HUNT</span><h1>发现好物</h1><p>没有标价，只有彼此刚好需要。</p></div>
    <div className="filter-bar">
      <div className="category-tabs"><button className={!categoryId ? 'active' : ''} onClick={() => setQuery('categoryId', '')}>全部</button>{categories.map(c => <button className={String(c.categoryId) === categoryId ? 'active' : ''} key={c.categoryId} onClick={() => setQuery('categoryId', c.categoryId)}>{c.name}</button>)}</div>
      <form onSubmit={e => { e.preventDefault(); setQuery('keyword', keyword) }}><Search size={17} /><input value={keyword} onChange={e => setKeyword(e.target.value)} placeholder="搜索好物" /></form>
    </div>
    <div className="result-meta"><span>找到 <b>{data.total}</b> 件正在等待新主人的物品</span></div>
    {loading ? <CardSkeleton /> : data.records?.length ? <div className="item-grid market-grid">{data.records.map(item => <ItemCard key={item.itemId} item={item} />)}</div> : <Empty text="这里暂时空空的，换个关键词试试" />}
    {data.total > 8 && <Pagination current={page} total={Math.ceil(data.total / 8)} onChange={p => setQuery('page', p)} />}
  </div>
}

function RecommendBanner({ source, reason, label, tone, loading, loggedIn }) {
  if (loading) return <div className="recommend-banner skeleton"><span /><p /></div>
  if (!source) return null
  return <div className={`recommend-banner tone-${tone}`}>
    <div className="recommend-banner-main">
      <span className="recommend-badge">{source === 'llm' ? <Sparkles size={15} /> : source === 'hot' ? <Zap size={15} /> : <CircleUserRound size={15} />}{label}</span>
      {reason && <p>{reason}</p>}
    </div>
    {source === 'llm' && <span className="recommend-engine">Gemini</span>}
    {source === 'fallback' && loggedIn && <span className="recommend-engine muted">规则引擎</span>}
  </div>
}

function ItemCard({ item, aiPick = false }) {
  return <Link className="item-card" to={`/item/${item.itemId}`}>
    <div className="item-image">{imageOf(item) ? <img src={imageOf(item)} alt={item.title} /> : <div className="placeholder-art"><Box size={34} /><span>等待一张好看的照片</span></div>}{aiPick && <span className="ai-pick"><Sparkles size={12} />AI 精选</span>}<span className={`status-pill status-${item.status}`}>{itemStatus[item.status]}</span></div>
    <div className="item-info"><div className="item-time">{formatDate(item.updateTime)}</div><h3>{item.title}</h3><p>{item.exchangeWish || '期待一次有趣的交换'}</p><span className="view-link">看看它 <ArrowRight size={15} /></span></div>
  </Link>
}

function ItemDetail({ user, requireAuth, notify }) {
  const { id } = useParams(), navigate = useNavigate()
  const [item, setItem] = useState(null), [publisher, setPublisher] = useState(null), [mine, setMine] = useState([])
  const [exchangeOpen, setExchangeOpen] = useState(false), [favorite, setFavorite] = useState(false)
  useEffect(() => { api.item(id).then(setItem).catch(e => notify(e.message, 'error')); api.publisher(id).then(setPublisher).catch(() => {}) }, [id])
  useEffect(() => { if (user) Promise.all([api.myItems(1), api.favorites()]).then(([m, f]) => { setMine(m); setFavorite(f.some(x => String(x.itemId) === id)) }).catch(() => {}) }, [user, id])
  if (!item) return <div className="page-wrap"><CardSkeleton /></div>
  const toggleFavorite = () => requireAuth(async () => { try { favorite ? await api.unfavorite(id) : await api.favorite(id); setFavorite(!favorite); notify(favorite ? '已取消关注' : '已加入关注') } catch (e) { notify(e.message, 'error') } })
  return <div className="page-wrap detail-page">
    <button className="back-link" onClick={() => navigate(-1)}><ChevronLeft size={17} />返回</button>
    <div className="detail-layout">
      <div className="detail-image">{imageOf(item) ? <img src={imageOf(item)} alt={item.title} /> : <div className="placeholder-art large"><Box size={64} /><span>这件好物还没有照片</span></div>}</div>
      <div className="detail-copy">
        <div className="detail-meta"><span className={`status-pill status-${item.status}`}>{itemStatus[item.status]}</span><span>更新于 {formatDate(item.updateTime)}</span></div>
        <h1>{item.title}</h1>
        <div className="wish-box"><span>TA 想换</span><strong>{item.exchangeWish}</strong></div>
        <div className="description"><h3>关于这件物品</h3><p>{item.description || '发布者暂时没有留下更多描述。'}</p></div>
        {publisher && <div className="publisher"><div className="avatar large-avatar">{publisher.nickname?.slice(0, 1) || '友'}</div><div><span>来自</span><strong>{publisher.nickname}</strong><p>{publisher.profile || '愿每一次交换，都能遇见同频的人。'}</p>{publisher.contactVisible && <em>联系方式：{publisher.contactInfo}</em>}</div></div>}
        <div className="detail-actions">
          {user?.id !== item.userId && item.status === 1 && <button className="button button-accent" onClick={() => requireAuth(() => setExchangeOpen(true))}><MessageCircleMore size={18} />发起交换</button>}
          <button className={`button button-outline ${favorite ? 'liked' : ''}`} onClick={toggleFavorite}><Heart size={18} fill={favorite ? 'currentColor' : 'none'} />{favorite ? '已关注' : '关注它'}</button>
        </div>
        {!publisher?.contactVisible && <div className="privacy-note"><ShieldCheck size={16} />为保护隐私，双方同意交换后将显示联系方式</div>}
      </div>
    </div>
    {exchangeOpen && <ExchangeModal item={item} mine={mine} onClose={() => setExchangeOpen(false)} notify={notify} />}
  </div>
}

function ExchangeModal({ item, mine, onClose, notify }) {
  const [offerItemId, setOffer] = useState(mine[0]?.itemId || ''), [remark, setRemark] = useState(''), [busy, setBusy] = useState(false)
  const submit = async (e) => { e.preventDefault(); if (!offerItemId) return notify('请先发布一件可交换物品', 'error'); setBusy(true); try { await api.createOrder({ offerItemId: Number(offerItemId), targetItemId: item.itemId, remark }); notify('交换邀请已经送达'); onClose() } catch (e) { notify(e.message, 'error') } finally { setBusy(false) } }
  return <Modal title="发起一次交换" subtitle={`你想用哪件物品交换「${item.title}」？`} onClose={onClose}><form className="stack-form" onSubmit={submit}><label>我的交换物品<select value={offerItemId} onChange={e => setOffer(e.target.value)}><option value="">请选择一件在架物品</option>{mine.map(x => <option key={x.itemId} value={x.itemId}>{x.title}</option>)}</select></label><label>想对 TA 说<textarea rows="4" value={remark} onChange={e => setRemark(e.target.value)} placeholder="介绍一下你的物品，或约定方便的时间……" /></label><button className="button button-accent full" disabled={busy}>{busy ? '正在发送…' : '确认发出邀请'}</button></form></Modal>
}

function Publish({ notify }) {
  const [categories, setCategories] = useState([]), [images, setImages] = useState([]), [busy, setBusy] = useState(false)
  const [form, setForm] = useState({ title: '', categoryId: '', description: '', exchangeWish: '' })
  const navigate = useNavigate()
  useEffect(() => { api.categories().then(setCategories).catch(() => {}) }, [])
  const upload = async (files) => {
    const selected = [...files].slice(0, 5 - images.length)
    for (const file of selected) {
      const body = new FormData(); body.append('file', file)
      try { const url = await api.upload(body); setImages(prev => [...prev, url]) } catch (e) { notify(e.message, 'error') }
    }
  }
  const submit = async (e) => { e.preventDefault(); setBusy(true); try { await api.publish({ ...form, categoryId: Number(form.categoryId), images: images.join(',') }); notify('物品发布成功，等待有缘人'); navigate('/profile') } catch (e) { notify(e.message, 'error') } finally { setBusy(false) } }
  return <div className="page-wrap narrow-page">
    <div className="page-title"><span className="eyebrow">SHARE SOMETHING GOOD</span><h1>发布一件闲置</h1><p>认真写下它的故事，更容易遇见合适的新主人。</p></div>
    <form className="paper-form" onSubmit={submit}>
      <div className="form-section"><span className="form-index">01</span><div><h2>先来几张照片</h2><p>最多 5 张，仅支持 JPG / PNG，单张不超过 2MB。</p><div className="upload-grid">{images.map((src, i) => <div className="upload-preview" key={src}><img src={src} /><button type="button" onClick={() => setImages(images.filter((_, x) => x !== i))}><X size={15} /></button></div>)}{images.length < 5 && <label className="upload-box"><UploadCloud size={26} /><span>上传照片</span><input type="file" accept=".jpg,.jpeg,.png" multiple onChange={e => upload(e.target.files)} /></label>}</div></div></div>
      <div className="form-section"><span className="form-index">02</span><div className="form-fields"><h2>介绍一下它</h2><label>物品名称<input required maxLength="128" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} placeholder="例如：九成新拍立得相机" /></label><label>物品分类<select required value={form.categoryId} onChange={e => setForm({ ...form, categoryId: e.target.value })}><option value="">请选择分类</option>{categories.map(c => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}</select></label><label>详细描述<textarea rows="5" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="成色如何？有哪些使用痕迹？配件是否齐全？" /></label></div></div>
      <div className="form-section"><span className="form-index">03</span><div className="form-fields"><h2>你期待换到什么？</h2><label>交换愿望<textarea required rows="3" value={form.exchangeWish} onChange={e => setForm({ ...form, exchangeWish: e.target.value })} placeholder="例如：想换一本摄影集，也接受其他有趣的小物件" /></label><button className="button button-accent publish-button" disabled={busy}><Sparkles size={18} />{busy ? '发布中…' : '确认发布'}</button></div></div>
    </form>
  </div>
}

function Favorites({ notify }) {
  const [items, setItems] = useState([]), [loading, setLoading] = useState(true)
  const load = () => api.favorites().then(setItems).catch(e => notify(e.message, 'error')).finally(() => setLoading(false))
  useEffect(() => { load() }, [])
  const remove = async (id) => { try { await api.unfavorite(id); setItems(items.filter(x => x.itemId !== id)); notify('已取消关注') } catch (e) { notify(e.message, 'error') } }
  return <div className="page-wrap"><div className="page-title left"><span className="eyebrow">SAVED FOR LATER</span><h1>我的关注</h1><p>把心动先放在这里，等一个合适的交换时机。</p></div>{loading ? <CardSkeleton /> : items.length ? <div className="item-grid market-grid">{items.map(item => <div className="favorite-wrap" key={item.itemId}><ItemCard item={item} /><button onClick={() => remove(item.itemId)}><Heart size={17} fill="currentColor" />取消关注</button></div>)}</div> : <Empty text="还没有关注的物品，去市集逛逛吧" action={<Link className="button button-dark" to="/market">发现好物</Link>} />}</div>
}

function Orders({ user, notify }) {
  const [orders, setOrders] = useState([]), [filter, setFilter] = useState('all')
  const load = () => api.orders().then(setOrders).catch(e => notify(e.message, 'error'))
  useEffect(() => { load() }, [])
  const action = async (id, type) => { try { const msg = await api.orderAction(id, type); notify(msg); load() } catch (e) { notify(e.message, 'error') } }
  const visible = orders.filter(o => filter === 'all' || String(o.status) === filter)
  return <div className="page-wrap"><div className="page-title left"><span className="eyebrow">MY EXCHANGES</span><h1>交换记录</h1><p>每一次物品流动，都从一句“你好”开始。</p></div>
    <div className="order-tabs">{[['all','全部'],['0','待确认'],['1','交换中'],['3','已完成']].map(([v,n]) => <button className={filter === v ? 'active' : ''} onClick={() => setFilter(v)} key={v}>{n}</button>)}</div>
    <div className="order-list">{visible.map(o => {
      const received = o.targetId === user.id
      const myConfirmed = received ? o.targetConfirmed : o.initiatorConfirmed
      return <article className="order-card" key={o.orderId}>
        <div className="order-top"><span>交换单 #{o.orderId}</span><time><Clock3 size={14} />{formatDate(o.createTime)}</time><b className={`order-status order-${o.status}`}>{orderStatus[o.status]}</b></div>
        <div className="exchange-flow">
          <MiniItem image={o.offerItemImage} title={o.offerItemTitle} owner={received ? o.initiatorNickname : '我的物品'} />
          <div className="swap-mark"><span>⇄</span><small>交换</small></div>
          <MiniItem image={o.targetItemImage} title={o.targetItemTitle} owner={received ? '我的物品' : o.targetNickname} />
        </div>
        {o.remark && <p className="order-remark">“{o.remark}”</p>}
        <div className="order-actions"><span>{received ? `${o.initiatorNickname} 向你发起了交换` : `发给 ${o.targetNickname}`}</span><div>
          {o.status === 0 && received && <><button className="button button-outline button-small" onClick={() => action(o.orderId, 'REJECT')}>婉拒</button><button className="button button-dark button-small" onClick={() => action(o.orderId, 'ACCEPT')}>同意交换</button></>}
          {o.status === 1 && <><button className="button button-outline button-small" onClick={() => action(o.orderId, 'CANCEL')}>取消交换</button><button disabled={myConfirmed} className="button button-accent button-small" onClick={() => action(o.orderId, 'FINISH')}>{myConfirmed ? '已确认，等待对方' : '确认完成'}</button></>}
        </div></div>
      </article>
    })}{!visible.length && <Empty text="这个分类下暂时没有交换记录" />}</div>
  </div>
}

function MiniItem({ image, title, owner }) { return <div className="mini-item"><div>{image ? <img src={image} /> : <Box size={24} />}</div><strong>{title || '物品已下架'}</strong><span>{owner}</span></div> }

function Profile({ user, setUser, notify }) {
  const [profile, setProfile] = useState(user), [items, setItems] = useState([]), [editing, setEditing] = useState(false)
  const load = () => Promise.all([api.profile(), api.myItems()]).then(([p, i]) => { setProfile(p); setItems(i) }).catch(e => notify(e.message, 'error'))
  useEffect(() => { load() }, [])
  const status = async (id, value) => { try { await api.itemStatus(id, value); notify('物品状态已更新'); load() } catch (e) { notify(e.message, 'error') } }
  return <div className="page-wrap profile-page">
    <section className="profile-banner"><div className="profile-avatar">{(profile.nickname || profile.username).slice(0, 1)}</div><div><span>@{profile.username}</span><h1>{profile.nickname || '还没有昵称'}</h1><p>{profile.profile || '在这里写下你的交换宣言吧。'}</p></div><button className="button button-light" onClick={() => setEditing(true)}><Edit3 size={16} />编辑资料</button></section>
    <div className="profile-stats"><div><b>{items.length}</b><span>发布物品</span></div><div><b>{items.filter(x => x.status === 1).length}</b><span>正在交换</span></div><div><b>{items.filter(x => x.status === 3).length}</b><span>成功换出</span></div></div>
    <div className="section-head compact"><div><span className="kicker">MY SHELF</span><h2>我的物品架</h2></div><Link to="/publish" className="button button-dark button-small"><Plus size={16} />发布新物品</Link></div>
    <div className="my-item-list">{items.map(item => <div className="my-item" key={item.itemId}><Link to={`/item/${item.itemId}`} className="my-item-image">{imageOf(item) ? <img src={imageOf(item)} /> : <Box />}</Link><div><span className={`status-pill status-${item.status}`}>{itemStatus[item.status]}</span><h3>{item.title}</h3><p>{item.exchangeWish}</p></div><select value={item.status} onChange={e => status(item.itemId, Number(e.target.value))}><option value="1">上架</option><option value="4">下架</option>{item.status === 2 && <option value="2">交换中</option>}{item.status === 3 && <option value="3">已换出</option>}</select></div>)}{!items.length && <Empty text="物品架还是空的，发布第一件闲置吧" />}</div>
    {editing && <ProfileModal profile={profile} onClose={() => setEditing(false)} onSaved={(p) => { session.updateUser({ ...user, ...p }); setUser({ ...user, ...p }); setProfile({ ...profile, ...p }); setEditing(false); notify('个人资料已更新') }} notify={notify} />}
  </div>
}

function ProfileModal({ profile, onClose, onSaved, notify }) {
  const [form, setForm] = useState({ nickname: profile.nickname || '', contactInfo: profile.contactInfo || '', profile: profile.profile || '' })
  const submit = async (e) => { e.preventDefault(); try { await api.updateProfile(form); onSaved(form) } catch (e) { notify(e.message, 'error') } }
  return <Modal title="编辑个人资料" subtitle="完善联系方式后，才能发起交换。" onClose={onClose}><form className="stack-form" onSubmit={submit}><label>昵称<input required value={form.nickname} onChange={e => setForm({ ...form, nickname: e.target.value })} /></label><label>联系方式<input required value={form.contactInfo} onChange={e => setForm({ ...form, contactInfo: e.target.value })} placeholder="微信 / QQ / 手机号" /></label><label>个人简介<textarea rows="3" value={form.profile} onChange={e => setForm({ ...form, profile: e.target.value })} /></label><button className="button button-accent full">保存资料</button></form></Modal>
}

function Admin({ notify }) {
  const [tab, setTab] = useState('overview'), [stats, setStats] = useState({}), [users, setUsers] = useState([]), [categories, setCategories] = useState([]), [orders, setOrders] = useState([])
  const load = () => Promise.all([api.adminStats(), api.adminUsers(), api.adminCategories(), api.adminOrders()]).then(([s,u,c,o]) => { setStats(s); setUsers(u); setCategories(c); setOrders(o) }).catch(e => notify(e.message, 'error'))
  useEffect(() => { load() }, [])
  const userStatus = async (id, status) => { try { await api.adminUserStatus(id, status); notify('用户状态已更新'); load() } catch(e) { notify(e.message, 'error') } }
  const addCategory = async () => { const name = window.prompt('新分类名称'); if (!name) return; try { await api.addCategory({ name, parentId: 0, sort: categories.length + 1 }); notify('分类已添加'); load() } catch(e) { notify(e.message, 'error') } }
  const removeCategory = async (id) => { if (!window.confirm('确定删除这个分类吗？分类下物品会转移至“其他”。')) return; try { await api.deleteCategory(id); notify('分类已删除'); load() } catch(e) { notify(e.message, 'error') } }
  return <div className="admin-layout">
    <aside className="admin-side"><div><span>ADMIN SPACE</span><h2>拾光管理台</h2></div>{[['overview',LayoutDashboard,'数据概览'],['users',Users,'用户管理'],['categories',BookOpen,'分类管理'],['orders',PackageCheck,'订单流水']].map(([v,Icon,n]) => <button key={v} className={tab === v ? 'active' : ''} onClick={() => setTab(v)}><Icon size={18} />{n}</button>)}</aside>
    <section className="admin-content">
      {tab === 'overview' && <><div className="admin-title"><span>OVERVIEW</span><h1>今天也在认真流动</h1><p>平台关键数据一览。</p></div><div className="stat-grid"><Stat icon={Users} label="注册用户" value={stats.userCount || 0} color="coral" /><Stat icon={ShoppingBag} label="在架物品" value={stats.availableItemCount || 0} color="green" /><Stat icon={MessageCircleMore} label="进行中交换" value={stats.processingOrderCount || 0} color="blue" /></div><div className="admin-note"><Sparkles /><div><h3>让每一次交换都安心发生</h3><p>及时处理异常账号，维护清晰的分类，也别忘了看看那些正在发生的交换故事。</p></div></div></>}
      {tab === 'users' && <AdminTable title="用户管理" subtitle="查看账号状态并执行冻结或恢复。"><table><thead><tr><th>用户</th><th>角色</th><th>联系方式</th><th>注册时间</th><th>状态</th><th>操作</th></tr></thead><tbody>{users.map(u => <tr key={u.id}><td><b>{u.nickname || u.username}</b><small>@{u.username}</small></td><td>{u.role === 1 ? '管理员' : '普通会员'}</td><td>{u.contactInfo || '未填写'}</td><td>{formatDate(u.createTime)}</td><td><span className={`tiny-status ${u.status ? 'good' : 'off'}`}>{u.status ? '正常' : '已冻结'}</span></td><td>{u.role !== 1 && <button className="text-button" onClick={() => userStatus(u.id, u.status ? 0 : 1)}>{u.status ? '冻结' : '恢复'}</button>}</td></tr>)}</tbody></table></AdminTable>}
      {tab === 'categories' && <AdminTable title="分类管理" subtitle="维护前台物品分类。顶部分类会优先展示。" action={<button className="button button-dark button-small" onClick={addCategory}><Plus size={15} />新增分类</button>}><div className="category-admin-list">{categories.map((c,i) => <div key={c.categoryId}><span className="category-icon small">{categoryEmoji[i % categoryEmoji.length]}</span><div><b>{c.name}</b><small>ID {c.categoryId} · 排序 {c.sort}</small></div><button onClick={() => removeCategory(c.categoryId)}><X size={17} /></button></div>)}</div></AdminTable>}
      {tab === 'orders' && <AdminTable title="订单流水" subtitle="全站交换意向与状态记录。"><table><thead><tr><th>订单号</th><th>发起人 ID</th><th>接收人 ID</th><th>交换物品</th><th>状态</th><th>创建时间</th></tr></thead><tbody>{orders.map(o => <tr key={o.orderId}><td>#{o.orderId}</td><td>{o.initiatorId}</td><td>{o.targetId}</td><td>{o.offerItemId} ⇄ {o.targetItemId}</td><td><span className={`tiny-status order-${o.status}`}>{orderStatus[o.status]}</span></td><td>{formatDate(o.createTime)}</td></tr>)}</tbody></table></AdminTable>}
    </section>
  </div>
}

function Stat({ icon: Icon, label, value, color }) { return <div className={`stat-card ${color}`}><span><Icon size={22} /></span><div><b>{value}</b><small>{label}</small></div></div> }
function AdminTable({ title, subtitle, action, children }) { return <div><div className="admin-title row"><div><span>MANAGEMENT</span><h1>{title}</h1><p>{subtitle}</p></div>{action}</div><div className="admin-panel">{children}</div></div> }

function AuthModal({ onClose, onLogin, notify }) {
  const [mode, setMode] = useState('login'), [busy, setBusy] = useState(false)
  const [form, setForm] = useState({ username: '', password: '', nickname: '', contactInfo: '' })
  const submit = async (e) => { e.preventDefault(); setBusy(true); try { if (mode === 'register') { await api.register(form); notify('注册成功，请登录'); setMode('login') } else onLogin(await api.login(form)) } catch (e) { notify(e.message, 'error') } finally { setBusy(false) } }
  return <div className="modal-backdrop"><div className="auth-modal"><button className="modal-close" onClick={onClose}><X /></button><div className="auth-art"><div className="auth-symbol">⇄</div><h2>好东西，<br />值得继续被喜欢。</h2><p>欢迎来到没有标价的校园市集。</p></div><div className="auth-form"><div className="auth-tabs"><button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>登录</button><button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>注册</button></div><form className="stack-form" onSubmit={submit}>{mode === 'register' && <><label>昵称<input required value={form.nickname} onChange={e => setForm({...form,nickname:e.target.value})} placeholder="大家怎么称呼你？" /></label><label>联系方式<input value={form.contactInfo} onChange={e => setForm({...form,contactInfo:e.target.value})} placeholder="也可以登录后再填写" /></label></>}<label>账号<input required autoFocus value={form.username} onChange={e => setForm({...form,username:e.target.value})} placeholder="请输入用户名" /></label><label>密码<input required type="password" value={form.password} onChange={e => setForm({...form,password:e.target.value})} placeholder="请输入密码" /></label><button className="button button-accent full" disabled={busy}>{busy ? '请稍候…' : mode === 'login' ? '进入交换所' : '创建我的账号'}</button></form>{mode === 'login' && <p className="demo-tip">管理员体验账号：admin / admin123</p>}</div></div></div>
}

function Modal({ title, subtitle, onClose, children }) { return <div className="modal-backdrop"><div className="plain-modal"><button className="modal-close" onClick={onClose}><X /></button><div className="modal-title"><span className="eyebrow">EXCHANGE MOMENT</span><h2>{title}</h2><p>{subtitle}</p></div>{children}</div></div> }
function Protected({ user, admin, openAuth, children }) { useEffect(() => { if (!user) openAuth() }, [user]); if (!user) return <div className="page-wrap"><Empty text="登录后才能来到这里" /></div>; if (admin && user.role !== 1) return <div className="page-wrap"><Empty text="这里是管理员专属空间" /></div>; return children }
function Empty({ text, action }) { return <div className="empty"><div>⌁</div><h3>{text}</h3>{action}</div> }
function CardSkeleton() { return <div className="item-grid">{[1,2,3,4].map(x => <div className="item-card skeleton-card" key={x}><div /><span /><span /></div>)}</div> }
function Pagination({ current, total, onChange }) { return <div className="pagination"><button disabled={current <= 1} onClick={() => onChange(current - 1)}><ChevronLeft /></button><span>{current} / {total}</span><button disabled={current >= total} onClick={() => onChange(current + 1)}><ChevronRight /></button></div> }
function NotFound() { return <div className="page-wrap"><Empty text="这条路暂时没有好物" action={<Link className="button button-dark" to="/">回到首页</Link>} /></div> }
function Footer() { return <footer><Link className="brand footer-brand" to="/"><span className="brand-mark"><Sparkles size={18} /></span><span>拾光<em>交换所</em></span></Link><p>让物品流动，让故事继续。</p><span>© 2026 Campus Exchange</span></footer> }

export default App
