import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnDestroy,
  Output
} from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AssignDialogComponent } from '../../shared/assign-dialog/assign-dialog.component';

@Component({
  selector: 'app-assign-action',
  templateUrl: './assign-action.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssignActionComponent implements OnDestroy {
  @Input()
  assignee?: string;

  @Output()
  assign = new EventEmitter();

  private dialogRef?: MatDialogRef<AssignDialogComponent>;
  private dialogSubscription?: Subscription;

  constructor(private matDialog: MatDialog) {}

  ngOnDestroy() {
    this.dialogRef?.close();
    this.dialogSubscription?.unsubscribe();
  }

  @HostListener('click', ['$event'])
  stopPropagation(event: MouseEvent): void {
    event.stopPropagation();
  }

  showDialog(): void {
    this.dialogRef = this.matDialog.open(AssignDialogComponent, {
      data: { assignee: this.assignee }
    });
    this.dialogSubscription = this.dialogRef
      .afterClosed()
      .pipe(filter((value) => value !== undefined))
      .subscribe((user) => this.assign.emit(user));
  }

  claim(): void {
    this.assign.emit('demo');
  }
}
