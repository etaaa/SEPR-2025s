import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from 'src/app/component/header/header.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  imports: [
    HeaderComponent,
    RouterOutlet
  ],
  standalone: true,
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
}
