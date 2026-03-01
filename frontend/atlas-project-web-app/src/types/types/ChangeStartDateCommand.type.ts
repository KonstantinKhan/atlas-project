import { LocalDate } from "./LocalDate.type"
import { TaskCommandType } from "./TaskCommandType"
import { TaskId } from "./TaskId.type"

export type ChangeStartDateCommand = {
	type: typeof TaskCommandType.ChangeStartDate
	taskId: TaskId
	newStartDate: LocalDate
}