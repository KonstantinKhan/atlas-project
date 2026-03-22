import { GanttChart } from '@/components/GanttChart'

export default async function ProjectPage({ params }: { params: Promise<{ projectId: string }> }) {
	const { projectId } = await params
	return <GanttChart projectId={projectId} />
}
