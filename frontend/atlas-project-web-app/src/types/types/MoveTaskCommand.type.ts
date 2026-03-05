import { LocalDate } from './LocalDate.type'
import { TaskCommandType } from './TaskCommandType'
import { TaskId } from './TaskId.type'

export type MoveTaskCommand = {
	type: typeof TaskCommandType.MoveTask
	taskId: TaskId
	newStartDate: LocalDate
}
