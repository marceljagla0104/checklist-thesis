export interface OperationListItemDTO {
  id: string;
  name: string;
  createdAt: Date;
}

export interface DocumentationListItemDTO {
  id: string;
  operationId: string;
  operationName: string;
  roomId: string;
  createdAt: Date;
  savedAt: Date;
}
