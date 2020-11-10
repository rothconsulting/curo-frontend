import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CuroCoreModule } from '@umb-ag/curo-core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

@NgModule({
  declarations: [AppComponent],
  imports: [BrowserModule, AppRoutingModule, CuroCoreModule],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
