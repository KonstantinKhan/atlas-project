'use client'

import { use } from 'react'
import { CrossProjectResourceLoadPage } from '@/components/ResourceLoad/CrossProjectResourceLoadPage'

export default function PortfolioResourceLoadRoute({
	params,
}: {
	params: Promise<{ portfolioId: string }>
}) {
	const { portfolioId } = use(params)
	return (
		<CrossProjectResourceLoadPage
			mode="portfolio"
			portfolioId={portfolioId}
			backHref={`/portfolios/${portfolioId}`}
			title="Нагрузка ресурсов портфеля"
		/>
	)
}
