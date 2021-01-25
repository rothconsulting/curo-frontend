import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ProcessService } from '@umb-ag/curo-core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor(private processService: ProcessService, private router: Router) {}

  startProcess(): void {
    this.processService
      .startProcess('new-technic-suggestion', undefined, undefined, {
        flowToNext: true,
        flowToNextIgnoreAssignee: true
      })
      .subscribe((response) =>
        this.router.navigate([
          'tasks',
          response.flowToNext ? response.flowToNext[0] : ''
        ])
      );
  }
}
