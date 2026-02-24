import { create } from 'zustand'
import { WorkCalendar } from '@/types'

/**
 * UI-настройки для отображения календаря
 */
interface CalendarUIState {
	/** Показывать ли праздничные дни */
	showHolidays: boolean
	/** Показывать ли выходные дни */
	showWeekends: boolean
	/** Открыта ли панель настроек календаря */
	isSettingsPanelOpen: boolean
}

interface WorkCalendarStore {
	/** Текущий рабочий календарь (синхронизируется с React Query) */
	calendar: WorkCalendar | null
	/** UI-состояние */
	ui: CalendarUIState

	/** Установить календарь извне (например, из React Query) */
	setCalendar: (calendar: WorkCalendar) => void
	/** Переключить показ праздников */
	toggleHolidays: () => void
	/** Переключить показ выходных */
	toggleWeekends: () => void
	/** Открыть/закрыть панель настроек */
	setSettingsPanelOpen: (open: boolean) => void
}

const DEFAULT_CALENDAR: WorkCalendar = {
	weekendDays: [0, 6],
	holidays: [],
	workingWeekends: [],
}

export const useWorkCalendarStore = create<WorkCalendarStore>((set) => ({
	calendar: null,
	ui: {
		showHolidays: true,
		showWeekends: true,
		isSettingsPanelOpen: false,
	},

	setCalendar: (calendar) => set({ calendar }),

	toggleHolidays: () =>
		set((state) => ({
			ui: { ...state.ui, showHolidays: !state.ui.showHolidays },
		})),

	toggleWeekends: () =>
		set((state) => ({
			ui: { ...state.ui, showWeekends: !state.ui.showWeekends },
		})),

	setSettingsPanelOpen: (open) =>
		set((state) => ({
			ui: { ...state.ui, isSettingsPanelOpen: open },
		})),
}))
