import { TaskCommandType } from './TaskCommandType'
import { TaskId } from './TaskId.type'

export type UpdateTitleCommand = {
	type: typeof TaskCommandType.UpdateTitle
	taskId: TaskId
	newTitle: string
}
