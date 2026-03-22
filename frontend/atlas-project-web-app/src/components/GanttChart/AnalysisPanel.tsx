'use client'

import { X } from 'lucide-react'
import { useTimelineCalendarStore, type AnalysisTab } from '@/store/timelineCalendarStore'
import { useBlockerChain, useAvailableTasks, useWhatIf, useWhatIfEnd } from '@/hooks/useProjectTasks'
import type { GanttTask } from '@/types'

interface AnalysisPanelProps {
	projectId: string
	tasks: GanttTask[]
	onClose: () => void
}

const TAB_CONFIG: { key: AnalysisTab; label: string }[] = [
	{ key: 'blockers', label: 'Блокеры' },
	{ key: 'available', label: 'Доступные' },
	{ key: 'whatif', label: 'Что-если' },
]

export default function AnalysisPanel({ projectId, tasks, onClose }: AnalysisPanelProps) {
	const activeTab = useTimelineCalendarStore((s) => s.ui.analysisPanelTab)
	const setTab = useTimelineCalendarStore((s) => s.setAnalysisPanelTab)
	const selectedTaskId = useTimelineCalendarStore((s) => s.ui.selectedAnalysisTaskId)
	const setSelectedTaskId = useTimelineCalendarStore((s) => s.setSelectedAnalysisTaskId)
	const whatIfMode = useTimelineCalendarStore((s) => s.ui.whatIfMode)
	const setWhatIfMode = useTimelineCalendarStore((s) => s.setWhatIfMode)
	const whatIfNewStart = useTimelineCalendarStore((s) => s.ui.whatIfNewStart)
	const setWhatIfNewStart = useTimelineCalendarStore((s) => s.setWhatIfNewStart)
	const whatIfNewEnd = useTimelineCalendarStore((s) => s.ui.whatIfNewEnd)
	const setWhatIfNewEnd = useTimelineCalendarStore((s) => s.setWhatIfNewEnd)

	const scheduledTasks = tasks.filter((t) => t.start && t.end)

	return (
		<div className="flex flex-col h-full">
			{/* Header */}
			<div className="flex items-center justify-between px-3 py-2 border-b border-gray-200 dark:border-zinc-700 shrink-0">
				<span className="text-sm font-semibold text-gray-800 dark:text-zinc-200">
					Аналитика
				</span>
				<button
					onClick={onClose}
					className="p-1 rounded hover:bg-gray-100 dark:hover:bg-zinc-800 text-gray-500 dark:text-zinc-400"
				>
					<X size={16} />
				</button>
			</div>

			{/* Tabs */}
			<div className="flex border-b border-gray-200 dark:border-zinc-700 shrink-0">
				{TAB_CONFIG.map(({ key, label }) => (
					<button
						key={key}
						onClick={() => setTab(key)}
						className={`flex-1 px-2 py-2 text-xs font-medium transition-colors ${
							activeTab === key
								? 'text-indigo-600 dark:text-indigo-400 border-b-2 border-indigo-500'
								: 'text-gray-500 dark:text-zinc-400 hover:text-gray-700 dark:hover:text-zinc-300'
						}`}
					>
						{label}
					</button>
				))}
			</div>

			{/* Tab content */}
			<div className="flex-1 overflow-y-auto p-3">
				{activeTab === 'blockers' && (
					<BlockersTab
						projectId={projectId}
						scheduledTasks={scheduledTasks}
						selectedTaskId={selectedTaskId}
						onSelectTask={setSelectedTaskId}
					/>
				)}
				{activeTab === 'available' && <AvailableTab projectId={projectId} />}
				{activeTab === 'whatif' && (
					<WhatIfTab
						projectId={projectId}
						scheduledTasks={scheduledTasks}
						selectedTaskId={selectedTaskId}
						onSelectTask={setSelectedTaskId}
						mode={whatIfMode}
						onModeChange={setWhatIfMode}
						newStart={whatIfNewStart}
						onNewStartChange={setWhatIfNewStart}
						newEnd={whatIfNewEnd}
						onNewEndChange={setWhatIfNewEnd}
					/>
				)}
			</div>
		</div>
	)
}

function TaskSelect({
	tasks,
	value,
	onChange,
	label,
}: {
	tasks: GanttTask[]
	value: string | null
	onChange: (id: string | null) => void
	label: string
}) {
	return (
		<div className="mb-3">
			<label className="block text-xs text-gray-500 dark:text-zinc-400 mb-1">{label}</label>
			<select
				value={value ?? ''}
				onChange={(e) => onChange(e.target.value || null)}
				className="w-full text-xs px-2 py-1.5 rounded border border-gray-300 dark:border-zinc-600 bg-white dark:bg-zinc-800 text-gray-800 dark:text-zinc-200"
			>
				<option value="">-- Выберите задачу --</option>
				{tasks.map((t) => (
					<option key={t.id} value={t.id}>
						{t.title}
					</option>
				))}
			</select>
		</div>
	)
}

function StatusBadge({ status }: { status: string }) {
	const colors: Record<string, string> = {
		done: 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300',
		'in progress': 'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300',
		backlog: 'bg-gray-100 text-gray-600 dark:bg-zinc-700 dark:text-zinc-300',
		blocked: 'bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-300',
	}
	return (
		<span className={`inline-block px-1.5 py-0.5 text-[10px] rounded ${colors[status] ?? 'bg-gray-100 text-gray-600 dark:bg-zinc-700 dark:text-zinc-300'}`}>
			{status}
		</span>
	)
}

// --- Blockers Tab ---

function BlockersTab({
	projectId,
	scheduledTasks,
	selectedTaskId,
	onSelectTask,
}: {
	projectId: string
	scheduledTasks: GanttTask[]
	selectedTaskId: string | null
	onSelectTask: (id: string | null) => void
}) {
	const { data, isLoading } = useBlockerChain(projectId, selectedTaskId)

	return (
		<>
			<TaskSelect tasks={scheduledTasks} value={selectedTaskId} onChange={onSelectTask} label="Задача" />

			{isLoading && <p className="text-xs text-gray-400">Загрузка...</p>}

			{data && data.blockers.length === 0 && (
				<p className="text-xs text-gray-400 dark:text-zinc-500">Нет блокирующих задач</p>
			)}

			{data && data.blockers.length > 0 && (
				<div className="space-y-1.5">
					{data.blockers.map((b) => (
						<div
							key={b.taskId}
							className={`p-2 rounded border text-xs ${
								b.status === 'done'
									? 'opacity-50 border-gray-200 dark:border-zinc-700'
									: 'border-gray-300 dark:border-zinc-600'
							}`}
							style={{ marginLeft: `${Math.max(0, (b.depth - 1)) * 12}px` }}
						>
							<div className="flex items-center justify-between gap-1">
								<span className={`font-medium text-gray-800 dark:text-zinc-200 ${b.status === 'done' ? 'line-through' : ''}`}>
									{b.title}
								</span>
								<StatusBadge status={b.status} />
							</div>
							{b.start && b.end && (
								<div className="text-[10px] text-gray-400 dark:text-zinc-500 mt-0.5">
									{b.start} - {b.end}
								</div>
							)}
						</div>
					))}
				</div>
			)}
		</>
	)
}

// --- Available Tab ---

function AvailableTab({ projectId }: { projectId: string }) {
	const { data, isLoading } = useAvailableTasks(projectId)

	if (isLoading) return <p className="text-xs text-gray-400">Загрузка...</p>

	if (!data || data.tasks.length === 0) {
		return <p className="text-xs text-gray-400 dark:text-zinc-500">Нет задач для старта сегодня</p>
	}

	return (
		<div className="space-y-1.5">
			{data.tasks.map((t) => (
				<div
					key={t.taskId}
					className="p-2 rounded border border-green-200 dark:border-green-800 bg-green-50 dark:bg-green-950"
				>
					<div className="flex items-center justify-between gap-1">
						<span className="text-xs font-medium text-gray-800 dark:text-zinc-200">{t.title}</span>
						<StatusBadge status={t.status} />
					</div>
					<div className="text-[10px] text-gray-400 dark:text-zinc-500 mt-0.5">
						{t.start} - {t.end}
					</div>
				</div>
			))}
		</div>
	)
}

// --- What-If Tab ---

function WhatIfTab({
	projectId,
	scheduledTasks,
	selectedTaskId,
	onSelectTask,
	mode,
	onModeChange,
	newStart,
	onNewStartChange,
	newEnd,
	onNewEndChange,
}: {
	projectId: string
	scheduledTasks: GanttTask[]
	selectedTaskId: string | null
	onSelectTask: (id: string | null) => void
	mode: 'start' | 'end'
	onModeChange: (mode: 'start' | 'end') => void
	newStart: string | null
	onNewStartChange: (date: string | null) => void
	newEnd: string | null
	onNewEndChange: (date: string | null) => void
}) {
	const startQuery = useWhatIf(
		projectId,
		mode === 'start' ? selectedTaskId : null,
		mode === 'start' ? newStart : null,
	)
	const endQuery = useWhatIfEnd(
		projectId,
		mode === 'end' ? selectedTaskId : null,
		mode === 'end' ? newEnd : null,
	)

	const data = mode === 'start' ? startQuery.data : endQuery.data
	const isLoading = mode === 'start' ? startQuery.isLoading : endQuery.isLoading

	return (
		<>
			<TaskSelect tasks={scheduledTasks} value={selectedTaskId} onChange={onSelectTask} label="Задача" />

			{/* Mode toggle */}
			<div className="mb-3">
				<label className="block text-xs text-gray-500 dark:text-zinc-400 mb-1">Сценарий</label>
				<div className="flex rounded border border-gray-300 dark:border-zinc-600 overflow-hidden">
					<button
						onClick={() => onModeChange('start')}
						className={`flex-1 px-2 py-1.5 text-xs ${
							mode === 'start'
								? 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900 dark:text-indigo-300 font-semibold'
								: 'text-gray-600 dark:text-zinc-400 hover:bg-gray-50 dark:hover:bg-zinc-800'
						}`}
					>
						Сдвиг начала
					</button>
					<button
						onClick={() => onModeChange('end')}
						className={`flex-1 px-2 py-1.5 text-xs border-l border-gray-300 dark:border-zinc-600 ${
							mode === 'end'
								? 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900 dark:text-indigo-300 font-semibold'
								: 'text-gray-600 dark:text-zinc-400 hover:bg-gray-50 dark:hover:bg-zinc-800'
						}`}
					>
						Сдвиг конца
					</button>
				</div>
			</div>

			{/* Date input */}
			{mode === 'start' && (
				<div className="mb-3">
					<label className="block text-xs text-gray-500 dark:text-zinc-400 mb-1">
						Новая дата начала
					</label>
					<input
						type="date"
						value={newStart ?? ''}
						onChange={(e) => onNewStartChange(e.target.value || null)}
						className="w-full text-xs px-2 py-1.5 rounded border border-gray-300 dark:border-zinc-600 bg-white dark:bg-zinc-800 text-gray-800 dark:text-zinc-200"
					/>
				</div>
			)}
			{mode === 'end' && (
				<div className="mb-3">
					<label className="block text-xs text-gray-500 dark:text-zinc-400 mb-1">
						Новая дата конца
					</label>
					<input
						type="date"
						value={newEnd ?? ''}
						onChange={(e) => onNewEndChange(e.target.value || null)}
						className="w-full text-xs px-2 py-1.5 rounded border border-gray-300 dark:border-zinc-600 bg-white dark:bg-zinc-800 text-gray-800 dark:text-zinc-200"
					/>
				</div>
			)}

			{isLoading && <p className="text-xs text-gray-400">Загрузка...</p>}

			{data && data.impacts.length === 0 && (
				<p className="text-xs text-gray-400 dark:text-zinc-500">Нет затронутых задач</p>
			)}

			{data && data.impacts.length > 0 && (
				<ImpactList impacts={data.impacts} />
			)}
		</>
	)
}

function ImpactList({ impacts }: { impacts: { taskId: string; title: string; oldStart: string; oldEnd: string; newStart: string; newEnd: string; deltaStartDays: number; deltaEndDays: number }[] }) {
	return (
		<div className="space-y-1.5">
			{impacts.map((impact) => (
				<div
					key={impact.taskId}
					className="p-2 rounded border border-gray-300 dark:border-zinc-600 text-xs"
				>
					<div className="font-medium text-gray-800 dark:text-zinc-200 mb-1">
						{impact.title}
					</div>
					<div className="grid grid-cols-2 gap-x-2 gap-y-0.5 text-[10px]">
						<span className="text-gray-400">Начало:</span>
						<span>
							<span className="text-gray-500 line-through">{impact.oldStart}</span>
							{' '}
							<span className={impact.deltaStartDays > 0 ? 'text-red-500' : impact.deltaStartDays < 0 ? 'text-green-500' : 'text-gray-500'}>
								{impact.newStart}
							</span>
						</span>
						<span className="text-gray-400">Конец:</span>
						<span>
							<span className="text-gray-500 line-through">{impact.oldEnd}</span>
							{' '}
							<span className={impact.deltaEndDays > 0 ? 'text-red-500' : impact.deltaEndDays < 0 ? 'text-green-500' : 'text-gray-500'}>
								{impact.newEnd}
							</span>
						</span>
						<span className="text-gray-400">Сдвиг:</span>
						<span className={impact.deltaEndDays > 0 ? 'text-red-500 font-medium' : impact.deltaEndDays < 0 ? 'text-green-500 font-medium' : 'text-gray-500'}>
							{impact.deltaEndDays > 0 ? '+' : ''}{impact.deltaEndDays} дн.
						</span>
					</div>
				</div>
			))}
		</div>
	)
}
