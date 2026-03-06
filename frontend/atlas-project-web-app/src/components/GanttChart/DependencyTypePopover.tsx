'use client'

import { useEffect, useRef } from 'react'

interface DependencyTypePopoverProps {
	position: { x: number; y: number }
	onSelect: (type: string) => void
	onCancel: () => void
}

const DEPENDENCY_TYPES = [
	{ value: 'FS', label: 'FS', description: 'Finish-to-Start' },
	{ value: 'SS', label: 'SS', description: 'Start-to-Start' },
	{ value: 'FF', label: 'FF', description: 'Finish-to-Finish' },
	{ value: 'SF', label: 'SF', description: 'Start-to-Finish' },
] as const

export default function DependencyTypePopover({
	position,
	onSelect,
	onCancel,
}: DependencyTypePopoverProps) {
	const ref = useRef<HTMLDivElement>(null)

	useEffect(() => {
		const handleKeyDown = (e: KeyboardEvent) => {
			if (e.key === 'Escape') onCancel()
		}
		const handleClickOutside = (e: MouseEvent) => {
			if (ref.current && !ref.current.contains(e.target as Node)) {
				onCancel()
			}
		}
		document.addEventListener('keydown', handleKeyDown)
		document.addEventListener('mousedown', handleClickOutside)
		return () => {
			document.removeEventListener('keydown', handleKeyDown)
			document.removeEventListener('mousedown', handleClickOutside)
		}
	}, [onCancel])

	return (
		<div
			ref={ref}
			className="fixed z-50 bg-white dark:bg-zinc-900 rounded-lg shadow-xl border border-gray-200 dark:border-zinc-700 p-2"
			style={{ left: position.x, top: position.y }}
		>
			<p className="text-xs text-gray-500 dark:text-zinc-400 px-2 pb-1">Тип связи</p>
			<div className="flex flex-col gap-0.5">
				{DEPENDENCY_TYPES.map((dt) => (
					<button
						key={dt.value}
						onClick={() => onSelect(dt.value)}
						className="flex items-center gap-2 px-3 py-1.5 text-sm rounded-md hover:bg-gray-100 dark:hover:bg-zinc-800 text-gray-800 dark:text-zinc-200 transition-colors text-left"
					>
						<span className="font-mono font-semibold w-6">{dt.label}</span>
						<span className="text-gray-500 dark:text-zinc-400 text-xs">{dt.description}</span>
					</button>
				))}
			</div>
		</div>
	)
}
