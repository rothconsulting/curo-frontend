import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { UserService } from '@umb-ag/curo-core';
import { of } from 'rxjs';
import { AssignDialogComponent } from './assign-dialog.component';

describe('AssignDialogComponent', () => {
  let component: AssignDialogComponent;
  let fixture: ComponentFixture<AssignDialogComponent>;
  let userService: UserService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        HttpClientTestingModule,
        ReactiveFormsModule,

        MatAutocompleteModule,
        MatDialogModule,
        MatInputModule
      ],
      declarations: [AssignDialogComponent],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: { assignee: 'demo' }
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    userService = TestBed.inject(UserService);
    spyOn(userService, 'getUsers').and.returnValue(
      of([
        {
          id: '1',
          firstname: 'Thor',
          lastname: 'Odinson'
        },
        {
          id: '2',
          firstname: 'Thor',
          lastname: 'test'
        },
        {
          id: '3',
          firstname: 'Loki',
          lastname: 'Odinson'
        }
      ])
    );

    fixture = TestBed.createComponent(AssignDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should filter users', (done) => {
    component.filteredUsers$?.subscribe((users) => {
      expect(users).toEqual([
        {
          id: '1',
          firstname: 'Thor',
          lastname: 'Odinson'
        },
        {
          id: '2',
          firstname: 'Thor',
          lastname: 'test'
        }
      ]);
      done();
    });
    component.user.patchValue('thor');
  });
});
