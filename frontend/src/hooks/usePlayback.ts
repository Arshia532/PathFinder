import { useEffect, useState } from 'react'
import type { SearchResponse } from '../api/types'

const TICK_MS = 30
const TARGET_TICKS = 200 // a full replay takes about TARGET_TICKS * TICK_MS, whatever the grid size

export type Playback = {
    index: number // steps shown; total = finished frame
    total: number
    playing: boolean
    first: () => void
    previous: () => void
    togglePlay: () => void
    next: () => void
    last: () => void
    celebrate: boolean
}

type PlaybackPosition = {
    owner: SearchResponse | null // the result this position belongs to
    index: number
    playing: boolean
    celebrate: boolean
}

export function usePlayback(result: SearchResponse | null): Playback {
    
    const total = result?.steps.length ?? 0
    const [stored, setStored] = useState<PlaybackPosition>({ owner: null, index: 0, playing: false, celebrate: false })

    const position = stored.owner === result ? stored : { owner: result, index: total, playing: false, celebrate: false }

    useEffect(() => {
        if (!position.playing) return
        const stepsPerTick = Math.max(1, Math.round(total / TARGET_TICKS))
        const timer = setInterval(() => {
            setStored((current) => {
                if (current.owner !== result || !current.playing) return current
                const index = Math.min(current.index + stepsPerTick, total)
                return { ...current, index, playing: index < total, celebrate: index >= total }
            })
        }, TICK_MS)
        return () => clearInterval(timer)
    }, [position.playing, result, total])

    const goTo = (index: number) =>
        setStored({ owner: result, index: Math.max(0, Math.min(index, total)), playing: false, celebrate: false })

    return {
        index: position.index,
        total,
        playing: position.playing,
        first: () => goTo(0),
        previous: () => goTo(position.index - 1),
        next: () => goTo(position.index + 1),
        last: () => goTo(total),
        togglePlay: () => {
            if (!result) return
            if (position.playing) {
                setStored({ ...position, playing: false, celebrate: false })
            } else {
                setStored({ owner: result, index: position.index >= total ? 0 : position.index, playing: true, celebrate: false })
            }
        },
        celebrate: position.celebrate,
    }
}