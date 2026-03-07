import { TaskCommandType } from './TaskCommandType'
import { TaskId } from './TaskId.type'

export type UpdateDescriptionCommand = {
	type: typeof TaskCommandType.UpdateDescription
	taskId: TaskId
	newDescription: string
}
