'use client'

import { useState } from 'react'
import { Plus, ArrowLeft, LayoutDashboard, BarChart3 } from 'lucide-react'
import Link from 'next/link'
import { usePortfolio, useProjects, useCreateProject } from '@/hooks/usePortfolios'

export function PortfolioDashboard({ portfolioId }: { portfolioId: string }) {
	const { data: portfolio, isLoading: portfolioLoading } = usePortfolio(portfolioId)
	const { data: projects, isLoading: projectsLoading } = useProjects(portfolioId)
	const createProjectMutation = useCreateProject()

	const [showCreate, setShowCreate] = useState(false)
	const [newProjectName, setNewProjectName] = useState('')

	const handleCreateProject = () => {
		if (!newProjectName.trim()) return
		createProjectMutation.mutate(
			{ portfolioId, name: newProjectName.trim() },
			{
				onSuccess: () => {
					setNewProjectName('')
					setShowCreate(false)
				},
			},
		)
	}

	if (portfolioLoading || projectsLoading) {
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
					<Link href="/portfolios" className="text-gray-400 hover:text-gray-600">
						<ArrowLeft size={20} />
					</Link>
					<div>
						<h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">{portfolio?.name}</h1>
						{portfolio?.description && (
							<p className="text-sm text-gray-500">{portfolio.description}</p>
						)}
					</div>
				</div>
				<div className="flex items-center gap-2">
					<Link
						href={`/portfolios/${portfolioId}/resource-load`}
						className="flex items-center gap-2 rounded border border-gray-300 px-4 py-2 text-sm text-gray-600 hover:bg-gray-50"
					>
						<BarChart3 size={16} />
						Нагрузка ресурсов
					</Link>
					<button
						onClick={() => setShowCreate(true)}
						className="flex items-center gap-2 rounded bg-indigo-600 px-4 py-2 text-sm text-white hover:bg-indigo-700"
					>
						<Plus size={16} />
						Новый проект
					</button>
				</div>
			</div>

			{showCreate && (
				<div className="mb-6 rounded-lg border border-gray-200 bg-gray-50 p-4">
					<label className="mb-1 block text-sm text-gray-600">Название проекта</label>
					<input
						value={newProjectName}
						onChange={(e) => setNewProjectName(e.target.value)}
						onKeyDown={(e) => e.key === 'Enter' && handleCreateProject()}
						placeholder="Новый проект"
						className="mb-3 w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
						autoFocus
					/>
					<div className="flex gap-2">
						<button
							onClick={handleCreateProject}
							disabled={!newProjectName.trim()}
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

			{projects && projects.length === 0 ? (
				<div className="rounded-lg border border-dashed border-gray-300 p-12 text-center">
					<LayoutDashboard className="mx-auto mb-4 text-gray-400" size={48} />
					<p className="text-gray-500">Проекты ещё не созданы</p>
					<button
						onClick={() => setShowCreate(true)}
						className="mt-4 text-sm text-indigo-600 hover:text-indigo-800"
					>
						Создать первый проект
					</button>
				</div>
			) : (
				<div className="grid gap-3">
					{projects?.map((project) => (
						<Link
							key={project.id}
							href={`/projects/${project.id}`}
							className="group flex items-center justify-between rounded-lg border border-gray-200 p-4 hover:border-indigo-200 hover:shadow-sm transition-all"
						>
							<div className="flex items-center gap-3">
								<div className="flex h-8 w-8 items-center justify-center rounded bg-indigo-100 text-indigo-600">
									<LayoutDashboard size={16} />
								</div>
								<div>
									<h3 className="font-medium text-gray-900 dark:text-gray-100 group-hover:text-indigo-600">
										{project.name}
									</h3>
									<p className="text-xs text-gray-400">
										{project.taskCount} задач
									</p>
								</div>
							</div>
							<div className="flex items-center gap-2">
								{project.priority !== 'MEDIUM' && (
									<span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray-500">
										Приоритет: {project.priority === 'HIGH' ? 'Высокий' : 'Низкий'}
									</span>
								)}
							</div>
						</Link>
					))}
				</div>
			)}
		</div>
	)
}
