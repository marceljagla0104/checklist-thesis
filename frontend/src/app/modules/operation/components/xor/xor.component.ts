import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, OnInit, Output,} from '@angular/core';
import {Child, Element} from '../../models';
import {UntypedFormControl, UntypedFormGroup} from '@angular/forms';
import {filter, Subject, takeUntil} from 'rxjs';
import {Role} from '../../../shared/models';
import {DocumentationService} from '../../../documentation/services';
import {SyncService} from '../../../shared/services';

// checklist item for displaying xor options
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'app-xor',
  templateUrl: './xor.component.html',
  styleUrls: ['./xor.component.scss'],
})
export class XorComponent implements OnInit, OnDestroy {
  @Input()
  id: string;

  @Input()
  name: string;

  @Input()
  children: Child[];

  @Input()
  paths: Map<string, Element[]>;

  @Output()
  showElements = new EventEmitter<string>();

  formGroup: UntypedFormGroup;

  disabled = false;

  role: Role;

  private documentationId: string;
  private DESCRIPTION_NAME: string;

  private destroy$: Subject<boolean> = new Subject<boolean>();

  constructor(
    private documentationService: DocumentationService,
    private syncService: SyncService,
  ) {}

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  ngOnInit(): void {
    this.documentationId = window.localStorage.getItem('documentationId');
    this.DESCRIPTION_NAME = this.name + ': ';
    this.formGroup = new UntypedFormGroup({
      selectedOption: new UntypedFormControl(''),
    });

    this.role = window.sessionStorage.getItem('role') as Role;
    if (this.role !== Role.SURGEON) {
      this.disabled = true;
    }

    this.loadStatusInitially();
    this.listenToStatusChanges();
  }

  setSelected(selected: string) {
    this.toggleShowCallback();
    if (this.disabled) {
      return;
    }

    this.formGroup.get('selectedOption').setValue(selected);
    this.documentationService
      .createOrUpdateEntry(
        this.documentationId,
        this.id,
        this.DESCRIPTION_NAME + selected,
        null,
        new Date(),
      )
      .subscribe();
  }

  toggleShowCallback() {
    this.showElements.emit(this.id);
  }

  getPathElements(id: string) {
    return this.paths.get(id);
  }

  isChecked(id: string) {
    return this.formGroup.get('selectedOption').value === id;
  }

  getSuccessors(id: string) {
    const successors = this.getPathElements(id);
    return successors
      .map((successor) => {
        let text = ' ';
        if (this.role === Role.SCRUB) {
          if (successor.instruments && successor.instruments.length > 0) {
            text += successor.instruments.join(', ');
          }
        } else {
          text += successor.name;
        }
        return text;
      })
      .filter((text) => text.trim().length > 0)
      .slice(0, 3) // only first three
      .toString();
  }

  private loadStatusInitially() {
    this.documentationService
      .loadEntry(this.id)
      .pipe(filter((entry) => !!entry))
      .subscribe((entry) => {
        this.formGroup
          .get('selectedOption')
          .setValue(this.removeNameFromDescription(entry.description));
        this.toggleShowCallback();
      });
  }

  private listenToStatusChanges() {
    this.syncService
      .onEntryUpdated(this.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((change) => {
        if (change.finishedAt) {
          this.formGroup
            .get('selectedOption')
            .setValue(this.removeNameFromDescription(change.description));
          this.toggleShowCallback();
        }
      });
  }

  private removeNameFromDescription(description: string) {
    return description.replace(this.DESCRIPTION_NAME, '');
  }
}
