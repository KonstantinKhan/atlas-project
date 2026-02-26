import { create } from 'zustand'

interface CalendarUIState {
	showHolidays: boolean
	showWeekends: boolean
	isSettingsPanelOpen: boolean
}

interface TimelineCalendarStore {
	ui: CalendarUIState

	toggleHolidays: () => void
	toggleWeekends: () => void
	setSettingsPanelOpen: (open: boolean) => void
}

export const useTimelineCalendarStore = create<TimelineCalendarStore>((set) => ({
	ui: {
		showHolidays: true,
		showWeekends: true,
		isSettingsPanelOpen: false,
	},

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
