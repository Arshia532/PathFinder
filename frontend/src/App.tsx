import styles from './App.module.css';
import {Header} from './components/Header/Header';
import {GridPanel} from './components/GridPanel/GridPanel'
import { StatsCards } from './components/StatCards/StatsCards'
import { ControlsPanel } from './components/ControlsPanel/ControlsPanel';
import { AlgorithmCard } from './components/AlgorithmCard/AlgorithmsCard';

export function App() {
  return (
    <div className = {styles.page}>
      <div className = {styles.layout}>
        <main className = {styles.mainColumn}>
          <Header />
          <GridPanel />
          <StatsCards />
        </main>

        <div className = {styles.sideColumn}>
          <ControlsPanel />
          <AlgorithmCard />
        </div>
      </div>
    </div>
  );
}