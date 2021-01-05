import { Component } from '@angular/core';
import { ProcessService } from '@umb-ag/curo-core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor(private processService: ProcessService) {}

  startProcess(): void {
    this.processService.startProcess('new-technic-suggestion').subscribe();
  }
}
