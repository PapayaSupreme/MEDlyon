class TransitStop {
    constructor(id, lat, lng, name, additionalInformation = []) {
        this.id = id
        this.lat = lat
        this.lng = lng
        this.name = name
        this.Additional_Information = additionalInformation
    }
}

const baseUrl = import.meta.env.VITE_JAVA_LINK ?? 'http://localhost:8080/api'

function parseStopId(additionalInformation) {
    if (!Array.isArray(additionalInformation)) return ''
    const match = additionalInformation.find(item => typeof item === 'string' && item.startsWith('stop_id='))
    return match ? match.slice('stop_id='.length) : ''
}

async function requestJson(path, failureLabel) {
    const response = await fetch(`${baseUrl}${path}`, {
        method: 'GET',
    }).catch(err => {
        console.warn(failureLabel)
        console.error(err)
        return null
    })

    if (!response || !response.ok) return undefined
    return response.json()
}

async function getStops() {
    const data = await requestJson('/stops', 'There was an error while loading the stop list.')
    return Array.isArray(data)
        ? data.map(item => new TransitStop(item.id, item.lat, item.lng, item.name, item.Additional_Information ?? []))
        : []
}

async function computePath(sNode, eNode) {
    const data = await requestJson(
        `/path?slat=${encodeURIComponent(sNode.lat)}&slng=${encodeURIComponent(sNode.lng)}&elat=${encodeURIComponent(eNode.lat)}&elng=${encodeURIComponent(eNode.lng)}`,
        'There was an error while computing the shortest path.'
    )

    if (!data || !Array.isArray(data.Path)) return []

    return data.Path.map(item => new TransitStop(
        parseStopId(item.Additional_Information),
        item.lat,
        item.lng,
        item.name,
        item.Additional_Information ?? []
    ))
}

async function comparePathAlgorithms(sNode, eNode) {
    const data = await requestJson(
        `/compare-path?slat=${encodeURIComponent(sNode.lat)}&slng=${encodeURIComponent(sNode.lng)}&elat=${encodeURIComponent(eNode.lat)}&elng=${encodeURIComponent(eNode.lng)}`,
        'There was an error while comparing Dijkstra and A*.'
    )

    if (!data) {
        return {
            dijkstra: [],
            aStar: [],
            dijkstraTimeNanos: 0,
            aStarTimeNanos: 0,
        }
    }

    const mapPath = (path) => (path?.Path ?? []).map(item => new TransitStop(
        parseStopId(item.Additional_Information),
        item.lat,
        item.lng,
        item.name,
        item.Additional_Information ?? []
    ))

    return {
        dijkstra: mapPath(data.dijkstra),
        aStar: mapPath(data.aStar),
        dijkstraTimeNanos: data.dijkstraTimeNanos ?? 0,
        aStarTimeNanos: data.aStarTimeNanos ?? 0,
    }
}

async function getPosition(location) {
    const data = await requestJson(`/position?Nodename=${encodeURIComponent(location)}`, 'There was an error while retrieving a stop position.')
    if (!data) return undefined
    return new TransitStop(parseStopId(data.Additional_Information), data.lat, data.lng, data.name, data.Additional_Information ?? [])
}

async function getClosestNode(lat, lng) {
    const data = await requestJson(`/closest-node?lat=${encodeURIComponent(lat)}&lng=${encodeURIComponent(lng)}`, 'There was an error while retrieving the closest stop.')
    if (!data) return undefined
    return new TransitStop(parseStopId(data.Additional_Information), data.lat, data.lng, data.name, data.Additional_Information ?? [])
}

export {
    TransitStop,
    getClosestNode,
    getPosition,
    getStops,
    computePath,
    comparePathAlgorithms
}
