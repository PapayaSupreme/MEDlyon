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
        setStatus('No stops were loaded from the backend.')
      }
    }

    loadStops().catch(() => {
      if (mounted) {
        setStops([])
        setLoadingStops(false)
        setStatus('Unable to load the stop list from Spring Boot.')
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
    setComputing(false)

    if (!comparison.dijkstra.length && !comparison.aStar.length) {
      setStatus('No itinerary was returned by the backend.')
      return
    }

    setStatus(`Itinerary found with ${comparison.dijkstra.length} stop${comparison.dijkstra.length > 1 ? 's' : ''}.`)
  }

  const pathCoordinates = itinerary.map(stop => [stop.lat, stop.lng])
  const formatMs = nanos => `${(nanos / 1_000_000).toFixed(3)} ms`

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="panel">
          <div className="panel-header">
            <h1>MEDLyon</h1>
            <p>Pick a departure stop, pick an arrival stop, then compute the route.</p>
          </div>

          <div className="toggle-row" role="tablist" aria-label="Selection target">
            <button
              type="button"
              className={selectionTarget === 'departure' ? 'toggle active' : 'toggle'}
              onClick={() => setSelectionTarget('departure')}
            >
              Departure
            </button>
            <button
              type="button"
              className={selectionTarget === 'arrival' ? 'toggle active' : 'toggle'}
              onClick={() => setSelectionTarget('arrival')}
            >
              Arrival
            </button>
          </div>

          <label className="search-field">
            <span>Search stop</span>
            <input
              type="search"
              value={query}
              placeholder="Type a stop name or id"
              onChange={e => setQuery(e.target.value)}
            />
          </label>

          <div className="selection-summary">
            <div>
              <span>Departure</span>
              <strong>{departure ? departure.name : 'None selected'}</strong>
            </div>
            <div>
              <span>Arrival</span>
              <strong>{arrival ? arrival.name : 'None selected'}</strong>
            </div>
          </div>

          <button type="button" className="compute-button" onClick={handleCompute} disabled={computing || loadingStops}>
            {computing ? 'Computing...' : 'Compute itinerary'}
          </button>

          <p className="status-line">{status}</p>
        </div>

        <div className="panel results-panel">
          <div className="results-header">
            <h2>Stops</h2>
            <span>{loadingStops ? 'Loading...' : `${filteredStops.length} shown`}</span>
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
            <h2>Itinerary</h2>
            <span>{itinerary.length ? `${itinerary.length} stops` : 'None'}</span>
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
            <h2>Compute time</h2>
            <span>Backend timing</span>
          </div>

          <div className="timing-grid">
            <div>
              <span>Dijkstra</span>
              <strong>{timings.dijkstra ? formatMs(timings.dijkstra) : '---'}</strong>
            </div>
            <div>
              <span>A*</span>
              <strong>{timings.aStar ? formatMs(timings.aStar) : '---'}</strong>
            </div>
            <div>
              <span>Stops</span>
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
