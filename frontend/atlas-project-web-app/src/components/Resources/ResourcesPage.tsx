'use client'

import { useState } from 'react'
import { Plus, Trash2, Pencil, User, Users, ArrowLeft } from 'lucide-react'
import Link from 'next/link'
import { useResources, useCreateResource, useDeleteResource, useUpdateResource } from '@/hooks/useResources'
import { CreateResourceDialog } from './CreateResourceDialog'
import type { Resource } from '@/types/schemas/resource.schema'

export function ResourcesPage({ projectId }: { projectId: string }) {
	const { data: resources, isLoading } = useResources()
	const createMutation = useCreateResource()
	const deleteMutation = useDeleteResource()
	const updateMutation = useUpdateResource()
	const [showCreateDialog, setShowCreateDialog] = useState(false)
	const [editingId, setEditingId] = useState<string | null>(null)
	const [editName, setEditName] = useState('')
	const [editCapacity, setEditCapacity] = useState(8)

	const handleCreate = (data: { name: string; type: string; capacityHoursPerDay: number }) => {
		createMutation.mutate(data)
	}

	const handleDelete = (id: string) => {
		deleteMutation.mutate({ id })
	}

	const startEdit = (resource: Resource) => {
		setEditingId(resource.id)
		setEditName(resource.name)
		setEditCapacity(resource.capacityHoursPerDay)
	}

	const saveEdit = (id: string) => {
		updateMutation.mutate(
			{ id, updates: { name: editName, capacityHoursPerDay: editCapacity } },
			{ onSuccess: () => setEditingId(null) },
		)
	}

	const cancelEdit = () => {
		setEditingId(null)
	}

	if (isLoading) {
		return (
			<div className="flex h-screen items-center justify-center">
				<div className="text-gray-500">Загрузка...</div>
			</div>
		)
	}

	return (
		<div className="mx-auto max-w-4xl p-6">
			<div className="mb-6 flex items-center justify-between">
				<div className="flex items-center gap-4">
					<Link href={`/projects/${projectId}`} className="text-gray-400 hover:text-gray-600">
						<ArrowLeft size={20} />
					</Link>
					<h1 className="text-2xl font-bold text-gray-900">Ресурсы</h1>
				</div>
				<button
					onClick={() => setShowCreateDialog(true)}
					className="flex items-center gap-2 rounded bg-indigo-600 px-4 py-2 text-sm text-white hover:bg-indigo-700"
				>
					<Plus size={16} />
					Добавить ресурс
				</button>
			</div>

			{resources && resources.length === 0 ? (
				<div className="rounded-lg border border-dashed border-gray-300 p-12 text-center">
					<Users className="mx-auto mb-4 text-gray-400" size={48} />
					<p className="text-gray-500">Ресурсы ещё не созданы</p>
					<button
						onClick={() => setShowCreateDialog(true)}
						className="mt-4 text-sm text-indigo-600 hover:text-indigo-800"
					>
						Создать первый ресурс
					</button>
				</div>
			) : (
				<div className="overflow-hidden rounded-lg border border-gray-200">
					<table className="w-full">
						<thead>
							<tr className="border-b bg-gray-50 text-left text-sm text-gray-600">
								<th className="px-4 py-3 font-medium">Имя</th>
								<th className="px-4 py-3 font-medium">Тип</th>
								<th className="px-4 py-3 font-medium">Часы/день</th>
								<th className="w-24 px-4 py-3 font-medium"></th>
							</tr>
						</thead>
						<tbody>
							{resources?.map((resource) => (
								<tr key={resource.id} className="group border-b last:border-0 hover:bg-gray-50">
									<td className="px-4 py-3">
										{editingId === resource.id ? (
											<input
												value={editName}
												onChange={(e) => setEditName(e.target.value)}
												onKeyDown={(e) => {
													if (e.key === 'Enter') saveEdit(resource.id)
													if (e.key === 'Escape') cancelEdit()
												}}
												className="w-full rounded border border-gray-300 px-2 py-1 text-sm focus:border-indigo-500 focus:outline-none"
												autoFocus
											/>
										) : (
											<div className="flex items-center gap-2">
												<User size={16} className="text-gray-400" />
												<span className="text-sm">{resource.name}</span>
											</div>
										)}
									</td>
									<td className="px-4 py-3">
										<span className="rounded-full bg-gray-100 px-2 py-1 text-xs text-gray-600">
											{resource.type === 'PERSON' ? 'Человек' : 'Роль'}
										</span>
									</td>
									<td className="px-4 py-3">
										{editingId === resource.id ? (
											<input
												type="number"
												value={editCapacity}
												onChange={(e) => setEditCapacity(Number(e.target.value))}
												onKeyDown={(e) => {
													if (e.key === 'Enter') saveEdit(resource.id)
													if (e.key === 'Escape') cancelEdit()
												}}
												min={0.5}
												max={24}
												step={0.5}
												className="w-20 rounded border border-gray-300 px-2 py-1 text-sm focus:border-indigo-500 focus:outline-none"
											/>
										) : (
											<span className="text-sm">{resource.capacityHoursPerDay}ч</span>
										)}
									</td>
									<td className="px-4 py-3">
										<div className="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100">
											{editingId === resource.id ? (
												<>
													<button
														onClick={() => saveEdit(resource.id)}
														className="rounded px-2 py-1 text-xs text-indigo-600 hover:bg-indigo-50"
													>
														Сохранить
													</button>
													<button
														onClick={cancelEdit}
														className="rounded px-2 py-1 text-xs text-gray-500 hover:bg-gray-100"
													>
														Отмена
													</button>
												</>
											) : (
												<>
													<button
														onClick={() => startEdit(resource)}
														className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
													>
														<Pencil size={14} />
													</button>
													<button
														onClick={() => handleDelete(resource.id)}
														className="rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-600"
													>
														<Trash2 size={14} />
													</button>
												</>
											)}
										</div>
									</td>
								</tr>
							))}
						</tbody>
					</table>
				</div>
			)}

			<CreateResourceDialog
				open={showCreateDialog}
				onClose={() => setShowCreateDialog(false)}
				onSubmit={handleCreate}
			/>
		</div>
	)
}
