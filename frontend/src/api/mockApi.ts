import type { Cell, GridMap, SearchRequest, SearchResponse, SearchStep } from './types'

const MOCK_DELAY_MS = 150

export function mockSearch(request: SearchRequest, signal?: AbortSignal): Promise<SearchResponse> {
    return new Promise ((resolve, reject) => {
        if (signal?.aborted) {
            reject(signal.reason)
            return
        }

        const timer = setTimeout(() => {
            signal?.removeEventListener('abort', onAbort)
            try {
                resolve(runBfs(request))
            }catch (error){
                reject(error)
            }
        }, MOCK_DELAY_MS)

        function onAbort () {
            clearTimeout(timer)
            reject(signal?.reason)
        }

        signal?.addEventListener('abort', onAbort, { once: true })
    })
}

function runBfs ({width, height, start, end, walls}: SearchRequest): SearchResponse {
    const cellCount = width * height;
    const indexOf = (cell: Cell) => cell.row * width + cell.column
    const cellAt = (index: number): Cell => ({ row: Math.floor(index / width), column: index % width})

    const blocked = new Array<boolean>(cellCount).fill(false)
    for (const wall of walls) blocked[indexOf(wall)] = true

    const startIndex = indexOf(start)
    const endIndex = indexOf(end)

    if (blocked[startIndex] || blocked[endIndex]){
        throw new Error('Start and end must be open cells')
    }
    if (startIndex === endIndex) {
        return {found: true, steps: [], path:[start], stats: {visited: 0, pathLength: 0, pathCost: 0 }
        }
    }

    const neighbors = (index: number): number[] => {
        const row = Math.floor(index/width)
        const column = index % width
        const result: number[] = []

        if(row > 0) result.push(index - width)
        if(column < width - 1) result.push(index + 1)
        if(row < height - 1) result.push(index + width)
        if (column > 0) result.push(index - 1)
        return result.filter((next) => !blocked[next])
    }

    const seen = new Array<boolean>(cellCount).fill(false)
    const parent = new Array<number>(cellCount).fill(-1)

    const queue = [startIndex]
    let head = 0
    seen[startIndex] = true

    const steps: SearchStep[] = []

    while (head < queue.length){
        const current = queue[head++]
        const discovered: Cell[] = []

        if (current !== endIndex) {
            for (const next of neighbors(current)){
                if (!seen[next]) {
                    seen[next] = true
                    parent[next] = current
                    queue.push(next)
                    discovered.push(cellAt(next))
                }
            }
        }
        steps.push({ expanded: cellAt(current), discovered, side: 'forward' })

        if(current == endIndex){
            const path: Cell[] = []
            for (let i = endIndex; i !== -1; i = parent[i]) path.push(cellAt(i))
            path.reverse()
            const moves = path.length - 1
            return {found: true, steps, path, stats: {visited: steps.length, pathLength: moves, pathCost: moves}}
        }
    }
    return {found: false, steps, path: [], stats: {visited: steps.length, pathLength: null, pathCost: null}}
}

export function mockExportMap(map: GridMap): Promise<string> {
    const { width, height, start, end, walls } = map
    return Promise.resolve(JSON.stringify({ width, height, start, end, walls }, null, 2))
}

export function mockImportMap(text: string): Promise<GridMap> {
    try {
        return Promise.resolve(JSON.parse(text) as GridMap)
    } catch {
        return Promise.reject(new Error("This file isn't valid JSON."))
    }
}

export function mockRandomMap(): Promise<GridMap> {
    return Promise.reject(new Error('Random maps need the backend (set VITE_API_MODE=real).'))
}