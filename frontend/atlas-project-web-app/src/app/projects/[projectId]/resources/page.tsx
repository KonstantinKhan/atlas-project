import { ResourcesPage } from '@/components/Resources/ResourcesPage'

export default async function Resources({ params }: { params: Promise<{ projectId: string }> }) {
	const { projectId } = await params
	return <ResourcesPage projectId={projectId} />
}
