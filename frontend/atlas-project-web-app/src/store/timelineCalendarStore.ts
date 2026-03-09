import { create } from 'zustand'

export type ViewMode = 'day' | 'week'

interface CalendarUIState {
	viewMode: ViewMode
	showHolidays: boolean
	showWeekends: boolean
	isSettingsPanelOpen: boolean
}

interface TimelineCalendarStore {
	ui: CalendarUIState

	setViewMode: (mode: ViewMode) => void
	toggleHolidays: () => void
	toggleWeekends: () => void
	setSettingsPanelOpen: (open: boolean) => void
}

export const useTimelineCalendarStore = create<TimelineCalendarStore>((set) => ({
	ui: {
		viewMode: 'day',
		showHolidays: true,
		showWeekends: true,
		isSettingsPanelOpen: false,
	},

	setViewMode: (mode) =>
		set((state) => ({
			ui: { ...state.ui, viewMode: mode },
		})),

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
