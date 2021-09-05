export interface ICategory {
  id?: number;
  name?: string | null;
  slug?: string | null;
  description?: string | null;
  imageContentType?: string | null;
  image?: string | null;
}

export class Category implements ICategory {
  constructor(
    public id?: number,
    public name?: string | null,
    public slug?: string | null,
    public description?: string | null,
    public imageContentType?: string | null,
    public image?: string | null
  ) {}
}

export function getCategoryIdentifier(category: ICategory): number | undefined {
  return category.id;
}
