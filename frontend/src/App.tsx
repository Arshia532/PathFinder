import { useEffect, useReducer, useState } from 'react'
import styles from './App.module.css'
import { generateMap, searchMap, type GenerateMode } from './api/client'
import type { AlgorithmId, Cell, SearchResponse } from './api/types'
import { Header } from './components/Header/Header'
import { GridPanel } from './components/GridPanel/GridPanel'
import { StatsCards } from './components/StatCards/StatsCards'
import { ControlsPanel } from './components/ControlsPanel/ControlsPanel'
import { AlgorithmCard } from './components/AlgorithmCard/AlgorithmCard'
import { appReducer, emptyMap, initialState } from './state/appState'
import { playbackFrame } from './state/playback'

const FRAME_MS = 45

export function App() {
    const [state, dispatch] = useReducer(appReducer, initialState)
    const [result, setResult] = useState<SearchResponse | null>(null)
    const [cursor, setCursor] = useState(0)
    const [playing, setPlaying] = useState(false)
    const [busy, setBusy] = useState<'generate' | 'search' | null>(null)
    const [error, setError] = useState('')

    const total = result?.steps.length ?? 0
    const advancing = playing && cursor < total
    const frame = playbackFrame(state.map, result, cursor)

    useEffect(() => {
        if (!advancing) return
        const timer = window.setTimeout(() => setCursor(cursor + 1), FRAME_MS)
        return () => window.clearTimeout(timer)
    }, [advancing, cursor])

    function clearSearch() {
        setPlaying(false)
        setCursor(0)
        setResult(null)
        setError('')
    }

    async function onGenerate(width: number, height: number, seed: string, mode: GenerateMode) {
        setBusy('generate')
        setError('')
        try {
            const map = await generateMap(width, height, seed.trim(), mode)
            clearSearch()
            dispatch({ type: 'setMap', map })
        } catch (failure) {
            setError(failure instanceof Error ? failure.message : 'Could not generate a map.')
        } finally {
            setBusy(null)
        }
    }

    async function runSearch() {
        clearSearch()
        setBusy('search')
        try {
            const search = await searchMap({ ...state.map, algorithm: state.algorithm, bidirectional: false })
            setResult(search)
        } catch (failure) {
            setError(failure instanceof Error ? failure.message : 'Could not run the search.')
        } finally {
            setBusy(null)
        }
    }

    function selectAlgorithm(algorithm: AlgorithmId) {
        if (state.algorithm === algorithm) return
        clearSearch()
        dispatch({ type: 'setAlgorithm', algorithm })
    }

    function editCell(cell: Cell) {
        if (busy) return
        clearSearch()
        dispatch({ type: 'editCell', cell })
    }

    function seek(step: number) {
        setPlaying(false)
        setCursor(Math.min(total, Math.max(0, step)))
    }

    function togglePlay() {
        if (!total) return
        if (cursor === total) setCursor(0)
        setPlaying(!advancing)
    }

    return (
        <div className={styles.page}>
            <div className={styles.layout}>
                <main className={styles.mainColumn}>
                    <Header />
                    <GridPanel map={state.map} frame={frame} onEdit={editCell} />
                    <StatsCards
                        visited={frame.visited.size}
                        frontier={frame.frontier.size}
                        openCells={state.map.width * state.map.height - state.map.walls.length}
                        pathLength={frame.finished ? result?.stats.pathLength ?? null : null}
                    />
                </main>
                <div className={styles.sideColumn}>
                    <ControlsPanel
                        mapControls={{
                            tool: state.tool,
                            busy: busy !== null,
                            onToolChange: tool => dispatch({ type: 'setTool', tool }),
                            onGenerate,
                            onReset: (width, height) => {
                                clearSearch()
                                dispatch({ type: 'setMap', map: emptyMap(width, height) })
                            },
                        }}
                        algorithm={state.algorithm}
                        onAlgorithmChange={selectAlgorithm}
                        playback={{
                            busy,
                            error,
                            cursor,
                            result,
                            playing: advancing,
                            onSearch: runSearch,
                            onSeek: seek,
                            onTogglePlay: togglePlay,
                        }}
                    />
                    <AlgorithmCard algorithm={state.algorithm} bidirectional={false} />
                </div>
            </div>
        </div>
    )
}
