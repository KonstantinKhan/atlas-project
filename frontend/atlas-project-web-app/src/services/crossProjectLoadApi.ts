import {
	CrossProjectOverloadReportSchema,
	type CrossProjectOverloadReport,
} from '@/types/schemas/crossProjectLoad.schema'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export async function getGlobalResourceLoad(
	from?: string | null,
	to?: string | null,
): Promise<CrossProjectOverloadReport> {
	const params = new URLSearchParams()
	if (from) params.set('from', from)
	if (to) params.set('to', to)
	const qs = params.toString() ? `?${params.toString()}` : ''
	const response = await fetch(`${API_BASE_URL}/resource-load${qs}`, {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch global resource load')
	return CrossProjectOverloadReportSchema.parse(await response.json())
}

export async function getPortfolioResourceLoad(
	portfolioId: string,
	from?: string | null,
	to?: string | null,
): Promise<CrossProjectOverloadReport> {
	const params = new URLSearchParams()
	if (from) params.set('from', from)
	if (to) params.set('to', to)
	const qs = params.toString() ? `?${params.toString()}` : ''
	const response = await fetch(`${API_BASE_URL}/portfolios/${portfolioId}/resource-load${qs}`, {
		headers: { Accept: 'application/json' },
	})
	if (!response.ok) throw new Error('Failed to fetch portfolio resource load')
	return CrossProjectOverloadReportSchema.parse(await response.json())
}
