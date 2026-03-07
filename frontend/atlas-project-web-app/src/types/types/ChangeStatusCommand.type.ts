import { TaskCommandType } from './TaskCommandType'
import { TaskId } from './TaskId.type'
import { ProjectTaskStatus } from '@/types/generated/enums/project-task-status.enum'

export type ChangeStatusCommand = {
	type: typeof TaskCommandType.ChangeStatus
	taskId: TaskId
	newStatus: ProjectTaskStatus
}
