export const formatDate = (date: Date): string => {
	return new Intl.DateTimeFormat('ru-RU', {
		day: 'numeric',
		month: 'short',
		year: 'numeric',
	}).format(date)
}
