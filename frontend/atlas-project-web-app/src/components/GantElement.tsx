'use client'

import { useState, useRef, useCallback, useEffect } from 'react'
import {
	DragDropProvider,
	useDraggable,
	useDragDropMonitor,
} from '@dnd-kit/react'

interface GantElementProps {
	title?: string
	width?: number
	minWidth?: number
	maxWidth?: number
	onChange?: (width: number) => void
	onResizeStart?: () => void
	onResizeEnd?: (width: number) => void
	onPositionChange?: (position: { x: number; y: number }) => void
	className?: string
	children?: React.ReactNode
	x?: number
	y?: number
}

function GantElementContent({
	title,
	width: controlledWidth,
	minWidth = 100,
	maxWidth,
	onChange,
	onResizeStart,
	onResizeEnd,
	onPositionChange,
	className = '',
	children,
	x: controlledX,
	y: controlledY,
}: GantElementProps) {
	const [internalWidth, setInternalWidth] = useState(controlledWidth ?? 200)
	const [isResizing, setIsResizing] = useState(false)
	const [resizeHandle, setResizeHandle] = useState<'left' | 'right' | null>(
		null,
	)
	const [position, setPosition] = useState({ x: 0, y: 0 })
	const containerRef = useRef<HTMLDivElement>(null)
	const leftHandleRef = useRef<HTMLDivElement>(null)
	const rightHandleRef = useRef<HTMLDivElement>(null)
	const isResizingRef = useRef(false)
	const resizeHandleRef = useRef<'left' | 'right' | null>(null)
	const startX = useRef(0)
	const startWidth = useRef(0)
	const startPositionX = useRef(0)
	const startPositionY = useRef(0)
	const positionRef = useRef(position)
	const onPositionChangeRef = useRef(onPositionChange)
	const accumulatedPositionRef = useRef({ x: 0, y: 0 })

	const { ref: dragRef, isDragging } = useDraggable({
		id: `gant-element-${title || 'unknown'}`,
		element: containerRef,
		data: { title },
	})

	const width = controlledWidth ?? internalWidth

	// Обновляем ref
	useEffect(() => {
		positionRef.current = position
	}, [position])

	useEffect(() => {
		onPositionChangeRef.current = onPositionChange
	}, [onPositionChange])

	// Синхронизируем resizeHandleRef с состоянием
	useEffect(() => {
		resizeHandleRef.current = resizeHandle
	}, [resizeHandle])

	// Используем controlled или state значение
	const currentX = controlledX ?? position.x
	const currentY = controlledY ?? position.y

	const handleResizeStart = useCallback(
		(handle: 'left' | 'right') => (event: React.MouseEvent) => {
			event.preventDefault()
			event.stopPropagation()
			setIsResizing(true)
			setResizeHandle(handle)
			isResizingRef.current = true
			resizeHandleRef.current = handle
			startX.current = event.clientX
			startWidth.current = width
			startPositionX.current = positionRef.current.x
			startPositionY.current = positionRef.current.y
			onResizeStart?.()
		},
		[width, onResizeStart],
	)

	const handleResize = useCallback(
		(event: MouseEvent) => {
			if (!isResizingRef.current) return

			const deltaX = event.clientX - startX.current
			let newWidth = startWidth.current
			const currentHandle = resizeHandleRef.current

			if (currentHandle === 'right') {
				// Правая граница: движение вправо увеличивает ширину
				newWidth = startWidth.current + deltaX
			} else if (currentHandle === 'left') {
				// Левая граница: движение влево (отрицательный deltaX) увеличивает ширину
				newWidth = startWidth.current - deltaX

				// Проверяем, достигла ли ширина минимума
				const widthAtMin = newWidth <= minWidth
				const isMovingRight = deltaX > 0 // Движение вправо уменьшает ширину

				// Перемещаем элемент только если ширина не уперлась в минимум
				// или если мы двигаемся влево (увеличиваем ширину)
				if (controlledX === undefined) {
					// Ограничиваем deltaX, чтобы элемент не "сжимался"
					const maxDeltaX = startWidth.current - minWidth
					const limitedDeltaX = isMovingRight && widthAtMin ? maxDeltaX : deltaX

					const newX = startPositionX.current + limitedDeltaX
					const newY = startPositionY.current
					setPosition({ x: newX, y: newY })
				}
			}

			newWidth = Math.max(minWidth, newWidth)
			if (maxWidth) {
				newWidth = Math.min(maxWidth, newWidth)
			}

			if (controlledWidth !== undefined) {
				onChange?.(newWidth)
			} else {
				setInternalWidth(newWidth)
				onChange?.(newWidth)
			}
		},
		[minWidth, maxWidth, controlledWidth, onChange, controlledX],
	)

	const handleResizeEnd = useCallback(() => {
		if (isResizingRef.current) {
			setIsResizing(false)
			setResizeHandle(null)
			isResizingRef.current = false
			resizeHandleRef.current = null
			onResizeEnd?.(width)
		}
	}, [width, onResizeEnd])

	useEffect(() => {
		if (isResizing) {
			window.addEventListener('mousemove', handleResize)
			window.addEventListener('mouseup', handleResizeEnd)
			return () => {
				window.removeEventListener('mousemove', handleResize)
				window.removeEventListener('mouseup', handleResizeEnd)
			}
		}
	}, [isResizing, handleResize, handleResizeEnd])

	// Используем useDragDropMonitor для отслеживания перетаскивания через dnd-kit
	useDragDropMonitor({
		onBeforeDragStart(event) {
			// Отменяем перетаскивание, если изменяем размер
			if (isResizingRef.current) {
				event.preventDefault()
			}
		},
		onDragStart(event) {
			const sourceId = event.operation.source?.id as string
			if (sourceId === `gant-element-${title || 'unknown'}`) {
				// Сохраняем текущую позицию как базовую для накопления
				accumulatedPositionRef.current = { x: currentX, y: currentY }
			}
		},
		onDragMove(event) {
			const sourceId = event.operation.source?.id as string
			if (sourceId === `gant-element-${title || 'unknown'}`) {
				// Получаем трансформацию от dnd-kit
				const transform = event.operation.transform
				if (transform) {
					// Новая позиция = накопленная позиция + transform
					const newX = accumulatedPositionRef.current.x + transform.x
					const newY = accumulatedPositionRef.current.y + transform.y

					if (controlledX === undefined) {
						setPosition({ x: newX, y: newY })
					}
					if (controlledY === undefined) {
						setPosition({ x: newX, y: newY })
					}
				}
			}
		},
		onDragEnd(event) {
			const sourceId = event.operation.source?.id as string
			if (sourceId === `gant-element-${title || 'unknown'}`) {
				// Сохраняем финальную позицию
				onPositionChangeRef.current?.(positionRef.current)
			}
		},
	})

	// Обработчики для зацепов - используем pointerdown для раннего перехвата
	const handleResizePointerDown = useCallback((handle: 'left' | 'right') => {
		isResizingRef.current = true
		resizeHandleRef.current = handle
	}, [])

	const handleResizePointerUp = useCallback(() => {
		// Сбрасываем только если не в процессе изменения размера
		setTimeout(() => {
			if (!isResizing) {
				isResizingRef.current = false
				resizeHandleRef.current = null
			}
		}, 0)
	}, [isResizing])

	return (
		<div
			ref={(node) => {
				containerRef.current = node
				dragRef(node)
			}}
			className={`relative flex items-center h-12 bg-indigo-500 rounded-lg shadow-md select-none ${className} ${
				isDragging ? 'opacity-50 cursor-grabbing' : 'cursor-grab'
			}`}
			style={{
				width: `${width}px`,
				left: `${currentX}px`,
				top: `${currentY}px`,
				position: 'fixed',
			}}
		>
			{/* Левый зацеп */}
			<div
				ref={leftHandleRef}
				className={`absolute left-0 top-0 bottom-0 w-3 cursor-w-resize rounded-l-lg flex items-center justify-center transition-colors ${
					isResizing && resizeHandle === 'left'
						? 'bg-indigo-700'
						: 'bg-indigo-600 hover:bg-indigo-400'
				}`}
				onMouseDown={handleResizeStart('left')}
				onPointerDown={() => handleResizePointerDown('left')}
				onPointerUp={handleResizePointerUp}
			>
				<div className="w-0.5 h-6 border-l-2 border-white/50 rounded" />
			</div>

			{/* Контент */}
			<div className="flex-1 px-4 text-white font-medium truncate text-center">
				{children ?? title}
			</div>

			{/* Правый зацеп */}
			<div
				ref={rightHandleRef}
				className={`absolute right-0 top-0 bottom-0 w-3 cursor-e-resize rounded-r-lg flex items-center justify-center transition-colors ${
					isResizing && resizeHandle === 'right'
						? 'bg-indigo-700'
						: 'bg-indigo-600 hover:bg-indigo-400'
				}`}
				onMouseDown={handleResizeStart('right')}
				onPointerDown={() => handleResizePointerDown('right')}
				onPointerUp={handleResizePointerUp}
			>
				<div className="w-0.5 h-6 border-r-2 border-white/50 rounded" />
			</div>
		</div>
	)
}

export const GantElement = (props: GantElementProps) => {
	return (
		<DragDropProvider>
			<GantElementContent {...props} />
		</DragDropProvider>
	)
}

export default GantElement
