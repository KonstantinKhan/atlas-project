import { TaskCommandType } from './TaskCommandType'
import { TaskId } from './TaskId.type'

export type UpdateFieldsCommand = {
	type: typeof TaskCommandType.UpdateFields
	taskId: TaskId
	updates: {
		actualStartDate?: string | null
		actualEndDate?: string | null
		baselineEffortHours?: number | null
		additionalEffortHours?: number | null
	}
}
