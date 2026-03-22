'use client'

import { useEffect, useRef, useState } from 'react'
import { X, RotateCcw } from 'lucide-react'
import { useSetDayOverride, useDeleteDayOverride } from '@/hooks/useAssignments'
import type { TaskAssignment } from '@/types/schemas/assignment.schema'
import type { GanttTask } from '@/types'

interface DayOverrideEditorProps {
	projectId: string
	resourceId: string
	resourceName: string
	date: string
	assignments: TaskAssignment[]
	tasks: GanttTask[]
	position: { x: number; y: number }
	onClose: () => void
}

export function DayOverrideEditor({
	projectId,
	resourceId,
	resourceName,
	date,
	assignments,
	tasks,
	position,
	onClose,
}: DayOverrideEditorProps) {
	const setOverrideMutation = useSetDayOverride(projectId)
	const deleteOverrideMutation = useDeleteDayOverride(projectId)
	const ref = useRef<HTMLDivElement>(null)

	const taskMap = new Map(tasks.map((t) => [t.id, t]))

	const resourceAssignments = assignments.filter((a) => {
		if (a.resourceId !== resourceId) return false
		const task = taskMap.get(a.taskId)
		if (!task?.start || !task?.end) return false
		const start = typeof task.start === 'string' ? task.start : task.start.toISOString().slice(0, 10)
		const end = typeof task.end === 'string' ? task.end : task.end.toISOString().slice(0, 10)
		return date >= start && date <= end
	})

	const [localHours, setLocalHours] = useState<Map<string, string>>(() => {
		const m = new Map<string, string>()
		for (const a of resourceAssignments) {
			m.set(a.id, String(a.hoursPerDay))
		}
		return m
	})

	useEffect(() => {
		function handleClickOutside(e: MouseEvent) {
			if (ref.current && !ref.current.contains(e.target as Node)) {
				onClose()
			}
		}
		document.addEventListener('mousedown', handleClickOutside)
		return () => document.removeEventListener('mousedown', handleClickOutside)
	}, [onClose])

	const formatDate = (dateStr: string) => {
		const d = new Date(dateStr)
		return d.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric' })
	}

	const handleSetOverride = (assignmentId: string) => {
		const val = Number(localHours.get(assignmentId))
		if (isNaN(val) || val < 0) return
		setOverrideMutation.mutate({ assignmentId, date, hours: val })
	}

	const handleResetOverride = (assignmentId: string) => {
		deleteOverrideMutation.mutate({ assignmentId, date })
		const assignment = resourceAssignments.find((a) => a.id === assignmentId)
		if (assignment) {
			setLocalHours((prev) => {
				const next = new Map(prev)
				next.set(assignmentId, String(assignment.hoursPerDay))
				return next
			})
		}
	}

	const clampPosition = {
		left: Math.min(position.x, window.innerWidth - 280),
		top: Math.min(position.y, window.innerHeight - 300),
	}

	if (resourceAssignments.length === 0) {
		return (
			<div
				ref={ref}
				className="fixed z-50 w-56 rounded-lg border border-gray-200 bg-white shadow-xl"
				style={clampPosition}
			>
				<div className="flex items-center justify-between border-b border-gray-100 px-3 py-2">
					<span className="text-xs text-gray-500">{formatDate(date)}</span>
					<button onClick={onClose} className="rounded p-0.5 text-gray-400 hover:bg-gray-100">
						<X size={14} />
					</button>
				</div>
				<div className="px-3 py-4 text-center text-xs text-gray-400">
					Нет назначений на этот день
				</div>
			</div>
		)
	}

	return (
		<div
			ref={ref}
			className="fixed z-50 w-64 rounded-lg border border-gray-200 bg-white shadow-xl"
			style={clampPosition}
		>
			<div className="flex items-center justify-between border-b border-gray-100 px-3 py-2">
				<div>
					<div className="text-sm font-medium text-gray-700 truncate">{resourceName}</div>
					<div className="text-xs text-gray-400">{formatDate(date)}</div>
				</div>
				<button onClick={onClose} className="rounded p-0.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600">
					<X size={14} />
				</button>
			</div>

			<div className="p-3 space-y-2">
				{resourceAssignments.map((assignment) => {
					const task = taskMap.get(assignment.taskId)
					return (
						<div key={assignment.id} className="space-y-1">
							<span className="text-xs text-gray-600 truncate block">
								{task?.title ?? 'Задача'}
							</span>
							<div className="flex items-center gap-1.5">
								<input
									type="number"
									value={localHours.get(assignment.id) ?? ''}
									onChange={(e) => {
										setLocalHours((prev) => {
											const next = new Map(prev)
											next.set(assignment.id, e.target.value)
											return next
										})
									}}
									onBlur={() => handleSetOverride(assignment.id)}
									onKeyDown={(e) => {
										if (e.key === 'Enter') handleSetOverride(assignment.id)
									}}
									min={0}
									max={24}
									step={0.5}
									className="w-16 rounded border border-gray-200 px-1.5 py-1 text-xs text-right focus:border-indigo-400 focus:outline-none"
								/>
								<span className="text-[10px] text-gray-400">ч</span>
								<span className="text-[10px] text-gray-300">/ {assignment.hoursPerDay}</span>
								<button
									onClick={() => handleResetOverride(assignment.id)}
									className="ml-auto rounded p-0.5 text-gray-300 hover:bg-gray-100 hover:text-gray-500"
									title="Сбросить к дефолту"
								>
									<RotateCcw size={12} />
								</button>
							</div>
						</div>
					)
				})}
			</div>
		</div>
	)
}
