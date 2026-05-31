export enum WebsocketMsgType {
  ENTRY_UPDATED = 'ENTRY_UPDATED',
  ENTRY_REMOVED = 'ENTRY_REMOVED',
  CIRCULATING_TASK_DONE = 'CIRCULATING_TASK_DONE',
  CIRCULATING_TASK_STARTED = 'CIRCULATING_TASK_STARTED',
  CHANGE_TAB = 'CHANGE_TAB',
}

export class ChecklistWebsocketMsg {
  type: WebsocketMsgType;
  documentationId: string;
  roomId: string;
  payload: any;
}
