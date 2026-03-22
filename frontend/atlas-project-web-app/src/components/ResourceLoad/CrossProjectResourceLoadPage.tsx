'use client'

import { useMemo, useState } from 'react'
import { ArrowLeft, AlertTriangle } from 'lucide-react'
import Link from 'next/link'
import { useGlobalResourceLoad, usePortfolioResourceLoad } from '@/hooks/useCrossProjectLoad'
import type {
	CrossProjectResourceLoad,
	CrossProjectDayLoad,
	CrossProjectOverloadReport,
	ProjectInfo,
} from '@/types/schemas/crossProjectLoad.schema'

const PROJECT_COLORS = [
	'#6366f1', // indigo
	'#06b6d4', // cyan
	'#f59e0b', // amber
	'#10b981', // emerald
	'#ef4444', // red
	'#8b5cf6', // violet
	'#f97316', // orange
	'#14b8a6', // teal
]

const EXTERNAL_COLOR = '#9ca3af' // gray-400

interface Props {
	mode: 'global' | 'portfolio'
	portfolioId?: string
	backHref: string
	title: string
}

function getDaysArray(report: CrossProjectOverloadReport): string[] {
	const dateSet = new Set<string>()
	for (const resource of report.resources) {
		for (const day of resource.days) {
			dateSet.add(day.date)
		}
	}
	return [...dateSet].sort()
}

function StackedDayCell({
	day,
	date,
	dayWidth,
	projectColorMap,
	portfolioProjectIds,
	mode,
}: {
	day: CrossProjectDayLoad | undefined
	date: string
	dayWidth: number
	projectColorMap: Map<string, string>
	portfolioProjectIds: Set<string>
	mode: 'global' | 'portfolio'
}) {
	if (!day || day.totalAssignedHours === 0) {
		return (
			<div
				style={{ width: dayWidth, minWidth: dayWidth }}
				className="h-8 border-r border-gray-100"
			/>
		)
	}

	const ratio = day.totalAssignedHours / day.capacityHours
	const maxRatio = Math.min(ratio, 1.5)

	// Group contributions: in portfolio mode, external projects are merged
	let segments: Array<{ color: string; fraction: number; label: string }>
	if (mode === 'global') {
		segments = day.projectBreakdown.map((c) => ({
			color: projectColorMap.get(c.projectId) ?? EXTERNAL_COLOR,
			fraction: c.hours / day.capacityHours,
			label: `${c.projectName}: ${c.hours}ч`,
		}))
	} else {
		const internal = day.projectBreakdown.filter((c) => portfolioProjectIds.has(c.projectId))
		const externalHours = day.projectBreakdown
			.filter((c) => !portfolioProjectIds.has(c.projectId))
			.reduce((sum, c) => sum + c.hours, 0)

		segments = internal.map((c) => ({
			color: projectColorMap.get(c.projectId) ?? EXTERNAL_COLOR,
			fraction: c.hours / day.capacityHours,
			label: `${c.projectName}: ${c.hours}ч`,
		}))
		if (externalHours > 0) {
			segments.push({
				color: EXTERNAL_COLOR,
				fraction: externalHours / day.capacityHours,
				label: `Другие портфели: ${externalHours}ч`,
			})
		}
	}

	const tooltipLines = [
		`${day.totalAssignedHours}ч / ${day.capacityHours}ч`,
		...segments.map((s) => s.label),
	]

	// Scale segments to fit within maxRatio
	const totalFraction = segments.reduce((s, seg) => s + seg.fraction, 0)
	const scale = totalFraction > 0 ? maxRatio / totalFraction : 0

	let borderColor = 'border-gray-100'
	if (ratio > 1) borderColor = 'border-red-200'

	return (
		<div
			style={{ width: dayWidth, minWidth: dayWidth }}
			className={`relative h-8 border-r ${borderColor}`}
			title={tooltipLines.join('\n')}
		>
			<div className="absolute bottom-0 left-0 right-0 flex flex-col-reverse">
				{segments.map((seg, i) => (
					<div
						key={i}
						style={{
							backgroundColor: seg.color,
							height: `${(seg.fraction * scale / 1.5) * 100 * (32 / 100)}px`,
							opacity: 0.8,
						}}
					/>
				))}
			</div>
			{day.isOverloaded && (
				<div className="absolute inset-0 flex items-center justify-center pointer-events-none">
					<AlertTriangle size={10} className="text-red-800" />
				</div>
			)}
		</div>
	)
}

function CrossResourceRow({
	resource,
	allDays,
	dayWidth,
	projectColorMap,
	portfolioProjectIds,
	mode,
}: {
	resource: CrossProjectResourceLoad
	allDays: string[]
	dayWidth: number
	projectColorMap: Map<string, string>
	portfolioProjectIds: Set<string>
	mode: 'global' | 'portfolio'
}) {
	const dayMap = useMemo(() => {
		const m = new Map<string, CrossProjectDayLoad>()
		for (const d of resource.days) m.set(d.date, d)
		return m
	}, [resource.days])

	return (
		<div className="flex border-b border-gray-200">
			<div className="sticky left-0 z-10 w-48 shrink-0 border-r border-gray-200 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 flex flex-col justify-center">
				<div className="flex items-center justify-between">
					<span className="truncate">{resource.resourceName}</span>
					{resource.overloadedDaysCount > 0 && (
						<span className="rounded bg-red-100 px-1.5 py-0.5 text-[10px] text-red-700 font-semibold">
							{resource.overloadedDaysCount}
						</span>
					)}
				</div>
				{resource.totalAllocatedHours > 0 && (
					<span className="text-[10px] text-gray-400">{resource.totalAllocatedHours}ч назначено</span>
				)}
			</div>
			<div className="flex">
				{allDays.map((date) => (
					<StackedDayCell
						key={date}
						date={date}
						day={dayMap.get(date)}
						dayWidth={dayWidth}
						projectColorMap={projectColorMap}
						portfolioProjectIds={portfolioProjectIds}
						mode={mode}
					/>
				))}
			</div>
		</div>
	)
}

export function CrossProjectResourceLoadPage({ mode, portfolioId, backHref, title }: Props) {
	const globalQuery = useGlobalResourceLoad(
		mode === 'global' ? undefined : undefined,
		mode === 'global' ? undefined : undefined,
	)
	const portfolioQuery = usePortfolioResourceLoad(
		portfolioId ?? '',
		undefined,
		undefined,
	)

	const report = mode === 'global' ? globalQuery.data : portfolioQuery.data
	const isLoading = mode === 'global' ? globalQuery.isLoading : portfolioQuery.isLoading

	const allDays = useMemo(() => {
		if (!report) return []
		return getDaysArray(report)
	}, [report])

	const projectColorMap = useMemo(() => {
		if (!report) return new Map<string, string>()
		const map = new Map<string, string>()
		report.projects.forEach((p, i) => {
			map.set(p.id, PROJECT_COLORS[i % PROJECT_COLORS.length])
		})
		return map
	}, [report])

	const portfolioProjectIds = useMemo(() => {
		if (!report || mode === 'global') return new Set<string>()
		// In portfolio mode, "own" projects share the portfolioId
		return new Set(
			report.projects
				.filter((p) => p.portfolioId === portfolioId)
				.map((p) => p.id),
		)
	}, [report, mode, portfolioId])

	const dayWidth = 28

	if (isLoading) {
		return (
			<div className="flex h-screen items-center justify-center">
				<div className="text-gray-500">Загрузка...</div>
			</div>
		)
	}

	if (!report || report.resources.length === 0) {
		return (
			<div className="mx-auto max-w-4xl p-6">
				<div className="mb-6 flex items-center gap-4">
					<Link href={backHref} className="text-gray-400 hover:text-gray-600">
						<ArrowLeft size={20} />
					</Link>
					<h1 className="text-2xl font-bold text-gray-900">{title}</h1>
				</div>
				<div className="rounded-lg border border-dashed border-gray-300 p-12 text-center">
					<p className="text-gray-500">Нет данных о нагрузке ресурсов</p>
				</div>
			</div>
		)
	}

	return (
		<div className="flex h-screen flex-col">
			<div className="flex items-center justify-between border-b border-gray-200 px-6 py-3">
				<div className="flex items-center gap-4">
					<Link href={backHref} className="text-gray-400 hover:text-gray-600">
						<ArrowLeft size={20} />
					</Link>
					<h1 className="text-lg font-bold text-gray-900">{title}</h1>
				</div>
				<div className="flex items-center gap-3">
					{report.totalOverloadedDays > 0 && (
						<div className="flex items-center gap-2 rounded bg-red-50 px-3 py-1.5 text-sm text-red-700">
							<AlertTriangle size={14} />
							Перегрузок: {report.totalOverloadedDays} дн.
						</div>
					)}
					{mode === 'portfolio' && (
						<Link
							href="/resource-load"
							className="rounded border border-gray-300 px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50"
						>
							Глобальная нагрузка
						</Link>
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
				{report.resources.map((resource) => (
					<CrossResourceRow
						key={resource.resourceId}
						resource={resource}
						allDays={allDays}
						dayWidth={dayWidth}
						projectColorMap={projectColorMap}
						portfolioProjectIds={portfolioProjectIds}
						mode={mode}
					/>
				))}
			</div>

			{/* Legend */}
			<div className="flex flex-wrap items-center gap-4 border-t border-gray-200 px-6 py-2 text-xs text-gray-500">
				{mode === 'portfolio' && (
					<>
						{report.projects
							.filter((p) => portfolioProjectIds.has(p.id))
							.map((p) => (
								<div key={p.id} className="flex items-center gap-1">
									<div
										className="h-3 w-3 rounded"
										style={{ backgroundColor: projectColorMap.get(p.id) }}
									/>
									{p.name}
								</div>
							))}
						{report.projects.some((p) => !portfolioProjectIds.has(p.id)) && (
							<div className="flex items-center gap-1">
								<div className="h-3 w-3 rounded" style={{ backgroundColor: EXTERNAL_COLOR }} />
								Другие портфели
							</div>
						)}
					</>
				)}
				{mode === 'global' &&
					report.projects.map((p) => (
						<div key={p.id} className="flex items-center gap-1">
							<div
								className="h-3 w-3 rounded"
								style={{ backgroundColor: projectColorMap.get(p.id) }}
							/>
							{p.name}
						</div>
					))}
				<div className="ml-auto flex items-center gap-3">
					<div className="flex items-center gap-1">
						<AlertTriangle size={10} className="text-red-800" /> Перегрузка
					</div>
				</div>
			</div>
		</div>
	)
}
