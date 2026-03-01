import { LocalDate } from "./LocalDate.type"
import { TaskCommandType } from "./TaskCommandType"
import { TaskId } from "./TaskId.type"

export type ChangeEndDateCommand = {
	type: typeof TaskCommandType.ChangeEndDate
	taskId: TaskId
	newEndDate: LocalDate
}
