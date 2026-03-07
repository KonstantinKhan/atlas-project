import { CreateTaskInPoolCommand } from "./CreateTaskInPoolCommand.type"
import { ChangeStartDateCommand } from "./ChangeStartDateCommand.type"
import { ChangeEndDateCommand } from "./ChangeEndDateCommand.type"
import { UpdateTitleCommand } from "./UpdateTitleCommand.type"
import { UpdateDescriptionCommand } from "./UpdateDescriptionCommand.type"
import { ChangeStatusCommand } from "./ChangeStatusCommand.type"
import { DeleteTaskCommand } from "./DeleteTaskCommand.type"
import { PlanFromEndCommand } from "./PlanFromEndCommand.type"
import { AssignScheduleCommand } from "./AssignScheduleCommand.type"
import { MoveTaskCommand } from "./MoveTaskCommand.type"
import { ResizeTaskCommand } from "./ResizeTaskCommand.type"

export type TaskCommand =
	| CreateTaskInPoolCommand
	| ChangeStartDateCommand
	| ChangeEndDateCommand
	| UpdateTitleCommand
	| UpdateDescriptionCommand
	| ChangeStatusCommand
	| DeleteTaskCommand
	| PlanFromEndCommand
	| AssignScheduleCommand
	| MoveTaskCommand
	| ResizeTaskCommand
