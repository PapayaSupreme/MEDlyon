import { MapContainer } from 'react-leaflet/MapContainer'
import { LayersControl, ScaleControl } from 'react-leaflet'
import { LayerGroup } from 'react-leaflet/LayerGroup'
import { TileLayer } from 'react-leaflet/TileLayer'
import { useMap } from 'react-leaflet/hooks'
import "leaflet/dist/leaflet.css"
import './Map.css'
import { Control, Layer } from 'leaflet'

function Map(){

    return (
    <div className='leaflet-position'>
        {/* Initial try
        <iframe 
            width="425" 
            height="350" 
            src="https://www.openstreetmap.org/export/embed.html?bbox=4.540100097656251%2C45.646208310900626%2C5.247344970703125%2C45.8876184503559&amp;layer=transportmap" >
        </iframe>
        <br/>
        <small>
            <a href="https://www.openstreetmap.org/?#map=11/45.7670/4.8937&amp;layers=T">View Larger Map</a>
        </small>*/}

        {/** With react-leaflet: */}
        <MapContainer center={[45.74944,4.83604]} zoom={13}>
            <LayersControl position='topright'>
                <LayersControl.BaseLayer name="Osm default map" checked>
                    <TileLayer
                        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        minZoom={11}
                    />
                </LayersControl.BaseLayer>
                <LayersControl.BaseLayer name="Thunderforest map (Uses APIkey)" disabled>
                    <LayerGroup>
                        <TileLayer 
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors,  Tiles courtesy of<a href="https://www.thunderforest.com/"> Andy Allan.</a> (<a href="https://www.thunderforest.com/" target="_blank">&copy; Thunderforest</a>)'    
                            url={"https://api.thunderforest.com/transport-dark/{z}/{x}/{y}{r}.png?apikey="+import.meta.env.VITE_THUNDERFOREST_APIKEY}
                            minZoom={11}
                        /> 
                    </LayerGroup>
                </LayersControl.BaseLayer>
                <LayersControl.Overlay name="Extra information">
                    <LayerGroup>

                    </LayerGroup>
                </LayersControl.Overlay>
                <LayersControl.Overlay name="Path (Polylines)" checked>
                    <LayerGroup>

                    </LayerGroup>
                </LayersControl.Overlay>
            </LayersControl>
            <ScaleControl position='bottomleft' />

        </MapContainer>
    </div>
    )

}

export default Map