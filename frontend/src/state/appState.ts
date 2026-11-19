import type { AlgorithmId, GridMap } from "../api/types";

export type Tool = 'start' | 'end' | 'wall' | 'erase'

export type AppState = {
    map: GridMap
    tool: Tool
    algorithm: AlgorithmId
    bidirectional: boolean
}

export type AppAction = 
| {type: 'setTool'; tool: Tool}
| {type: 'setAlgorithm'; algorithm: AlgorithmId}
| {type: 'setBidirectional'; bidirectional: boolean}

export const initialState: AppState = {
    map: {
        width: 30,
        height: 21,
        start: {row: 15, column: 3},
        end: {row: 5, column: 26},
        walls: [],
    },
    tool: 'wall',
    algorithm: 'astar',
    bidirectional: false
}

export function appReducer(state: AppState, action: AppAction): AppState {
    switch (action.type) {
        case 'setTool': 
            return {...state, tool: action.tool}
        case 'setAlgorithm': 
            return {...state, algorithm: action.algorithm}
        case 'setBidirectional': 
            return {...state, bidirectional: action.bidirectional}
    } 
}