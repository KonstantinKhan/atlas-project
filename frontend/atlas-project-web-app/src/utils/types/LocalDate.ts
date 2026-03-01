import type { LocalDate } from "@/types/types/LocalDate.type"

export function LocalDate(value: string): LocalDate {
  // todo(): валидация YYYY-MM-DD
  return value as LocalDate
}