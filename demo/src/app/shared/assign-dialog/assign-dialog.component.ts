import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  OnInit
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { UserService } from '@umb-ag/curo-core';
import { combineLatest, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-assign-dialog',
  templateUrl: './assign-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AssignDialogComponent implements OnInit {
  user = new FormControl();
  users$: Observable<any[]>;
  filteredUsers$?: Observable<any[]>;

  constructor(
    private userService: UserService,
    @Inject(MAT_DIALOG_DATA) private data: { assignee: string }
  ) {
    this.users$ = this.userService.getUsers();
    this.user.patchValue(this.data.assignee);
  }

  ngOnInit(): void {
    this.filteredUsers$ = combineLatest([
      this.user.valueChanges.pipe(map((value) => value.toLowerCase())),
      this.users$
    ]).pipe(
      map(([value, users]) =>
        users.filter(
          (user) =>
            value !== '' &&
            (`${user.firstname} ${user.lastname}`
              .toLowerCase()
              .includes(value) ||
              user.id.includes(value))
        )
      )
    );
  }
}
