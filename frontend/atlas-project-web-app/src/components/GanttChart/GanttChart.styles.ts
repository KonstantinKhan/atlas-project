import { tv } from 'tailwind-variants'
import { TaskStatus } from '@/types/enums/task-status.enum'

export const ganttBar = tv({
	base: 'absolute top-1.5 bottom-1.5 rounded-md flex items-center cursor-default group',
	variants: {
		status: {
			[TaskStatus.EMPTY]: 'bg-gray-300 dark:bg-zinc-600',
			[TaskStatus.BACKLOG]: 'bg-slate-400 dark:bg-slate-600',
			[TaskStatus.IN_PROGRESS]: 'bg-blue-500 dark:bg-blue-600',
			[TaskStatus.DONE]: 'bg-emerald-500 dark:bg-emerald-600',
			[TaskStatus.BLOCKED]: 'bg-red-400 dark:bg-red-600',
		},
	},
	defaultVariants: {
		status: TaskStatus.BACKLOG,
	},
})

export const ganttDayCell = tv({
	base: 'border-r border-b border-gray-200 dark:border-zinc-800',
	variants: {
		isNonWorking: {
			true: 'bg-red-50 dark:bg-red-900/20',
		},
		isToday: {
			true: 'bg-cyan-50 dark:bg-cyan-900/30',
		},
	},
	compoundVariants: [
		{
			isNonWorking: true,
			isToday: true,
			className: 'bg-gradient-to-r from-red-50 from-[35%] via-cyan-50 via-[50%] to-red-50 to-[65%] dark:from-red-900/20 dark:from-[35%] dark:via-cyan-900/30 dark:via-[50%] dark:to-red-900/20 dark:to-[65%]',
		},
	],
})

export const ganttHeaderDay = tv({
	base: 'text-xs text-center border-r border-gray-200 dark:border-zinc-800 select-none',
	variants: {
		isNonWorking: {
			true: 'text-gray-400 dark:text-zinc-600',
			false: 'text-gray-600 dark:text-zinc-400',
		},
		isToday: {
			true: 'font-bold text-blue-600 dark:text-blue-400',
		},
	},
})
