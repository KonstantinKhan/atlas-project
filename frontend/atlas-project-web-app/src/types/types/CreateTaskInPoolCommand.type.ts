import { TaskCommandType } from './TaskCommandType'

export interface CreateTaskInPoolCommand {
	type: typeof TaskCommandType.CreateTaskInPool
	title: string
}
