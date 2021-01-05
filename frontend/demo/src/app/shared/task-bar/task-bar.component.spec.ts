import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { TaskBarComponent } from './task-bar.component';

describe('TaskBarComponent', () => {
  let component: TaskBarComponent;
  let fixture: ComponentFixture<TaskBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatIconModule],
      declarations: [TaskBarComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TaskBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit complete task', () => {
    component.completeTask.subscribe((value: any) =>
      expect(value).toBeUndefined()
    );
    component.complete();
  });

  it('should emit assign task', () => {
    component.assignTask.subscribe((userId: string) =>
      expect(userId).toBeUndefined()
    );
    component.assign();
  });
});
