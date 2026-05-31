import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { Role } from '../../modules/shared/models';
import { SyncService } from '../../modules/shared/services';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './start.page.html',
  styleUrls: ['./start.page.scss'],
})
export class StartPage {
  step = 1;

  constructor(
    private router: Router,
    private syncService: SyncService,
  ) {}

  choseRole(role: Role) {
    window.sessionStorage.setItem('role', role);

    if (role === Role.CIRCULATING) {
      // if switching role to circulating and websocket connection is open, close it
      this.syncService.closeConnection();
      this.router.navigate(['/circulating']);
      return;
    }
    this.nextStep();
  }

  nextStep() {
    this.step++;
  }

  back() {
    this.step--;
  }

  choseRoom(no: number) {
    window.sessionStorage.setItem('roomId', no.toString());
    this.router.navigate(['/start']);
  }

  protected readonly Role = Role;

  gotToAdmin() {
    this.router.navigate(['/admin']);
  }
}
