import { Component, inject, signal } from "@angular/core";
import { SplashScreenService } from "../../service/splash-screen.service";

@Component({
  selector: 'app-splash-screen',
  imports: [],
  templateUrl: './splash-screen.component.html',
  styleUrl: './splash-screen.component.css'
})
export class SplashScreenComponent {
  splashScreenService = inject(SplashScreenService);

  splashScreen = signal(true);

  constructor() {
    this.splashScreenService.processing$.subscribe(
      (val) => {
        this.splashScreen.set(val);
      }
    );
  }
}