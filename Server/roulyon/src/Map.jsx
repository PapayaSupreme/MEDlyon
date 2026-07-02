import { MapContainer } from 'react-leaflet/MapContainer'
import { LayersControl, Marker, Polyline, ScaleControl } from 'react-leaflet'
import { LayerGroup } from 'react-leaflet/LayerGroup'
import { TileLayer } from 'react-leaflet/TileLayer'
import { useMap, useMapEvent } from 'react-leaflet/hooks'
import "leaflet/dist/leaflet.css"
import './Map.css'
import { Control, Layer } from 'leaflet'
import { Nodes, Paths, AddNode } from './function'
import { useState } from 'react'
import  DraggableMarker  from './DraggableMarker'

function Map(){
    const [pathId, setPathId] = useState(0)

    return (
    <div className='leaflet-position'>
        {/** With react-leaflet: */}
        <MapContainer center={[45.74944,4.83604]} zoom={13} doubleClickZoom={false}>
            <LayersControl position='topright'>
                <LayersControl.BaseLayer name="Osm default map" checked>
                    <TileLayer
                        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        minZoom={11}
                    />
                </LayersControl.BaseLayer>
                <LayersControl.BaseLayer name="Transport - Thunderforest (Uses APIkey)">
                    <LayerGroup>
                        <TileLayer 
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors,  Tiles courtesy of<a href="https://www.thunderforest.com/"> Andy Allan.</a> (<a href="https://www.thunderforest.com/" target="_blank">&copy; Thunderforest</a>)'    
                            url={"https://api.thunderforest.com/transport/{z}/{x}/{y}{r}.png?apikey="+import.meta.env.VITE_THUNDERFOREST_APIKEY}
                            minZoom={11}
                        /> 
                    </LayerGroup>
                </LayersControl.BaseLayer>
                <LayersControl.BaseLayer name="Transport dark - Thunderforest (Uses APIkey)">
                    <LayerGroup>
                        <TileLayer 
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors,  Tiles courtesy of<a href="https://www.thunderforest.com/"> Andy Allan.</a> (<a href="https://www.thunderforest.com/" target="_blank">&copy; Thunderforest</a>)'    
                            url={"https://api.thunderforest.com/transport-dark/{z}/{x}/{y}{r}.png?apikey="+import.meta.env.VITE_THUNDERFOREST_APIKEY}
                            minZoom={11}
                        /> 
                    </LayerGroup>
                </LayersControl.BaseLayer>
                <LayersControl.BaseLayer name="Atlas - Thunderforest (Uses APIkey)">
                    <LayerGroup>
                        <TileLayer 
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors,  Tiles courtesy of<a href="https://www.thunderforest.com/"> Andy Allan.</a> (<a href="https://www.thunderforest.com/" target="_blank">&copy; Thunderforest</a>)'    
                            url={"https://api.thunderforest.com/mobile-atlas/{z}/{x}/{y}{r}.png?apikey="+import.meta.env.VITE_THUNDERFOREST_APIKEY}
                            minZoom={11}
                        /> 
                    </LayerGroup>
                </LayersControl.BaseLayer>
                <LayersControl.BaseLayer name="Neighbourhood - Thunderforest (Uses APIkey)">
                    <LayerGroup>
                        <TileLayer 
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors,  Tiles courtesy of<a href="https://www.thunderforest.com/"> Andy Allan.</a> (<a href="https://www.thunderforest.com/" target="_blank">&copy; Thunderforest</a>)'    
                            url={"https://api.thunderforest.com/neighbourhood/{z}/{x}/{y}{r}.png?apikey="+import.meta.env.VITE_THUNDERFOREST_APIKEY}
                            minZoom={11}
                        /> 
                    </LayerGroup>
                </LayersControl.BaseLayer>
                <LayersControl.Overlay name="Markers (starting and ending nodes)">
                    <LayerGroup>
                        <Markers/>{/**A function defined below */}
                    </LayerGroup>
                </LayersControl.Overlay>
                <LayersControl.Overlay name="Path (Polylines)" checked>
                    <LayerGroup>
                        <Polyline pathOptions={{color:"red"}} positions={Paths[pathId].Path}></Polyline>
                    </LayerGroup>
                </LayersControl.Overlay>
            </LayersControl>
            <ScaleControl position='bottomleft' />
        </MapContainer>
        <div className='PathDisplay'>
            <ol>
                <ShowPaths setPathId />
            </ol>
        </div>
        {/**The following buttons are developper mode buttons. */}
        <button onClick={e=>{addPolyline();console.log(Paths[pathId]);setPathId(0)}}>Temp add new line</button>
        <button onClick={e=>{setPathId(-1)}}>ChangePathId</button>
    </div>
    )

}
/**
 * This is only a test function that will be removed when the connection will exist.
 */
function addPolyline(){
    Paths[0].Path.push([45.74944+0.01*Paths[0].length,4.83604+0.01*Paths[0].length])
}

function Markers(){
    const map = useMapEvent({
        dblclick(e){
            AddNode(e.latlng)
        }
    })

    const marker= []
    for (let i=0 ; i < Nodes.length ; i++){
        marker.push(<DraggableMarker pos={[Nodes[i].lat,Nodes[i].lng]} name={Nodes[i].name} />)
    }
    return marker
}

function ShowPaths({setPathId}){
    const itinerary = []
    for (let i=0 ; i < Paths.length ; i++){
        itinerary.push(<li key={i}><h2>Path {i}:</h2></li>)
        if (i ==0 ) itinerary.push(<>(Dev path)</>);
        itinerary.push(<br />)
        for (let x=0; x < Paths[i].Nodes.length; x++){
        itinerary.push(
            <Itinerary x={x} node={Paths[i].Nodes[x]}></Itinerary>
            )
        }
    }
    return <ul>{itinerary}</ul>
}

function Itinerary({node}){
    return <>{x}. {node.name} {node.Additional_Information ? '' : '('+node.Additional_Information+')'} <br /></>
}

export default Map