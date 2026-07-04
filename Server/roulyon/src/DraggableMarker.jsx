import { useState, useRef, useMemo, useCallback } from "react"
import { Marker, Popup } from "react-leaflet"
/**
 * Disclaimer :
 * This function was directly taken from the official documentation.
 * https://react-leaflet.js.org/docs/example-draggable-marker/
 */
function DraggableMarker({pos , name}) {
  const [draggable, setDraggable] = useState(false)
  const [position, setPosition] = useState(pos)
  const markerRef = useRef(null)
  const eventHandlers = useMemo(
    () => ({
      dragend() {
        const marker = markerRef.current
        if (marker != null) {
          setPosition(marker.getLatLng())
        }
      },
    }),
    [],
  )
  const toggleDraggable = useCallback(() => {
    setDraggable((d) => !d)
  }, [])

  return (
    <Marker
      draggable={draggable}
      eventHandlers={eventHandlers}
      position={position}
      ref={markerRef}>
      <Popup minWidth={90}>
        {name}<br />
        <span onClick={toggleDraggable}>
          {draggable
            ? 'Marker is draggable'
            : 'Click here to make marker draggable'}
        </span>
      </Popup>
    </Marker>
  )
}

export default DraggableMarker