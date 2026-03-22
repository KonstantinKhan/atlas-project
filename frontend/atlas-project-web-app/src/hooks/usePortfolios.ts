import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
	getPortfolios,
	getPortfolio,
	createPortfolio,
	updatePortfolio,
	deletePortfolio,
	getProjects,
	createProject,
	reorderProjects,
} from '@/services/portfoliosApi'
import type { Portfolio, ProjectSummary } from '@/types/schemas/portfolio.schema'

export function usePortfolios() {
	return useQuery<Portfolio[]>({
		queryKey: ['portfolios'],
		queryFn: getPortfolios,
	})
}

export function usePortfolio(id: string | null) {
	return useQuery<Portfolio>({
		queryKey: ['portfolio', id],
		queryFn: () => getPortfolio(id!),
		enabled: !!id,
	})
}

export function useCreatePortfolio() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ name, description }: { name: string; description?: string }) =>
			createPortfolio(name, description),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['portfolios'] })
		},
	})
}

export function useUpdatePortfolio() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ id, updates }: { id: string; updates: { name?: string; description?: string } }) =>
			updatePortfolio(id, updates),
		onSuccess: (_, { id }) => {
			queryClient.invalidateQueries({ queryKey: ['portfolios'] })
			queryClient.invalidateQueries({ queryKey: ['portfolio', id] })
		},
	})
}

export function useDeletePortfolio() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ id }: { id: string }) => deletePortfolio(id),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ['portfolios'] })
		},
	})
}

export function useProjects(portfolioId: string | null) {
	return useQuery<ProjectSummary[]>({
		queryKey: ['projects', portfolioId],
		queryFn: () => getProjects(portfolioId!),
		enabled: !!portfolioId,
	})
}

export function useCreateProject() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({ portfolioId, name, priority }: { portfolioId: string; name: string; priority?: number }) =>
			createProject(portfolioId, name, priority),
		onSuccess: (_, { portfolioId }) => {
			queryClient.invalidateQueries({ queryKey: ['projects', portfolioId] })
		},
	})
}

export function useReorderProjects() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: ({
			portfolioId,
			projectPriorities,
		}: {
			portfolioId: string
			projectPriorities: Array<{ projectId: string; priority: number }>
		}) => reorderProjects(portfolioId, projectPriorities),
		onSuccess: (_, { portfolioId }) => {
			queryClient.invalidateQueries({ queryKey: ['projects', portfolioId] })
		},
	})
}
