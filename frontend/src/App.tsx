import styles from './App.module.css';
import {Header} from './components/Header/Header';
import {GridPanel} from './components/GridPanel/GridPanel'
import { StatsCards } from './components/StatCards/StatsCards'
import { ControlsPanel } from './components/ControlsPanel/ControlsPanel';

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
          <div className = {styles.placeholder}>Algorithem Card</div>
        </div>
      </div>
    </div>
  );
}