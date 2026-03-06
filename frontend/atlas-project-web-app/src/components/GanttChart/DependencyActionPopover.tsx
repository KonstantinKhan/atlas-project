'use client'

import { useEffect, useRef } from 'react'

const DEPENDENCY_TYPES = ['FS', 'SS', 'FF', 'SF'] as const

interface DependencyActionPopoverProps {
	position: { x: number; y: number }
	dependencyType: string
	onChangeType: (newType: string) => void
	onDelete: () => void
	onCancel: () => void
}

export default function DependencyActionPopover({
	position,
	dependencyType,
	onChangeType,
	onDelete,
	onCancel,
}: DependencyActionPopoverProps) {
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
			<div className="flex gap-0.5 px-1 pb-1.5">
				{DEPENDENCY_TYPES.map((t) => (
					<button
						key={t}
						onClick={() => { if (t !== dependencyType) onChangeType(t) }}
						className={`px-2 py-0.5 text-xs font-mono font-semibold rounded transition-colors ${
							t === dependencyType
								? 'bg-indigo-100 dark:bg-indigo-900 text-indigo-700 dark:text-indigo-300'
								: 'text-gray-500 dark:text-zinc-400 hover:bg-gray-100 dark:hover:bg-zinc-800'
						}`}
					>
						{t}
					</button>
				))}
			</div>
			<button
				onClick={onDelete}
				className="flex items-center gap-2 px-3 py-1.5 text-sm rounded-md hover:bg-red-50 dark:hover:bg-red-950 text-red-600 dark:text-red-400 transition-colors text-left w-full"
			>
				Удалить
			</button>
		</div>
	)
}
