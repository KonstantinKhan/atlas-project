'use client'

import { useEffect } from 'react'
import { X } from 'lucide-react'

interface ToastProps {
	message: string
	onClose: () => void
}

export default function Toast({ message, onClose }: ToastProps) {
	useEffect(() => {
		const timer = setTimeout(onClose, 4000)
		return () => clearTimeout(timer)
	}, [onClose])

	return (
		<div className="fixed bottom-6 right-6 z-50 flex items-center gap-2 bg-red-600 text-white px-4 py-3 rounded-lg shadow-lg animate-in slide-in-from-bottom-2">
			<span className="text-sm">{message}</span>
			<button onClick={onClose} className="text-white/80 hover:text-white">
				<X size={16} />
			</button>
		</div>
	)
}
