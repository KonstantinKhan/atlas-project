import { TaskCommandType } from './TaskCommandType'

export interface CreateTaskCommand {
	type: typeof TaskCommandType.CreateTask
}
