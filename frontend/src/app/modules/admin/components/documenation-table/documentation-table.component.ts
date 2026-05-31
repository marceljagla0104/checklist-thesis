import { Component, Input, OnInit } from '@angular/core';
import { DocumentationListItemDTO } from '../../services/api/admin';
import { Router } from '@angular/router';
import { Role } from '../../../shared/models';

@Component({
  selector: 'app-documentation-table',
  templateUrl: './documentation-table.component.html',
  styleUrls: ['./documentation-table.component.scss'],
})
export class DocumentationTableComponent implements OnInit {
  @Input()
  docus: DocumentationListItemDTO[];

  @Input()
  finished = false;

  displayedColumns: string[];

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.displayedColumns = this.finished
      ? ['room', 'name', 'createdAt', 'savedAt']
      : ['room', 'name', 'createdAt'];
  }

  openDocu(id: string, operationId: string) {
    window.localStorage.setItem('documentationId', id);
    window.sessionStorage.setItem('role', Role.SURGEON);
    this.router.navigate(['../checklist', operationId]);
  }
}
