import { TaskCommandType } from './TaskCommandType'
import { TaskId } from './TaskId.type'

export type DeleteTaskCommand = {
	type: typeof TaskCommandType.DeleteTask
	taskId: TaskId
}
