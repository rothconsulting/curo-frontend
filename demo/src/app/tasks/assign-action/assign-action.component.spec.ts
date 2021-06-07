import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { Subject } from 'rxjs';
import { AssignActionComponent } from './assign-action.component';

describe('AssignActionComponent', () => {
  let component: AssignActionComponent;
  let fixture: ComponentFixture<AssignActionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatDialogModule, MatIconModule],
      declarations: [AssignActionComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignActionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should stop propagation', () => {
    const mock = {
      stopPropagation: () => {}
    };

    spyOn(mock, 'stopPropagation');

    component.stopPropagation(mock as any);

    expect(mock.stopPropagation).toHaveBeenCalled();
  });

  it('should open AssignDialogComponent', (done) => {
    const closeSubject = new Subject();
    const matDialog = TestBed.inject(MatDialog);
    spyOn(matDialog, 'open').and.returnValue({
      afterClosed: () => closeSubject.asObservable(),
      close: () => {}
    } as any);

    component.showDialog();

    component.assign.subscribe((user) => {
      expect(user).toEqual('testuser');
      done();
    });

    closeSubject.next('testuser');
  });

  it('should claim', (done) => {
    component.assign.subscribe((user) => {
      expect(user).toEqual('demo');
      done();
    });

    component.claim();
  });
});
