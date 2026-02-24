import { tv } from 'tailwind-variants'
import { TaskStatus } from '@/types/enums/task-status.enum'

export const taskCard = tv({
	base: 'group relative bg-white dark:bg-zinc-900 rounded-xl border border-gray-200 dark:border-zinc-800 p-5 hover:shadow-lg transition-all duration-300 ease-in-out',
	variants: {
		status: {
			[TaskStatus.EMPTY]: 'hover:border-gray-300 dark:hover:border-zinc-700',
			[TaskStatus.BACKLOG]: 'hover:border-slate-300 dark:hover:border-slate-700',
			[TaskStatus.IN_PROGRESS]: 'hover:border-blue-300 dark:hover:border-blue-700 hover:shadow-blue-500/10',
			[TaskStatus.DONE]: 'hover:border-emerald-300 dark:hover:border-emerald-700 hover:shadow-emerald-500/10',
			[TaskStatus.BLOCKED]: 'hover:border-red-300 dark:hover:border-red-700 hover:shadow-red-500/10',
		},
	},
	defaultVariants: {
		status: TaskStatus.BACKLOG,
	},
})

export const statusBadge = tv({
	base: 'text-xs font-medium px-2.5 py-1 rounded-full whitespace-nowrap',
	variants: {
		status: {
			[TaskStatus.EMPTY]: 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300',
			[TaskStatus.BACKLOG]: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300',
			[TaskStatus.IN_PROGRESS]: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300',
			[TaskStatus.DONE]: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300',
			[TaskStatus.BLOCKED]: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300',
		},
	},
	defaultVariants: {
		status: TaskStatus.BACKLOG,
	},
})

export const statusIndicator = tv({
	base: 'absolute left-0 top-4 bottom-4 w-1 rounded-r-full',
	variants: {
		status: {
			[TaskStatus.EMPTY]: 'bg-gray-300 dark:bg-zinc-700',
			[TaskStatus.BACKLOG]: 'bg-slate-300 dark:bg-zinc-700',
			[TaskStatus.IN_PROGRESS]: 'bg-blue-500',
			[TaskStatus.DONE]: 'bg-emerald-500',
			[TaskStatus.BLOCKED]: 'bg-red-500',
		},
	},
	defaultVariants: {
		status: TaskStatus.BACKLOG,
	},
})

export const taskIdBadge = tv({
	base: 'text-xs font-mono text-gray-400 dark:text-zinc-500 bg-gray-100 dark:bg-zinc-800 px-2 py-0.5 rounded',
})

export const taskTitle = tv({
	base: 'font-semibold text-gray-900 dark:text-white truncate flex-1',
})

export const taskDescription = tv({
	base: 'text-sm text-gray-600 dark:text-zinc-400 mb-4 line-clamp-2',
})

export const taskMetadata = tv({
	base: 'flex items-center flex-wrap gap-4 text-xs text-gray-500 dark:text-zinc-500',
})

export const taskMetadataItem = tv({
	base: 'flex items-center gap-1.5',
})
