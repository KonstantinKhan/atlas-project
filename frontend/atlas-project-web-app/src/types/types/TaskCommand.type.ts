import { ChangeStartDateCommand } from "./ChangeStartDateCommand.type"
import { ChangeEndDateCommand } from "./ChangeEndDateCommand.type"
import { UpdateTitleCommand } from "./UpdateTitleCommand.type"

export type TaskCommand =
	| ChangeStartDateCommand
	| ChangeEndDateCommand
	| UpdateTitleCommand