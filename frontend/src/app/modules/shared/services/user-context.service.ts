import { Injectable } from "@angular/core";

@Injectable({providedIn: 'root'})
export class UserContextService {

    getCurrentRole(): string{
        return sessionStorage.getItem('role') || 'MOCK_ROLE_CHIRURG';
    }

    getCurrentRoomId(): string{
        return sessionStorage.getItem('roomId') || 'MOCK_ROOM_000';
    }
}