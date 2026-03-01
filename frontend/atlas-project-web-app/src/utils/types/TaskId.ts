import type { TaskId } from "@/types/types/TaskId.type";

export function TaskId(value: string): TaskId {
	return value as TaskId
}