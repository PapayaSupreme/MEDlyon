/**
 * This file contains the functions that will bridge between the java backend and this Node server. 
 * DO NOT USE THIS FILE AS A REACT COMPONENT
 */

/**
 * A simple class to represent the points on the map.
 * It contains latitude and longitude
 * And may have a name.
 */
class Node{
    constructor(lat,lng,name,Additional_Information){
        this.lat=lat;
        this.lng=lng;
        this.name=name;//May not be set
        this.Info=Additional_Information;//May not be set
    }
}

/**
 * This functions takes in two Nodes, a Starting node (s) and an Ending Node (e)
 * It will contact the java backend to compute the path between these two Nodes.
 * The response will be structured the following way :
 * 
 * {
 *  Path:[
 *      sNode,
 *      ...,
 *      eNode
 *      ]
 * }
 * 
 * With information inside the Nodes : { lat:xxx , lng:xxx , name:xxx , Additional_Information : [ xxx ] }
 * 
 * Additional_Information can contain many things like the distance, the instruction etc...
 */
async function computePath(sNode, eNode){
    endpoint="" //unknown
    var Path=[]
    await fetch(import.meta.env.Vite_Javalink+endpoint+`?slat=${sNode.lat}&slng=${sNode.lng}&elat=${eNode.lat}&elng=${eNode.lng}`, {
        method:"GET",
    }).then(resp => {
        return resp.json()
    }).then(data => {
        for (item in data.Path){
            Path.push(new Node(item.lat,item.lng,item.name,item.Additional_Information))
        }
    }).catch(err =>{
        console.warn("There was an error while computing the shortest path.")
        console.error(err)
    })

    return Path
}

/**
 * This function takes in one string, Location
 * It represents the Location of the Node that the user type inside the input.
 * Or it could also be interpreted as an Id if you can use ParseInt on it.
 */ 
async function getPosition(Location){
    endpoint="" //unknown
    var NodePos
    await fetch(import.meta.env.Vite_Javalink+endpoint+`?Nodename=${Location}`, {
        method:"GET"
    }).then(resp => {
        return resp.json()
    }).then(data => {
        NodePos = new Node(data.lat,data.lng,data.name)
        //We rename the Node to autocorrect to the right writing if there are some differences.
    }).catch(err => {
        console.warn("There was an error while trying to retrieve the position of the node you searched for.")
        console.error(err)
    })

    return NodePos
} 

/**
 * This function takes in two integers, latitude and longitude.
 * It calls the Java backend and returns a string in the parameter name.
 * And the actual position of the Node with parameters lat and lng.
 * This string represents the name of the node closest to the provided latitude and longitude.
 * (This is to give a name to markers and make selection of points easier for the user.)
 */
async function getClosestNode(lat,lng){
    endpoint="" //unknown
    var ClosestNode
    await fetch(import.meta.env.Vite_Javalink+endpoint+`?lat=${lat}&lng=${lng}`, {
        method:"GET"
    }).then(resp => {
        return resp.json()
    }).then(data => {
        ClosestNode = new Node(data.lat,data.lng,data.name)
    }).catch(err => {
        console.warn("There was an error while trying to retrieve the name of the node closest to the point you clicked.")
        console.error(err)
    })

    //To create a polyline between the point and the closest Node! But this happens outside this function.
    return ClosestNode 
}

export {
    getClosestNode,
    getPosition,
    computePath
}

/* Template for fetch requests.
async function computePath(){
    endpoint="" //unknown
    var xxx
    await fetch(import.meta.env.Vite_Javalink+endpoint+`?`, {
        method:"GET"
    }).then(resp => {
        return resp.json()
    }).then(data => {

    })
} 
*/