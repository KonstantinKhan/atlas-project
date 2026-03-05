import { LocalDate } from './LocalDate.type'
import { TaskCommandType } from './TaskCommandType'
import { TaskId } from './TaskId.type'

export type PlanFromEndCommand = {
	type: typeof TaskCommandType.PlanFromEnd
	taskId: TaskId
	newEndDate: LocalDate
}
