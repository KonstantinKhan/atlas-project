'use client'

import { CrossProjectResourceLoadPage } from '@/components/ResourceLoad/CrossProjectResourceLoadPage'

export default function GlobalResourceLoadRoute() {
	return (
		<CrossProjectResourceLoadPage
			mode="global"
			backHref="/portfolios"
			title="Глобальная нагрузка ресурсов"
		/>
	)
}
