'use client'

interface ConfirmDeleteModalProps {
	taskTitle: string
	affectedDependenciesCount: number
	onConfirm: () => void
	onCancel: () => void
}

export default function ConfirmDeleteModal({
	taskTitle,
	affectedDependenciesCount,
	onConfirm,
	onCancel,
}: ConfirmDeleteModalProps) {
	return (
		<div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
			<div className="bg-white dark:bg-zinc-900 rounded-lg shadow-xl p-6 w-full max-w-sm mx-4">
				<h2 className="text-base font-semibold text-gray-900 dark:text-white mb-2">
					Удалить задачу «{taskTitle}»?
				</h2>
				{affectedDependenciesCount > 0 && (
					<p className="text-sm text-gray-500 dark:text-zinc-400 mb-4">
						Также будут удалены зависимости: {affectedDependenciesCount} шт.
					</p>
				)}
				<div className="flex justify-end gap-3 mt-4">
					<button
						onClick={onCancel}
						className="px-4 py-2 text-sm rounded-md bg-gray-100 dark:bg-zinc-700 text-gray-700 dark:text-zinc-200 hover:bg-gray-200 dark:hover:bg-zinc-600 transition-colors"
					>
						Отмена
					</button>
					<button
						onClick={onConfirm}
						className="px-4 py-2 text-sm rounded-md bg-red-500 text-white hover:bg-red-600 transition-colors"
					>
						Удалить
					</button>
				</div>
			</div>
		</div>
	)
}
