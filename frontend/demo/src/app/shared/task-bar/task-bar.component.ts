import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

@Component({
  selector: 'app-task-bar',
  templateUrl: './task-bar.component.html',
  styleUrls: ['./task-bar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TaskBarComponent {
  @Input()
  title?: string;

  @Input()
  completeLabel = 'Next';

  @Output()
  completeTask = new EventEmitter();

  @Output()
  assignTask = new EventEmitter();

  complete(): void {
    this.completeTask.emit();
  }

  assign(): void {
    this.assignTask.emit();
  }
}
