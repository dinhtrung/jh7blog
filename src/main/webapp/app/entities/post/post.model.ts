import * as dayjs from 'dayjs';
import { ICategory } from 'app/entities/category/category.model';

export interface IPost {
  id?: number;
  title?: string;
  slug?: string | null;
  summary?: string | null;
  body?: string | null;
  createdAt?: dayjs.Dayjs | null;
  createdBy?: string | null;
  publishedDate?: dayjs.Dayjs | null;
  state?: number | null;
  tags?: string | null;
  updatedAt?: dayjs.Dayjs | null;
  updatedBy?: string | null;
  category?: ICategory | null;
}

export class Post implements IPost {
  constructor(
    public id?: number,
    public title?: string,
    public slug?: string | null,
    public summary?: string | null,
    public body?: string | null,
    public createdAt?: dayjs.Dayjs | null,
    public createdBy?: string | null,
    public publishedDate?: dayjs.Dayjs | null,
    public state?: number | null,
    public tags?: string | null,
    public updatedAt?: dayjs.Dayjs | null,
    public updatedBy?: string | null,
    public category?: ICategory | null
  ) {}
}

export function getPostIdentifier(post: IPost): number | undefined {
  return post.id;
}
