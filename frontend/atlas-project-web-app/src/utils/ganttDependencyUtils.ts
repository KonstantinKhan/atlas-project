import { Task } from '@/types'

export function daysBetween(earlier: Date, later: Date): number {
	const e = new Date(earlier.getFullYear(), earlier.getMonth(), earlier.getDate())
	const l = new Date(later.getFullYear(), later.getMonth(), later.getDate())
	return Math.round((l.getTime() - e.getTime()) / 86400000)
}

export function cascadeDependencies(tasks: Task[], changedId: string): Task[] {
	let result = [...tasks]
	const queue = [changedId]
	const visited = new Set<string>()

	while (queue.length > 0) {
		const currentId = queue.shift()!
		if (visited.has(currentId)) continue
		visited.add(currentId)

		const current = result.find((t) => t.id === currentId)
		if (!current?.plannedEndDate) continue

		const dependents = result.filter((t) => t.dependsOn?.includes(currentId))

		for (const dep of dependents) {
			const preds = result.filter(
				(t) => dep.dependsOn?.includes(t.id) && t.plannedEndDate
			)
			if (preds.length === 0) continue

			let latestNewStart = new Date(0)
			for (const pred of preds) {
				const lag = dep.dependsOnLag?.[pred.id] ?? 0
				const cs = new Date(pred.plannedEndDate!)
				cs.setDate(cs.getDate() + 1 + lag)
				if (cs > latestNewStart) latestNewStart = cs
			}
			const newStart = latestNewStart

			let newEnd: Date | undefined
			if (dep.plannedStartDate && dep.plannedEndDate) {
				const durationMs =
					dep.plannedEndDate.getTime() - dep.plannedStartDate.getTime()
				newEnd = new Date(newStart.getTime() + durationMs)
			}

			result = result.map((t) =>
				t.id === dep.id
					? {
							...t,
							plannedStartDate: newStart,
							...(newEnd ? { plannedEndDate: newEnd } : {}),
					  }
					: t
			)
			queue.push(dep.id)
		}
	}

	return result
}
