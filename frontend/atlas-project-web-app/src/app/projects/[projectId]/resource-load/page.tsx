import { ResourceLoadPage } from '@/components/ResourceLoad/ResourceLoadPage'

export default async function ResourceLoadRoute({ params }: { params: Promise<{ projectId: string }> }) {
	const { projectId } = await params
	return <ResourceLoadPage projectId={projectId} />
}
