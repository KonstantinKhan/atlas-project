'use client'

import { useMemo, useState } from 'react'
import { ArrowLeft, AlertTriangle, Wand2 } from 'lucide-react'
import Link from 'next/link'
import { useResources } from '@/hooks/useResources'
import { useAssignments, useResourceLoad, useLevelingPreview, useApplyLeveling } from '@/hooks/useAssignments'
import { useProjectPlan } from '@/hooks/useProjectTasks'
import { LevelingPreviewDialog } from './LevelingPreviewDialog'
import { DayOverrideEditor } from './DayOverrideEditor'
import type { ResourceLoadResult, ResourceDayLoad, LevelingResult } from '@/types/schemas/assignment.schema'

function getDateRange(plan: { tasks: Array<{ start?: string | Date | null; end?: string | Date | null }> }) {
	let min: string | null = null
	let max: string | null = null
	for (const task of plan.tasks) {
		const s = task.start ? (typeof task.start === 'string' ? task.start : task.start.toISOString().slice(0, 10)) : null
		const e = task.end ? (typeof task.end === 'string' ? task.end : task.end.toISOString().slice(0, 10)) : null
		if (s && (!min || s < min)) min = s
		if (e && (!max || e > max)) max = e
	}
	return { from: min, to: max }
}

function getDaysArray(from: string, to: string): string[] {
	const days: string[] = []
	const d = new Date(from)
	const end = new Date(to)
	while (d <= end) {
		days.push(d.toISOString().slice(0, 10))
		d.setDate(d.getDate() + 1)
	}
	return days
}

function DayCell({ day, date, dayWidth, onClick }: { day: ResourceDayLoad | undefined; date: string; dayWidth: number; onClick: (date: string, e: React.MouseEvent) => void }) {
	if (!day || day.assignedHours === 0) {
		return (
			<div
				style={{ width: dayWidth, minWidth: dayWidth }}
				className="h-8 border-r border-gray-100 cursor-pointer hover:bg-gray-50"
				onClick={(e) => onClick(date, e)}
			/>
		)
	}

	const ratio = day.assignedHours / day.capacityHours
	let bg = 'bg-blue-200'
	if (ratio > 1) bg = 'bg-red-400'
	else if (ratio > 0.8) bg = 'bg-amber-300'

	const fillHeight = Math.min(ratio, 1.5) / 1.5 * 100

	return (
		<div
			style={{ width: dayWidth, minWidth: dayWidth }}
			className="relative h-8 border-r border-gray-100 cursor-pointer"
			title={`${day.assignedHours}ч / ${day.capacityHours}ч`}
			onClick={(e) => onClick(date, e)}
		>
			<div
				className={`absolute bottom-0 left-0 right-0 ${bg} transition-all`}
				style={{ height: `${fillHeight}%` }}
			/>
			{day.isOverloaded && (
				<div className="absolute inset-0 flex items-center justify-center pointer-events-none">
					<AlertTriangle size={10} className="text-red-800" />
				</div>
			)}
		</div>
	)
}

function ResourceRow({
	result,
	allDays,
	dayWidth,
	onDayClick,
}: {
	result: ResourceLoadResult
	allDays: string[]
	dayWidth: number
	onDayClick: (resourceId: string, date: string, e: React.MouseEvent) => void
}) {
	const dayMap = useMemo(() => {
		const m = new Map<string, ResourceDayLoad>()
		for (const d of result.days) m.set(d.date, d)
		return m
	}, [result.days])

	return (
		<div className="flex border-b border-gray-200">
			<div className="sticky left-0 z-10 w-48 shrink-0 border-r border-gray-200 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 flex flex-col justify-center">
				<div className="flex items-center justify-between">
					<span className="truncate">{result.resourceName}</span>
					<div className="flex items-center gap-1">
						{result.effortDeficit != null && result.effortDeficit > 0 && (
							<span className="rounded bg-amber-100 px-1.5 py-0.5 text-[10px] text-amber-700 font-semibold" title={`Дефицит: ${result.effortDeficit}ч`}>
								-{result.effortDeficit}ч
							</span>
						)}
						{result.overloadedDaysCount > 0 && (
							<span className="rounded bg-red-100 px-1.5 py-0.5 text-[10px] text-red-700 font-semibold">
								{result.overloadedDaysCount}
							</span>
						)}
					</div>
				</div>
				{result.allocatedHours > 0 && (
					<span className="text-[10px] text-gray-400">{result.allocatedHours}ч назначено</span>
				)}
			</div>
			<div className="flex">
				{allDays.map((date) => (
					<DayCell key={date} date={date} day={dayMap.get(date)} dayWidth={dayWidth} onClick={(d, e) => onDayClick(result.resourceId, d, e)} />
				))}
			</div>
		</div>
	)
}

export function ResourceLoadPage({ projectId }: { projectId: string }) {
	const { data: plan, isLoading: planLoading } = useProjectPlan(projectId)
	const { data: resources, isLoading: resourcesLoading } = useResources()
	const { data: allAssignments } = useAssignments(projectId)
	const previewMutation = useLevelingPreview(projectId)
	const applyMutation = useApplyLeveling(projectId)
	const [levelingPreview, setLevelingPreview] = useState<LevelingResult | null>(null)
	const [dayEditor, setDayEditor] = useState<{ resourceId: string; date: string; position: { x: number; y: number } } | null>(null)

	const dateRange = useMemo(() => {
		if (!plan) return { from: null, to: null }
		return getDateRange(plan)
	}, [plan])

	const { data: report, isLoading: loadLoading } = useResourceLoad(projectId, dateRange.from, dateRange.to)

	const allDays = useMemo(() => {
		if (!dateRange.from || !dateRange.to) return []
		return getDaysArray(dateRange.from, dateRange.to)
	}, [dateRange.from, dateRange.to])

	const handlePreviewLeveling = () => {
		previewMutation.mutate(undefined, {
			onSuccess: (result) => setLevelingPreview(result),
		})
	}

	const handleApplyLeveling = () => {
		applyMutation.mutate(undefined, {
			onSuccess: () => setLevelingPreview(null),
		})
	}

	const handleDayClick = (resourceId: string, date: string, e: React.MouseEvent) => {
		setDayEditor({ resourceId, date, position: { x: e.clientX, y: e.clientY } })
	}

	const isLoading = planLoading || resourcesLoading || loadLoading
	const dayWidth = 28

	if (isLoading) {
		return (
			<div className="flex h-screen items-center justify-center">
				<div className="text-gray-500">Загрузка...</div>
			</div>
		)
	}

	if (!resources || resources.length === 0) {
		return (
			<div className="mx-auto max-w-4xl p-6">
				<div className="mb-6 flex items-center gap-4">
					<Link href={`/projects/${projectId}`} className="text-gray-400 hover:text-gray-600">
						<ArrowLeft size={20} />
					</Link>
					<h1 className="text-2xl font-bold text-gray-900">Нагрузка ресурсов</h1>
				</div>
				<div className="rounded-lg border border-dashed border-gray-300 p-12 text-center">
					<p className="text-gray-500">Сначала создайте ресурсы</p>
					<Link href={`/projects/${projectId}/resources`} className="mt-2 inline-block text-sm text-indigo-600 hover:text-indigo-800">
						Перейти к ресурсам
					</Link>
				</div>
			</div>
		)
	}

	return (
		<div className="flex h-screen flex-col">
			<div className="flex items-center justify-between border-b border-gray-200 px-6 py-3">
				<div className="flex items-center gap-4">
					<Link href={`/projects/${projectId}`} className="text-gray-400 hover:text-gray-600">
						<ArrowLeft size={20} />
					</Link>
					<h1 className="text-lg font-bold text-gray-900">Нагрузка ресурсов</h1>
				</div>
				<div className="flex items-center gap-3">
					{report && report.totalEffortDeficit > 0 && (
						<div className="flex items-center gap-2 rounded bg-amber-50 px-3 py-1.5 text-sm text-amber-700">
							<AlertTriangle size={14} />
							Дефицит: {report.totalEffortDeficit}ч
						</div>
					)}
					{report && report.totalOverloadedDays > 0 && (
						<>
							<div className="flex items-center gap-2 rounded bg-red-50 px-3 py-1.5 text-sm text-red-700">
								<AlertTriangle size={14} />
								Перегрузок: {report.totalOverloadedDays} дн.
							</div>
							<button
								onClick={handlePreviewLeveling}
								disabled={previewMutation.isPending}
								className="flex items-center gap-1.5 rounded bg-indigo-600 px-3 py-1.5 text-sm text-white hover:bg-indigo-700 disabled:opacity-40"
							>
								<Wand2 size={14} />
								{previewMutation.isPending ? 'Расчёт...' : 'Выровнять'}
							</button>
						</>
					)}
				</div>
			</div>

			<div className="flex-1 overflow-auto">
				{/* Day header */}
				<div className="sticky top-0 z-20 flex border-b border-gray-300 bg-white">
					<div className="sticky left-0 z-10 w-48 shrink-0 border-r border-gray-200 bg-gray-50 px-3 py-1 text-xs font-medium text-gray-500">
						Ресурс
					</div>
					<div className="flex">
						{allDays.map((date) => {
							const d = new Date(date)
							const isWeekend = d.getDay() === 0 || d.getDay() === 6
							return (
								<div
									key={date}
									style={{ width: dayWidth, minWidth: dayWidth }}
									className={`border-r border-gray-100 py-1 text-center text-[9px] ${
										isWeekend ? 'bg-gray-50 text-gray-400' : 'text-gray-500'
									}`}
								>
									{d.getDate()}
								</div>
							)
						})}
					</div>
				</div>

				{/* Resource rows */}
				{report?.resources.map((result) => (
					<ResourceRow key={result.resourceId} result={result} allDays={allDays} dayWidth={dayWidth} onDayClick={handleDayClick} />
				))}
			</div>

			{/* Legend */}
			<div className="flex items-center gap-4 border-t border-gray-200 px-6 py-2 text-xs text-gray-500">
				<div className="flex items-center gap-1">
					<div className="h-3 w-3 rounded bg-blue-200" /> До 80%
				</div>
				<div className="flex items-center gap-1">
					<div className="h-3 w-3 rounded bg-amber-300" /> 80-100%
				</div>
				<div className="flex items-center gap-1">
					<div className="h-3 w-3 rounded bg-red-400" /> Перегрузка
				</div>
			</div>

			{dayEditor && plan && allAssignments && (
				<DayOverrideEditor
					projectId={projectId}
					resourceId={dayEditor.resourceId}
					resourceName={resources?.find((r) => r.id === dayEditor.resourceId)?.name ?? ''}
					date={dayEditor.date}
					assignments={allAssignments}
					tasks={plan.tasks}
					position={dayEditor.position}
					onClose={() => setDayEditor(null)}
				/>
			)}

			{levelingPreview && plan && (
				<LevelingPreviewDialog
					result={levelingPreview}
					tasks={plan.tasks}
					isApplying={applyMutation.isPending}
					onApply={handleApplyLeveling}
					onCancel={() => setLevelingPreview(null)}
				/>
			)}
		</div>
	)
}
