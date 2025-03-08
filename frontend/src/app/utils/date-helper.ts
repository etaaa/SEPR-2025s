import { formatDate } from '@angular/common';

export function formatIsoDate(date: Date): string {
  return formatDate(date, 'yyyy-MM-dd', 'en-DK');
}
