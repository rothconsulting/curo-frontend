import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CAMUNDA_BASE_PATH, CuroCamundaModule } from '@umb-ag/curo-camunda';
import { CuroCoreModule, CURO_FORM_ACCESSOR } from '@umb-ag/curo-core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { DemoFormsModule } from './demo-forms/demo-forms.module';
import { StartFormComponent } from './demo-forms/start-form/start-form.component';
import { SharedModule } from './shared/shared.module';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,

    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    MatSidenavModule,
    MatToolbarModule,

    CuroCoreModule,
    CuroCamundaModule,

    AppRoutingModule,
    CoreModule,
    SharedModule,
    DemoFormsModule
  ],
  providers: [
    {
      provide: CAMUNDA_BASE_PATH,
      useValue: '/api/engine-rest'
    },
    {
      provide: CURO_FORM_ACCESSOR,
      useValue: StartFormComponent,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
