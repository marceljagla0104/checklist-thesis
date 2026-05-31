export interface OperationListItemDTO {
  id: string;
  name: string;
  createdAt: Date;
}

export interface ImageDTO {
  id: string;
  url: string;
  caption: string;
  createdAt: Date; // todo could also be string
}

export interface ChildDTO {
  id: string;
  pathDescription: string;
}

export interface ElementDTO {
  id: string;
  roles: string[];
  type: string;
  inputType: string;
  name: string;
  pathIds: string[];
  eventIds: string[];
  children: ChildDTO[];
  surgeonImages: ImageDTO[];
  surgeonInfo: string;
  studentImages: ImageDTO[];
  studentInfo: string;
  instruments: string[];
  circulatingTriggerId: string;
  allChildIds: string[];
  createdAt: Date;
}

export interface SubprocessDTO {
  id: string;
  name: string;
  elements: ElementDTO[];
  createdAt: Date;
}

export interface OperationDTO {
  id: string;
  name: string;
  subprocesses: SubprocessDTO[];
  createdAt: Date;
}
