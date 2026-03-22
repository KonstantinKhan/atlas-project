'use client'

import { useState, useMemo, useCallback } from 'react'
import type { OverloadReport } from '@/types/schemas/assignment.schema'

const LOAD_ROW_HEIGHT = 48
const BAR_MAX_HEIGHT = 36
const BAR_PADDING_BOTTOM = 4

function dateToStr(d: Date): string {
	const y = d.getFullYear()
	const m = String(d.getMonth() + 1).padStart(2, '0')
	const day = String(d.getDate()).padStart(2, '0')
	return `${y}-${m}-${day}`
}

export type ProjectDetail = { projectId: string; projectName: string; hours: number }
export type CrossProjectDayData = {
	thisProject: number
	otherProjects: number
	capacity: number
	otherProjectDetails: ProjectDetail[]
}
export type CrossProjectMap = Map<string, Map<string, CrossProjectDayData>>

export type TaskDetail = { taskId: string; taskTitle: string; hours: number }
export type TaskBreakdownMap = Map<string, Map<string, TaskDetail[]>>

interface ResourceLoadPanelProps {
	report: OverloadReport
	days: Date[]
	dayWidth: number
	crossProjectMap?: CrossProjectMap
	taskBreakdownMap?: TaskBreakdownMap
}

interface TooltipData {
	resourceName: string
	date: string
	thisProject: number
	otherProjects: number
	otherProjectDetails: ProjectDetail[]
	taskDetails: TaskDetail[]
	total: number
	capacityHours: number
	mouseX: number
	mouseY: number
}

export default function ResourceLoadPanel({ report, days, dayWidth, crossProjectMap, taskBreakdownMap }: ResourceLoadPanelProps) {
	const [tooltip, setTooltip] = useState<TooltipData | null>(null)

	const dateStrs = useMemo(() => days.map(dateToStr), [days])

	// Per-resource: date → load data (from single-project report as fallback)
	const resourceDayMaps = useMemo(() => {
		return report.resources.map((r) => {
			const map = new Map<string, { assignedHours: number; capacityHours: number }>()
			for (const d of r.days) {
				map.set(d.date, { assignedHours: d.assignedHours, capacityHours: d.capacityHours })
			}
			return map
		})
	}, [report])

	// Per-resource max hours for scaling
	const resourceScales = useMemo(() => {
		return report.resources.map((r) => {
			let maxTotal = 0
			const cap = r.days.length > 0 ? r.days[0].capacityHours : 8

			// If we have cross-project data, max should include other projects
			const crossDays = crossProjectMap?.get(r.resourceId)
			if (crossDays) {
				for (const [, cpd] of crossDays) {
					const total = cpd.thisProject + cpd.otherProjects
					if (total > maxTotal) maxTotal = total
				}
			} else {
				for (const d of r.days) {
					if (d.assignedHours > maxTotal) maxTotal = d.assignedHours
				}
			}

			const maxHours = Math.max(cap, maxTotal, 1)
			return { maxHours, capacity: cap }
		})
	}, [report, crossProjectMap])

	const handleMouseMove = useCallback((e: React.MouseEvent<HTMLDivElement>, resourceIndex: number) => {
		const rect = e.currentTarget.getBoundingClientRect()
		const offsetX = e.clientX - rect.left
		const dayIndex = Math.floor(offsetX / dayWidth)
		if (dayIndex < 0 || dayIndex >= dateStrs.length) {
			setTooltip(null)
			return
		}
		const dateStr = dateStrs[dayIndex]
		const resource = report.resources[resourceIndex]
		const crossDays = crossProjectMap?.get(resource.resourceId)
		const cpd = crossDays?.get(dateStr)
		const fallback = resourceDayMaps[resourceIndex].get(dateStr)

		const thisProject = cpd?.thisProject ?? fallback?.assignedHours ?? 0
		const otherProjects = cpd?.otherProjects ?? 0
		const capacityHours = cpd?.capacity ?? fallback?.capacityHours ?? 8

		const otherProjectDetails = cpd?.otherProjectDetails ?? []
		const taskDetails = taskBreakdownMap?.get(resource.resourceId)?.get(dateStr) ?? []

		setTooltip({
			resourceName: resource.resourceName,
			date: dateStr,
			thisProject,
			otherProjects,
			otherProjectDetails,
			taskDetails,
			total: thisProject + otherProjects,
			capacityHours,
			mouseX: e.clientX,
			mouseY: e.clientY,
		})
	}, [dayWidth, dateStrs, resourceDayMaps, report, crossProjectMap, taskBreakdownMap])

	const totalWidth = days.length * dayWidth

	return (
		<div className="relative" style={{ width: totalWidth }}>
			{report.resources.map((resource, ri) => {
				const dayMap = resourceDayMaps[ri]
				const crossDays = crossProjectMap?.get(resource.resourceId)
				const { maxHours, capacity } = resourceScales[ri]
				const capacityLineBottom = BAR_PADDING_BOTTOM + (capacity / maxHours) * BAR_MAX_HEIGHT

				return (
					<div
						key={resource.resourceId}
						className="relative flex"
						style={{ height: LOAD_ROW_HEIGHT }}
						onMouseMove={(e) => handleMouseMove(e, ri)}
						onMouseLeave={() => setTooltip(null)}
					>
						{/* Capacity line */}
						<div
							className="absolute left-0 right-0 border-t border-dashed border-gray-400/50 dark:border-zinc-500/50 z-10 pointer-events-none"
							style={{ bottom: capacityLineBottom }}
						/>
						{/* Day bars */}
						{dateStrs.map((dateStr) => {
							const cpd = crossDays?.get(dateStr)
							const fallback = dayMap.get(dateStr)

							const thisProjectH = cpd?.thisProject ?? fallback?.assignedHours ?? 0
							const otherProjectsH = cpd?.otherProjects ?? 0
							const totalH = thisProjectH + otherProjectsH

							if (totalH === 0) {
								return <div key={dateStr} style={{ width: dayWidth, minWidth: dayWidth }} />
							}

							const overloadH = totalH > capacity
								? Math.min(((totalH - capacity) / maxHours), 1) * BAR_MAX_HEIGHT
								: 0
							const belowCapTotal = Math.min(totalH, capacity)
							const otherBelowCap = Math.min(otherProjectsH, belowCapTotal)
							const thisBelowCap = belowCapTotal - otherBelowCap

							const grayH = (otherBelowCap / maxHours) * BAR_MAX_HEIGHT
							const blueH = (thisBelowCap / maxHours) * BAR_MAX_HEIGHT
							const redH = overloadH

							return (
								<div
									key={dateStr}
									className="relative"
									style={{ width: dayWidth, minWidth: dayWidth, height: LOAD_ROW_HEIGHT }}
								>
									{/* Other projects (gray) — bottom */}
									{grayH > 0 && (
										<div
											className="absolute left-0.5 right-0.5 bg-gray-400/50 dark:bg-zinc-500/50"
											style={{
												bottom: BAR_PADDING_BOTTOM,
												height: grayH,
											}}
										/>
									)}
									{/* This project (blue) — middle */}
									{blueH > 0 && (
										<div
											className="absolute left-0.5 right-0.5 bg-sky-400/70 dark:bg-sky-500/60"
											style={{
												bottom: BAR_PADDING_BOTTOM + grayH,
												height: blueH,
											}}
										/>
									)}
									{/* Overload (red) — top */}
									{redH > 0 && (
										<div
											className="absolute left-0.5 right-0.5 bg-red-400/80 dark:bg-red-500/70 rounded-t-sm"
											style={{
												bottom: BAR_PADDING_BOTTOM + grayH + blueH,
												height: redH,
											}}
										/>
									)}
								</div>
							)
						})}
					</div>
				)
			})}
			{/* Tooltip — fixed positioning to avoid overflow clipping */}
			{tooltip && (
				<div
					className="fixed z-50 pointer-events-none"
					style={{ left: tooltip.mouseX, top: tooltip.mouseY - 8, transform: 'translate(-50%, -100%)' }}
				>
					<div className="bg-gray-900 text-white text-[10px] rounded px-2 py-1.5 shadow-lg space-y-0.5 max-w-64">
						<div className="font-medium">{tooltip.resourceName} &mdash; {tooltip.date}</div>
						{/* This project — task breakdown */}
						{tooltip.thisProject > 0 && (
							<div className="space-y-px">
								<div className="flex items-center gap-1.5 font-medium">
									<span className="inline-block w-2 h-2 rounded-sm bg-sky-400 shrink-0" />
									Этот проект: {tooltip.thisProject}ч
								</div>
								{tooltip.taskDetails.length > 0 && (
									<div className="pl-3.5 space-y-px">
										{tooltip.taskDetails.map((t) => (
											<div key={t.taskId} className="text-white/70 truncate">
												{t.taskTitle}: {t.hours}ч
											</div>
										))}
									</div>
								)}
							</div>
						)}
						{/* Other projects — project breakdown */}
						{tooltip.otherProjects > 0 && (
							<div className="space-y-px">
								<div className="flex items-center gap-1.5 font-medium">
									<span className="inline-block w-2 h-2 rounded-sm bg-gray-400 shrink-0" />
									Другие проекты: {tooltip.otherProjects}ч
								</div>
								{tooltip.otherProjectDetails.length > 0 && (
									<div className="pl-3.5 space-y-px">
										{tooltip.otherProjectDetails.map((p) => (
											<div key={p.projectId} className="text-white/70 truncate">
												{p.projectName}: {p.hours}ч
											</div>
										))}
									</div>
								)}
							</div>
						)}
						<div className={`border-t border-white/20 pt-0.5 ${tooltip.total > tooltip.capacityHours ? 'text-red-300' : ''}`}>
							Итого: {tooltip.total}ч / {tooltip.capacityHours}ч ({Math.round((tooltip.total / Math.max(tooltip.capacityHours, 1)) * 100)}%)
						</div>
					</div>
				</div>
			)}
		</div>
	)
}

export { LOAD_ROW_HEIGHT }
