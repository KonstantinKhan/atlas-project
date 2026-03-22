'use client'

import { useState } from 'react'
import { X } from 'lucide-react'

interface CreateResourceDialogProps {
	open: boolean
	onClose: () => void
	onSubmit: (data: { name: string; type: string; capacityHoursPerDay: number }) => void
}

export function CreateResourceDialog({ open, onClose, onSubmit }: CreateResourceDialogProps) {
	const [name, setName] = useState('')
	const [type, setType] = useState('PERSON')
	const [capacity, setCapacity] = useState(8)

	if (!open) return null

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault()
		if (!name.trim()) return
		onSubmit({ name: name.trim(), type, capacityHoursPerDay: capacity })
		setName('')
		setType('PERSON')
		setCapacity(8)
		onClose()
	}

	return (
		<div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
			<div className="w-96 rounded-lg bg-white p-6 shadow-xl">
				<div className="mb-4 flex items-center justify-between">
					<h2 className="text-lg font-semibold">Новый ресурс</h2>
					<button onClick={onClose} className="text-gray-400 hover:text-gray-600">
						<X size={20} />
					</button>
				</div>
				<form onSubmit={handleSubmit} className="space-y-4">
					<div>
						<label className="mb-1 block text-sm font-medium text-gray-700">Имя</label>
						<input
							type="text"
							value={name}
							onChange={(e) => setName(e.target.value)}
							className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
							placeholder="Введите имя ресурса"
							autoFocus
						/>
					</div>
					<div>
						<label className="mb-1 block text-sm font-medium text-gray-700">Тип</label>
						<select
							value={type}
							onChange={(e) => setType(e.target.value)}
							className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
						>
							<option value="PERSON">Человек</option>
							<option value="ROLE">Роль</option>
						</select>
					</div>
					<div>
						<label className="mb-1 block text-sm font-medium text-gray-700">Часы в день</label>
						<input
							type="number"
							value={capacity}
							onChange={(e) => setCapacity(Number(e.target.value))}
							min={0.5}
							max={24}
							step={0.5}
							className="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
						/>
					</div>
					<div className="flex justify-end gap-2">
						<button
							type="button"
							onClick={onClose}
							className="rounded px-4 py-2 text-sm text-gray-600 hover:bg-gray-100"
						>
							Отмена
						</button>
						<button
							type="submit"
							disabled={!name.trim()}
							className="rounded bg-indigo-600 px-4 py-2 text-sm text-white hover:bg-indigo-700 disabled:opacity-50"
						>
							Создать
						</button>
					</div>
				</form>
			</div>
		</div>
	)
}
