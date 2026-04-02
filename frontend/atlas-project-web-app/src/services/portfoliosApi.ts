import {
	PortfolioSchema,
	PortfolioListSchema,
	ProjectSummarySchema,
	ProjectSummaryListSchema,
	type Portfolio,
	type ProjectSummary,
	type ProjectPriority,
	ReadPortfolioResponseSchema,
	CreatePortfolioSchema,
	UpdatePortfolioSchema,
} from '@/types/schemas/portfolio.schema'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export async function getPortfolios(): Promise<Portfolio[]> {
	const response = await fetch(`${API_BASE_URL}/portfolios`, {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch portfolios')
	const data = PortfolioListSchema.parse(await response.json())
	return data.foundPortfolios
}

export async function getPortfolio(id: string): Promise<Portfolio> {
	const response = await fetch(`${API_BASE_URL}/portfolios/${id}`, {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch portfolio')
	const data = ReadPortfolioResponseSchema.parse(response.json)
	return data.readPortfolio
}

export async function createPortfolio(
	name: string,
	description: string = '',
): Promise<Portfolio> {
	const requestBody = {
		createPortfolio: {
			name,
			description,
		},
	}

	const response = await fetch(`${API_BASE_URL}/portfolios`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(requestBody),
	})

	if (!response.ok) throw new Error('Failed to create portfolio')

	const data = CreatePortfolioSchema.parse(response.json)
	return data.createdPortfolio
}

export async function updatePortfolio(
	id: string,
	updates: { name?: string; description?: string },
): Promise<Portfolio> {
	const requestBody = {
		updatePortfolio: {
			id,
			...updates,
		},
	}

	const response = await fetch(`${API_BASE_URL}/portfolios/${id}`, {
		method: 'PATCH',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(requestBody),
	})

	if (!response.ok) throw new Error('Failed to update portfolio')

	const data = UpdatePortfolioSchema.parse(response.json)

	return data.updatedPortfolio
}

export async function deletePortfolio(id: string): Promise<void> {
	const response = await fetch(`${API_BASE_URL}/portfolios/${id}`, {
		method: 'DELETE',
	})
	if (!response.ok) throw new Error('Failed to delete portfolio')
}

export async function getProjects(
	portfolioId: string,
): Promise<ProjectSummary[]> {
	const response = await fetch(
		`${API_BASE_URL}/portfolios/${portfolioId}/projects`,
		{
			headers: { Accept: 'application/json' },
		},
	)
	if (!response.ok) throw new Error('Failed to fetch projects')
	const data = ProjectSummaryListSchema.parse(await response.json())
	return data.projects
}

export async function createProject(
	portfolioId: string,
	name: string,
	priority: ProjectPriority = 'MEDIUM',
): Promise<ProjectSummary> {
	const response = await fetch(
		`${API_BASE_URL}/portfolios/${portfolioId}/projects`,
		{
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ name, priority }),
		},
	)
	if (!response.ok) throw new Error('Failed to create project')
	return ProjectSummarySchema.parse(await response.json())
}

export async function reorderProjects(
	portfolioId: string,
	projectSortOrders: Array<{ projectId: string; sortOrder: number }>,
): Promise<void> {
	const response = await fetch(
		`${API_BASE_URL}/portfolios/${portfolioId}/projects/reorder`,
		{
			method: 'PATCH',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ projectSortOrders }),
		},
	)
	if (!response.ok) throw new Error('Failed to reorder projects')
}
