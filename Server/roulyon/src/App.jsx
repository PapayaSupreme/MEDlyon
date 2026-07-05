import { useEffect, useMemo, useState } from 'react'
import TransitMap from './Map'
import { comparePathAlgorithms, getStops } from './bridge'
import './App.css'

function App() {
  const [stops, setStops] = useState([])
  const [loadingStops, setLoadingStops] = useState(true)
  const [query, setQuery] = useState('')
  const [selectionTarget, setSelectionTarget] = useState('departure')
  const [departure, setDeparture] = useState(null)
  const [arrival, setArrival] = useState(null)
  const [itinerary, setItinerary] = useState([])
  const [aStarItinerary, setAStarItinerary] = useState([])
  const [timings, setTimings] = useState({ dijkstra: 0, aStar: 0 })
  const [co2, setCo2] = useState({ dijkstra: 0, aStar: 0 })
  const [status, setStatus] = useState('')
  const [computing, setComputing] = useState(false)

  useEffect(() => {
    let mounted = true

    async function loadStops() {
      setLoadingStops(true)
      const data = await getStops()
      if (!mounted) return
      setStops(data)
      setLoadingStops(false)
      if (!data.length) {
        setStatus('Auncun arrêt chargé depuis le Backend.')
      }
    }

    loadStops().catch(() => {
      if (mounted) {
        setStops([])
        setLoadingStops(false)
        setStatus('Impossible de charger les arrêts depuis le Spring Boot.')
      }
    })

    return () => {
      mounted = false
    }
  }, [])

  const filteredStops = useMemo(() => {
    const needle = query.trim().toLowerCase()
    const ranked = [...stops].filter(stop => {
      if (!needle) return true
      return stop.name.toLowerCase().includes(needle) || stop.id.toLowerCase().includes(needle)
    })

    ranked.sort((a, b) => a.name.localeCompare(b.name) || a.id.localeCompare(b.id))
    return ranked.slice(0, 120)
  }, [query, stops])

  function selectStop(stop) {
    if (selectionTarget === 'departure') {
      setDeparture(stop)
      if (arrival && arrival.id === stop.id) {
        setArrival(null)
      }
      return
    }

    setArrival(stop)
    if (departure && departure.id === stop.id) {
      setDeparture(null)
    }
  }

  async function handleCompute() {
    if (!departure || !arrival) {
      setStatus('Select a departure and an arrival stop first.')
      return
    }
    if (departure.id === arrival.id) {
      setStatus('Departure and arrival must be different stops.')
      return
    }

    setComputing(true)
    setStatus('Computing itinerary...')
    const comparison = await comparePathAlgorithms(departure, arrival)
    setItinerary(comparison.dijkstra)
    setAStarItinerary(comparison.aStar)
    setTimings({
      dijkstra: comparison.dijkstraTimeNanos,
      aStar: comparison.aStarTimeNanos,
    })
    setCo2({
      dijkstra: comparison.dijkstraCo2Grams,
      aStar: comparison.aStarCo2Grams,
    })
    setComputing(false)

    if (!comparison.dijkstra.length && !comparison.aStar.length) {
      setStatus('No itinerary was returned by the backend.')
      return
    }

    setStatus(`Itinerary found with ${comparison.dijkstra.length} stop${comparison.dijkstra.length > 1 ? 's' : ''}.`)
  }

  const pathCoordinates = itinerary.map(stop => [stop.lat, stop.lng])
  const formatMs = nanos => `${(nanos / 1_000_000).toFixed(3)} ms`
  const formatCo2 = grams => `${grams.toFixed(1)} g`

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="panel">
          <div className="panel-header">
            <h1>Roulyon</h1>
            <p>Choissisez un point de départ, un point d'arrivée, puis calculez le itinéraire.</p>
          </div>

          <div className="toggle-row" role="tablist" aria-label="Selection target">
            <button
              type="button"
              className={selectionTarget === 'departure' ? 'toggle active' : 'toggle'}
              onClick={() => setSelectionTarget('departure')}
            >
              Départ
            </button>
            <button
              type="button"
              className={selectionTarget === 'arrival' ? 'toggle active' : 'toggle'}
              onClick={() => setSelectionTarget('arrival')}
            >
              Arrivé
            </button>
          </div>

          <label className="search-field">
            <span>Rechercher un arrêt</span>
            <input
              type="search"
              value={query}
              placeholder="Tape un nom ou un ID d'arrêt"
              onChange={e => setQuery(e.target.value)}
            />
          </label>

          <div className="selection-summary">
            <div>
              <span>Départ</span>
              <strong>{departure ? departure.name : 'Aucun sélectionné'}</strong>
            </div>
            <div>
              <span>Arrivé</span>
              <strong>{arrival ? arrival.name : 'Aucun sélectionné'}</strong>
            </div>
          </div>

          <button type="button" className="compute-button" onClick={handleCompute} disabled={computing || loadingStops}>
            {computing ? 'Computing...' : 'Calculer l’itinéraire'}
          </button>

          <p className="status-line">{status}</p>
        </div>

        <div className="panel results-panel">
          <div className="results-header">
            <h2>Arrêts</h2>
            <span>{loadingStops ? 'Loading...' : `${filteredStops.length} résultats`}</span>
          </div>

          <div className="stop-list">
            {filteredStops.map(stop => (
              <button
                key={stop.id}
                type="button"
                className="stop-row"
                onClick={() => selectStop(stop)}
              >
                <strong>{stop.name}</strong>
                <span>{stop.id}</span>
              </button>
            ))}
          </div>
        </div>

        <div className="panel">
          <div className="results-header">
            <h2>Itinéraire</h2>
            <span>{itinerary.length ? `${itinerary.length} stops` : 'aucun'}</span>
          </div>

          <ol className="itinerary-list">
            {itinerary.map((stop, index) => (
              <li key={`${stop.id}-${index}`}>
                <strong>{stop.name}</strong>
                <span>{stop.id}</span>
              </li>
            ))}
          </ol>
        </div>

        <div className="panel">
          <div className="results-header">
            <h2>Statistiques</h2>
            <span>temps de Backend et CO2</span>
          </div>

          <div className="timing-grid">
            <div>
              <span>Dijkstra</span>
              <strong>{timings.dijkstra ? formatMs(timings.dijkstra) : '---'}</strong>
              <small>{formatCo2(co2.dijkstra)}</small>
            </div>
            <div>
              <span>A*</span>
              <strong>{timings.aStar ? formatMs(timings.aStar) : '---'}</strong>
              <small>{formatCo2(co2.aStar)}</small>
            </div>
            <div>
              <span>Arrêts</span>
              <strong>{aStarItinerary.length || itinerary.length || 0}</strong>
            </div>
          </div>
        </div>
      </aside>

      <main className="map-shell">
        <TransitMap
          stops={stops}
          departure={departure}
          arrival={arrival}
          path={pathCoordinates}
          onSelectStop={selectStop}
          onSelectDeparture={stop => {
            setSelectionTarget('departure')
            setDeparture(stop)
          }}
          onSelectArrival={stop => {
            setSelectionTarget('arrival')
            setArrival(stop)
          }}
        />
      </main>
    </div>
  )
}

export default App
