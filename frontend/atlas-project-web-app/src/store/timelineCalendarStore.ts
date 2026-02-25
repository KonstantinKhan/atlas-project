import { create } from 'zustand'
import { TimelineCalendar } from '@/types'
import { createDefaultTimelineCalendar } from '@/types/schemas/timeline-calendar.schema'

interface CalendarUIState {
	showHolidays: boolean
	showWeekends: boolean
	isSettingsPanelOpen: boolean
}

interface TimelineCalendarStore {
	calendar: TimelineCalendar
	ui: CalendarUIState

	setCalendar: (calendar: TimelineCalendar) => void
	toggleHolidays: () => void
	toggleWeekends: () => void
	setSettingsPanelOpen: (open: boolean) => void
}

export const useTimelineCalendarStore = create<TimelineCalendarStore>((set) => ({
	calendar: createDefaultTimelineCalendar(),
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
