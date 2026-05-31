export interface DocumentationListItemDTO {
  id: string;
  operationName: string;
  createdAt: Date;
}

export interface CreateDocumentationReq {
  operationId: string;
  roomId: string;
}

interface EntryDTO {
  id: string;
  elementId: string;
  description: string;
  textEvent?: string;
  phrases: string[];
  duration?: number;
  calculatedDuration?: number;
  startedAt: Date;
  finishedAt: Date;
  createdAt: Date;
}

export interface DocumentationDTO {
  id: string;
  entries: EntryDTO[];
}
