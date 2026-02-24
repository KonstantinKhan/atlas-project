'use client'

import { DragDropProvider, useDraggable } from '@dnd-kit/react'

export const MyBlock = () => {
	const { ref } = useDraggable({
		id: 'my-block',
	})

	return (
		<DragDropProvider>
			<div
				ref={ref}
				className={`
				w-24
				pl-2
				bg-amber-800
				rounded-sm
				cursor-grab
				`}
			>
				<h1>Block</h1>
			</div>
		</DragDropProvider>
	)
}
