import { MapContainer, TileLayer, CircleMarker, Popup, Polyline, LayersControl, ScaleControl, LayerGroup } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import './Map.css'

function TransitMap({ stops, departure, arrival, path, onSelectStop, onSelectDeparture, onSelectArrival }) {
  const center = [45.764, 4.8357]

  return (
    <div className="transit-map">
      <MapContainer center={center} zoom={13} doubleClickZoom={false}>
        <LayersControl position="topright">
          <LayersControl.BaseLayer checked name="OpenStreetMap">
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
          </LayersControl.BaseLayer>
          <LayersControl.Overlay checked name="Stops">
            <LayerGroup>
              <LayerGroupStops
                stops={stops}
                departure={departure}
                arrival={arrival}
                onSelectStop={onSelectStop}
                onSelectDeparture={onSelectDeparture}
                onSelectArrival={onSelectArrival}
              />
            </LayerGroup>
          </LayersControl.Overlay>
          <LayersControl.Overlay checked name="Itinerary">
            {path.length > 1 ? (
              <Polyline pathOptions={{color: '#C8102E',weight: 6,opacity: 0.9}}/>
            ) : null}
          </LayersControl.Overlay>
        </LayersControl>
        <ScaleControl position="bottomleft" />
      </MapContainer>
    </div>
  )
}

function LayerGroupStops({ stops, departure, arrival, onSelectStop, onSelectDeparture, onSelectArrival }) {
  return stops.map(stop => {
    const isDeparture = departure?.id === stop.id
    const isArrival = arrival?.id === stop.id
    const color = isDeparture ? "#0055A4" : isArrival ? "#C8102E" : "#666666";
    const radius = isDeparture || isArrival ? 8 : 4

    return (
      <CircleMarker
        key={stop.id}
        center={[stop.lat, stop.lng]}
        radius={radius}
        pathOptions={{
          color,
          fillColor: color,
          fillOpacity: 0.8,
          weight: 2,
        }}
        eventHandlers={{
          click: () => onSelectStop(stop),
        }}
      >
        <Popup>
          <strong>{stop.name}</strong>
          <div className="popup-meta">{stop.id}</div>
          <div className="popup-actions">
            <button type="button" onClick={() => onSelectDeparture(stop)}>Set departure</button>
            <button type="button" onClick={() => onSelectArrival(stop)}>Set arrival</button>
          </div>
        </Popup>
      </CircleMarker>
    )
  })
}

export default TransitMap
