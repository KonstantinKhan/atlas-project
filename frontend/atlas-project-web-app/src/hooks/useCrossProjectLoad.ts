import { useQuery } from '@tanstack/react-query'
import { getGlobalResourceLoad, getPortfolioResourceLoad } from '@/services/crossProjectLoadApi'
import type { CrossProjectOverloadReport } from '@/types/schemas/crossProjectLoad.schema'

export function useGlobalResourceLoad(from?: string | null, to?: string | null) {
	return useQuery<CrossProjectOverloadReport>({
		queryKey: ['globalResourceLoad', from, to],
		queryFn: () => getGlobalResourceLoad(from, to),
	})
}

export function usePortfolioResourceLoad(
	portfolioId: string,
	from?: string | null,
	to?: string | null,
) {
	return useQuery<CrossProjectOverloadReport>({
		queryKey: ['portfolioResourceLoad', portfolioId, from, to],
		queryFn: () => getPortfolioResourceLoad(portfolioId, from, to),
		enabled: !!portfolioId,
	})
}
