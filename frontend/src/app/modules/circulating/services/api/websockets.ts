export enum WebsocketMsgType {
  CALL_CIRCULATING = 'CALL_CIRCULATING',
  CIRCULATING_TASK_DONE = 'CIRCULATING_TASK_DONE',
}

export class ChecklistWebsocketMsg {
  type: WebsocketMsgType;
  documentationId: string;
  roomId: string;
  payload: any;
}
