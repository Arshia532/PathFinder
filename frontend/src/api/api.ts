import { exportMapRequest, importMapRequest, searchRequest } from './client'
import { mockExportMap, mockImportMap, mockSearch } from './mockApi'


const useMock = import.meta.env.VITE_API_MODE !== 'real'

console.info(`PathFinder API: ${useMock ? 'mock' : 'Spring Boot backend'}`)

export const api = useMock
    ? { search: mockSearch, exportMap: mockExportMap, importMap: mockImportMap }
    : { search: searchRequest, exportMap: exportMapRequest, importMap: importMapRequest }