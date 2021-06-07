export interface MenuElement {
  name: string;
  icon?: string;
  type: 'TASK_FILTER' | 'LINK' | 'FOLDER';
  order: number;
  color: string;
  link?: string;
  filterId?: string;
  subElements?: MenuElement[];
  additionalAttributes?: any;
}
