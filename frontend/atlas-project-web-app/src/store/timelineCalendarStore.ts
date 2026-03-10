import { create } from 'zustand'

export type ViewMode = 'day' | 'week'

export type AnalysisTab = 'blockers' | 'available' | 'whatif'

interface CalendarUIState {
	viewMode: ViewMode
	showHolidays: boolean
	showWeekends: boolean
	isSettingsPanelOpen: boolean
	isAnalysisPanelOpen: boolean
	analysisPanelTab: AnalysisTab
	selectedAnalysisTaskId: string | null
	whatIfMode: 'start' | 'end'
	whatIfNewStart: string | null
	whatIfNewEnd: string | null
}

interface TimelineCalendarStore {
	ui: CalendarUIState

	setViewMode: (mode: ViewMode) => void
	toggleHolidays: () => void
	toggleWeekends: () => void
	setSettingsPanelOpen: (open: boolean) => void
	setAnalysisPanelOpen: (open: boolean) => void
	setAnalysisPanelTab: (tab: AnalysisTab) => void
	setSelectedAnalysisTaskId: (id: string | null) => void
	setWhatIfMode: (mode: 'start' | 'end') => void
	setWhatIfNewStart: (date: string | null) => void
	setWhatIfNewEnd: (date: string | null) => void
}

export const useTimelineCalendarStore = create<TimelineCalendarStore>((set) => ({
	ui: {
		viewMode: 'day',
		showHolidays: true,
		showWeekends: true,
		isSettingsPanelOpen: false,
		isAnalysisPanelOpen: false,
		analysisPanelTab: 'blockers',
		selectedAnalysisTaskId: null,
		whatIfMode: 'start',
		whatIfNewStart: null,
		whatIfNewEnd: null,
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

	setAnalysisPanelOpen: (open) =>
		set((state) => ({
			ui: { ...state.ui, isAnalysisPanelOpen: open },
		})),

	setAnalysisPanelTab: (tab) =>
		set((state) => ({
			ui: { ...state.ui, analysisPanelTab: tab },
		})),

	setSelectedAnalysisTaskId: (id) =>
		set((state) => ({
			ui: { ...state.ui, selectedAnalysisTaskId: id },
		})),

	setWhatIfMode: (mode) =>
		set((state) => ({
			ui: { ...state.ui, whatIfMode: mode },
		})),

	setWhatIfNewStart: (date) =>
		set((state) => ({
			ui: { ...state.ui, whatIfNewStart: date },
		})),

	setWhatIfNewEnd: (date) =>
		set((state) => ({
			ui: { ...state.ui, whatIfNewEnd: date },
		})),
}))
