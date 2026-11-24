import styles from "./App.module.css";
import { Header } from "./components/Header/Header";
import { GridPanel } from "./components/GridPanel/GridPanel";
import { StatsCards } from "./components/StatCards/StatsCards";
import { ControlsPanel } from "./components/ControlsPanel/ControlsPanel";
import { AlgorithmCard } from "./components/AlgorithmCard/AlgorithmCard";
import { appReducer, initialState } from "./state/appState";
import { useEffect, useReducer } from 'react';
import { useSearch } from './hooks/useSearch';
import { buildFrame, resultMessage } from './state/searchView';
import { usePlayback } from './hooks/usePlayback';
import { api } from './api/api'
import { downloadText } from './utils/download';

export function App() {
  const [state, dispatch] = useReducer(appReducer, initialState);

  useEffect(() => {
    const endPaint = () => dispatch({ type: 'paintEnd' })
    window.addEventListener('pointerup', endPaint)
    window.addEventListener('pointercancel', endPaint)
    return () => {
      window.removeEventListener('pointerup', endPaint)
      window.removeEventListener('pointercancel', endPaint)
    }
    
  }, [])
  const search = useSearch(state.map, state.algorithm, state.bidirectional, state.painting)

  const playback = usePlayback(search.result)

  const frame = search.result && search.resultRequest
    ? buildFrame(search.resultRequest, search.result, playback.index)
    : null
  const openCells = state.map.width * state.map.height - state.map.walls.length

    const errorMessage = (error: unknown) => error instanceof Error ? error.message : 'Something went wrong.'

  const saveMap = () => {
    const { width, height } = state.map
    api.exportMap(state.map)
      .then((json) => downloadText(json, `pathfinder-map-${width}x${height}.json`))
      .catch((error: unknown) => dispatch({ type: 'fileFailed', message: errorMessage(error) }))
  }

  const loadMapFile = (file: File) => {
    if (file.size > 1_000_000) {
      dispatch({ type: 'fileFailed', message: 'That file is too large to be a map.' })
      return
    }
    file.text()
      .then(api.importMap)
      .then((map) => dispatch({ type: 'loadMap', map }))
      .catch((error: unknown) => dispatch({ type: 'fileFailed', message: errorMessage(error) }))
  }

    const generateMap = (seedText: string) => {
    const seed = Number(seedText)
    if (seedText.trim() === '' || !Number.isSafeInteger(seed)) {
      dispatch({ type: 'fileFailed', message: 'The seed must be a whole number.' })
      return
    }
    const { width, height } = state.map
    api.randomMap({ width, height, seed })
      .then((map) => dispatch({ type: 'loadMap', map }))
      .catch((error: unknown) => dispatch({ type: 'fileFailed', message: errorMessage(error) }))
  }

  const generateNewMap = () => {
    const seed = String(Math.floor(Math.random() * 100_000))
    dispatch({ type: 'setSeed', seed })
    generateMap(seed)
  }

  return (
    <div className={styles.page}>
      <div className={styles.layout}>
        <main className={styles.mainColumn}>
          <Header onSave={saveMap} onLoadFile={loadMapFile} />
          <GridPanel
            map={state.map}
            marks={frame?.marks ?? null}
            stale={search.pending}
            onPaintStart={(cell) => dispatch({ type: 'paintStart', cell })}
            onPaintMove={(cell) => dispatch({ type: 'paintMove', cell })}
            pathOrder={frame?.pathOrder ?? null}
            animatePath={playback.celebrate && search.result?.found === true}
          />
          <StatsCards
            visited={frame?.visited ?? 0}
            frontier={frame?.frontier ?? 0}
            pathLength={frame?.pathLength ?? null}
            openCells={openCells}
          />
        </main>

        <div className={styles.sideColumn}>
          <ControlsPanel
            algorithm={state.algorithm}
            bidirectional={state.bidirectional}
            onAlgorithmChange={(algorithm) =>
              dispatch({ type: "setAlgorithm", algorithm })
            }
            onBidirectionalChange={(bidirectional) =>
              dispatch({ type: "setBidirectional", bidirectional })
            }
            tool = {state.tool}
            onToolChange = {(tool) => dispatch({ type: 'setTool', tool })}
            onResetMap = {() => dispatch({ type: 'resetMap' })}
            step = {frame?.step ?? { verb: 'Start at', cell: state.map.start, index: 0, total: 0 }}
            result = {state.fileError ? { tone: 'error', text: state.fileError } : resultMessage(search)}
            playback = {playback}
            width = {state.map.width}
            height = {state.map.height}
            onResize = {(width, height) => dispatch({ type: 'resizeMap', width, height })}
            seed = {state.seed}
            onSeedChange = {(seed) => dispatch({ type: 'setSeed', seed })}
            onGenerateRandom = {generateNewMap}
            onGenerateFromSeed = {() => generateMap(state.seed)}
          />
          <AlgorithmCard
            algorithm={state.algorithm}
            bidirectional={state.bidirectional}
          />
        </div>
      </div>
    </div>
  );
}
