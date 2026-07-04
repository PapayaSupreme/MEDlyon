/**
 * This file contains the functions that will be used to control the Nodes and Paths that we got.
 * DO NOT USE THIS FILE AS A REACT COMPONENT
 */
import {
    getPosition,
    computePath,
    getClosestNode
} from './bridge'

const Nodes=[], Paths=[{Path:[],Nodes:[]}]

async function ComputeFullPath(setfailure) {
    if (Nodes.length<2){
        setfailure("You need at least 2 Nodes, one start and one ending node.")
        return;
    }
    let pathId=Paths.length
    Paths.push({Path:[],Nodes:[]})
    for (let i=0; i<Nodes.length-1 ; i++){
        const segment = await computePath(Nodes[i],Nodes[i+1])
        Paths[pathId].Nodes.push(...segment)
        Paths[pathId].Path.push(...segment)
    }
    setfailure('')
}

async function setNewPos(Location,i){
    const node = await getPosition(Location)
    if (i>Nodes.length-1){
        Nodes.push(node)
        return
    }
    Nodes[i]=node
}

function AddNode(position){
    getClosestNode(position.lat, position.lng).then(Nod =>{
        if (Nod) Nodes.push(Nod)
    })
}

export {
    ComputeFullPath,
    setNewPos,
    AddNode,
    Nodes,
    Paths,
}
