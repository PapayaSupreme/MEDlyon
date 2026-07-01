/**
 * This file contains the functions that will be used to control the Nodes and Paths that we got.
 * DO NOT USE THIS FILE AS A REACT COMPONENT
 */
import {
    getPosition,
    computePath
} from './bridge'

const Nodes=[], Paths=[]
var failure=false, failuremsg=""

async function ComputeFullPath() {
    if (Nodes.length<2){
        failure=true
        failuremsg="You need at least 2 Nodes, one start and one ending node."
    }
    let pathId=Paths.length
    Paths.push([])
    for (let i=0; i<Nodes.length-1 ; i++){
        Paths[pathId].push(computePath(Nodes[i],Nodes[i+1]))
        //For the time being at a stop there will be duplicate nodes.
    }
    failure=false
    
}

function setNewPos(Location,i){
    console.log("Was called!")
    if (i>Node.length-1){
        Nodes.push(getPosition(Location))
    }
    Nodes[i]=getPosition(Location)
}

export {
    ComputeFullPath,
    failure,
    failuremsg,
    setNewPos,
    Nodes,
    Paths,
}