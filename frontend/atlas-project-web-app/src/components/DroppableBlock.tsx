'use client'

import { useDroppable } from '@dnd-kit/react'
import { useRef } from 'react'

export interface DroppableBlockProps {
	id: string
	value: number
	label?: string
	children?: React.ReactNode
}

export const DroppableBlock = ({ id, value, label = 'Значение:', children }: DroppableBlockProps) => {
	const elementRef = useRef<HTMLDivElement>(null)
	const { ref, isDropTarget } = useDroppable({
		id,
		element: elementRef,
	})

	return (
		<div className="flex flex-col items-center gap-2">
			<div
				ref={(node) => {
					elementRef.current = node
					ref(node)
				}}
				className={`w-32 h-32 rounded-xl flex items-center justify-center shadow-lg transition-colors relative ${
					isDropTarget ? 'bg-green-200' : 'bg-gray-200'
				}`}
			>
				{children}
			</div>
			<span className="text-gray-700 font-medium">{label} {value}</span>
		</div>
	)
}
