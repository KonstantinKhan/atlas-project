export const TaskCommandType = {
	CreateTask: 'createTask',
	CreateTaskInPool: 'createTaskInPool',
	ChangeStartDate: 'changeStartDate',
	ChangeEndDate: 'changeEndDate',
	UpdateTitle: 'updateTitle',
	DeleteTask: 'deleteTask',
	PlanFromEnd: 'planFromEnd',
	AssignSchedule: 'assignSchedule',
	MoveTask: 'moveTask',
	ResizeTask: 'resizeTask',
} as const