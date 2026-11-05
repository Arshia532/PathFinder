export const algorithms = [
  {
    id: 'bfs',
    label: 'BFS',
    name: 'Breadth-first search',
    description:
      'Explores in rings: every cell 1 step away, then every cell 2 steps away, and so on, using a queue. On a grid where every move costs the same, it always finds the shortest path.',
  },
  {
    id: 'dfs',
    label: 'DFS',
    name: 'Depth-first search',
    description:
      'Follows one direction as far as it can using a stack, and only backs up at dead ends. It is quick to write and uses little memory, but the path it finds is usually not the shortest.',
  },
  {
    id: 'astar',
    label: 'A*',
    name: 'A* search',
    description:
      'Like Dijkstra, but each step also adds a guess of the remaining distance to the end (Manhattan distance here). That pull toward the goal means far fewer cells are explored, and the path is still the shortest as long as the guess never overestimates.',
  },
  {
    id: 'dijkstra',
    label: 'Dijkstra',
    name: 'Dijkstra',
    description:
      'Starts with distance 0 at the start and infinity everywhere else, then keeps visiting the unvisited cell with the smallest known distance and updating its neighbours. It always finds the cheapest path, and it matters most once cells have different costs.',
  },
] as const