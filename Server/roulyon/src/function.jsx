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
    Paths.push([{Path:[],Nodes:[]}])
    for (let i=0; i<Nodes.length-1 ; i++){
        Paths[pathId].Nodes.push(computePath(Nodes[i],Nodes[i+1]))
        //For the time being at a stop there will be duplicate nodes.
    }
    setfailure('')

    nodetopath(pathId)
}

async function nodetopath(pathId){
    for (let i=0; i< Paths[pathId].Nodes.length; i++){
        Paths[pathId].Path.push([Paths[pathId].Nodes[i],Paths[pathId].Nodes[i]])
    }
}

async function setNewPos(Location,i){
    if (i>Node.length-1){
        Nodes.push(getPosition(Location))
    }
    Nodes[i]=getPosition(Location)
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