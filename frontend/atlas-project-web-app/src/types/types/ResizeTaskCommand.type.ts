import { LocalDate } from './LocalDate.type'
import { TaskCommandType } from './TaskCommandType'
import { TaskId } from './TaskId.type'

export type ResizeTaskCommand = {
	type: typeof TaskCommandType.ResizeTask
	taskId: TaskId
	newEndDate: LocalDate
}
