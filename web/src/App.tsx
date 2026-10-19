import { useEffect, useMemo, useState } from 'react'
import {
  ArrowDownToLine,
  ArrowRight,
  Check,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  LogIn,
  LogOut,
  MapPin,
  Search,
  Sparkles,
  X,
} from 'lucide-react'
import {
  getErrorMessage,
  getLocalPokemonPage,
  getPokemon,
  getPokemonPage,
  login,
  register,
  syncPokemon,
  updateLocalPokemon,
  type AuthResponse,
  type EvolutionStage,
  type LocalPokemon,
  type PokemonDetail,
  type PokemonSummary,
} from './api'
import './App.css'

type View = 'discover' | 'collection'
type AuthMode = 'login' | 'register'
type PokemonEntry = PokemonSummary | LocalPokemon

const filters = ['All Pokémon', 'Starters', 'Electric', 'Water', 'Fire', 'Grass']
const starterIds = new Set([
  1, 4, 7, 152, 155, 158, 252, 255, 258, 387, 390, 393, 495, 498, 501,
  650, 653, 656, 722, 725, 728, 810, 813, 816, 906, 909, 912,
])

function capitalize(value: string) {
  return value.replace(/-/g, ' ').replace(/\b\w/g, (letter) => letter.toUpperCase())
}

function pokemonNumber(id: number) {
  return `#${String(id).padStart(3, '0')}`
}

function categoryClass(category: string) {
  const value = category.toLowerCase()
  if (value.includes('mouse') || value.includes('electric') || value.includes('volt')) return 'electric'
  if (value.includes('turtle') || value.includes('water') || value.includes('shell')) return 'water'
  if (value.includes('lizard') || value.includes('fire') || value.includes('flame')) return 'fire'
  if (value.includes('seed') || value.includes('grass') || value.includes('flower')) return 'grass'
  return 'neutral'
}

function isLocalPokemon(pokemon: PokemonSummary | PokemonDetail | LocalPokemon): pokemon is LocalPokemon {
  return 'syncedAt' in pokemon
}

function App() {
  const [view, setView] = useState<View>('discover')
  const [page, setPage] = useState(0)
  const [filter, setFilter] = useState(filters[0])
  const [query, setQuery] = useState('')
  const [pokemon, setPokemon] = useState<PokemonEntry[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [pageError, setPageError] = useState('')
  const [reloadCount, setReloadCount] = useState(0)
  const [selected, setSelected] = useState<PokemonEntry | null>(null)
  const [auth, setAuth] = useState<AuthResponse | null>(() => {
    const saved = localStorage.getItem('pokejava.auth')
    if (!saved) return null
    try {
      return JSON.parse(saved) as AuthResponse
    } catch {
      localStorage.removeItem('pokejava.auth')
      return null
    }
  })
  const [authOpen, setAuthOpen] = useState(false)
  const [toast, setToast] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    setLoading(true)
    setPageError('')
    const request = view === 'discover'
      ? getPokemonPage(page, controller.signal)
      : getLocalPokemonPage(page, controller.signal)

    request.then((result) => {
      setPokemon(result.content)
      setTotal(result.totalElements)
    }).catch((error: unknown) => {
      if (!controller.signal.aborted) setPageError(getErrorMessage(error))
    }).finally(() => {
      if (!controller.signal.aborted) setLoading(false)
    })

    return () => controller.abort()
  }, [view, page, reloadCount])

  useEffect(() => {
    if (!toast) return
    const timeout = window.setTimeout(() => setToast(''), 3200)
    return () => window.clearTimeout(timeout)
  }, [toast])

  const visiblePokemon = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase()
    return pokemon.filter((entry) => {
      const category = categoryClass(entry.category)
      const matchesQuery = !normalizedQuery
        || entry.name.includes(normalizedQuery)
        || entry.category.toLowerCase().includes(normalizedQuery)
        || (isLocalPokemon(entry) && (entry.localName ?? '').toLowerCase().includes(normalizedQuery))
      const matchesFilter = filter === 'All Pokémon'
        || (filter === 'Starters' && (starterIds.has(entry.id) || (isLocalPokemon(entry) && entry.tags.includes('starter'))))
        || category === filter.toLowerCase()
      return matchesQuery && matchesFilter
    })
  }, [filter, pokemon, query])

  function changeView(nextView: View) {
    setView(nextView)
    setPage(0)
    setFilter(filters[0])
    setQuery('')
  }

  function handleAuth(response: AuthResponse) {
    setAuth(response)
    localStorage.setItem('pokejava.auth', JSON.stringify(response))
    setAuthOpen(false)
    setToast(`Welcome, ${response.displayName}`)
  }

  function signOut() {
    setAuth(null)
    localStorage.removeItem('pokejava.auth')
    setToast('You have signed out')
  }

  async function handleSync(entry: PokemonEntry) {
    if (!auth) {
      setAuthOpen(true)
      return
    }
    try {
      const saved = await syncPokemon(entry.name, auth.accessToken)
      setToast(`${capitalize(saved.name)} added to your collection`)
      if (view === 'collection') {
        setPokemon((current) => current.some((item) => item.id === saved.id)
          ? current.map((item) => item.id === saved.id ? saved : item)
          : [saved, ...current])
        setTotal((count) => count + (pokemon.some((item) => item.id === saved.id) ? 0 : 1))
      }
      setSelected(saved)
    } catch (error) {
      setToast(getErrorMessage(error))
    }
  }

  async function handleUpdate(
    idOrName: string,
    enrichment: { localName: string; region: string; tags: string[] },
  ): Promise<LocalPokemon> {
    if (!auth) {
      setAuthOpen(true)
      throw new Error('Sign in to edit your field notes.')
    }
    const saved = await updateLocalPokemon(idOrName, auth.accessToken, enrichment)
    setSelected(saved)
    setPokemon((current) => current.map((item) => item.id === saved.id ? saved : item))
    setToast(`${capitalize(saved.name)} details saved`)
    return saved
  }

  const pageCount = Math.max(1, Math.ceil(total / 20))

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#top" aria-label="Pokédex home">
          <span className="brand-mark"><span /></span>
          <span>pokedex<span className="brand-dot">.</span></span>
        </a>
        <div className="sidebar-caption">YOUR FIELD GUIDE</div>
        <nav className="primary-nav" aria-label="Main navigation">
          <button className={view === 'discover' ? 'nav-item active' : 'nav-item'} onClick={() => changeView('discover')}>
            <Search size={18} strokeWidth={1.8} />
            <span>Discover</span>
            <ChevronRight className="nav-arrow" size={15} />
          </button>
          <button className={view === 'collection' ? 'nav-item active' : 'nav-item'} onClick={() => changeView('collection')}>
            <ArrowDownToLine size={18} strokeWidth={1.8} />
            <span>My collection</span>
            <ChevronRight className="nav-arrow" size={15} />
          </button>
        </nav>
        <div className="sidebar-rule" />
        <div className="sidebar-caption">QUICK FILTERS</div>
        <div className="quick-filters">
          {filters.slice(1).map((item) => (
            <button
              key={item}
              className={`quick-filter ${filter === item ? 'selected' : ''}`}
              onClick={() => {
                setFilter(filter === item ? filters[0] : item)
                setPage(0)
              }}
            >
              <span className={`filter-dot ${item.toLowerCase()}`} />
              {item}
            </button>
          ))}
        </div>
        <div className="sidebar-spacer" />
        <div className="sidebar-card">
          <div className="sidebar-card-icon"><Sparkles size={17} /></div>
          <strong>A world to explore</strong>
          <p>Discover details, compare stats, and build your own collection.</p>
          <button onClick={() => changeView('discover')}>Explore Pokédex <ArrowRight size={14} /></button>
        </div>
        <div className="sidebar-footer">Made for curious trainers <span>✳</span></div>
      </aside>

      <main className="main-content" id="top">
        <header className="topbar">
          <button className="mobile-brand" onClick={() => changeView('discover')} aria-label="Go to discover">
            <span className="brand-mark"><span /></span>
            <span>pokedex<span className="brand-dot">.</span></span>
          </button>
          <div className="breadcrumb"><span>Pokédex</span><ChevronRight size={14} /><strong>{view === 'discover' ? 'Discover' : 'My collection'}</strong></div>
          <div className="topbar-actions">
            <span className="api-status"><span /> Live PokéAPI</span>
            {auth ? (
              <div className="user-menu">
                <span className="avatar">{auth.displayName.trim().slice(0, 1).toUpperCase()}</span>
                <span className="user-name">{auth.displayName}</span>
                <button className="icon-button signout-button" onClick={signOut} aria-label="Sign out" title="Sign out"><LogOut size={17} /></button>
              </div>
            ) : (
              <button className="login-button" onClick={() => setAuthOpen(true)}><LogIn size={16} /> Sign in</button>
            )}
          </div>
        </header>

        <div className="page-container">
          <section className="welcome-row">
            <div>
              <div className="eyebrow"><span className="eyebrow-line" /> THE WORLD OF POKÉMON</div>
              <h1>{view === 'discover' ? <>Find your<br /><span>next favorite.</span></> : <>Your growing<br /><span>collection.</span></>}</h1>
              <p className="welcome-copy">{view === 'discover'
                ? 'Every Pokémon has a story. Find yours in the field guide.'
                : 'The Pokémon you’ve brought back from your adventures.'}</p>
            </div>
            <div className="welcome-art" aria-hidden="true">
              <div className="art-orbit orbit-one" />
              <div className="art-orbit orbit-two" />
              <div className="art-sun" />
              <img src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png" alt="" />
              <span className="art-spark spark-one">✳</span>
              <span className="art-spark spark-two">✦</span>
            </div>
          </section>

          <section className="catalog-section">
            <div className="catalog-heading">
              <div>
                <div className="section-kicker">{view === 'discover' ? 'THE FIELD GUIDE' : 'SAVED LOCALLY'}</div>
                <h2>{view === 'discover' ? 'Pokémon index' : 'Your Pokémon'}</h2>
                <p>{view === 'discover'
                  ? 'A few good places to start your journey.'
                  : `${total} ${total === 1 ? 'Pokémon' : 'Pokémon'} in your collection`}</p>
              </div>
              <div className="catalog-tools">
                <label className="search-box">
                  <Search size={17} />
                  <input
                    aria-label="Filter visible Pokémon"
                    placeholder="Filter this page..."
                    value={query}
                    onChange={(event) => setQuery(event.target.value)}
                  />
                  {query && <button onClick={() => setQuery('')} aria-label="Clear search"><X size={15} /></button>}
                </label>
                <label className="filter-select">
                  <span className={filter === filters[0] ? 'filter-select-dot all' : `filter-select-dot ${filter.toLowerCase()}`} />
                  <select aria-label="Choose Pokémon filter" value={filter} onChange={(event) => setFilter(event.target.value)}>
                    {filters.map((item) => <option key={item}>{item}</option>)}
                  </select>
                  <ChevronDown size={15} />
                </label>
              </div>
            </div>

            <div className="active-filter-row">
              <div className="filter-tabs" role="group" aria-label="Filter Pokémon">
                {filters.map((item) => (
                  <button key={item} className={filter === item ? 'filter-tab active' : 'filter-tab'} onClick={() => setFilter(item)}>
                    {item === 'All Pokémon' && <span className="all-tab-dot" />}
                    {item}
                  </button>
                ))}
              </div>
              <span className="result-count">{loading ? 'Loading...' : `${visiblePokemon.length} shown`}</span>
            </div>

            {pageError ? (
              <div className="state-panel error-panel">
                <div className="state-icon">!</div>
                <h3>Couldn’t load Pokémon</h3>
                <p>{pageError}</p>
                <button className="primary-button" onClick={() => setReloadCount((count) => count + 1)}>Try again</button>
              </div>
            ) : loading ? (
              <div className="pokemon-grid" aria-label="Loading Pokémon">
                {Array.from({ length: 8 }, (_, index) => <div className="pokemon-skeleton" key={index} />)}
              </div>
            ) : visiblePokemon.length ? (
              <div className="pokemon-grid">
                {visiblePokemon.map((entry, index) => (
                  <PokemonCard
                    key={entry.id}
                    pokemon={entry}
                    index={index}
                    onOpen={() => setSelected(entry)}
                    onSync={() => handleSync(entry)}
                    isSaved={isLocalPokemon(entry)}
                  />
                ))}
              </div>
            ) : (
              <div className="state-panel">
                <div className="state-icon"><Search size={20} /></div>
                <h3>{view === 'collection' && total === 0 ? 'Your collection is waiting' : 'No Pokémon found'}</h3>
                <p>{view === 'collection' && total === 0
                  ? 'Visit Discover and save a Pokémon to start your collection.'
                  : 'Try another name or choose a different filter. Search filters this page of results.'}</p>
                {view === 'collection' && total === 0 && <button className="primary-button" onClick={() => changeView('discover')}>Explore Pokémon <ArrowRight size={15} /></button>}
              </div>
            )}

            {!loading && !pageError && total > 20 && (
              <div className="pagination">
                <span>Showing <strong>{page * 20 + 1}–{Math.min((page + 1) * 20, total)}</strong> of <strong>{total}</strong></span>
                <div>
                  <button className="page-button" aria-label="Previous page" disabled={page === 0} onClick={() => setPage((current) => Math.max(0, current - 1))}><ChevronLeft size={17} /></button>
                  <span className="page-number">{page + 1} <span>/ {pageCount}</span></span>
                  <button className="page-button" aria-label="Next page" disabled={page + 1 >= pageCount} onClick={() => setPage((current) => current + 1)}><ChevronRight size={17} /></button>
                </div>
              </div>
            )}
          </section>
          <footer className="page-footer"><span>Pokédex</span><span>Built for the curious, powered by <a href="https://pokeapi.co/" target="_blank" rel="noreferrer">PokéAPI</a>.</span></footer>
        </div>
      </main>

      {selected && (
        <PokemonDialog
          key={`${selected.id}-${isLocalPokemon(selected) ? 'local' : 'catalog'}`}
          selected={selected}
          token={auth?.accessToken}
          onClose={() => setSelected(null)}
          onSync={() => handleSync(selected)}
          onAuthRequired={() => setAuthOpen(true)}
          onUpdate={handleUpdate}
        />
      )}
      {authOpen && <AuthDialog onClose={() => setAuthOpen(false)} onAuthenticated={handleAuth} />}
      {toast && <div className="toast" role="status"><Check size={16} />{toast}</div>}
    </div>
  )
}

function PokemonCard({
  pokemon,
  index,
  onOpen,
  onSync,
  isSaved,
}: {
  pokemon: PokemonEntry
  index: number
  onOpen: () => void
  onSync: () => void
  isSaved: boolean
}) {
  const [imageFailed, setImageFailed] = useState(false)
  const image = 'imageUrl' in pokemon ? pokemon.imageUrl : pokemon.spriteUrl
  const tone = categoryClass(pokemon.category)

  return (
    <article className={`pokemon-card tone-${tone}`} style={{ animationDelay: `${index * 45}ms` }}>
      <button className="card-main" onClick={onOpen} aria-label={`View ${capitalize(pokemon.name)} details`}>
        <div className="card-image-wrap">
          <span className="pokemon-number">{pokemonNumber(pokemon.id)}</span>
          <span className="card-sparkle" aria-hidden="true">✳</span>
          {!imageFailed && <img className="pokemon-image" src={image} alt={capitalize(pokemon.name)} loading="lazy" onError={() => setImageFailed(true)} />}
          {imageFailed && <span className="image-fallback">{capitalize(pokemon.name).slice(0, 1)}</span>}
          {isSaved && <span className="saved-stamp" title="In your collection"><Check size={13} /></span>}
        </div>
        <div className="card-info">
          <div className="card-title-line"><h3>{capitalize(pokemon.name)}</h3><ArrowRight size={15} /></div>
          <div className="card-category">{pokemon.category}</div>
          <div className="card-meta">
            <span>{pokemon.weightKg.toFixed(1)} <small>kg</small></span>
            <span className="meta-separator" />
            <span>{pokemon.abilities.slice(0, 2).map(capitalize).join(' · ')}</span>
          </div>
        </div>
      </button>
      {!isSaved && (
        <button className="card-save" onClick={onSync} aria-label={`Add ${capitalize(pokemon.name)} to collection`} title="Add to collection">
          <ArrowDownToLine size={16} />
        </button>
      )}
    </article>
  )
}

function PokemonDialog({
  selected,
  token,
  onClose,
  onSync,
  onAuthRequired,
  onUpdate,
}: {
  selected: PokemonEntry
  token?: string
  onClose: () => void
  onSync: () => void
  onAuthRequired: () => void
  onUpdate: (idOrName: string, enrichment: { localName: string; region: string; tags: string[] }) => Promise<LocalPokemon>
}) {
  const selectedHasDetail = 'description' in selected
    && (!isLocalPokemon(selected) || Boolean(selected.evolutionChain))
  const [detail, setDetail] = useState<PokemonDetail | LocalPokemon | null>(
    selectedHasDetail ? selected : null,
  )
  const [loading, setLoading] = useState(!selectedHasDetail)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState(false)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')
  const [saved, setSaved] = useState(false)
  const local = detail ? isLocalPokemon(detail) : isLocalPokemon(selected)

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', handleKeyDown)
    document.body.classList.add('modal-open')
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.classList.remove('modal-open')
    }
  }, [onClose])

  useEffect(() => {
    if (detail) return
    let active = true
    getPokemon(selected.name, isLocalPokemon(selected)).then((result) => {
      if (active) setDetail(result)
    }).catch((requestError: unknown) => {
      if (active) setError(getErrorMessage(requestError))
    }).finally(() => {
      if (active) setLoading(false)
    })
    return () => { active = false }
  }, [detail, selected])

  async function saveEnrichment(form: FormData) {
    setSaving(true)
    setSaveError('')
    try {
      const updated = await onUpdate(selected.name, {
        localName: String(form.get('localName') ?? ''),
        region: String(form.get('region') ?? ''),
        tags: String(form.get('tags') ?? '')
          .split(',')
          .map((tag) => tag.trim())
          .filter(Boolean),
      })
      setDetail((current) => ({
        ...updated,
        evolutionChain: current?.evolutionChain,
      }))
      setEditing(false)
      setSaved(true)
    } catch (requestError) {
      setSaveError(getErrorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="dialog-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
      <section className="pokemon-dialog" role="dialog" aria-modal="true" aria-label="Pokémon details">
        <button className="dialog-close icon-button" onClick={onClose} aria-label="Close details"><X size={19} /></button>
        {loading ? (
          <div className="dialog-loading"><div className="loading-pokeball" /><span>Following the trail...</span></div>
        ) : error ? (
          <div className="state-panel dialog-state"><div className="state-icon">!</div><h3>Details unavailable</h3><p>{error}</p><button className="secondary-button" onClick={onClose}>Close</button></div>
        ) : detail ? (
          <div className="detail-layout">
            <div className={`detail-art tone-${categoryClass(detail.category)}`}>
              <span className="detail-number">{pokemonNumber(detail.id)}</span>
              <div className="detail-art-orbit" />
              <img src={detail.imageUrl} alt={capitalize(detail.name)} />
              <span className="detail-art-label">FIELD GUIDE · {String(detail.id).padStart(3, '0')}</span>
            </div>
            <div className="detail-content">
              <div className="detail-kicker">{detail.category.toUpperCase()} <span>·</span> #{String(detail.id).padStart(3, '0')}</div>
              <h2>{capitalize(detail.name)}</h2>
              <div className="detail-facts">
                <div><span>WEIGHT</span><strong>{detail.weightKg.toFixed(1)} <small>kg</small></strong></div>
                <div><span>ABILITIES</span><strong>{detail.abilities.map(capitalize).join(', ')}</strong></div>
              </div>
              <div className="detail-section">
                <div className="detail-section-title"><span>01</span><h3>A little about</h3></div>
                <p className="detail-description">{detail.description || 'No field notes available for this Pokémon yet.'}</p>
              </div>
              <div className="detail-section">
                <div className="detail-section-title"><span>02</span><h3>Base stats</h3></div>
                <div className="stat-list">
                  {detail.stats.map((stat) => (
                    <div className="stat-row" key={stat.name}>
                      <span>{capitalize(stat.name)}</span><strong>{stat.baseStat}</strong>
                      <div className="stat-track"><span style={{ width: `${Math.min(stat.baseStat / 2.55, 100)}%` }} /></div>
                    </div>
                  ))}
                </div>
              </div>
              {detail.evolutionChain && (
                <div className="detail-section">
                  <div className="detail-section-title"><span>03</span><h3>Evolution line</h3></div>
                  <EvolutionLine stage={detail.evolutionChain} current={detail.name} />
                </div>
              )}
              {local ? (
                <div className="enrichment-section">
                  <div className="detail-section-title"><span>04</span><h3>Your field notes</h3></div>
                  {editing ? (
                    <form onSubmit={(event) => { event.preventDefault(); void saveEnrichment(new FormData(event.currentTarget)) }}>
                      <label className="form-field">Local name<input name="localName" maxLength={150} defaultValue={isLocalPokemon(detail) ? detail.localName ?? '' : ''} placeholder="Give it a local name" /></label>
                      <label className="form-field">Region<input name="region" maxLength={150} defaultValue={isLocalPokemon(detail) ? detail.region ?? '' : ''} placeholder="Where did you meet?" /></label>
                      <label className="form-field">Tags <span>(comma separated)</span><input name="tags" maxLength={250} defaultValue={isLocalPokemon(detail) ? detail.tags.join(', ') : ''} placeholder="starter, favorite" /></label>
                      {saveError && <p className="form-error" role="alert">{saveError}</p>}
                      <div className="form-actions"><button type="button" className="secondary-button" onClick={() => setEditing(false)}>Cancel</button><button type="submit" className="primary-button" disabled={saving}>{saving ? 'Saving...' : 'Save notes'}</button></div>
                    </form>
                  ) : (
                    <div className="field-notes">
                      <div><span>Local name</span><strong>{isLocalPokemon(detail) && detail.localName ? detail.localName : 'Not set'}</strong></div>
                      <div><span>Region</span><strong>{isLocalPokemon(detail) && detail.region ? <><MapPin size={13} />{detail.region}</> : 'Not set'}</strong></div>
                      <div><span>Tags</span><strong className="tag-list">{isLocalPokemon(detail) && detail.tags.length ? detail.tags.map((tag) => <em key={tag}>{tag}</em>) : 'No tags yet'}</strong></div>
                      <button className="secondary-button notes-edit" onClick={() => token ? setEditing(true) : onAuthRequired()}>
                        {token ? 'Edit field notes' : 'Sign in to edit field notes'} <ArrowRight size={14} />
                      </button>
                    </div>
                  )}
                  {saved && <div className="inline-success"><Check size={14} /> Field notes saved</div>}
                </div>
              ) : (
                <div className="dialog-footer">
                  <p>Keep this one in your collection?</p>
                  <button className="primary-button" onClick={onSync}><ArrowDownToLine size={16} /> Add to collection</button>
                </div>
              )}
            </div>
          </div>
        ) : null}
      </section>
    </div>
  )
}

function EvolutionLine({ stage, current }: { stage: EvolutionStage; current: string }) {
  if (!stage.evolvesTo.length) return <p className="no-evolution">This Pokémon has no known evolutions.</p>
  return <EvolutionBranch stage={stage} current={current} />
}

function EvolutionBranch({ stage, current }: { stage: EvolutionStage; current: string }) {
  return (
    <div className="evolution-node">
      <div className={stage.name === current ? 'evolution-entry current' : 'evolution-entry'}>
        {evolutionId(stage.name) > 0 && <img src={`https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${evolutionId(stage.name)}.png`} alt="" />}
        <span>{capitalize(stage.name)}</span>
      </div>
      {stage.evolvesTo.length > 0 && (
        <div className="evolution-children">
          {stage.evolvesTo.map((child) => (
            <div className="evolution-branch" key={child.name}>
              <ArrowRight size={14} />
              <EvolutionBranch stage={child} current={current} />
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

function evolutionId(name: string) {
  const knownIds: Record<string, number> = {
    bulbasaur: 1, ivysaur: 2, venusaur: 3, charmander: 4, charmeleon: 5,
    charizard: 6, squirtle: 7, wartortle: 8, blastoise: 9, pikachu: 25,
    raichu: 26, eevee: 133, vaporeon: 134, jolteon: 135, flareon: 136,
    espeon: 196, umbreon: 197, leafeon: 470, glaceon: 471, sylveon: 700,
  }
  return knownIds[name] ?? 0
}

function AuthDialog({ onClose, onAuthenticated }: { onClose: () => void; onAuthenticated: (response: AuthResponse) => void }) {
  const [mode, setMode] = useState<AuthMode>('login')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', handleKeyDown)
    document.body.classList.add('modal-open')
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      document.body.classList.remove('modal-open')
    }
  }, [onClose])

  async function submit(form: FormData) {
    setBusy(true)
    setError('')
    try {
      const username = String(form.get('username') ?? '')
      const password = String(form.get('password') ?? '')
      const response = mode === 'login'
        ? await login(username, password)
        : await register(username, password, String(form.get('displayName') ?? ''))
      onAuthenticated(response)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="dialog-backdrop auth-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
      <section className="auth-dialog" role="dialog" aria-modal="true" aria-labelledby="auth-title">
        <button className="dialog-close icon-button" onClick={onClose} aria-label="Close sign in"><X size={19} /></button>
        <div className="auth-emblem"><span className="brand-mark"><span /></span></div>
        <div className="section-kicker">GOOD TO HAVE YOU HERE</div>
        <h2 id="auth-title">{mode === 'login' ? 'Welcome back.' : 'Join the adventure.'}</h2>
        <p>{mode === 'login' ? 'Sign in to save Pokémon and keep field notes.' : 'Create an account to start your own collection.'}</p>
        <form onSubmit={(event) => { event.preventDefault(); void submit(new FormData(event.currentTarget)) }}>
          {mode === 'register' && <label className="form-field">Display name<input name="displayName" required minLength={1} maxLength={100} autoComplete="name" placeholder="How should we call you?" /></label>}
          <label className="form-field">Username<input name="username" required minLength={3} maxLength={50} autoComplete="username" placeholder="Your username" /></label>
          <label className="form-field">Password<input name="password" type="password" required minLength={8} maxLength={72} autoComplete={mode === 'login' ? 'current-password' : 'new-password'} placeholder="At least 8 characters" /></label>
          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="primary-button auth-submit" type="submit" disabled={busy}>{busy ? 'One moment...' : mode === 'login' ? 'Sign in' : 'Create account'}<ArrowRight size={15} /></button>
        </form>
        <div className="auth-switch">{mode === 'login' ? 'New around here?' : 'Already have an account?'} <button onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError('') }}>{mode === 'login' ? 'Create an account' : 'Sign in'}</button></div>
        <div className="demo-credentials"><strong>Demo trainer</strong><span>demo / demo1234</span></div>
      </section>
    </div>
  )
}

export default App
