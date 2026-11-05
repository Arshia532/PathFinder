export const GRID_COLS = 30;
export const GRID_ROWS = 21;


export const placeholderStats = {
    visited : '0', 
    pathLength : '-',
    frontier : '0', 
    coverage: '0%',
    openCells: '630', 
} 

export const placeholderSeed = '48213';

export const placeholderTool = 'wall';

export const placeholderAlgorithm = 'astar'

export const placeholderBidirection = false;

export const placeholderStep = { verb: 'Start at', x: 3, y: 15, current: '0', total: '0' }

export const placeholderResult = {
  tone: 'info',
  text: 'Press play to run the search',
} as const
