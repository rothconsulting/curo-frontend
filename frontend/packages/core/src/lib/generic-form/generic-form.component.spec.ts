import {
  Component,
  ComponentFactoryResolver,
  ViewContainerRef
} from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CURO_FORM_ACCESSOR } from '../curo-form-accessor';
import { GenericFormComponent } from './generic-form.component';

describe('GenericFormComponent', () => {
  let component: GenericFormComponent;
  let fixture: ComponentFixture<GenericFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GenericFormComponent, TestComponent],
      providers: [
        {
          provide: CURO_FORM_ACCESSOR,
          useValue: {
            key: 'TestComponent',
            component: TestComponent
          },
          multi: true
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GenericFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create the component if a formKey is set', () => {
    const componentFactoryResolver = TestBed.inject(ComponentFactoryResolver);
    spyOn(componentFactoryResolver, 'resolveComponentFactory');
    spyOn(component.formHost as ViewContainerRef, 'createComponent');

    component.formKey = 'TestComponent';

    expect(
      componentFactoryResolver.resolveComponentFactory
    ).toHaveBeenCalledWith(TestComponent);

    expect(component.formHost?.createComponent).toHaveBeenCalled();
  });

  it('should not create the component if no formKey is set', () => {
    const componentFactoryResolver = TestBed.inject(ComponentFactoryResolver);
    spyOn(componentFactoryResolver, 'resolveComponentFactory');
    spyOn(component.formHost as ViewContainerRef, 'createComponent');

    component.formKey = null;

    expect(
      componentFactoryResolver.resolveComponentFactory
    ).not.toHaveBeenCalledWith(TestComponent);

    expect(component.formHost?.createComponent).not.toHaveBeenCalled();
  });

  it('should not create the component if no component can be found', () => {
    const componentFactoryResolver = TestBed.inject(ComponentFactoryResolver);
    spyOn(componentFactoryResolver, 'resolveComponentFactory');
    spyOn(component.formHost as ViewContainerRef, 'createComponent');

    component.formKey = 'GenericFormComponent';

    expect(
      componentFactoryResolver.resolveComponentFactory
    ).not.toHaveBeenCalledWith(GenericFormComponent);

    expect(component.formHost?.createComponent).not.toHaveBeenCalled();
  });
});

@Component({
  selector: 'cu-test'
})
class TestComponent {}
