import { ProjectTaskStatus } from '@/types/generated/enums/project-task-status.enum'

export enum TaskStatus {
	EMPTY = 'empty',
	BACKLOG = 'backlog',
	IN_PROGRESS = 'in progress',
	DONE = 'done',
	BLOCKED = 'blocked',
}

export const TaskStatusLabels: Record<TaskStatus, string> = {
	[TaskStatus.EMPTY]: 'Empty',
	[TaskStatus.BACKLOG]: 'Backlog',
	[TaskStatus.IN_PROGRESS]: 'In Progress',
	[TaskStatus.DONE]: 'Done',
	[TaskStatus.BLOCKED]: 'Blocked',
}

export const ProjectTaskStatusLabels: Record<ProjectTaskStatus, string> = {
	[ProjectTaskStatus.EMPTY]: 'Empty',
	[ProjectTaskStatus.BACKLOG]: 'Backlog',
	[ProjectTaskStatus.IN_PROGRESS]: 'In Progress',
	[ProjectTaskStatus.DONE]: 'Done',
	[ProjectTaskStatus.BLOCKED]: 'Blocked',
}
