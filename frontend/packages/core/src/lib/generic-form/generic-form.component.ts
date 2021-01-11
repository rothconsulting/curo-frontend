import {
  ChangeDetectionStrategy,
  Component,
  ComponentFactoryResolver,
  Inject,
  Input,
  Type,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import { CuroFormAccessor, CURO_FORM_ACCESSOR } from '../curo-form-accessor';

@Component({
  selector: 'cu-generic-form',
  templateUrl: './generic-form.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GenericFormComponent {
  @ViewChild('formHost', { read: ViewContainerRef, static: true })
  formHost!: ViewContainerRef;

  constructor(
    private componentFactoryResolver: ComponentFactoryResolver,
    @Inject(CURO_FORM_ACCESSOR)
    private curoFormAccessors: Type<CuroFormAccessor>[]
  ) {}

  @Input()
  set formKey(formKey: string | null) {
    this.formHost.clear();

    if (formKey) {
      const component = this.curoFormAccessors.find(
        (form) => form.name === formKey
      );

      if (component) {
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory<CuroFormAccessor>(
          component
        );

        this.formHost.createComponent(componentFactory);
      }
    }
  }
}
