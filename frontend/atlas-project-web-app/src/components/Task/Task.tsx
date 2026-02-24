import { Task } from '@/types'
import { TaskStatusLabels } from '@/types/enums/task-status.enum'
import { FC } from 'react'
import {
	taskCard,
	statusBadge,
	statusIndicator,
	taskIdBadge,
	taskTitle,
	taskDescription,
	taskMetadata,
	taskMetadataItem,
} from './Task.styles'
import { formatDate } from '@/utils/formatDate'
import { Clock, Calendar, CalendarCheck2 } from 'lucide-react'

interface TaskProps {
	task: Task
}

const TaskCard: FC<TaskProps> = ({ task }) => {
	return (
		<div className={taskCard({ status: task.status })}>
			<div className={statusIndicator({ status: task.status })} />
			<div className="pl-3">
				<div className="flex items-start justify-between gap-3 mb-3">
					<div className="flex items-center gap-2 flex-1 min-w-0">
						<span className={taskIdBadge()}>#{task.id}</span>
						<h3 className={taskTitle()}>{task.title}</h3>
					</div>
					<span className={statusBadge({ status: task.status })}>
						{TaskStatusLabels[task.status]}
					</span>
				</div>
				<p className={taskDescription()}>{task.description}</p>
				<div className={taskMetadata()}>
					{task.plannedCalendarDuration && (
						<div className={taskMetadataItem()}>
							<Clock size={16} />
							<span>
								{task.plannedCalendarDuration} {task.plannedCalendarDuration === 1 ? 'day' : 'days'}
							</span>
						</div>
					)}
					{task.plannedStartDate && (
						<div className={taskMetadataItem()}>
							<Calendar size={16} />
							<span>{formatDate(task.plannedStartDate)}</span>
						</div>
					)}
					{task.plannedEndDate && (
						<div className={taskMetadataItem()}>
							<CalendarCheck2 size={16} />
							<span>{formatDate(task.plannedEndDate)}</span>
						</div>
					)}
				</div>
			</div>
		</div>
	)
}

export default TaskCard
