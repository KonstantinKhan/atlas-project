'use client'

import { useState } from 'react'
import { Plus, Trash2, Pencil, FolderKanban, BarChart3 } from 'lucide-react'
import Link from 'next/link'
import { usePortfolios, useCreatePortfolio, useDeletePortfolio, useUpdatePortfolio } from '@/hooks/usePortfolios'
import type { Portfolio } from '@/types/schemas/portfolio.schema'

export function PortfolioListPage() {
	const { data: portfolios, isLoading } = usePortfolios()
	const createMutation = useCreatePortfolio()
	const deleteMutation = useDeletePortfolio()
	const updateMutation = useUpdatePortfolio()

	const [showCreate, setShowCreate] = useState(false)
	const [newName, setNewName] = useState('')
	const [newDesc, setNewDesc] = useState('')
	const [editingId, setEditingId] = useState<string | null>(null)
	const [editName, setEditName] = useState('')

	const handleCreate = () => {
		if (!newName.trim()) return
		createMutation.mutate(
			{ name: newName.trim(), description: newDesc.trim() },
			{
				onSuccess: () => {
					setNewName('')
					setNewDesc('')
					setShowCreate(false)
				},
			},
		)
	}

	const startEdit = (portfolio: Portfolio) => {
		setEditingId(portfolio.id)
		setEditName(portfolio.name)
	}

	const saveEdit = (id: string) => {
		updateMutation.mutate(
			{ id, updates: { name: editName } },
			{ onSuccess: () => setEditingId(null) },
		)
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
				<h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Портфели проектов</h1>
				<div className="flex items-center gap-2">
					<Link
						href="/resource-load"
						className="flex items-center gap-2 rounded border border-gray-300 px-4 py-2 text-sm text-gray-600 hover:bg-gray-50"
					>
						<BarChart3 size={16} />
						Глобальная нагрузка
					</Link>
					<button
						onClick={() => setShowCreate(true)}
						className="flex items-center gap-2 rounded bg-indigo-600 px-4 py-2 text-sm text-white hover:bg-indigo-700"
					>
						<Plus size={16} />
						Создать портфель
					</button>
				</div>
			</div>

			{showCreate && (
				<div className="mb-6 rounded-lg border border-gray-200 bg-gray-50 p-4">
					<div className="mb-3">
						<label className="mb-1 block text-sm text-gray-600">Название</label>
						<input
							value={newName}
							onChange={(e) => setNewName(e.target.value)}
							onKeyDown={(e) => e.key === 'Enter' && handleCreate()}
							placeholder="Мой портфель"
							className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
							autoFocus
						/>
					</div>
					<div className="mb-3">
						<label className="mb-1 block text-sm text-gray-600">Описание</label>
						<input
							value={newDesc}
							onChange={(e) => setNewDesc(e.target.value)}
							placeholder="Описание (необязательно)"
							className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
						/>
					</div>
					<div className="flex gap-2">
						<button
							onClick={handleCreate}
							disabled={!newName.trim()}
							className="rounded bg-indigo-600 px-4 py-2 text-sm text-white hover:bg-indigo-700 disabled:opacity-40"
						>
							Создать
						</button>
						<button
							onClick={() => setShowCreate(false)}
							className="rounded px-4 py-2 text-sm text-gray-500 hover:bg-gray-100"
						>
							Отмена
						</button>
					</div>
				</div>
			)}

			{portfolios && portfolios.length === 0 ? (
				<div className="rounded-lg border border-dashed border-gray-300 p-12 text-center">
					<FolderKanban className="mx-auto mb-4 text-gray-400" size={48} />
					<p className="text-gray-500">Портфели ещё не созданы</p>
					<button
						onClick={() => setShowCreate(true)}
						className="mt-4 text-sm text-indigo-600 hover:text-indigo-800"
					>
						Создать первый портфель
					</button>
				</div>
			) : (
				<div className="grid gap-4">
					{portfolios?.map((portfolio) => (
						<div
							key={portfolio.id}
							className="group rounded-lg border border-gray-200 p-4 hover:border-indigo-200 hover:shadow-sm transition-all"
						>
							<div className="flex items-center justify-between">
								{editingId === portfolio.id ? (
									<input
										value={editName}
										onChange={(e) => setEditName(e.target.value)}
										onKeyDown={(e) => {
											if (e.key === 'Enter') saveEdit(portfolio.id)
											if (e.key === 'Escape') setEditingId(null)
										}}
										className="flex-1 rounded border border-gray-300 px-2 py-1 text-lg font-semibold focus:border-indigo-500 focus:outline-none"
										autoFocus
									/>
								) : (
									<Link
										href={`/portfolios/${portfolio.id}`}
										className="flex-1"
									>
										<h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100 hover:text-indigo-600">
											{portfolio.name}
										</h2>
										{portfolio.description && (
											<p className="mt-1 text-sm text-gray-500">{portfolio.description}</p>
										)}
									</Link>
								)}
								<div className="flex items-center gap-1 opacity-0 group-hover:opacity-100">
									{editingId === portfolio.id ? (
										<>
											<button
												onClick={() => saveEdit(portfolio.id)}
												className="rounded px-2 py-1 text-xs text-indigo-600 hover:bg-indigo-50"
											>
												Сохранить
											</button>
											<button
												onClick={() => setEditingId(null)}
												className="rounded px-2 py-1 text-xs text-gray-500 hover:bg-gray-100"
											>
												Отмена
											</button>
										</>
									) : (
										<>
											<button
												onClick={() => startEdit(portfolio)}
												className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
											>
												<Pencil size={14} />
											</button>
											<button
												onClick={() => deleteMutation.mutate({ id: portfolio.id })}
												className="rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-600"
											>
												<Trash2 size={14} />
											</button>
										</>
									)}
								</div>
							</div>
						</div>
					))}
				</div>
			)}
		</div>
	)
}
