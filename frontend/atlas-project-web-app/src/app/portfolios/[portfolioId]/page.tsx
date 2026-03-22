import { PortfolioDashboard } from '@/components/Portfolios/PortfolioDashboard'

export default async function PortfolioPage({ params }: { params: Promise<{ portfolioId: string }> }) {
	const { portfolioId } = await params
	return <PortfolioDashboard portfolioId={portfolioId} />
}
