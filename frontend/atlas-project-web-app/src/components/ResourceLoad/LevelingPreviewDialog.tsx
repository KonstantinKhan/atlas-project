'use client'

import { X, Check, AlertTriangle } from 'lucide-react'
import type { LevelingResult } from '@/types/schemas/assignment.schema'
import type { GanttTask } from '@/types'

interface LevelingPreviewDialogProps {
	result: LevelingResult
	tasks: GanttTask[]
	isApplying: boolean
	onApply: () => void
	onCancel: () => void
}

export function LevelingPreviewDialog({ result, tasks, isApplying, onApply, onCancel }: LevelingPreviewDialogProps) {
	const taskMap = new Map(tasks.map((t) => [t.id, t]))

	const formatDate = (dateStr: string) => {
		const d = new Date(dateStr)
		return d.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit' })
	}

	return (
		<div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
			<div className="w-[560px] max-h-[80vh] flex flex-col rounded-lg bg-white shadow-2xl">
				<div className="flex items-center justify-between border-b border-gray-200 px-5 py-3">
					<h2 className="text-base font-semibold text-gray-900">Предпросмотр выравнивания</h2>
					<button onClick={onCancel} className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600">
						<X size={16} />
					</button>
				</div>

				<div className="flex items-center gap-3 border-b border-gray-100 px-5 py-2.5 text-sm">
					<div className="flex items-center gap-1.5 text-green-700">
						<Check size={14} />
						Устранено: {result.resolvedOverloads}
					</div>
					{result.remainingOverloads > 0 && (
						<div className="flex items-center gap-1.5 text-amber-700">
							<AlertTriangle size={14} />
							Осталось: {result.remainingOverloads}
						</div>
					)}
					<div className="text-gray-500">
						Сдвигов: {result.updatedSchedules.length}
					</div>
				</div>

				<div className="flex-1 overflow-auto px-5 py-3">
					{result.updatedSchedules.length === 0 ? (
						<p className="py-8 text-center text-gray-400">Нет изменений</p>
					) : (
						<table className="w-full text-sm">
							<thead>
								<tr className="text-left text-xs text-gray-500">
									<th className="pb-2 font-medium">Задача</th>
									<th className="pb-2 font-medium">Новый старт</th>
									<th className="pb-2 font-medium">Новый финиш</th>
								</tr>
							</thead>
							<tbody>
								{result.updatedSchedules.map((upd) => {
									const task = taskMap.get(upd.taskId)
									return (
										<tr key={upd.taskId} className="border-t border-gray-100">
											<td className="py-1.5 pr-3">
												<span className="text-gray-800">{task?.title ?? upd.taskId}</span>
											</td>
											<td className="py-1.5 pr-3 tabular-nums text-gray-600">
												{formatDate(upd.start)}
											</td>
											<td className="py-1.5 tabular-nums text-gray-600">
												{formatDate(upd.end)}
											</td>
										</tr>
									)
								})}
							</tbody>
						</table>
					)}
				</div>

				<div className="flex items-center justify-end gap-2 border-t border-gray-200 px-5 py-3">
					<button
						onClick={onCancel}
						className="rounded px-4 py-1.5 text-sm text-gray-600 hover:bg-gray-100"
					>
						Отмена
					</button>
					<button
						onClick={onApply}
						disabled={isApplying || result.updatedSchedules.length === 0}
						className="rounded bg-indigo-600 px-4 py-1.5 text-sm text-white hover:bg-indigo-700 disabled:opacity-40"
					>
						{isApplying ? 'Применяю...' : 'Применить'}
					</button>
				</div>
			</div>
		</div>
	)
}
