import type { AlgorithmId, Cell, GridMap } from "../api/types";
import { GRID_MAX_SIZE, GRID_MIN_SIZE } from "../data/gridPresets";

export type Tool = 'start' | 'end' | 'wall' | 'erase'

export type AppState = {
    map: GridMap
    tool: Tool
    algorithm: AlgorithmId
    bidirectional: boolean
    painting: boolean
    fileError: string | null
    seed: string
}

export type AppAction = 
| {type: 'setTool'; tool: Tool}
| {type: 'setAlgorithm'; algorithm: AlgorithmId}
| {type: 'setBidirectional'; bidirectional: boolean}
| {type: 'paintStart'; cell: Cell}
| {type: 'paintMove'; cell: Cell}
| {type: 'paintEnd'}
| {type: 'resetMap'}
| {type: 'resizeMap'; width: number; height: number}
| {type: 'loadMap'; map: GridMap}
| {type: 'fileFailed'; message: string}
| {type: 'setSeed'; seed: string}


// Default start and end. MapService.random in the backend uses the same spots; change both together.
export function createMap(width: number, height: number): GridMap {
    return {
        width,
        height,
        start: {row: height - 6, column: 3},
        end: {row: 5, column: width - 4},
        walls: [],
    }
}


export const initialState: AppState = {
    map: createMap(30, 21),
    tool: 'wall',
    algorithm: 'bfs',
    bidirectional: false,
    painting: false,
    fileError: null,
    seed: '',
}

export function appReducer(state: AppState, action: AppAction): AppState {
    switch (action.type) {
        case 'setTool': 
            return {...state, tool: action.tool}
        case 'setAlgorithm': 
            return {...state, algorithm: action.algorithm}
        case 'setBidirectional': 
            return {...state, bidirectional: action.bidirectional}
        case 'paintStart':
            return {...state, painting: true, fileError: null, map: paintCell(state.map, state.tool, action.cell)}
        case 'paintMove': {
            if (!state.painting) return state
            const movingEndpoint = state.tool === 'start' || state.tool === 'end'
            if (movingEndpoint && isWall(state.map, action.cell)) return state
            const map = paintCell(state.map, state.tool, action.cell)
            return map === state.map ? state : {...state, map}
        }
        case 'paintEnd':
            return state.painting ? {...state, painting: false} : state
        case 'resetMap':
            return {...state, fileError: null, map: createMap(state.map.width, state.map.height)}
        case 'resizeMap': {
            const width = clampSize(action.width)
            const height = clampSize(action.height)
            if (width === state.map.width && height === state.map.height) return state
            return {...state, fileError: null, map: resizeMap(state.map, width, height)}
        }
        case 'loadMap':
            return {...state, map: action.map, fileError: null}
        case 'fileFailed':
            return {...state, fileError: action.message}
        case 'setSeed':
            return {...state, seed: action.seed}

    }   
}

function isWall(map: GridMap, cell: Cell) {
    return map.walls.some((wall) => sameCell(wall, cell))
}

function sameCell(a: Cell, b: Cell) {
    return a.row === b.row && a.column === b.column
}

function paintCell(map: GridMap, tool: Tool, cell: Cell): GridMap {
    const isStart = sameCell(cell, map.start)
    const isEnd = sameCell(cell, map.end)
    const cellIsWall = isWall(map, cell)
    const wallsWithoutCell = map.walls.filter((wall) => !sameCell(wall, cell))

    switch (tool) {
        case 'start':
            if (isStart || isEnd) return map
            return {...map, start: cell, walls: wallsWithoutCell}
        case 'end':
            if (isEnd || isStart) return map
            return {...map, end: cell, walls: wallsWithoutCell}
        case 'wall':
            if (isStart || isEnd || cellIsWall) return map
            return {...map, walls: [...map.walls, cell]}
        case 'erase':
            if (!cellIsWall) return map
            return {...map, walls: wallsWithoutCell}
    }
}

function clampSize(size: number) {
    return Math.max(GRID_MIN_SIZE, Math.min(GRID_MAX_SIZE, Math.round(size)))
}

/** Keeps walls that still fit. Endpoints outside the new size move to the nearest edge cell. */
function resizeMap(map: GridMap, width: number, height: number): GridMap {
    const clamp = (cell: Cell): Cell => ({
        row: Math.min(cell.row, height - 1),
        column: Math.min(cell.column, width - 1),
    })
    const start = clamp(map.start)
    const end = clamp(map.end)
    if (sameCell(start, end)) return createMap(width, height)

    const walls = map.walls.filter((wall) =>
        wall.row < height && wall.column < width && !sameCell(wall, start) && !sameCell(wall, end),
    )
    return {width, height, start, end, walls}
}