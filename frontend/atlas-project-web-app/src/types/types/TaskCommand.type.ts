import { CreateTaskInPoolCommand } from "./CreateTaskInPoolCommand.type"
import { ChangeStartDateCommand } from "./ChangeStartDateCommand.type"
import { ChangeEndDateCommand } from "./ChangeEndDateCommand.type"
import { UpdateTitleCommand } from "./UpdateTitleCommand.type"

export type TaskCommand =
	| CreateTaskInPoolCommand
	| ChangeStartDateCommand
	| ChangeEndDateCommand
	| UpdateTitleCommand