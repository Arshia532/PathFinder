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

  return (
    <div className={styles.page}>
      <div className={styles.layout}>
        <main className={styles.mainColumn}>
          <Header />
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
            result = {resultMessage(search)}
            playback = {playback}
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
