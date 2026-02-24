'use client'

import { useDraggable } from '@dnd-kit/react'
import { useRef } from 'react'

export interface DraggableBlockProps {
	id: string
	value: number
}

export const DraggableBlock = ({ id, value }: DraggableBlockProps) => {
	const elementRef = useRef<HTMLDivElement>(null)
	const { ref, isDragging } = useDraggable({
		id,
		element: elementRef,
		data: { value },
	})

	return (
		<div className="flex flex-col items-center gap-2">
			<div
				ref={(node) => {
					elementRef.current = node
					ref(node)
				}}
				className={`w-10 h-10 rounded-xl bg-blue-500 flex items-center justify-center cursor-grab active:cursor-grabbing shadow-lg hover:bg-blue-600 transition-colors ${
					isDragging ? 'opacity-50' : ''
				}`}
			>
				<span className="text-white text-2xl font-bold">{value}</span>
			</div>
		</div>
	)
}
