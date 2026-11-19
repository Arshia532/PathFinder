import { useReducer } from 'react';
import styles from './App.module.css';
import {Header} from './components/Header/Header';
import {GridPanel} from './components/GridPanel/GridPanel'
import { StatsCards } from './components/StatCards/StatsCards'
import { ControlsPanel } from './components/ControlsPanel/ControlsPanel';
import { AlgorithmCard } from './components/AlgorithmCard/AlgorithmCard';
import { appReducer, initialState } from './state/appState';

export function App() {

  const [state, dispatch] = useReducer(appReducer, initialState)

  return (
    <div className = {styles.page}>
      <div className = {styles.layout}>
        <main className = {styles.mainColumn}>
          <Header />
          <GridPanel />
          <StatsCards />
        </main>

        <div className = {styles.sideColumn}>
          <ControlsPanel 
            algorithm = {state.algorithm}
            bidirectional = {state.bidirectional}
            onAlgorithmChange = {(algorithm) => dispatch({ type:'setAlgorithm',  algorithm})}
            onBidirectionalChange = {(bidirectional) => dispatch({type: 'setBidirectional', bidirectional})}
          />
          <AlgorithmCard algorithm={state.algorithm} bidirectional={state.bidirectional}/>
        </div>
      </div>
    </div>
  );
}