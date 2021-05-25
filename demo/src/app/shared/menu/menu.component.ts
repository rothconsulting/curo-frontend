import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MenuElement, MenuService } from '@umb-ag/curo-core';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MenuComponent {
  taskLists$: Observable<MenuElement[]>;

  constructor(private menuService: MenuService) {
    this.taskLists$ = this.menuService.getMenu();
  }
}
