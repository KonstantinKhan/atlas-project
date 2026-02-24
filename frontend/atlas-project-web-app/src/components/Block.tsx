'use client'

import { DragDropProvider, useDragDropMonitor, useDragOperation } from '@dnd-kit/react'
import { useState, useMemo } from 'react'
import { DraggableBlock } from './DraggableBlock'
import { DroppableBlock } from './DroppableBlock'
import { DefaultZone } from './DefaultZone'

interface DraggableItem {
	id: string
	value: number
	location: string // 'default' | id принимающего блока
}

const initialDraggableItems: DraggableItem[] = [
	{ id: 'draggable-1', value: 10, location: 'default' },
	{ id: 'draggable-2', value: 5, location: 'default' },
]

const droppableZones = [
	{ id: 'zone-a', label: 'Зона A' },
	{ id: 'zone-b', label: 'Зона B' },
]

function BlockContent() {
	const [items, setItems] = useState<DraggableItem[]>(initialDraggableItems)
	const dragOperation = useDragOperation()

	useDragDropMonitor({
		onDragEnd() {
			if (!dragOperation.source) return

			const draggedId = dragOperation.source.id as string
			const targetId = dragOperation.target?.id as string | undefined

			setItems((prevItems) =>
				prevItems.map((item) => {
					if (item.id === draggedId) {
						// Если перетащили на принимающий блок - обновляем локацию
						if (targetId && droppableZones.some((z) => z.id === targetId)) {
							return { ...item, location: targetId }
						}
						// Иначе - возвращаем в зону по умолчанию
						return { ...item, location: 'default' }
					}
					return item
				})
			)
		},
	})

	// Вычисляем значения для зон
	const zoneValues = useMemo(() => {
		const values: Record<string, number> = {}
		for (const zone of droppableZones) {
			values[zone.id] = items
				.filter((item) => item.location === zone.id)
				.reduce((sum, item) => sum + item.value, 0)
		}
		return values
	}, [items])

	// Вычисляем значение зоны по умолчанию
	const defaultValue = useMemo(() => {
		return items
			.filter((item) => item.location === 'default')
			.reduce((sum, item) => sum + item.value, 0)
	}, [items])

	// Блоки в зоне по умолчанию
	const defaultItems = items.filter((item) => item.location === 'default')

	return (
		<div className="min-h-screen flex flex-col items-center justify-center gap-8 p-8">
			{/* Зона по умолчанию */}
			<div className="w-full max-w-md">
				<DefaultZone value={defaultValue} />
				{/* Блоки в зоне по умолчанию рендерим здесь */}
				<div className="flex gap-4 mt-4 justify-center flex-wrap">
					{defaultItems.map((item) => (
						<DraggableBlock
							key={item.id}
							id={item.id}
							value={item.value}
						/>
					))}
				</div>
			</div>

			{/* Принимающие зоны */}
			<div className="flex gap-16">
				{droppableZones.map((zone) => {
					const zoneItems = items.filter((item) => item.location === zone.id)
					return (
						<DroppableBlock key={zone.id} id={zone.id} value={zoneValues[zone.id]} label={zone.label}>
							{/* Блоки в этой зоне рендерим внутри зоны */}
							<div className="flex gap-2 flex-wrap justify-center">
								{zoneItems.map((item) => (
									<DraggableBlock
										key={item.id}
										id={item.id}
										value={item.value}
									/>
								))}
							</div>
						</DroppableBlock>
					)
				})}
			</div>
		</div>
	)
}

export default function Block() {
	return (
		<DragDropProvider>
			<BlockContent />
		</DragDropProvider>
	)
}
