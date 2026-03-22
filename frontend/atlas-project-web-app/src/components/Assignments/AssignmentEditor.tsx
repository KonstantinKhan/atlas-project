'use client'

import { useState } from 'react'
import { X, Plus, Trash2 } from 'lucide-react'
import { useResources } from '@/hooks/useResources'
import { useAssignments, useCreateAssignment, useUpdateAssignment, useDeleteAssignment } from '@/hooks/useAssignments'
import type { Resource } from '@/types/schemas/resource.schema'
import type { TaskAssignment } from '@/types/schemas/assignment.schema'

interface AssignmentEditorProps {
	projectId: string
	taskId: string
	taskTitle: string
	onClose: () => void
	position: { x: number; y: number }
}

export function AssignmentEditor({ projectId, taskId, taskTitle, onClose, position }: AssignmentEditorProps) {
	const { data: resources } = useResources()
	const { data: allAssignments } = useAssignments(projectId)
	const createMutation = useCreateAssignment(projectId)
	const updateMutation = useUpdateAssignment(projectId)
	const deleteMutation = useDeleteAssignment(projectId)
	const [addingResourceId, setAddingResourceId] = useState('')

	const taskAssignments = allAssignments?.filter((a) => a.taskId === taskId) ?? []
	const assignedResourceIds = new Set(taskAssignments.map((a) => a.resourceId))
	const availableResources = resources?.filter((r) => !assignedResourceIds.has(r.id)) ?? []

	const handleAdd = () => {
		if (!addingResourceId) return
		createMutation.mutate(
			{ taskId, resourceId: addingResourceId },
			{ onSuccess: () => setAddingResourceId('') },
		)
	}

	const handleUpdateHours = (assignment: TaskAssignment, hoursPerDay: number) => {
		if (hoursPerDay > 0 && hoursPerDay <= 24) {
			updateMutation.mutate({ id: assignment.id, hoursPerDay })
		}
	}

	const handleUpdateEffort = (assignment: TaskAssignment, value: string) => {
		const effort = value === '' ? null : Number(value)
		if (effort === null || (effort > 0 && effort <= 9999)) {
			updateMutation.mutate({ id: assignment.id, plannedEffortHours: effort })
		}
	}

	const handleDelete = (id: string) => {
		deleteMutation.mutate({ id })
	}

	const getResourceName = (resourceId: string): string => {
		return resources?.find((r) => r.id === resourceId)?.name ?? resourceId
	}

	return (
		<div
			className="fixed z-50 w-72 rounded-lg border border-gray-200 bg-white shadow-xl"
			style={{ left: position.x, top: position.y }}
		>
			<div className="flex items-center justify-between border-b border-gray-100 px-3 py-2">
				<span className="text-sm font-medium text-gray-700 truncate">{taskTitle}</span>
				<button onClick={onClose} className="rounded p-0.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600">
					<X size={14} />
				</button>
			</div>

			<div className="p-3">
				{taskAssignments.length === 0 ? (
					<p className="mb-2 text-xs text-gray-400">Нет назначений</p>
				) : (
					<div className="mb-2 space-y-1.5">
						{taskAssignments.map((assignment) => (
							<div key={assignment.id} className="space-y-1">
								<div className="flex items-center gap-2">
									<span className="flex-1 truncate text-sm text-gray-700">
										{getResourceName(assignment.resourceId)}
									</span>
									<input
										type="number"
										value={assignment.hoursPerDay}
										onChange={(e) => handleUpdateHours(assignment, Number(e.target.value))}
										min={0.5}
										max={24}
										step={0.5}
										className="w-14 rounded border border-gray-200 px-1.5 py-0.5 text-xs text-right focus:border-indigo-400 focus:outline-none"
									/>
									<span className="text-[10px] text-gray-400">ч/д</span>
									<button
										onClick={() => handleDelete(assignment.id)}
										className="rounded p-0.5 text-gray-300 hover:bg-red-50 hover:text-red-500"
									>
										<Trash2 size={12} />
									</button>
								</div>
								<div className="flex items-center gap-1.5 pl-0.5">
									<span className="text-[10px] text-gray-400">План:</span>
									<input
										type="number"
										value={assignment.plannedEffortHours ?? ''}
										onChange={(e) => handleUpdateEffort(assignment, e.target.value)}
										min={0.5}
										step={0.5}
										placeholder="—"
										className="w-14 rounded border border-gray-200 px-1.5 py-0.5 text-xs text-right focus:border-indigo-400 focus:outline-none"
									/>
									<span className="text-[10px] text-gray-400">ч</span>
								</div>
							</div>
						))}
					</div>
				)}

				{availableResources.length > 0 && (
					<div className="flex items-center gap-1.5 border-t border-gray-100 pt-2">
						<select
							value={addingResourceId}
							onChange={(e) => setAddingResourceId(e.target.value)}
							className="flex-1 rounded border border-gray-200 px-1.5 py-1 text-xs focus:border-indigo-400 focus:outline-none"
						>
							<option value="">Выбрать ресурс...</option>
							{availableResources.map((r) => (
								<option key={r.id} value={r.id}>
									{r.name}
								</option>
							))}
						</select>
						<button
							onClick={handleAdd}
							disabled={!addingResourceId}
							className="rounded bg-indigo-600 p-1 text-white hover:bg-indigo-700 disabled:opacity-40"
						>
							<Plus size={14} />
						</button>
					</div>
				)}
			</div>
		</div>
	)
}
