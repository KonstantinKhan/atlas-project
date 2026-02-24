'use client'

export interface DefaultZoneProps {
	value: number
	label?: string
}

export const DefaultZone = ({ value, label = 'В зоне по умолчанию:' }: DefaultZoneProps) => {
	return (
		<div className="flex flex-col items-center gap-2 p-4 border-2 border-dashed border-gray-300 rounded-xl bg-gray-50">
			<span className="text-gray-700 font-medium">{label} {value}</span>
		</div>
	)
}
