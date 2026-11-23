import { exportMapRequest, importMapRequest, randomMapRequest, searchRequest } from './client'
import { mockExportMap, mockImportMap, mockRandomMap, mockSearch } from './mockApi'



const useMock = import.meta.env.VITE_API_MODE !== 'real'

console.info(`PathFinder API: ${useMock ? 'mock' : 'Spring Boot backend'}`)

export const api = useMock
    ? { search: mockSearch, exportMap: mockExportMap, importMap: mockImportMap, randomMap: mockRandomMap }
    : { search: searchRequest, exportMap: exportMapRequest, importMap: importMapRequest, randomMap: randomMapRequest }